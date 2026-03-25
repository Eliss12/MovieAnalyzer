package bg.sofia.uni.project.movie.server.command;

import bg.sofia.uni.project.movie.server.command.impl.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommandParserTest {

    @Test
    void testParseGetMovieCommand() {
        Command cmd = CommandParser.parse("get-movie The Matrix");
        assertInstanceOf(MovieByNameCommand.class, cmd);
    }

    @Test
    void testParseGetMovieByIdCommand() {
        Command cmd = CommandParser.parse("get-movie-id tt0133093");
        assertInstanceOf(MovieByIdCommand.class, cmd);
    }

    @Test
    void testParseRecommendCommand() {
        Command cmd = CommandParser.parse("recommend Inception");
        assertInstanceOf(RecommendationCommand.class, cmd);
    }

    @Test
    void testParseUnknownCommand() {
        Command cmd = CommandParser.parse("unknown hello");
        assertInstanceOf(UnknownCommand.class, cmd);
    }

    @Test
    void testParseEmptyInput() {
        Command cmd = CommandParser.parse("");
        assertInstanceOf(UnknownCommand.class, cmd);
    }

    @Test
    void testParseCommandWithExtraSpaces() {
        Command cmd = CommandParser.parse("  get-movie    The Matrix  ");
        assertInstanceOf(MovieByNameCommand.class, cmd);
    }
}