package ru.mail.park.chat.loaders.images;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.widget.ImageView;

import com.google.android.gms.common.api.Api;

import org.apache.commons.io.IOUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ru.mail.park.chat.api.ApiSection;
import ru.mail.park.chat.api.BlurBuilder;
import ru.mail.park.chat.api.network.ServerConnection;
import ru.mail.park.chat.database.AttachmentsContract;
import ru.mail.park.chat.models.AttachedFile;
import ru.mail.park.chat.models.OwnerProfile;

/**
 * Created by Михаил on 22.05.2016.
 */
public class ImageFetchTask extends AsyncTask<Void, Void, Bitmap> {
    private static final String TAG = ImageFetchTask.class.getSimpleName();

    private IImageSettable imageView;
    private URL url;
    private ImageDownloadManager manager;
    @Nullable
    private IImageFilter filter;
    private int width;
    private int height;

    public ImageFetchTask(@NonNull IImageSettable imageView, @NonNull ImageDownloadManager manager, URL url, @Nullable IImageFilter filter) {
        this.imageView = imageView;
        this.url = url;
        this.manager = manager;
        this.filter = filter;

        width = imageView.getImageWidth();
        height = imageView.getImageHeight();
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        try {
            Log.v(TAG, "Fetching an image " + url.toString());

            Bitmap bm;
            bm = manager.getBitmapFromMemoryCache(url, width, height, filter);
            if (bm == null) {
                bm = manager.getBitmapFromDiskCache(url, width, height, filter);
                if (bm != null) {
                    Log.v(TAG, "Fetching an image from disk");
                    manager.addBitmapToMemCache(url, bm, width, height, filter);
                }
            } else {
                Log.v(TAG, "Fetching an image from cache");
            }

            if (bm != null) {
                return bm;
            } else {
                Log.v(TAG, "Fetching an image from web");
                try {
                    bm = downloadBitmap(manager, url, width, height);

                    if (bm != null) {
                        if (filter != null) {
                            bm = filter.filter(manager, bm);
                        }
                        manager.addBitmapToDiskCache(url, bm, width, height, filter);
                        manager.addBitmapToMemCache(url, bm, width, height, filter);
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

    @Nullable
    private static Bitmap downloadBitmap(Context context, URL url, int width, int height) throws IOException {
        final ServerConnection connection = new ServerConnection(context, ApiSection.SERVER_URL + "/file/image");

        final List<Pair<String, Object>> paramters = new ArrayList<>();
        paramters.add(new Pair<String, Object>("path", url.getPath()));
        paramters.add(new Pair<String, Object>("width", width));
        paramters.add(new Pair<String, Object>("height", height));
        paramters.add(new Pair<String, Object>(ApiSection.AUTH_TOKEN_PARAMETER_NAME, (new OwnerProfile(context).getAuthToken())));
        connection.setParameters(paramters);

        connection.setRequestMethod("GET");
        final HttpURLConnection httpURLConnection = connection.getConnection();

        try {
            if (httpURLConnection != null) {
                final InputStream inputStream = ServerConnection.getResponseStream(httpURLConnection);
                try {
                    if (inputStream != null) {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.RGB_565;
                        Bitmap bm = BitmapFactory.decodeStream(inputStream, null, options);

                        Log.v(TAG, "Downloaded " + AttachedFile.humanReadableByteCount(getBitmapSize(bm)));
                        return bm;
                    }
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                }
            }
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
        return null;
    }

    private static long getBitmapSize(@Nullable Bitmap bitmap) {
        if (bitmap != null) {
            long size;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                size = bitmap.getAllocationByteCount();
            } else {
                size = bitmap.getByteCount();
            }
            return size;
        }
        return 0;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        manager.remove(imageView);
        if (bitmap != null) {
            Log.v(TAG, "Setting image with " + AttachedFile.humanReadableByteCount(getBitmapSize(bitmap)) + " size");
            imageView.setImage(bitmap);
        }
    }

    private static Bitmap scaleDown(Bitmap realImage, float maxImageSizePx) {
        float ratio = Math.min(
                (float) maxImageSizePx / realImage.getWidth(),
                (float) maxImageSizePx / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        return getResizedBitmap(realImage, height, width);
    }

    public URL getUrl() {
        return url;
    }

    private static Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth)
    {
        int width = bm.getWidth();
        int height = bm.getHeight();
        if (width == newWidth && height == newHeight) {
            return bm;
        }
        Log.i("width", String.valueOf(width));
        Log.i("height", String.valueOf(height));


        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        float middleX = newWidth / 2.0f;
        float middleY = newHeight / 2.0f;
        // create a matrix for the manipulation
        Matrix matrix = new Matrix();
        // resize the bit map
        matrix.setScale(scaleWidth, scaleHeight, middleX, middleY);
        // recreate the new Bitmap
        Bitmap scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(matrix);
        canvas.drawBitmap(bm, middleX - bm.getWidth() / 2, middleY - bm.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));
        return scaledBitmap;
    }
}

