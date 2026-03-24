package bg.sofia.uni.project.movie.server.command.impl;

import bg.sofia.uni.project.movie.server.command.Command;
import bg.sofia.uni.project.movie.server.command.service.MovieService;

public class UnknownCommand extends Command {
    private final String originalCommand;

    public UnknownCommand(String originalCommand) {
        super("");
        this.originalCommand = originalCommand;
    }

    @Override
    public String execute(MovieService movieService) {
        return errorJson("Unknown command: " + originalCommand +
                ". Available commands: get-movie <title>, get-movie-id <id>, recommend <title>");
    }
}