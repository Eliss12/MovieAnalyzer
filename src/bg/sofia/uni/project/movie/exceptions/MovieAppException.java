package bg.sofia.uni.project.movie.exceptions;

public class MovieAppException extends Exception {

    public MovieAppException(String message) {
        super(message);
    }

    public MovieAppException(String message, Throwable cause) {
        super(message, cause);
    }
}
