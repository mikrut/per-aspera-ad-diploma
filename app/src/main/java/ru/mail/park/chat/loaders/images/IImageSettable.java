package ru.mail.park.chat.loaders.images;

import android.graphics.Bitmap;

/**
 * Created by mikrut on 22.05.16.
 */
public interface IImageSettable {
    void setImage(Bitmap image);
    int getWidth();
    int getHeight();
}
