package bg.sofia.uni.project.movie.server.command.service;

import bg.sofia.uni.project.movie.common.Movie;
import bg.sofia.uni.project.movie.common.MovieJsonParser;
import bg.sofia.uni.project.movie.exceptions.*;
import bg.sofia.uni.project.movie.server.cache.CacheService;
import bg.sofia.uni.project.movie.server.logger.LoggerUtil;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.stream.Stream;

public class OMDbMovieService implements MovieService {
    private static final HttpClient DEFAULT_HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .executor(Executors.newFixedThreadPool(10))
            .build();

    private final CacheService cache;
    private final String apiKey;
    private final RecommendationService recommendationService;
    private final int maxRecommendations;
    private final HttpClient httpClient;

    public OMDbMovieService(CacheService cache, String apiKey,
                            RecommendationService recommendationService,
                            int maxRecommendations) {
        this(cache, apiKey, recommendationService, maxRecommendations, DEFAULT_HTTP_CLIENT);
    }
    public OMDbMovieService(CacheService cache, String apiKey,
                            RecommendationService recommendationService,
                            int maxRecommendations, HttpClient httpClient) {
        this.cache = cache;
        this.apiKey = apiKey;
        this.recommendationService = recommendationService;
        this.maxRecommendations = maxRecommendations;
        this.httpClient = httpClient;
    }

    @Override
    public Movie getMovieByName(String title) throws MovieAppException {
        validateApiKey();
        validateTitle(title);

        String cacheKey = generateCacheKey("movie", title.replace(" ", "_"));

        try {
            if (cache.exists(cacheKey)) {
                String json = cache.read(cacheKey);
                return MovieJsonParser.parseMovie(json);
            }

            String url = String.format("https://www.omdbapi.com/?t=%s&apikey=%s",
                    URLEncoder.encode(title, StandardCharsets.UTF_8),
                    apiKey);

            return fetchAndCacheMovie(cacheKey, url);

        } catch (CacheException | IOException | InterruptedException e) {
            throw new MovieAppException("Failed to get movie: " + title, e);
        }
    }

    @Override
    public Movie getMovieById(String id) throws MovieAppException {
        validateApiKey();
        validateId(id);

        String cacheKey = generateCacheKey("movie", id);

        try {
            if (cache.exists(cacheKey)) {
                String json = cache.read(cacheKey);
                return MovieJsonParser.parseMovie(json);
            }

            String url = String.format("https://www.omdbapi.com/?i=%s&apikey=%s", id, apiKey);
            return fetchAndCacheMovie(cacheKey, url);

        } catch (CacheException | IOException | InterruptedException e) {
            throw new MovieAppException("Failed to get movie by ID: " + id, e);
        }
    }

    @Override
    public List<Movie> recommend(String title) throws MovieAppException {
        Movie target = getMovieByName(title);
        if (target == null || target.genre() == null || target.genre().equals("N/A")) {
            return List.of();
        }

        List<Movie> allMovies = loadAllCachedMovies();
        return recommendationService.recommend(target, allMovies, maxRecommendations);
    }

    private Movie fetchAndCacheMovie(String cacheKey, String url)
            throws IOException, InterruptedException, CacheException, ApiException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new ApiException("API returned status code: " + response.statusCode());
        }

        if (response.body() == null || response.body().isEmpty()) {
            throw new ApiException("Empty response from movie service.");
        }

        boolean isErrorResponse = response.body().contains("\"error\":");

        Movie movie = MovieJsonParser.parseMovie(response.body());

        // Only cache if it's a valid movie (not an error)
        if (!isErrorResponse && !movie.title().contains("error") && !movie.title().contains("Failed")) {
            cache.save(cacheKey, response.body());
        }

        return movie;
    }

    private List<Movie> loadAllCachedMovies() throws MovieAppException {
        List<Movie> movies = new ArrayList<>();
        Path cacheDir = cache.getCacheDir();

        if (!Files.exists(cacheDir)) {
            return movies;
        }

        try (Stream<Path> paths = Files.list(cacheDir)) {
            paths.filter(path -> path.toString().endsWith(".json"))
                    .forEach(path -> {
                        try {
                            String content = Files.readString(path);

                            // If the first line is a timestamp (all digits), remove it
                            int newlineIdx = content.indexOf('\n');
                            if (newlineIdx > 0 && content.substring(0, newlineIdx).matches("\\d+")) {
                                content = content.substring(newlineIdx + 1);
                            }
                            Movie movie = MovieJsonParser.parseMovie(content);
                            // Only add valid movies (not errors)
                            if (!movie.title().contains("error") && !movie.title().contains("Failed")) {
                                movies.add(movie);
                            }
                        } catch (Exception e) {
                            LoggerUtil.log(Level.WARNING, "Failed to load movie from cache: " + path, e);
                        }
                    });
        } catch (IOException e) {
            throw new MovieAppException("Failed to load cached movies", e);
        }

        return movies;
    }

    private String generateCacheKey(String prefix, String identifier) {
        return prefix + "_" + identifier + ".json";
    }

    private void validateApiKey() throws ApiException {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ApiException("Server configuration error: API key not set");
        }
    }

    private void validateTitle(String title) throws MovieAppException {
        if (title == null || title.isBlank()) {
            throw new MovieAppException("Movie title cannot be empty");
        }
        if (title.length() > 200) {
            throw new MovieAppException("Movie title too long (max 200 characters)");
        }
    }

    private void validateId(String id) throws MovieAppException {
        if (id == null || id.isBlank()) {
            throw new MovieAppException("Movie ID cannot be empty");
        }
        if (!id.matches("^tt\\d{7,8}$")) {
            throw new MovieAppException("Invalid IMDb ID format. Should be like tt1234567");
        }
    }
}