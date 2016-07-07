package ru.mail.park.chat;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by Михаил on 25.06.2016.
 */
public class AnalyticsApplication extends Application {
    private Tracker mTracker;

    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            analytics.enableAutoActivityReports(this);
            mTracker = analytics.newTracker(R.xml.app_tracker);
            mTracker.enableAdvertisingIdCollection(true);
        }
        return mTracker;
    }
}
