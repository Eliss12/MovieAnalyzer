package bg.sofia.uni.project.movie.server.service;

import bg.sofia.uni.project.movie.server.integration.Movie;

import java.util.List;

public interface RecommendationService {
    List<Movie> recommend(Movie target, List<Movie> candidates, int maxResults);
}