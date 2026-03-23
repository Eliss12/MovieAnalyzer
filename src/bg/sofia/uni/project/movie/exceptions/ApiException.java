package bg.sofia.uni.project.movie.exceptions;

public class ApiException extends MovieAppException {
    public ApiException(String message) {
        super(message);
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }
}