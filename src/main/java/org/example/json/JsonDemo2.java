package org.example.json;

import com.google.gson.*;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Instant;


public class JsonDemo2 {

    public static void main(String[] args) throws IOException {
        String apiUrl = "https://randomuser.me/api/?format=json&seed=abc&results=5&nat=us";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(apiUrl)
                .build();
        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();
//        System.out.println(responseBody);
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Instant.class, new InstantDeserializer())
                .create();
//        JsonElement jsonElement = new JsonParser().parse(responseBody);
//        String json = gson.toJson(jsonElement);
//        System.out.println(json);
        RandomUserAPI randomUserAPI = null;
        try {
            randomUserAPI = gson.fromJson(responseBody, RandomUserAPI.class);
        } catch (JsonSyntaxException | JsonIOException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
        for(RandomUser user: randomUserAPI.results) {
            System.out.println(user.name.title + " " + user.name.first + " " +  user.name.last);
            System.out.println(user.location.street.number + " " + user.location.street.name);
            System.out.println(user.location.city + ", " + user.location.state + " " + user.location.postcode);
            System.out.println("Email: " + user.email);
            System.out.println("Phone: " + user.phone);
            System.out.println("Birthday: " + user.dob.date);
            System.out.println();
        }
    }

    private static class InstantDeserializer implements JsonDeserializer<Instant> {
        @Override
        public Instant deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            return Instant.parse(json.getAsString());
        }
    }

    private static class RandomUserAPI {
        public RandomUser[] results;
    }
    private static class RandomUser {
        public String email;
        public String phone;
        public Name name;
        public Location location;
        public DateOfBirth dob;
    }
    private static class DateOfBirth {
        public Instant date;
    }
    public static class Location {
        public Street street;
        public String city;
        public String state;
        public String postcode;
    }
    public static class Street {
        public String number;
        public String name;
    }
    private static class Name {
        public String first;
        public String last;
        public String title;
    }
}
