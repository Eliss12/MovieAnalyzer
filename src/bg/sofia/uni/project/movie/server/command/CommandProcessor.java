package bg.sofia.uni.project.movie.server.command;

import bg.sofia.uni.project.movie.exceptions.*;
import bg.sofia.uni.project.movie.server.command.service.MovieService;
import bg.sofia.uni.project.movie.server.logger.LoggerUtil;

import java.util.logging.Level;

public class CommandProcessor {
    private final MovieService movieService;

    public CommandProcessor(MovieService movieService) {
        this.movieService = movieService;
    }

    public String process(String commandLine) {
        if (commandLine == null || commandLine.isBlank()) {
            return errorJson("Empty command");
        }

        Command command = CommandParser.parse(commandLine);

        try {
            return command.execute(movieService);
        } catch (MovieNotFoundException e) {
            LoggerUtil.log(Level.WARNING, "Movie not found: " + commandLine, e);
            return errorJson("Movie not found.");
        } catch (ApiException e) {
            LoggerUtil.log(Level.SEVERE, "API error", e);
            return errorJson("Movie service is currently unavailable. Please try again later.");
        } catch (CacheException e) {
            LoggerUtil.log(Level.SEVERE, "Cache error", e);
            return errorJson("Internal cache error. Please contact administrator.");
        } catch (MovieAppException e) {
            LoggerUtil.log(Level.SEVERE, "Movie app error", e);
            return errorJson(e.getMessage());
        } catch (Exception e) {
            LoggerUtil.log(Level.SEVERE, "Unexpected server error", e);
            return errorJson("Unexpected server error. Please try again later.");
        }
    }

    private static String errorJson(String message) {
        return String.format("{\"error\":\"%s\"}", message.replace("\"", "\\\""));
    }
}