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
public class ImageFetchTask extends AsyncTask<Void, Bitmap, Void> {
    private static final String TAG = ImageFetchTask.class.getSimpleName();

    private IImageSettable imageView;
    private URL url;
    private ImageDownloadManager.Size size;
    private ImageDownloadManager manager;

    public ImageFetchTask(IImageSettable imageView, ImageDownloadManager.Size size,
                          ImageDownloadManager manager, URL url) {
        this.imageView = imageView;
        this.url = url;
        this.size = size;
        this.manager = manager;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            Log.v(TAG, "Fetching an image " + url.toString());

            Bitmap bm;
            bm = manager.getBitmapFromMemoryCache(url, size);
            if (bm == null) {
                bm = manager.getBitmapFromDiskCache(url, size);
                if (bm != null) {
                    Log.v(TAG, "Fetching an image from disk");
                    manager.addBitmapToMemCache(url, bm, size);
                }
            } else {
                Log.v(TAG, "Fetching an image from cache");
            }

            if (bm != null) {
                publishProgress(bm);
                return null;
            } else {
                Log.v(TAG, "Fetching an image from web");
                try {
                    bm = downloadBitmap(manager, url);

                    Bitmap returnedBitmap = null;
                    if (bm != null) {
                        final ImageDownloadManager.Size[] sizes = ImageDownloadManager.Size.values();

                        for (ImageDownloadManager.Size iterationSize : sizes) {
                            Integer resize = iterationSize.toInteger(manager);

                            Resources r = manager.getResources();
                            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, resize, r.getDisplayMetrics());
                            Bitmap scaled = scaleDown(bm, px);
                            if (iterationSize == ImageDownloadManager.Size.HEADER_BACKGROUND) {
                                scaled = BlurBuilder.blur(manager, scaled);
                            }

                            manager.addBitmapToDiskCache(url, scaled, iterationSize);
                            if (iterationSize == size) {
                                returnedBitmap = scaled;
                                manager.addBitmapToMemCache(url, scaled, iterationSize);
                            } else {
                                scaled.recycle();
                            }
                        }

                        bm.recycle();
                    }
                    publishProgress(returnedBitmap);
                    return null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Bitmap runConversions(@NonNull ImageDownloadManager manager, @NonNull URL url,
                                        @Nullable ImageDownloadManager.Size size, @NonNull Bitmap bm) {
        final ImageDownloadManager.Size[] sizes = ImageDownloadManager.Size.values();
        Bitmap returnedBitmap = null;

        for (ImageDownloadManager.Size iterationSize : sizes) {
            Integer resize = iterationSize.toInteger(manager);

            Resources r = manager.getResources();
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, resize, r.getDisplayMetrics());
            Bitmap scaled = scaleDown(bm, px);
            if (iterationSize == ImageDownloadManager.Size.HEADER_BACKGROUND) {
                scaled = BlurBuilder.blur(manager, scaled);
            }

            manager.addBitmapToDiskCache(url, scaled, iterationSize);
            if (iterationSize == size) {
                returnedBitmap = scaled;
                manager.addBitmapToMemCache(url, scaled, iterationSize);
            } else {
                scaled.recycle();
            }
        }

        return returnedBitmap;
    }

    @Nullable
    private static Bitmap downloadBitmap(Context context, URL url) throws IOException {
        final ServerConnection connection = new ServerConnection(context, url.toString());
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

    private static long getBitmapSize(@NonNull Bitmap bitmap) {
        long size;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            size = bitmap.getAllocationByteCount();
        } else {
            size = bitmap.getByteCount();
        }
        return size;
    }

    @Override
    protected void onProgressUpdate(Bitmap... values) {
        super.onProgressUpdate(values);
        Bitmap bitmap = values[0];
        if (bitmap != null) {
            Log.v(TAG, "Setting image with " + AttachedFile.humanReadableByteCount(getBitmapSize(bitmap)) + " size");
            imageView.setImage(bitmap);
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        manager.remove(imageView);
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

    public ImageDownloadManager.Size getSize() {
        return size;
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

