package bg.sofia.uni.project.movie.server.service;

import bg.sofia.uni.project.movie.server.integration.Movie;

import java.util.ArrayList;
import java.util.List;

public class GenreBasedRecommendationService implements RecommendationService {

    @Override
    public List<Movie> recommend(Movie target, List<Movie> candidates, int maxResults) {
        if (target == null || target.genre() == null || candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        String[] targetGenres = target.genre().split(",\\s*");
        List<MovieScore> scored = new ArrayList<>();

        for (Movie movie : candidates) {
            if (movie.title().equalsIgnoreCase(target.title())) {
                continue;
            }

            int score = calculateSimilarity(targetGenres, movie.genre());
            if (score > 0) {
                scored.add(new MovieScore(movie, score));
            }
        }

        scored.sort((a, b) -> {
            if (b.getScore() != a.getScore()) {
                return Integer.compare(b.getScore(), a.getScore());
            }
            double r1 = parseRating(b.getMovie().rating());
            double r2 = parseRating(a.getMovie().rating());
            return Double.compare(r1, r2);
        });

        return scored.stream()
                .limit(maxResults)
                .map(MovieScore::getMovie)
                .toList();
    }

    private int calculateSimilarity(String[] targetGenres, String otherGenres) {
        if (otherGenres == null || otherGenres.isBlank()) {
            return 0;
        }

        String[] other = otherGenres.split(",\\s*");
        int score = 0;

        for (String g1 : targetGenres) {
            for (String g2 : other) {
                if (g1.equalsIgnoreCase(g2)) {
                    score++;
                }
            }
        }

        return score;
    }

    private double parseRating(String rating) {
        try {
            if (rating == null || rating.equals("N/A")) {
                return 0.0;
            }
            return Double.parseDouble(rating);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private static class MovieScore {
        private final Movie movie;
        private final int score;

        MovieScore(Movie movie, int score) {
            this.movie = movie;
            this.score = score;
        }

        public Movie getMovie() { return movie; }
        public int getScore() { return score; }
    }
}