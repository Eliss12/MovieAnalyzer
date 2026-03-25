package bg.sofia.uni.project.movie.server.command;

import bg.sofia.uni.project.movie.common.Movie;
import bg.sofia.uni.project.movie.exceptions.ApiException;
import bg.sofia.uni.project.movie.exceptions.CacheException;
import bg.sofia.uni.project.movie.exceptions.MovieAppException;
import bg.sofia.uni.project.movie.exceptions.MovieNotFoundException;
import bg.sofia.uni.project.movie.server.command.service.MovieService;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommandProcessorTest {

    @Mock
    private MovieService movieService;

    private CommandProcessor processor;
    private final Gson gson = new Gson();

    @BeforeEach
    void setUp() {

        processor = new CommandProcessor(movieService);
    }

    @Test
    void testProcessGetMovieCommand() throws Exception {
        Movie movie = new Movie("Inception", "2010", "Nolan", "8.8", "Action", "http://poster");
        when(movieService.getMovieByName("Inception")).thenReturn(movie);

        String response = processor.process("get-movie Inception");

        assertEquals(gson.toJson(movie), response);
    }

    @Test
    void testProcessGetMovieCommandWithEmptyTitle() throws Exception {
        String response = processor.process("get-movie ");
        assertTrue(response.contains("error") && response.contains("cannot be empty"));
    }

    @Test
    void testProcessGetMovieByIdCommand() throws Exception {
        Movie movie = new Movie("Inception", "2010", "Nolan", "8.8", "Action", "http://poster");
        when(movieService.getMovieById("tt1375666")).thenReturn(movie);

        String response = processor.process("get-movie-id tt1375666");

        assertEquals(gson.toJson(movie), response);
    }

    @Test
    void testProcessRecommendCommand() throws Exception {
        List<Movie> recs = List.of(new Movie("Interstellar", "2014", "Nolan", "8.6", "Sci-Fi", ""));
        when(movieService.recommend("Inception")).thenReturn(recs);

        String response = processor.process("recommend Inception");

        assertEquals(gson.toJson(recs), response);
    }

    @Test
    void testProcessUnknownCommand() {
        String response = processor.process("unknown command");
        assertTrue(response.contains("Unknown command"));
    }

    @Test
    void testProcessWithMovieNotFoundException() throws Exception {
        when(movieService.getMovieByName("Missing"))
                .thenThrow(new MovieNotFoundException("Movie not found: Missing"));

        String response = processor.process("get-movie Missing");
        assertTrue(response.contains("error") && response.contains("Movie not found."));
    }

    @Test
    void testProcessWithApiException() throws Exception {
        when(movieService.getMovieByName("Inception"))
                .thenThrow(new ApiException("API error"));

        String response = processor.process("get-movie Inception");
        assertTrue(response.contains("error") &&
                response.contains("Movie service is currently unavailable. Please try again later."));
    }

    @Test
    void testProcessWithCacheException() throws Exception {
        when(movieService.getMovieByName("Inception"))
                .thenThrow(new CacheException("Cache read failed"));

        String response = processor.process("get-movie Inception");

        assertTrue(response.contains("error") &&
                response.contains("Internal cache error. Please contact administrator."));
    }

    @Test
    void testProcessWithMovieAppException() throws Exception {
        when(movieService.getMovieByName("Inception"))
                .thenThrow(new MovieAppException("Custom message"));

        String response = processor.process("get-movie Inception");

        assertTrue(response.contains("error") && response.contains("Custom message"));
    }
}