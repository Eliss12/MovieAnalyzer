package bg.sofia.uni.project.movie.server.command.impl;

import bg.sofia.uni.project.movie.common.Movie;
import bg.sofia.uni.project.movie.exceptions.MovieAppException;
import bg.sofia.uni.project.movie.server.command.Command;
import bg.sofia.uni.project.movie.server.command.service.MovieService;

public class MovieByNameCommand extends Command {

    public MovieByNameCommand(String argument) {
        super(argument);
    }

    @Override
    public String execute(MovieService movieService) throws MovieAppException {
        if (argument.isEmpty()) {
            return errorJson("Movie title cannot be empty");
        }

        try {
            Movie movie = movieService.getMovieByName(argument);
            if (movie == null) {
                return errorJson("Movie not found: " + argument);
            }
            return toJson(movie);
        } catch (Exception e) {
            throw new MovieAppException("Failed to get movie: " + argument, e);
        }
    }
}