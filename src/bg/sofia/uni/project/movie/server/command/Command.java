package bg.sofia.uni.project.movie.server.command;

import bg.sofia.uni.project.movie.exceptions.MovieAppException;
import bg.sofia.uni.project.movie.server.command.service.MovieService;
import com.google.gson.Gson;

public abstract class Command {
    protected static final Gson gson = new Gson();
    protected final String argument;

    protected Command(String argument) {
        this.argument = argument != null ? argument.trim() : "";
    }

    public abstract String execute(MovieService movieService) throws MovieAppException;

    protected String toJson(Object obj) {
        return gson.toJson(obj);
    }

    protected String errorJson(String message) {
        return String.format("{\"error\":\"%s\"}", message.replace("\"", "\\\""));
    }
}