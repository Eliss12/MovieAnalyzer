package bg.sofia.uni.project.movie.server.command.impl;

import bg.sofia.uni.project.movie.server.integration.Movie;
import bg.sofia.uni.project.movie.server.command.Command;
import bg.sofia.uni.project.movie.server.command.service.MovieService;

import java.util.List;

public class RecommendationCommand extends Command {

    public RecommendationCommand(String argument) {
        super(argument);
    }

    @Override
    public String execute(MovieService movieService) throws Exception {
        if (argument.isEmpty()) {
            return errorJson("Movie title for recommendations cannot be empty");
        }


        List<Movie> recommendations = movieService.recommend(argument);
        if (recommendations.isEmpty()) {
            return errorJson("No recommendations found for: " + argument);
        }
        return toJson(toClientMovieList(recommendations));

    }
}