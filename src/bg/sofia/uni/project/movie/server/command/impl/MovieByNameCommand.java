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
    public String execute(MovieService movieService) throws Exception {
        if (argument.isEmpty()) {
            return errorJson("Movie title cannot be empty");
        }

        Movie movie = movieService.getMovieByName(argument);
        if (movie == null) {
            return errorJson("Movie not found: " + argument);
        }
        return toJson(movie);

    }
}