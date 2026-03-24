package bg.sofia.uni.project.movie.server.command;

import bg.sofia.uni.project.movie.server.command.impl.MovieByIdCommand;
import bg.sofia.uni.project.movie.server.command.impl.MovieByNameCommand;
import bg.sofia.uni.project.movie.server.command.impl.RecommendationCommand;
import bg.sofia.uni.project.movie.server.command.impl.UnknownCommand;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandParser {
    private static final Pattern COMMAND_PATTERN = Pattern.compile("^([^\\s]+)\\s*(.*)$");

    public static Command parse(String input) {
        if (input == null || input.isBlank()) {
            return new UnknownCommand("empty command");
        }

        input = input.trim();
        Matcher matcher = COMMAND_PATTERN.matcher(input);
        if (!matcher.matches()) {
            return new UnknownCommand(input);
        }

        String commandType = matcher.group(1);
        String argument = matcher.group(2);

        return switch (commandType) {
            case "get-movie" -> new MovieByNameCommand(argument);
            case "get-movie-id" -> new MovieByIdCommand(argument);
            case "recommend" -> new RecommendationCommand(argument);
            default -> new UnknownCommand(input);
        };
    }
}