/*package com.example.epiture;

import org.json.JSONObject;

import java.io.IOException;
import okhttp3.*;

public class imgur_api {

    public static void main(String[] args) throws IOException {
        System.out.println(get_images("fb6f4ff04bcefd36acc0cfffc96a009b0b890a44", "luckykamon", 0));
    }

    public static String get_images(String AccessToken, String username, int number_page) throws IOException {
        if (number_page == -1) {
            number_page = 0;
        }
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url("https://api.imgur.com/3/account/" + username + "/images/" + number_page)
                .method("GET", null)
                .addHeader("Authorization", "Bearer " + AccessToken)
                .build();
        Response response = client.newCall(request).execute();
        return (response.body().string());
    }

//    OkHttpClient client = new OkHttpClient();
//
//    String run(String url) throws IOException {
//        Request request = new Request.Builder()
//                .url(url)
//                .build();
//
//        try (Response response = client.newCall(request).execute()) {
//            return response.body().string();
//        }
//    }
//
//    public static void main(String[] args) throws IOException {
//        imgur_api example = new imgur_api();
//        String response = example.run("https://raw.github.com/square/okhttp/master/README.md");
//        System.out.println(response);
//    }


}*/
