package ru.mail.park.chat.activities;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.TreeSet;

import ru.mail.park.chat.R;
import ru.mail.park.chat.activities.adapters.PagerContactsAdapter;
import ru.mail.park.chat.activities.fragments.ContactsFragment;
import ru.mail.park.chat.loaders.images.ImageDownloadManager;
import ru.mail.park.chat.models.Contact;

public class ContactsActivity
        extends AImageDownloadServiceBindingActivity
        implements ContactsFragment.OnPickEventListener {
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
    protected void onSetImageManager(ImageDownloadManager mgr) {
        if (pagerAdapter != null) {
            pagerAdapter.setImageManager(mgr);
        }
    }

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
