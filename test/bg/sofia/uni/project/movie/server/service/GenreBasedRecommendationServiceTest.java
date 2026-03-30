package bg.sofia.uni.project.movie.server.command.service;

import bg.sofia.uni.project.movie.common.Movie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GenreBasedRecommendationServiceTest {

    private GenreBasedRecommendationService service;
    private Movie target;

    @BeforeEach
    void setUp() {
        service = new GenreBasedRecommendationService();
        target = new Movie("Inception", "2010", "Nolan", "8.8", "Action, Sci-Fi", "");
    }

    @Test
    void testRecommendWithNullTarget() {
        List<Movie> result = service.recommend(null, List.of(new Movie("Test", "", "", "", "", "")), 5);
        assertTrue(result.isEmpty());
    }

    @Test
    void testRecommendWithTargetGenreNull() {
        Movie targetWithNullGenre = new Movie("Test", "", "", "", null, "");
        List<Movie> result = service.recommend(targetWithNullGenre, List.of(new Movie("Other", "", "", "", "Action", "")), 5);
        assertTrue(result.isEmpty());
    }

    @Test
    void testRecommendWithNullCandidates() {
        List<Movie> result = service.recommend(target, null, 5);
        assertTrue(result.isEmpty());
    }

    @Test
    void testRecommendWithEmptyCandidates() {
        List<Movie> result = service.recommend(target, List.of(), 5);
        assertTrue(result.isEmpty());
    }

    @Test
    void testRecommendWithNoGenreMatches() {
        List<Movie> candidates = List.of(
                new Movie("The Godfather", "1972", "Coppola", "9.2", "Crime, Drama", ""),
                new Movie("Toy Story", "1995", "Lasseter", "8.3", "Animation", "")
        );
        List<Movie> result = service.recommend(target, candidates, 5);
        assertTrue(result.isEmpty());
    }

    @Test
    void testRecommendWithOneMatch() {
        Movie matching = new Movie("Interstellar", "2014", "Nolan", "8.6", "Sci-Fi, Drama", "");
        List<Movie> candidates = List.of(matching);
        List<Movie> result = service.recommend(target, candidates, 5);
        assertEquals(1, result.size());
        assertEquals("Interstellar", result.get(0).title());
    }

    @Test
    void testRecommendSkipsTargetItself() {
        Movie sameTitle = new Movie("Inception", "2010", "Nolan", "8.8", "Action, Sci-Fi", "");
        Movie otherMatch = new Movie("The Dark Knight", "2008", "Nolan", "9.0", "Action, Crime", "");
        List<Movie> candidates = List.of(sameTitle, otherMatch);
        List<Movie> result = service.recommend(target, candidates, 5);
        assertEquals(1, result.size());
        assertEquals("The Dark Knight", result.get(0).title());
    }

    @Test
    void testRecommendSortsByScoreDescending() {
        Movie highScore = new Movie("The Dark Knight", "2008", "Nolan", "9.0", "Action, Crime", "");
        Movie lowScore = new Movie("Interstellar", "2014", "Nolan", "8.6", "Sci-Fi", "");
        List<Movie> candidates = List.of(lowScore, highScore);
        List<Movie> result = service.recommend(target, candidates, 5);
        assertEquals(2, result.size());
        assertEquals("The Dark Knight", result.get(0).title());
        assertEquals("Interstellar", result.get(1).title());
    }

    @Test
    void testRecommendWhenScoresEqualSortsByRatingDescending() {
        Movie highRating = new Movie("The Dark Knight", "2008", "Nolan", "9.0", "Action", "");
        Movie mediumRating = new Movie("Batman Begins", "2005", "Nolan", "8.2", "Action", "");
        Movie lowRating = new Movie("The Matrix", "1999", "Wachowski", "8.7", "Action", "");

        List<Movie> candidates = List.of(mediumRating, lowRating, highRating);
        List<Movie> result = service.recommend(target, candidates, 5);
        assertEquals(3, result.size());
        assertEquals("The Dark Knight", result.get(0).title()); // 9.0
        assertEquals("The Matrix", result.get(1).title());      // 8.7
        assertEquals("Batman Begins", result.get(2).title());   // 8.2
    }

    @Test
    void testRecommendRespectsMaxResults() {
        List<Movie> candidates = List.of(
                new Movie("A", "2000", "D", "7.0", "Action", ""),
                new Movie("B", "2000", "D", "7.0", "Action", ""),
                new Movie("C", "2000", "D", "7.0", "Action", ""),
                new Movie("D", "2000", "D", "7.0", "Action", "")
        );
        List<Movie> result = service.recommend(target, candidates, 2);
        assertEquals(2, result.size());
    }

    @Test
    void testRecommendWithCaseInsensitiveGenreMatching() {
        Movie lowerCase = new Movie("Lower", "2000", "D", "7.0", "action", "");
        Movie upperCase = new Movie("Upper", "2000", "D", "7.0", "ACTION", "");
        Movie mixedCase = new Movie("Mixed", "2000", "D", "7.0", "AcTiOn", "");
        List<Movie> candidates = List.of(lowerCase, upperCase, mixedCase);
        List<Movie> result = service.recommend(target, candidates, 5);
        assertEquals(3, result.size());
    }

    @Test
    void testRecommendWithNullOtherGenres() {
        Movie nullGenre = new Movie("NullGenre", "2000", "D", "7.0", null, "");
        List<Movie> candidates = List.of(nullGenre);
        List<Movie> result = service.recommend(target, candidates, 5);
        assertTrue(result.isEmpty());
    }

    @Test
    void testRecommendWithBlankOtherGenres() {
        Movie blankGenre = new Movie("BlankGenre", "2000", "D", "7.0", "", "");
        List<Movie> candidates = List.of(blankGenre);
        List<Movie> result = service.recommend(target, candidates, 5);
        assertTrue(result.isEmpty());
    }

    @Test
    void testRecommendParsesRatingAsZero() {
        Movie naRating = new Movie("NA", "2000", "D", "N/A", "Action", "");
        Movie other = new Movie("Other", "2000", "D", "8.0", "Action", "");

        List<Movie> candidates = List.of(naRating, other);
        List<Movie> result = service.recommend(target, candidates, 5);
        assertEquals(2, result.size());
        assertEquals("Other", result.get(0).title());
        assertEquals("NA", result.get(1).title());
    }

    @Test
    void testRecommendWithInvalidRating() {
        Movie invalidRating = new Movie("Invalid", "2000", "D", "invalid", "Action", "");
        Movie other = new Movie("Other", "2000", "D", "8.0", "Action", "");
        List<Movie> candidates = List.of(invalidRating, other);
        List<Movie> result = service.recommend(target, candidates, 5);
        assertEquals(2, result.size());
        assertEquals("Other", result.get(0).title());
        assertEquals("Invalid", result.get(1).title());
    }

    @Test
    void testRecommendWithMultipleGenresCalculatesScoreCorrectly() {
        Movie movieWithTwoMatches = new Movie("Two", "2000", "D", "7.0", "Action, Sci-Fi", "");
        Movie movieWithOneMatch = new Movie("One", "2000", "D", "7.0", "Action", "");
        List<Movie> candidates = List.of(movieWithOneMatch, movieWithTwoMatches);
        List<Movie> result = service.recommend(target, candidates, 5);
        assertEquals(2, result.size());
        assertEquals("Two", result.get(0).title()); // score 2 vs 1
        assertEquals("One", result.get(1).title());
    }

    @Test
    void testRecommendWithEmptyStringGenresSplitsCorrectly() {
        // target has a single genre "Action"
        Movie targetAction = new Movie("Target", "2000", "D", "8.0", "Action", "");
        List<Movie> candidates = List.of(
                new Movie("Candidate", "2000", "D", "7.0", "Action, Sci-Fi", "")
        );
        List<Movie> result = service.recommend(targetAction, candidates, 5);
        assertEquals(1, result.size());
    }
}