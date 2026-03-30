package bg.sofia.uni.project.movie.server.command;

import bg.sofia.uni.project.movie.common.ClientMovie;
import bg.sofia.uni.project.movie.server.integration.Movie;
import bg.sofia.uni.project.movie.server.service.MovieService;
import com.google.gson.Gson;

import java.util.List;

import static bg.sofia.uni.project.movie.common.ClientMovieJsonParser.serialize;

public abstract class Command {
    protected static final Gson gson = new Gson();
    protected final String argument;

    protected Command(String argument) {
        this.argument = argument != null ? argument.trim() : "";
    }

    public abstract String execute(MovieService movieService) throws Exception;

    protected String toJson(Object obj) {
        return gson.toJson(obj);
    }

    protected String toJson(ClientMovie movie) {
        return serialize(movie);
    }

    protected String toJson(List<ClientMovie> movies) {
        return serialize(movies);
    }

    protected String errorJson(String message) {
        return String.format("{\"error\":\"%s\"}", message.replace("\"", "\\\""));
    }

    protected ClientMovie toClientMovie(Movie movie) {
        return new ClientMovie(
                movie.title(),
                movie.year(),
                movie.director(),
                movie.rating(),
                movie.genre(),
                movie.poster()
        );
    }

    protected List<ClientMovie> toClientMovieList(List<Movie> movies) {
        return movies.stream().map(this::toClientMovie).toList();
    }
}