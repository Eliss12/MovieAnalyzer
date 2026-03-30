package bg.sofia.uni.project.movie.server.command.impl;

import bg.sofia.uni.project.movie.server.integration.Movie;
import bg.sofia.uni.project.movie.server.command.Command;
import bg.sofia.uni.project.movie.server.command.service.MovieService;

public class MovieByIdCommand extends Command {

    public MovieByIdCommand(String argument) {
        super(argument);
    }

    @Override
    public String execute(MovieService movieService) throws Exception {
        if (argument.isEmpty()) {
            return errorJson("Movie ID cannot be empty");
        }

        Movie movie = movieService.getMovieById(argument);
        if (movie == null) {
            return errorJson("Movie not found with ID: " + argument);
        }
        return toJson(toClientMovie(movie));

    }
}