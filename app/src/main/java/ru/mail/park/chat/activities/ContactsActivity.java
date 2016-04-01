package ru.mail.park.chat.activities;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.List;

import ru.mail.park.chat.R;
import ru.mail.park.chat.activities.adapters.AContactAdapter;
import ru.mail.park.chat.activities.adapters.ContactAdapter;
import ru.mail.park.chat.loaders.ContactListDBLoader;
import ru.mail.park.chat.loaders.ContactListWebLoader;
import ru.mail.park.chat.models.Contact;

public class ContactsActivity extends AppCompatActivity {
    private RecyclerView contactsView;
    private LinearLayout findFriends;
    private SwipeRefreshLayout swipeRefreshLayout;

    private final static int DB_LOADER = 0;
    private final static int WEB_LOADER = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onSetContentView();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        findFriends = (LinearLayout) findViewById(R.id.findFriends);
        findFriends.setOnClickListener(findFriendsListener);

        contactsView = (RecyclerView) findViewById(R.id.contactsView);
        contactsView.setLayoutManager(new LinearLayoutManager(this));

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getLoaderManager().restartLoader(WEB_LOADER, null, contactsLoaderListener);
            }
        });
    }

    protected void onSetContentView() {
        setContentView(R.layout.activity_contacts);
    }

    protected AContactAdapter onCreateContactAdapter(@NonNull List<Contact> data) {
        return new ContactAdapter(data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(DB_LOADER, null, contactsLoaderListener);
    }

    View.OnClickListener findFriendsListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(ContactsActivity.this, ContactSearchActivity.class);
            startActivity(intent);
        }
    };

    LoaderManager.LoaderCallbacks<List<Contact>> contactsLoaderListener =
            new LoaderManager.LoaderCallbacks<List<Contact>>() {
                @Override
                public Loader<List<Contact>> onCreateLoader(int id, Bundle args) {
                    switch (id) {
                        case DB_LOADER:
                            return new ContactListDBLoader(ContactsActivity.this, id);
                        case WEB_LOADER:
                            return new ContactListWebLoader(ContactsActivity.this, id);
                        default:
                            return null;
                    }
                }

                @Override
                public void onLoadFinished(Loader<List<Contact>> loader, List<Contact> data) {
                    swipeRefreshLayout.setRefreshing(false);

                    if (data != null) {
                        contactsView.setAdapter(onCreateContactAdapter(data));
                    }

                    switch (loader.getId()) {
                        case DB_LOADER:
                            getLoaderManager().restartLoader(WEB_LOADER, null, contactsLoaderListener);
                            break;
                        case WEB_LOADER:
                            if (data == null)
                                Toast.makeText(ContactsActivity.this, "Load error. Check your connection.", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }

                @Override
                public void onLoaderReset(Loader<List<Contact>> loader) {
                    // TODO: something...
                }
            };
}
