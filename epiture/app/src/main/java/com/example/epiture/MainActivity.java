package com.example.epiture;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int IMAGE_CODE = 1000;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private final List<ImageItem> imageItemList = new ArrayList<>();
    private EditText rechercheText;
    private ImageView loginLogoutButton;
    private RadioButton tagMyPictures;
    private RadioButton tagMyFavorites;
    private RadioButton tagRecheche;
    private LinearLayout tagsLayout;
    private GridView gridImages;
    private InternalMemory memory;
    private ConstraintLayout principalLayout;
    private ConstraintLayout pleinEcranLayout;
    private ImageView pleinEcranImage;

    /**
     * @param recherche         contenu du le champs recheche de l'application
     * @param hasTagMyPictures  true si le tag mes photos est coché
     * @param hasTagMyFavorites true si le tag mes favoris est coché
     */
    private void addAllImages(String recherche, Boolean hasTagMyPictures, Boolean hasTagMyFavorites) {
        imageItemList.clear();
        Functions.getImages(recherche, hasTagMyPictures, hasTagMyFavorites, this, getApplicationContext());
    }

    public void callBackAddAllImages(List<ImageItem> imageItemList) {
        gridImages.setAdapter(new ImageItemAdapter(this, imageItemList, principalLayout, pleinEcranLayout, pleinEcranImage));
    }

    private void rechercheImages() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        rechercheText.clearFocus();
        tagsLayout.setVisibility(View.GONE);

        gridImages.setAdapter(null);

        this.addAllImages(rechercheText.getText().toString(), tagMyPictures.isChecked(), tagMyFavorites.isChecked());
    }

    /**
     * helper to retrieve the path of an image URI
     */
    private String getPath(Uri uri) {
        // just some safety built in
        if (uri == null) {
            // TODO perform some logging or show user feedback
            return null;
        }
        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        // this is our fallback here
        return uri.getPath();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IMAGE_CODE) {
                Uri selectedImage = data.getData();
                Functions.postImage(this, getPath(selectedImage));
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        requestPermissions(permissions, PERMISSION_REQUEST_CODE);
        this.memory = new InternalMemory(this);

        setContentView(R.layout.activity_main);

        ImageView addButton = findViewById(R.id.add_button);
        this.rechercheText = findViewById(R.id.recherche_text);
        ImageView rechercheButton = findViewById(R.id.recherche_button);
        ImageView tagButton = findViewById(R.id.tag_button);
        this.loginLogoutButton = findViewById(R.id.login_logout_button);
        this.tagMyPictures = findViewById(R.id.tag_my_pictures);
        this.tagMyFavorites = findViewById(R.id.tag_my_favorites);
        this.tagRecheche = findViewById(R.id.tag_recherche);
        this.tagsLayout = findViewById(R.id.tags_layout);
        this.gridImages = findViewById(R.id.images_grid);
        ImageView pleinEcranButton = findViewById(R.id.layout_plein_ecran_button);
        this.pleinEcranLayout = findViewById(R.id.layout_plein_ecran);
        this.pleinEcranImage = findViewById(R.id.layout_plein_ecran_image);
        this.principalLayout = findViewById(R.id.layout_principal);


        if (memory.isConnected()) {
            loginLogoutButton.setTag("login");
            loginLogoutButton.setImageDrawable(getResources().getDrawable(R.drawable.login));
        } else {
            loginLogoutButton.setTag("logout");
            loginLogoutButton.setImageDrawable(getResources().getDrawable(R.drawable.logout));
        }

        tagRecheche.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                tagMyPictures.setChecked(false);
                tagMyFavorites.setChecked(false);
            }
        });

        tagMyPictures.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                tagRecheche.setChecked(false);
                tagMyFavorites.setChecked(false);
            }
        });

        tagMyFavorites.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                tagMyPictures.setChecked(false);
                tagRecheche.setChecked(false);
            }
        });

        addButton.setOnClickListener(v -> {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, IMAGE_CODE);
        });

        pleinEcranButton.setOnClickListener(v -> {
            pleinEcranLayout.setVisibility(View.GONE);
            principalLayout.setVisibility(View.VISIBLE);
        });

        rechercheText.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                rechercheImages();
                return true;
            }
            return false;
        });

        rechercheButton.setOnClickListener(v -> {
            rechercheImages();

            View view = getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });

        loginLogoutButton.setOnClickListener(v -> {
            if (loginLogoutButton.getTag().toString().equals("login")) {
                memory.clearAll();
                Toast.makeText(this, "Déconnexion réussi", Toast.LENGTH_SHORT).show();
                loginLogoutButton.setTag("logout");
                loginLogoutButton.setImageDrawable(getResources().getDrawable(R.drawable.logout));
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
                loginLogoutButton.setTag("login");
                loginLogoutButton.setImageDrawable(getResources().getDrawable(R.drawable.login));

            }
        });

        tagButton.setOnClickListener(v -> {
            if (tagsLayout.getVisibility() == View.VISIBLE) tagsLayout.setVisibility(View.GONE);
            else if (tagsLayout.getVisibility() == View.GONE)
                tagsLayout.setVisibility(View.VISIBLE);
        });
    }
}
