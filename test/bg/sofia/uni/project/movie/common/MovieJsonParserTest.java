package bg.sofia.uni.project.movie.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MovieJsonParserTest {

    @Test
    void testParseMovieSuccess() {
        String json = "{\"Title\":\"The Matrix\",\"Year\":\"1999\",\"Director\":\"Wachowski\",\"imdbRating\":\"8.7\",\"Genre\":\"Action\",\"Poster\":\"http://poster\"}";
        Movie movie = MovieJsonParser.parseMovie(json);
        assertEquals("The Matrix", movie.title());
        assertEquals("8.7", movie.rating());
    }

    @Test
    void testParseMovieWithErrorField() {
        String json = "{\"error\":\"Movie not found!\"}";
        Movie movie = MovieJsonParser.parseMovie(json);
        assertTrue(movie.title().contains("Movie not found!"));
        assertEquals("N/A", movie.year());
    }

    @Test
    void testParseMovieWithInvalidJson() {
        String json = "invalid json";
        Movie movie = MovieJsonParser.parseMovie(json);
        assertTrue(movie.title().contains("Failed to parse"));
    }

    @Test
    void testParseMovieWithNullJson() {
        Movie movie = MovieJsonParser.parseMovie(null);
        assertTrue(movie.title().contains("Empty response received from server."));
    }

    @Test
    void testParseMovieWithNullMovieTitle() {
        String json = "{\"Title\":null,\"Year\":\"1999\",\"Director\":\"Wachowski\",\"imdbRating\":\"8.7\",\"Genre\":\"Action\",\"Poster\":\"http://poster\"}";
        Movie movie = MovieJsonParser.parseMovie(json);
        assertTrue(movie.title().contains("Invalid movie data received."));
    }


    @Test
    void testParseMoviesSuccess() {
        String json = "[{\"Title\":\"Movie1\"},{\"Title\":\"Movie2\"}]";
        Movie[] movies = MovieJsonParser.parseMovies(json);
        assertEquals(2, movies.length);
        assertEquals("Movie1", movies[0].title());
    }

    @Test
    void testParseMoviesWithInvalidJson() {
        String json = "invalid";
        Movie[] movies = MovieJsonParser.parseMovies(json);
        assertEquals(1, movies.length);
        assertTrue(movies[0].title().contains("Failed to parse"));
    }

    @Test
    void testParseMoviesWithNullJson() {
        Movie[] movies = MovieJsonParser.parseMovies(null);
        assertEquals(1, movies.length);
        assertTrue(movies[0].title().contains("Empty recommendations received."));
    }
}