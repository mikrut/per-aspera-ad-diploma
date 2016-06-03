package ru.mail.park.chat.activities.views;

import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import ru.mail.park.chat.R;
import ru.mail.park.chat.loaders.images.IImageSettable;

/**
 * Created by mikrut on 10.04.16.
 */
public class TitledPicturedViewHolder extends RecyclerView.ViewHolder implements IImageSettable {
    protected final ImageView image;
    private final TextView imageText;
    private boolean bitmapIsSet = false;

    public TitledPicturedViewHolder(View itemView) {
        super(itemView);
        image = (ImageView) itemView.findViewById(R.id.image);
        imageText = (TextView) itemView.findViewById(R.id.imageText);
    }

    public void setTitle(String title) {
        if (!bitmapIsSet) {
            setTitle(image, imageText, title);
        } else {
            imageText.setVisibility(View.GONE);
        }
    }

    // TODO: make custom titled image class
    public static void setTitle(ImageView image, TextView imageText, String title) {
        if (title == null) {
            title = "";
        }

        image.setImageBitmap(null);
        int[] backgroundColors = image.getContext().getResources().getIntArray(R.array.colorsImageBackground);
        int colorIndex = (title.hashCode() % backgroundColors.length + backgroundColors.length) % backgroundColors.length;

        Drawable color = new ColorDrawable(backgroundColors[colorIndex]);
        image.setImageDrawable(color);
        imageText.setVisibility(View.VISIBLE);

        if (title.length() >= 2) {
            char firstLetter = title.charAt(0);
            char secondLetter = '\0';
            if (title.contains(" ") && title.indexOf(' ') + 1 < title.length())
                secondLetter = title.charAt(title.indexOf(' ') + 1);
            imageText.setText(new StringBuilder().append(firstLetter).append(secondLetter).toString().toUpperCase());
        } else {
            imageText.setText("");
        }
    }

    public ImageView getImage() {
        return image;
    }

    @Override
    public void setImage(Bitmap imageBitmap) {
        image.setImageBitmap(imageBitmap);
        bitmapIsSet = (imageBitmap != null);

        if (bitmapIsSet) {
            imageText.setVisibility(View.GONE);
        }
    }
}
