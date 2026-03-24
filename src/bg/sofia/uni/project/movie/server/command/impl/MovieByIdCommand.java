package bg.sofia.uni.project.movie.server.command.impl;

import bg.sofia.uni.project.movie.common.Movie;
import bg.sofia.uni.project.movie.exceptions.MovieAppException;
import bg.sofia.uni.project.movie.server.command.Command;
import bg.sofia.uni.project.movie.server.command.service.MovieService;

public class MovieByIdCommand extends Command {

    public MovieByIdCommand(String argument) {
        super(argument);
    }

    @Override
    public String execute(MovieService movieService) throws MovieAppException {
        if (argument.isEmpty()) {
            return errorJson("Movie ID cannot be empty");
        }

        try {
            Movie movie = movieService.getMovieById(argument);
            if (movie == null) {
                return errorJson("Movie not found with ID: " + argument);
            }
            return toJson(movie);
        } catch (Exception e) {
            throw new MovieAppException("Failed to get movie by ID: " + argument, e);
        }
    }
}