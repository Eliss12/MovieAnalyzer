package bg.sofia.uni.project.movie.server.command.service;

import bg.sofia.uni.project.movie.common.Movie;

import java.util.List;

public interface RecommendationService {
    List<Movie> recommend(Movie target, List<Movie> candidates, int maxResults);
}