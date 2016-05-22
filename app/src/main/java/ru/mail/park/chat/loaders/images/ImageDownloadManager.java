package ru.mail.park.chat.loaders.images;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Михаил on 22.05.2016.
 */
public class ImageDownloadManager extends Service {
    public enum Size {
        SMALL,
        NORMAL;

        @Nullable
        public Integer toInteger() {
            switch (this) {
                case SMALL:
                    return 50;
                case NORMAL:
                default:
                    return null;
            }
        }
    }

    private final IBinder mBinder = new ImageDownloadBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class ImageDownloadBinder extends Binder {
        public ImageDownloadManager getService() {
            return ImageDownloadManager.this;
        }
    }

    private final BlockingQueue<Runnable> mFetchImageWorkQueue = new LinkedBlockingQueue<>();
    private ThreadPoolExecutor downloadThreadPool;
    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private static final int NUMBER_OF_THREADS = 5;

    private static final String UNIQUE_NAME = "images";

    private DiskLruCache diskCache;
    private static final int VALUE_COUNT = 1;
    private static final long MAX_SIZE = (long)(20 * Math.pow(2, 20));

    private LruCache<String, Bitmap> memoryCache;
    private static final int MAX_MEMCACHE_SIZE = 10;

    private Map<IImageSettable, ImageFetchTask> tasks = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(ImageDownloadManager.class.getSimpleName(), "Created a manager");
        memoryCache = new LruCache<>(MAX_MEMCACHE_SIZE);

        try {
            File cache = getDiskCacheDir(UNIQUE_NAME);
            diskCache = DiskLruCache.open(cache, 1, VALUE_COUNT, MAX_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        downloadThreadPool = new ThreadPoolExecutor(
                NUMBER_OF_THREADS,
                NUMBER_OF_THREADS * 2,
                KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT,
                mFetchImageWorkQueue
        );
    }

    private static String paramsToName(URL url, Size size) {
        return (url.getFile().replace('/','_').replace('.','_') + "_" + size.toString()).toLowerCase();
    }

    public void addBitmapToCache(URL url, Bitmap bitmap, Size size) {
        String bitmapName = paramsToName(url, size);
        memoryCache.put(bitmapName, bitmap);

        try {
            DiskLruCache.Editor editor = diskCache.edit(bitmapName);
            if (editor != null) {
                OutputStream out = editor.newOutputStream(0);
                bitmap.compress(Bitmap.CompressFormat.PNG, 0, out);
                diskCache.flush();
                editor.commit();
                Log.v(ImageDownloadManager.class.getSimpleName(), "Saved " + bitmapName);
            } else {
                Log.v(ImageDownloadManager.class.getSimpleName(), "Disk cache unavailable");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    Bitmap getBitmapFromMemoryCache(URL url, Size size) {
        String bitmapName = paramsToName(url, size);
        return memoryCache.get(bitmapName);
    }

    @Nullable
    Bitmap getBitmapFromDiskCache(URL url, Size size) {
        String bitmapName = paramsToName(url, size);
        try {
            DiskLruCache.Snapshot snapshot = diskCache.get(bitmapName);
            if (snapshot != null) {
                InputStream is = snapshot.getInputStream(0);
                if (is != null) {
                    return BitmapFactory.decodeStream(is);
                }
                snapshot.close();
            } else {
                Log.v(ImageDownloadManager.class.getSimpleName(), "no snapshot " + bitmapName);
            }
        } catch (IllegalArgumentException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setImage(@NonNull IImageSettable imageView, @NonNull URL path,
                         @NonNull Size size) {
        Log.v(ImageDownloadManager.class.getSimpleName(), ".setImage()");
        if (cancleGetImage(imageView, path, size)) {
            ImageFetchTask task = new ImageFetchTask(imageView, size, this, path);
            tasks.put(imageView, task);
            task.executeOnExecutor(downloadThreadPool);
        }
    }

    private boolean cancleGetImage(@NonNull IImageSettable imageView, @NonNull URL path, @NonNull Size size) {
        ImageFetchTask task = tasks.get(imageView);
        if (task != null) {
            if (task.getUrl().equals(path) && task.getSize().equals(size)) {
                return false;
            } else {
                task.cancel(false);
                tasks.remove(imageView);
                return true;
            }
        }
        return true;
    }

    void remove(IImageSettable image) {
        tasks.remove(image);
    }

    // Creates a unique subdirectory of the designated app cache directory. Tries to use external
    // but if not mounted, falls back on internal storage.
    private File getDiskCacheDir(String uniqueName) {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        File externalDir = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                !Environment.isExternalStorageRemovable() ? getExternalCacheDir() : null;
        final String cachePath = (externalDir != null) ?
                externalDir.getPath() :
                getCacheDir().getPath();

        File f = new File(cachePath + File.separator + uniqueName);
        if (!f.exists())
            f.mkdirs();
        return f;
    }
}
