package org.example.json;

import com.google.gson.*;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JsonDemo3 {
    public static void main(String[] args) throws IOException {
        // Remove before pushing to GitHub
        String apiKey = "";

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://api.themoviedb.org/3/trending/movie/day?language=en-US")
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();

        Request request2 = new Request.Builder()
                .url("https://api.themoviedb.org/3/genre/movie/list?language=en")
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();

        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();
//        System.out.println(responseBody);

        Response response2 = client.newCall(request2).execute();
        String responseBody2 = response2.body().string();
        System.out.println(responseBody2);

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDate.class, new LocalDateDeserializer())
                .create();
//        JsonElement jsonElement = new JsonParser().parse(responseBody);
//        String json = gson.toJson(jsonElement);
//        System.out.print(json);
        MovieDBAPI movieDBAPI = null;
        try {
            movieDBAPI = gson.fromJson(responseBody, MovieDBAPI.class);
        } catch (JsonSyntaxException | JsonIOException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }

        MovieDBGenre movieDBGenre = null;
        try {
            movieDBGenre = gson.fromJson(responseBody2, MovieDBGenre.class);
        } catch (JsonSyntaxException | JsonIOException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
        Genre[] genres = movieDBGenre.genres;
        List<Genre> genresList = Arrays.asList(genres);

        for(Movie movie : movieDBAPI.results) {
            System.out.println("ID: " + movie.id);
            System.out.println("Title: " + movie.title);
            System.out.println("Overview: " + movie.overview);
//            System.out.println("Genres: " + Arrays.toString(movie.genre_ids));
            System.out.print("Genres: ");
            for(int id: movie.genre_ids){
                Genre genre = genresList.stream().filter(a -> a.id == id).collect(Collectors.toList()).get(0);
                System.out.print(genre.name + ", ");
            }
            System.out.println();
            System.out.println("Vote Average: " + movie.vote_average);
            System.out.println("Video: " + (movie.video ? "Yes" : "No"));
            System.out.println("Release Date: " + movie.release_date);
            System.out.println();
        }
    }

    private static class LocalDateDeserializer implements JsonDeserializer<LocalDate> {
        @Override
        public LocalDate deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            return LocalDate.parse(json.getAsString());
        }
    }

    private static class MovieDBGenre {
        public Genre[] genres;
    }
    private static class Genre {
        public int id;
        public String name;
    }

    private static class MovieDBAPI {
        public Movie[] results;
    }
    private static class Movie {
        public int id;
        public String title;
        public String overview;
        public int[] genre_ids;
        public double vote_average;
        public boolean video;
        public LocalDate release_date;
    }
}
