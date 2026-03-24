package bg.sofia.uni.project.movie.server.command.impl;

import bg.sofia.uni.project.movie.common.Movie;
import bg.sofia.uni.project.movie.exceptions.MovieAppException;
import bg.sofia.uni.project.movie.server.command.Command;
import bg.sofia.uni.project.movie.server.command.service.MovieService;

import java.util.List;

public class RecommendationCommand extends Command {

    public RecommendationCommand(String argument) {
        super(argument);
    }

    @Override
    public String execute(MovieService movieService) throws MovieAppException {
        if (argument.isEmpty()) {
            return errorJson("Movie title for recommendations cannot be empty");
        }

        try {
            List<Movie> recommendations = movieService.recommend(argument);
            if (recommendations.isEmpty()) {
                return errorJson("No recommendations found for: " + argument);
            }
            return toJson(recommendations);
        } catch (Exception e) {
            throw new MovieAppException("Failed to get recommendations for: " + argument, e);
        }
    }
}