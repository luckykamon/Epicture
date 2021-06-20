package com.example.epiture;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Functions {

    private static final String CARACTERE_PARSE = "°";

    /**
     * @param imageItemList liste d'Images items
     * @return String encodé "id²link²0²°id²link²1²"
     */
    public static String encodeImages(List<ImageItem> imageItemList) {
        StringBuilder encodeString = new StringBuilder();
        int index = 0;

        for (ImageItem imageItem : imageItemList) {
            if (index == 0) encodeString.append(imageItem.getString());
            else encodeString.append(CARACTERE_PARSE).append(imageItem.getString());

            index++;
        }

        return encodeString.toString();
    }

    /**
     * @param encodeString String encodé "id²link²0²°id²link²1²"
     * @return liste d'Images items
     */
    public static List<ImageItem> decodeImages(String encodeString) {
        List<ImageItem> imageItemList = new ArrayList<>();
        String[] listeItems = encodeString.split(CARACTERE_PARSE);

        for (String item : listeItems) {
            imageItemList.add(new ImageItem(item));
        }

        return imageItemList;
    }

    public static void getImages(String recherche, Boolean hasTagMyPictures, Boolean hasTagMyFavorites,
                                 MainActivity mainActivity, Context context) {

        if (hasTagMyFavorites) {
            new requestGetMyFavorites(output -> {
                List<ImageItem> imageItemList = new ArrayList<>();
                try {
                    JSONObject json_output = new JSONObject(output);

                    JSONArray current = new JSONArray(json_output.get("data").toString());
                    JSONObject currentData;
                    for (int i = 0; i < current.length(); i++) {
                        try {
                            currentData = new JSONObject(current.get(i).toString());
                            if (currentData.getString("type").equals("image/jpeg") || currentData.getString("type").equals("image/png")) {
                                imageItemList.add(new ImageItem(currentData.getString("id"), currentData.getString("link"), currentData.getBoolean("favorite")));
                            }
                        } catch (JSONException e) {
                            System.out.println("Image not upload: " + i);
                        }
                    }
                    mainActivity.callBackAddAllImages(imageItemList);
                } catch (JSONException e) {
                    System.out.println("Erreur de transformation du string en json");
                    e.printStackTrace();
                }
            }, context).execute();
        } else if (hasTagMyPictures) {
            new requestGetMyImages(output -> {
                List<ImageItem> imageItemList = new ArrayList<>();
                try {
                    JSONObject json_output = new JSONObject(output);
                    JSONArray current = new JSONArray(json_output.get("data").toString());
                    JSONObject currentData;
                    for (int i = 0; i < current.length(); i++) {
                        try {
                            currentData = new JSONObject(current.get(i).toString());
                            if (currentData.getString("type").equals("image/jpeg") || currentData.getString("type").equals("image/png")) {
                                imageItemList.add(new ImageItem(currentData.getString("id"), currentData.getString("link"), currentData.getBoolean("favorite")));
                            }
                        } catch (JSONException e) {
                            System.out.println("Image not upload: " + i);
                        }
                    }
                    setFavoriteOnImageGet(context, imageItemList, mainActivity);
                } catch (JSONException e) {
                    System.out.println("Erreur de transformation du string en json");
                    e.printStackTrace();
                }
            }, context).execute();
        }
        if (!hasTagMyFavorites && !hasTagMyPictures) {
            new requestGetImages(output -> {
                List<ImageItem> imageItemList = new ArrayList<>();
                try {
                    JSONObject json_output = new JSONObject(output);
                    JSONArray current = new JSONArray(json_output.get("data").toString());
                    JSONObject currentData;
                    for (int i = 0; i < current.length(); i++) {
                        try {
                            currentData = new JSONObject(current.get(i).toString());
                            JSONArray currentArrayImages = currentData.getJSONArray("images");
                            JSONObject currentObjetImage = currentArrayImages.getJSONObject(0);
                            if (currentObjetImage.getString("type").equals("image/jpeg") || currentObjetImage.getString("type").equals("image/png")) {
                                imageItemList.add(new ImageItem(currentObjetImage.getString("id"), currentObjetImage.getString("link"), currentData.getBoolean("favorite")));
                            }
                        } catch (JSONException e) {
                            System.out.println("Image not upload: " + i);
                        }
                    }
                    setFavoriteOnImageGet(context, imageItemList, mainActivity);
                } catch (JSONException e) {
                    System.out.println("Erreur de transformation du string en json");
                    e.printStackTrace();
                }
            }, context, recherche).execute();
        }
    }

    public static void setImageFavorite(ImageItem imageItem, Context context) {
        new requestPostFavoriteImage(output -> {
        }, imageItem, context).execute();
    }

    private static void setFavoriteOnImageGet(Context context, List<ImageItem> imageItemListDefault, MainActivity mainActivity) {
        new requestSetFavoriteOnImageGet(output -> mainActivity.callBackAddAllImages(Functions.decodeImages(output)), context, imageItemListDefault).execute();
    }

    public static void postImage(Context context, String url) {
        new requestPost(output -> {
            if (output != null) {
                Toast.makeText(context, "Image mise en ligne", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Veuillez sélectionner une image correcte", Toast.LENGTH_SHORT).show();
            }
        }, context, url).execute();
    }

    private static final class requestGetMyFavorites extends AsyncTask<Void, Void, String> {

        public final AsyncResponse delegate;
        private final Context context;

        public requestGetMyFavorites(AsyncResponse delegate, Context context) {
            this.delegate = delegate;
            this.context = context;
        }

        @Override
        protected String doInBackground(Void... voids) {
            InternalMemory memory = new InternalMemory(this.context);
            if (!memory.isConnected()) {
                Intent intent = new Intent(this.context, Login.class);
                this.context.startActivity(intent);
            }
            String output = null;

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url("https://api.imgur.com/3/account/" + memory.getFromJson(memory.USERNAME) + "/favorites/0/{{favoritesSort}}")
                    .method("GET", null)
                    .addHeader("Authorization", "Bearer " + memory.getFromJson(memory.ACCESS_TOKEN))
                    .addHeader("Cookie", "UPSERVERID=upload.i-09ded7f2ea94b8711.production")
                    .build();
            Response response;
            try {
                response = client.newCall(request).execute();
                output = Objects.requireNonNull(response.body()).string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return output;
        }

        @Override
        protected void onPostExecute(String result) {
            delegate.processFinish(result);
        }
    }

    private static final class requestGetMyImages extends AsyncTask<Void, Void, String> {

        public final AsyncResponse delegate;
        private final Context context;

        public requestGetMyImages(AsyncResponse delegate, Context context) {
            this.delegate = delegate;
            this.context = context;
        }

        @Override
        protected String doInBackground(Void... voids) {
            InternalMemory memory = new InternalMemory(this.context);
            if (!memory.isConnected()) {
                Intent intent = new Intent(this.context, Login.class);
                this.context.startActivity(intent);
            }
            String output = null;

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url("https://api.imgur.com/3/account/" + memory.getFromJson(memory.USERNAME) + "/images/0")
                    .method("GET", null)
                    .addHeader("Authorization", "Bearer " + memory.getFromJson(memory.ACCESS_TOKEN))
                    .build();
            Response response;
            try {
                response = client.newCall(request).execute();

                output = Objects.requireNonNull(response.body()).string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return output;
        }

        @Override
        protected void onPostExecute(String result) {
            delegate.processFinish(result);
        }
    }

    private static final class requestGetImages extends AsyncTask<Void, Void, String> {

        public final AsyncResponse delegate;
        private final Context context;
        private final String recherche;

        public requestGetImages(AsyncResponse delegate, Context context, String recherche) {
            this.delegate = delegate;
            this.context = context;
            this.recherche = recherche;
        }

        @Override
        protected String doInBackground(Void... voids) {
            InternalMemory memory = new InternalMemory(this.context);
            if (!memory.isConnected()) {
                Intent intent = new Intent(this.context, Login.class);
                this.context.startActivity(intent);
            }
            String output = null;

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url("https://api.imgur.com/3/gallery/search/{{sort}}/{{window}}/{{page}}?q=" + this.recherche)
                    .method("GET", null)
                    .addHeader("Authorization", "Bearer " + memory.getFromJson(memory.ACCESS_TOKEN))
                    .build();
            Response response;
            try {
                response = client.newCall(request).execute();

                output = Objects.requireNonNull(response.body()).string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return output;
        }

        @Override
        protected void onPostExecute(String result) {
            delegate.processFinish(result);
        }
    }

    private static final class requestPostFavoriteImage extends AsyncTask<Void, Void, String> {

        public final AsyncResponse delegate;
        private final ImageItem imageItem;
        private final Context context;

        public requestPostFavoriteImage(AsyncResponse delegate, ImageItem imageItem, Context context) {
            this.delegate = delegate;
            this.imageItem = imageItem;
            this.context = context;
        }

        @Override
        protected String doInBackground(Void... voids) {
            InternalMemory memory = new InternalMemory(this.context);
            if (!memory.isConnected()) {
                Intent intent = new Intent(this.context, Login.class);
                this.context.startActivity(intent);
            }
            String output = null;

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(JSON, "{}");
            Request request = new Request.Builder()
                    .url("https://api.imgur.com/3/image/" + this.imageItem.getId() + "/favorite")
                    .method("POST", body)
                    .addHeader("Authorization", "Bearer " + memory.getFromJson(memory.ACCESS_TOKEN))
                    .addHeader("Cookie", "UPSERVERID=upload.i-09ded7f2ea94b8711.production")
                    .build();
            Response response;
            try {
                response = client.newCall(request).execute();
                output = Objects.requireNonNull(Objects.requireNonNull(response.body())).string();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return output;
        }

        @Override
        protected void onPostExecute(String result) {
            delegate.processFinish(result);
        }
    }

    private static final class requestSetFavoriteOnImageGet extends AsyncTask<Void, Void, String> {

        public final AsyncResponse delegate;
        private final List<ImageItem> imageItem;
        private final Context context;

        public requestSetFavoriteOnImageGet(AsyncResponse delegate, Context context, List<ImageItem> imageItem) {
            this.delegate = delegate;
            this.imageItem = imageItem;
            this.context = context;
        }

        @Override
        protected String doInBackground(Void... voids) {
            InternalMemory memory = new InternalMemory(this.context);
            if (!memory.isConnected()) {
                Intent intent = new Intent(this.context, Login.class);
                this.context.startActivity(intent);
            }
//            String output = null;

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();

            List<ImageItem> newimageItem = new ArrayList<>();

            for (int i = 0; i < Math.min(this.imageItem.size(), 20); i++) {
                Request request = new Request.Builder()
                        .url("https://api.imgur.com/3/account/" + memory.USERNAME + "/image/" + imageItem.get(i).getId())
                        .method("GET", null)
                        .addHeader("Authorization", "Bearer " + memory.getFromJson(memory.ACCESS_TOKEN))
                        .addHeader("Cookie", "UPSERVERID=upload.i-09ded7f2ea94b8711.production")
                        .build();
                Response response;
                try {
                    response = client.newCall(request).execute();
                    String output = Objects.requireNonNull(response.body()).string();
                    try {
                        JSONObject json_output = new JSONObject(output);
                        newimageItem.add(new ImageItem(imageItem.get(i).getId(), imageItem.get(i).getLink(), json_output.getJSONObject("data").getBoolean("favorite")));
                    } catch (JSONException e) {
                        System.out.println("Erreur de transformation du string en json");
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return Functions.encodeImages(newimageItem);
        }

        @Override
        protected void onPostExecute(String result) {
            delegate.processFinish(result);
        }
    }

    private static final class requestPost extends AsyncTask<Void, Void, String> {

        public final AsyncResponse delegate;
        private final Context context;
        private final String url;

        public requestPost(AsyncResponse delegate, Context context, String url) {
            this.delegate = delegate;
            this.context = context;
            this.url = url;
        }

        @Override
        protected String doInBackground(Void... voids) {
            InternalMemory memory = new InternalMemory(this.context);
            if (!memory.isConnected()) {
                Intent intent = new Intent(this.context, Login.class);
                this.context.startActivity(intent);
            }
            String output = null;

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("image", this.url,
                            RequestBody.create(MediaType.parse("application/octet-stream"),
                                    new File(this.url)))
                    .build();
            Request request = new Request.Builder()
                    .url("https://api.imgur.com/3/upload")
                    .method("POST", body)
                    .addHeader("Authorization", "Bearer " + memory.getFromJson(memory.ACCESS_TOKEN))
                    .build();
            Response response;
            try {
                response = client.newCall(request).execute();
                output = Objects.requireNonNull(response.body()).string();
                System.out.println(output);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return output;
        }

        @Override
        protected void onPostExecute(String result) {
            delegate.processFinish(result);
        }
    }


}
