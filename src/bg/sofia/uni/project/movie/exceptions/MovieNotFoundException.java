package bg.sofia.uni.project.movie.exceptions;

public class MovieNotFoundException extends MovieAppException {
    public MovieNotFoundException(String message) {
        super(message);
    }

    public MovieNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
