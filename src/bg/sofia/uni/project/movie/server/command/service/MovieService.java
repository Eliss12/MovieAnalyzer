package bg.sofia.uni.project.movie.server.command.service;

import bg.sofia.uni.project.movie.common.Movie;

import java.util.List;

public interface MovieService {

    Movie getMovieByName(String title) throws Exception;

    Movie getMovieById(String id) throws Exception;

    List<Movie> recommend(String title) throws Exception;
}
