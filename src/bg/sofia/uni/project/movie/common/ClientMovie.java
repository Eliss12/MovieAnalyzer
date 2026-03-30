package bg.sofia.uni.project.movie.common;

public record ClientMovie(
        String title,
        String year,
        String director,
        String rating,
        String genre,
        String posterUrl
) {}
