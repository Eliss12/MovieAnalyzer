package bg.sofia.uni.project.movie.common;

import com.google.gson.annotations.SerializedName;

public record Movie(

        @SerializedName("Title")
        String title,

        @SerializedName("Year")
        String year,

        @SerializedName("Director")
        String director,

        @SerializedName("imdbRating")
        String rating,

        @SerializedName("Genre")
        String genre,

        @SerializedName("Poster")
        String poster

) {}
