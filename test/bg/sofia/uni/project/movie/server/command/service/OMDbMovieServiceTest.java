package bg.sofia.uni.project.movie.server.command.service;

import bg.sofia.uni.project.movie.common.Movie;
import bg.sofia.uni.project.movie.exceptions.*;
import bg.sofia.uni.project.movie.server.cache.CacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OMDbMovieServiceTest {

    @Mock
    private CacheService cacheService;

    @Mock
    private RecommendationService recommendationService;

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<String> httpResponse;

    private OMDbMovieService movieService;

    private final String apiKey = "testKey";
    private final int maxRecs = 5;

    @BeforeEach
    void setUp() {
        movieService = new OMDbMovieService(cacheService, apiKey, recommendationService, maxRecs, httpClient);
    }

    @Test
    void testGetMovieByNameFromCache() throws Exception {
        String title = "The Matrix";
        String cacheKey = "movie_The_Matrix.json";
        String cachedJson = "{\"Title\":\"The Matrix\",\"Year\":\"1999\",\"Director\":\"Wachowski\",\"imdbRating\":\"8.7\",\"Genre\":\"Action\",\"Poster\":\"\"}";

        when(cacheService.exists(cacheKey)).thenReturn(true);
        when(cacheService.read(cacheKey)).thenReturn(cachedJson);

        Movie result = movieService.getMovieByName(title);

        assertEquals("The Matrix", result.title());
        assertEquals("8.7", result.rating());
        verify(httpClient, never()).send(any(), any());
        verify(cacheService, never()).save(any(), any());
    }

    @Test
    void testGetMovieByNameFromApiSuccess() throws Exception {
        String title = "The Matrix";
        String cacheKey = "movie_The_Matrix.json";
        String apiJson = "{\"Title\":\"The Matrix\",\"Year\":\"1999\",\"Director\":\"Wachowski\",\"imdbRating\":\"8.7\",\"Genre\":\"Action\",\"Poster\":\"\"}";

        when(cacheService.exists(cacheKey)).thenReturn(false);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(apiJson);

        Movie result = movieService.getMovieByName(title);

        assertEquals("The Matrix", result.title());
        verify(cacheService).save(eq(cacheKey), eq(apiJson));
    }

    @Test
    void testGetMovieByNameWthApiReturnsErrorStatus() throws Exception {
        String title = "Missing";
        String cacheKey = "movie_Missing.json";

        when(cacheService.exists(cacheKey)).thenReturn(false);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(404);

        assertThrows(MovieAppException.class, () -> movieService.getMovieByName(title));
        verify(cacheService, never()).save(any(), any());
    }

    @Test
    void testGetMovieByNameWithApiReturnsEmptyBody() throws Exception {
        String title = "Empty";
        String cacheKey = "movie_Empty.json";

        when(cacheService.exists(cacheKey)).thenReturn(false);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn("");

        assertThrows(MovieAppException.class, () -> movieService.getMovieByName(title));
    }

    @Test
    void testGetMovieByNameWithErrorJson() throws Exception {
        String title = "Invalid";
        String cacheKey = "movie_Invalid.json";
        String errorJson = "{\"error\":\"Movie not found!\"}";

        when(cacheService.exists(cacheKey)).thenReturn(false);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(errorJson);

        Movie result = movieService.getMovieByName(title);

        assertTrue(result.title().contains("Movie not found"));
        verify(cacheService, never()).save(any(), any());
    }

    @Test
    void testGetMovieByNameWthCacheReadFails() throws Exception {
        String title = "Matrix";
        String cacheKey = "movie_Matrix.json";

        when(cacheService.exists(cacheKey)).thenReturn(true);
        when(cacheService.read(cacheKey)).thenThrow(new CacheException("Cache read error"));

        assertThrows(MovieAppException.class, () -> movieService.getMovieByName(title));
    }

    @Test
    void testGetMovieByNameWithEmptyTitle() {
        assertThrows(MovieAppException.class, () -> movieService.getMovieByName(""));
    }

    @Test
    void testGetMovieByNameWithTooLongTitle() {
        String longTitle = "a".repeat(201);
        assertThrows(MovieAppException.class, () -> movieService.getMovieByName(longTitle));
    }

    @Test
    void testGetMovieByNameWithApiKeyMissing() {
        OMDbMovieService serviceWithoutKey = new OMDbMovieService(cacheService, "", recommendationService, maxRecs);
        assertThrows(MovieAppException.class, () -> serviceWithoutKey.getMovieByName("Matrix"));
    }

    @Test
    void testGetMovieByIdFromCache() throws Exception {
        String id = "tt0133093";
        String cacheKey = "movie_tt0133093.json";
        String cachedJson = "{\"Title\":\"The Matrix\",\"Year\":\"1999\",\"Director\":\"Wachowski\",\"imdbRating\":\"8.7\",\"Genre\":\"Action\",\"Poster\":\"\"}";

        when(cacheService.exists(cacheKey)).thenReturn(true);
        when(cacheService.read(cacheKey)).thenReturn(cachedJson);

        Movie result = movieService.getMovieById(id);

        assertEquals("The Matrix", result.title());
        verify(httpClient, never()).send(any(), any());
    }

    @Test
    void testGetMovieByIdFromApi() throws Exception {
        String id = "tt0133093";
        String cacheKey = "movie_tt0133093.json";
        String apiJson = "{\"Title\":\"The Matrix\",\"Year\":\"1999\",\"Director\":\"Wachowski\",\"imdbRating\":\"8.7\",\"Genre\":\"Action\",\"Poster\":\"\"}";

        when(cacheService.exists(cacheKey)).thenReturn(false);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(apiJson);

        Movie result = movieService.getMovieById(id);

        assertEquals("The Matrix", result.title());
        verify(cacheService).save(eq(cacheKey), eq(apiJson));
    }

    @Test
    void testGetMovieByIdWthEmptyId() {

        assertThrows(MovieAppException.class, () -> movieService.getMovieById(""));
    }

    @Test
    void testGetMovieByIdWithInvalidFormat() {
        assertThrows(MovieAppException.class, () -> movieService.getMovieById("abc"));
    }


    @Test
    void testRecommendSuccessfully() throws Exception {
        String title = "Inception";
        Movie target = new Movie("Inception", "2010", "Nolan", "8.8", "Action, Sci-Fi", "");
        List<Movie> allMovies = List.of(
                new Movie("Interstellar", "2014", "Nolan", "8.6", "Sci-Fi", ""),
                new Movie("The Dark Knight", "2008", "Nolan", "9.0", "Action", "")
        );
        List<Movie> recommendations = List.of(allMovies.getFirst());

        // Create temporary cache with two movies
        Path tempDir = Files.createTempDirectory("cache");
        CacheService realCache = new CacheService(tempDir);
        realCache.save("movie_Interstellar.json", "{\"Title\":\"Interstellar\",\"Year\":\"2014\",\"Director\":\"Nolan\",\"imdbRating\":\"8.6\",\"Genre\":\"Sci-Fi\",\"Poster\":\"\"}");
        realCache.save("movie_DarkKnight.json", "{\"Title\":\"The Dark Knight\",\"Year\":\"2008\",\"Director\":\"Nolan\",\"imdbRating\":\"9.0\",\"Genre\":\"Action\",\"Poster\":\"\"}");

        // Create service with real cache
        OMDbMovieService serviceWithRealCache = new OMDbMovieService(realCache, apiKey, recommendationService, maxRecs);
        OMDbMovieService spyService = spy(serviceWithRealCache);
        doReturn(target).when(spyService).getMovieByName(title);

        // Stub recommendationService to accept the list regardless of order
        when(recommendationService.recommend(eq(target), argThat(list ->
                list.size() == allMovies.size() && list.containsAll(allMovies) && allMovies.containsAll(list)), eq(maxRecs)))
                .thenReturn(recommendations);

        List<Movie> result = spyService.recommend(title);

        assertEquals(1, result.size());
        assertEquals("Interstellar", result.getFirst().title());

        // Clean up
        Files.walk(tempDir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    }

    @Test
    void testRecommendWithTargetHasNoGenre() throws Exception {
        Movie target = new Movie("NoGenre", "2020", "Dir", "N/A", "N/A", "");
        OMDbMovieService spyService = spy(movieService);
        doReturn(target).when(spyService).getMovieByName("NoGenre");

        List<Movie> result = spyService.recommend("NoGenre");

        assertTrue(result.isEmpty());
        verify(recommendationService, never()).recommend(any(), any(), anyInt());
    }

    @Test
    void testRecommend() throws Exception {
        OMDbMovieService spyService = spy(movieService);
        doThrow(new MovieAppException("Error")).when(spyService).getMovieByName("Error");

        assertThrows(MovieAppException.class, () -> spyService.recommend("Error"));
    }

}