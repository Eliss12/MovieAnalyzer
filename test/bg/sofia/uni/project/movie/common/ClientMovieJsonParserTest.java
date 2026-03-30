package bg.sofia.uni.project.movie.common;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ClientMovieJsonParserTest {

    private static final String VALID_MOVIE_JSON = "{\"title\":\"Inception\",\"year\":\"2010\",\"director\":\"Nolan\",\"rating\":\"8.8\",\"genre\":\"Action,Sci-Fi\",\"posterUrl\":\"http://example.com/poster.jpg\"}";
    private static final String VALID_MOVIES_JSON = "[{\"title\":\"A\",\"year\":\"2000\",\"director\":\"D1\",\"rating\":\"7.0\",\"genre\":\"G1\",\"posterUrl\":\"\"},{\"title\":\"B\",\"year\":\"2001\",\"director\":\"D2\",\"rating\":\"8.0\",\"genre\":\"G2\",\"posterUrl\":\"\"}]";


    @Test
    void testDeserializeMovie() {
        ClientMovie movie = ClientMovieJsonParser.deserializeMovie(VALID_MOVIE_JSON);
        assertNotNull(movie);
        assertEquals("Inception", movie.title());
        assertEquals("2010", movie.year());
        assertEquals("Nolan", movie.director());
        assertEquals("8.8", movie.rating());
        assertEquals("Action,Sci-Fi", movie.genre());
        assertEquals("http://example.com/poster.jpg", movie.posterUrl());
    }

    @Test
    void testDeserializeMovieWithNullInput() {
        ClientMovie movie = ClientMovieJsonParser.deserializeMovie(null);
        assertTrue(movie.title().contains("Empty response"));
        assertEquals("N/A", movie.year());
    }

    @Test
    void testDeserializeMovieWithBlankInput() {
        ClientMovie movie = ClientMovieJsonParser.deserializeMovie("   ");
        assertTrue(movie.title().contains("Empty response"));
    }

    @Test
    void testDeserializeMovieWithInvalidJson() {
        ClientMovie movie = ClientMovieJsonParser.deserializeMovie("not json");
        assertTrue(movie.title().contains("Failed to parse"));
    }

    @Test
    void testDeserializeMovieWithJsonWithErrorField() {
        String jsonWithError = "{\"error\":\"Movie not found\"}";
        ClientMovie movie = ClientMovieJsonParser.deserializeMovie(jsonWithError);
        assertTrue(movie.title().contains("Movie not found"));
        assertEquals("N/A", movie.year());
    }

    @Test
    void testDeserializeMovieWithJsonMissingFields() {
        String partialJson = "{\"title\":\"OnlyTitle\"}";
        ClientMovie movie = ClientMovieJsonParser.deserializeMovie(partialJson);
        assertNotNull(movie);
        assertEquals("OnlyTitle", movie.title());
        assertNull(movie.year());
    }

    @Test
    void testDeserializeMovies() {
        ClientMovie[] movies = ClientMovieJsonParser.deserializeMovies(VALID_MOVIES_JSON);
        assertNotNull(movies);
        assertEquals(2, movies.length);
        assertEquals("A", movies[0].title());
        assertEquals("7.0", movies[0].rating());
        assertEquals("B", movies[1].title());
        assertEquals("8.0", movies[1].rating());
    }

    @Test
    void testDeserializeMoviesWithNullInput() {
        ClientMovie[] movies = ClientMovieJsonParser.deserializeMovies(null);
        assertEquals(1, movies.length);
        assertTrue(movies[0].title().contains("Empty recommendations"));
    }

    @Test
    void testDeserializeMoviesWithBlankInput() {
        ClientMovie[] movies = ClientMovieJsonParser.deserializeMovies("");
        assertEquals(1, movies.length);
        assertTrue(movies[0].title().contains("Empty recommendations"));
    }

    @Test
    void testDeserializeMoviesWithInvalidJson() {
        ClientMovie[] movies = ClientMovieJsonParser.deserializeMovies("not an array");
        assertEquals(1, movies.length);
        assertTrue(movies[0].title().contains("Failed to parse recommendations"));
    }

    @Test
    void testDeserializeMoviesWithEmptyArray() {
        ClientMovie[] movies = ClientMovieJsonParser.deserializeMovies("[]");
        assertNotNull(movies);
        assertEquals(0, movies.length);
    }

    @Test
    void testSerializeSingleMovie() {
        ClientMovie movie = new ClientMovie("Test", "2020", "Dir", "7.5", "Drama", "http://poster");
        String json = ClientMovieJsonParser.serialize(movie);
        assertTrue(json.contains("\"title\":\"Test\""));
        assertTrue(json.contains("\"year\":\"2020\""));
        assertTrue(json.contains("\"rating\":\"7.5\""));
    }

    @Test
    void testSerializeWithNullMovie() {
        String json = ClientMovieJsonParser.serialize((ClientMovie) null);
        assertEquals("{}", json);
    }

    @Test
    void testSerializeWithMovieList() {
        List<ClientMovie> movies = List.of(
                new ClientMovie("A", "2000", "D1", "7.0", "G1", ""),
                new ClientMovie("B", "2001", "D2", "8.0", "G2", "")
        );
        String json = ClientMovieJsonParser.serialize(movies);
        assertTrue(json.startsWith("["));
        assertTrue(json.endsWith("]"));
        assertTrue(json.contains("\"title\":\"A\""));
        assertTrue(json.contains("\"title\":\"B\""));
    }

    @Test
    void testSerializeWithNullList() {
        String json = ClientMovieJsonParser.serialize((List<ClientMovie>) null);
        assertEquals("[]", json);
    }

    @Test
    void testSerializeWithEmptyList() {
        String json = ClientMovieJsonParser.serialize(List.of());
        assertEquals("[]", json);
    }
}