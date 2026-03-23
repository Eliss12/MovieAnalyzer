package bg.sofia.uni.project.movie.exceptions;

public class CacheException extends MovieAppException {
    public CacheException(String message) {
        super(message);
    }

    public CacheException(String message, Throwable cause) {
        super(message, cause);
    }
}