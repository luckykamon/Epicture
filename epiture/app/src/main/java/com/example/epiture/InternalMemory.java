package com.example.epiture;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class InternalMemory {

    public final String ACCESS_TOKEN = "access_token";
    public final String REFRESH_TOKEN = "refresh_token";
    public final String USERNAME = "username";
    private final File data_file;
    private JSONObject fileJson = new JSONObject();


    public InternalMemory(Context context) {

        File directory = context.getFilesDir();
        String fileName = "data.json";
        this.data_file = new File(directory, fileName);

        try {
            int octet;
            FileInputStream in = new FileInputStream(data_file);
            StringBuilder outputFileString = new StringBuilder();
            while ((octet = in.read()) != -1) {
                outputFileString.append((char) octet);
            }
            in.close();
            this.fileJson = new JSONObject(outputFileString.toString());
        } catch (IOException | JSONException e) { //Le fichier n'as pas encore été créé ou mal créé
            e.printStackTrace();
            try {
                FileOutputStream out = new FileOutputStream(data_file);
                out.write(fileJson.toString().getBytes());
                out.close();
            } catch (IOException f) { //Erreur lors de la création de fichier
                f.printStackTrace();
            }
        }
    }

    private void saveFileJson() {
        try {
            FileOutputStream out = new FileOutputStream(data_file);
            out.write(fileJson.toString().getBytes());
            out.close();
        } catch (IOException f) { //Erreur lors de la création de fichier
            f.printStackTrace();
        }
    }

    public boolean isConnected() {
        return (getFromJson(ACCESS_TOKEN) != null) && (getFromJson(REFRESH_TOKEN) != null);
    }

    public String getFromJson(String key) {
        try {
            return fileJson.getString(key);
        } catch (JSONException e) {
            return null;
        }
    }

    public void setFromJson(String key, String value) {
        try {
            fileJson.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        saveFileJson();
    }

    /**
     * A utiliser lors de la déconnection de l'utilisateur
     */
    public void clearAll() {
        this.fileJson = new JSONObject();
        saveFileJson();
    }

}
