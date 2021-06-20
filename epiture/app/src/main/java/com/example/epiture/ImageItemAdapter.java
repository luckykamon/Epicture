package com.example.epiture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.List;

public class ImageItemAdapter extends BaseAdapter {
    public final Context context;
    private final List<ImageItem> imageItemList;
    private final LayoutInflater inflater;
    private final ConstraintLayout pleinEcranLayout;
    private final ConstraintLayout principalLayout;
    private final ImageView imageFond;

    /**
     * @param imageItemList liste des ImageItem à affichés
     */
    public ImageItemAdapter(Context context, List<ImageItem> imageItemList, ConstraintLayout principalLayout, ConstraintLayout pleinEcranLayout, ImageView imageFond) {
        this.context = context;
        this.imageItemList = imageItemList;
        this.inflater = LayoutInflater.from(context);
        this.pleinEcranLayout = pleinEcranLayout;
        this.principalLayout = principalLayout;
        this.imageFond = imageFond;
    }

    private void saveToGallery(ImageView imageView) {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = bitmapDrawable.getBitmap();

        String filename = String.format("%d.jpg", System.currentTimeMillis());

        MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, filename, "");
    }

    @Override
    public int getCount() { //nb images sur la page
        return Math.min(imageItemList.size(), 20);
    }

    @Override
    public ImageItem getItem(int position) {
        return imageItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = inflater.inflate(R.layout.adapter_item, null);

        ImageView imageView = view.findViewById(R.id.imageView);
        ImageView buttonFavorite = view.findViewById(R.id.favorite_button);
        ImageView buttonDownload = view.findViewById(R.id.download_button);
        ImageView buttonPleinEcran = view.findViewById(R.id.plein_ecran_button);

        ImageItem imageItem = getItem(position);
        String imageLink = imageItem.getLink();
        Boolean isFavorite = imageItem.isFavorite();

        new DownLoadImageTask(imageView).execute(imageLink);

        if (isFavorite) {
            buttonFavorite.setTag("yellow");
            buttonFavorite.setColorFilter(context.getResources().getColor(R.color.yellow));
        } else {
            buttonFavorite.setTag("black");
            buttonFavorite.setColorFilter(context.getResources().getColor(R.color.black));
        }

        buttonPleinEcran.setOnClickListener(v -> {
            principalLayout.setVisibility(View.GONE);
            pleinEcranLayout.setVisibility(View.VISIBLE);

            imageFond.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageFond.setImageDrawable(imageView.getDrawable());
        });

        buttonFavorite.setOnClickListener(v -> {
            if (buttonFavorite.getTag().toString().equals("yellow")) {
                // code no-favori image
                buttonFavorite.setTag("black");
                buttonFavorite.setColorFilter(context.getResources().getColor(R.color.black));

                imageItem.setFavorite(false);
            } else {
                // code favori image
                buttonFavorite.setTag("yellow");
                buttonFavorite.setColorFilter(context.getResources().getColor(R.color.yellow));

                imageItem.setFavorite(true);
            }
            Functions.setImageFavorite(imageItem, context);
        });

        buttonDownload.setOnClickListener(v -> {
            // code download image

            saveToGallery(imageView);
            Toast.makeText(context, context.getResources().getString(R.string.download_image), Toast.LENGTH_SHORT).show();
        });

        return view;
    }
}
