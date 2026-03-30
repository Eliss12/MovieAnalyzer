package bg.sofia.uni.project.movie.common;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.util.List;

public class ClientMovieJsonParser {
    private static final Gson gson = new Gson();


    public static ClientMovie deserializeMovie(String json) {
        if (json == null || json.isBlank()) {
            return errorMovie("Empty response from server.");
        }
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            if (obj.has("error")) {
                return errorMovie(obj.get("error").getAsString());
            }
            ClientMovie movie = gson.fromJson(json, ClientMovie.class);
            return movie != null ? movie : errorMovie("Invalid movie data.");
        } catch (JsonSyntaxException e) {
            return errorMovie("Failed to parse server response.");
        }
    }

    public static ClientMovie[] deserializeMovies(String json) {
        if (json == null || json.isBlank()) {
            return new ClientMovie[]{errorMovie("Empty recommendations.")};
        }
        try {
            return gson.fromJson(json, ClientMovie[].class);
        } catch (JsonSyntaxException e) {
            return new ClientMovie[]{errorMovie("Failed to parse recommendations.")};
        }
    }

    private static ClientMovie errorMovie(String message) {
        return new ClientMovie(message, "N/A", "N/A", "N/A", "N/A", "N/A");
    }

    public static String serialize(ClientMovie movie) {
        return movie == null ? "{}" : gson.toJson(movie);
    }

    public static String serialize(List<ClientMovie> movies) {
        return (movies == null || movies.isEmpty()) ? "[]" : gson.toJson(movies);
    }
}