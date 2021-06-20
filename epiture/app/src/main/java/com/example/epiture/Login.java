package com.example.epiture;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import ca.mimic.oauth2library.OAuth2Client;
import ca.mimic.oauth2library.OAuthResponse;

public class Login extends AppCompatActivity {
    private Context context;
    private EditText usernameText;
    private EditText passwordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.activity_login);

        InternalMemory memory = new InternalMemory(this);

        this.usernameText = findViewById(R.id.text_username);
        this.passwordText = findViewById(R.id.text_password);

        Button buttonLogin = findViewById(R.id.button_login);
        Button buttonCreateAccount = findViewById(R.id.button_create_account);


        buttonLogin.setOnClickListener(v -> {
            String username = usernameText.getText().toString();
            String password = passwordText.getText().toString();

            requestToken asyncTask = (requestToken) new requestToken(output -> {
                System.out.println("OUTPUT: " + output);
                try {
                    JSONObject json_output = new JSONObject(output);
                    if (json_output.getString("success").equals("true")) {
                        memory.setFromJson(memory.ACCESS_TOKEN, json_output.getString("access_token"));
                        memory.setFromJson(memory.REFRESH_TOKEN, json_output.getString("refresh_token"));
                        memory.setFromJson(memory.USERNAME, username);
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(context, "Nom d'utilisateur ou mot de passe incorrect", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Nom d'utilisateur ou mot de passe incorrect", Toast.LENGTH_SHORT).show();
                }
            }, username, password).execute();
        });

        buttonCreateAccount.setOnClickListener(v -> {
            String url = "https://imgur.com/register";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });

    }

    private static final class requestToken extends AsyncTask<Void, Void, String> {

        public final AsyncResponse delegate;
        private final String username;
        private final String password;

        public requestToken(AsyncResponse delegate, String username, String password) {
            this.delegate = delegate;
            this.username = username;
            this.password = password;
        }

        @Override
        protected String doInBackground(Void... params) {
            String output;
            OAuth2Client client = new OAuth2Client.Builder(username, password, "clientId", "clientSecret", "https://api.imgur.com/oauth2/token").build();
            OAuthResponse response = null;
            try {
                response = client.requestAccessToken();
            } catch (IOException e) {
                e.printStackTrace();
            }
            assert response != null;
            System.out.println("RESPONSE : " + response.getHttpResponse().message());
            if (response.isSuccessful()) {
                String accessToken = response.getAccessToken();
                String refreshToken = response.getRefreshToken();
                output = "{access_token: " + accessToken + ", refresh_token: " + refreshToken + ", success: true";
            } else {
                output = "{success: false";
            }

            output += ",status_code: " + response.getCode();

            output += "}";


            return output;
        }

        @Override
        protected void onPostExecute(String result) {
            delegate.processFinish(result);
        }
    }

}
