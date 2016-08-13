package ru.mail.park.chat.loaders.images;

import android.content.Context;
import android.graphics.Bitmap;

/**
 * Created by Михаил on 13.08.2016.
 */
public interface IImageFilter {
    Bitmap filter(Context context, Bitmap image);
}
