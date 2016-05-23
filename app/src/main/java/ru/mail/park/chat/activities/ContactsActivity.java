package ru.mail.park.chat.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import java.util.TreeSet;

import ru.mail.park.chat.R;
import ru.mail.park.chat.activities.adapters.ChatsAdapter;
import ru.mail.park.chat.activities.adapters.PagerContactsAdapter;
import ru.mail.park.chat.activities.fragments.ContactsFragment;
import ru.mail.park.chat.loaders.images.ImageDownloadManager;
import ru.mail.park.chat.models.Contact;

public class ContactsActivity extends AppCompatActivity implements ContactsFragment.OnPickEventListener {
    ViewPager viewPager;
    PagerContactsAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        pagerAdapter = new PagerContactsAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null && toolbar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }

        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to ImageDownloadService
        Log.i(ChatsActivity.class.getSimpleName(), ".onStart()");
        Intent intent = new Intent(this, ImageDownloadManager.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private ImageDownloadManager mgr;
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
            mgr = binder.getService();
            if (pagerAdapter != null) {
                pagerAdapter.setImageManager(mgr);
            }
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    @Override
    public void onContactSetChanged(TreeSet<Contact> chosenContacts) {

    }

    @Override
    public void onContactClicked(Contact contact) {
        Intent intent = new Intent(this, ProfileViewActivity.class);
        intent.putExtra(ProfileViewActivity.UID_EXTRA, contact.getUid());
        startActivity(intent);
    }
}
