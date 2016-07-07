package ru.mail.park.chat.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ru.mail.park.chat.loaders.images.ImageDownloadManager;

/**
 * Created by Михаил on 03.06.2016.
 */

// TODO: think about using composition instead of inheritance
public abstract class AImageDownloadServiceBindingActivity extends AppCompatActivity {
    private List<IImageDownloadManagerBinderSubscriber> subscribers = new ArrayList<>();

    public interface IImageDownloadManagerBinderSubscriber {
        void onImageDownloadManagerAvailable(@NonNull ImageDownloadManager idm);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to ImageDownloadService
        Log.i(AImageDownloadServiceBindingActivity.class.getSimpleName(), ".onStart()");
        Intent intent = new Intent(this, ImageDownloadManager.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private ImageDownloadManager imageDownloadManager;
    private boolean bound = false;

    @Override
    protected void onStop() {
        super.onStop();
        if (bound) {
            unbindService(mConnection);
            bound = false;
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            ImageDownloadManager.ImageDownloadBinder binder =
                    (ImageDownloadManager.ImageDownloadBinder) service;
            imageDownloadManager = binder.getService();
            nonOverridableOnSetImageManager(imageDownloadManager);
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    private void nonOverridableOnSetImageManager(ImageDownloadManager mgr) {
        for (IImageDownloadManagerBinderSubscriber subscriber : subscribers) {
            subscriber.onImageDownloadManagerAvailable(mgr);
        }

        onSetImageManager(mgr);
    }

    protected void onSetImageManager(ImageDownloadManager mgr) {

    }

    protected ImageDownloadManager getImageDownloadManager() {
        return imageDownloadManager;
    }

    public void subscribe(IImageDownloadManagerBinderSubscriber subscriber) {
        subscribers.add(subscriber);
        if (imageDownloadManager != null) {
            subscriber.onImageDownloadManagerAvailable(imageDownloadManager);
        }
    }
}
