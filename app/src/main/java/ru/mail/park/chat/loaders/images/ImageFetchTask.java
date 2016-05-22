package ru.mail.park.chat.loaders.images;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by Михаил on 22.05.2016.
 */
public class ImageFetchTask extends AsyncTask<Void, Void, Bitmap> {
    private ImageView imageView;
    private URL url;
    private ImageDownloadManager.Size size;
    private ImageDownloadManager manager;

    public ImageFetchTask(ImageView imageView, ImageDownloadManager.Size size,
                          ImageDownloadManager manager, URL url) {
        this.imageView = imageView;
        this.url = url;
        this.size = size;
        this.manager = manager;
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        try {
            Log.v(ImageFetchTask.class.getSimpleName(), "Fetching an image " + url.toString());
            final ImageDownloadManager.Size[] sizes = ImageDownloadManager.Size.values();

            Bitmap bm;
            bm = manager.getBitmapFromMemoryCache(url, size);
            if (bm == null) {
                bm = manager.getBitmapFromDiskCache(url, size);
                if (bm != null)
                    Log.v(ImageFetchTask.class.getSimpleName(), "Fetching an image from disk");
            } else {
                Log.v(ImageFetchTask.class.getSimpleName(), "Fetching an image from cache");
            }
            if (bm != null) {
                return bm;
            } else {
                Log.v(ImageFetchTask.class.getSimpleName(), "Fetching an image from web");
                try {
                    InputStream in = url.openStream();
                    bm = BitmapFactory.decodeStream(in);

                    if (bm != null) {
                        for (ImageDownloadManager.Size size : sizes) {
                            Integer resize = size.toInteger();
                            Bitmap scaled;
                            if (resize != null) {
                                scaled = scaleDown(bm, resize);
                            } else {
                                scaled = bm;
                            }
                            manager.addBitmapToCache(url, scaled, size);
                        }
                    }

                    return bm;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        }
        manager.remove(imageView);
    }

    private static Bitmap scaleDown(Bitmap realImage, float maxImageSize) {
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        return Bitmap.createScaledBitmap(realImage, width, height, false);
    }

    public URL getUrl() {
        return url;
    }

    public ImageDownloadManager.Size getSize() {
        return size;
    }
}

