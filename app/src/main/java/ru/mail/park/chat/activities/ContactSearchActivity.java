package ru.mail.park.chat.activities;

import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;

import java.util.List;

import ru.mail.park.chat.R;
import ru.mail.park.chat.activities.adapters.ContactSearchAdapter;
import ru.mail.park.chat.database.ContactHelper;
import ru.mail.park.chat.loaders.ContactListDBWhereLoader;
import ru.mail.park.chat.loaders.ContactsSearchLoader;
import ru.mail.park.chat.models.Contact;

public class ContactSearchActivity extends AppCompatActivity {
    private SearchView searchView;
    private RecyclerView contactsView;

    private static final int SEARCH_LOADER_ID = 0;
    private static final int SEARCH_BASE_LOADER_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        contactsView = (RecyclerView) findViewById(R.id.contactsView);
        contactsView.setLayoutManager(new LinearLayoutManager(this));
        getLoaderManager().initLoader(SEARCH_LOADER_ID, null, contactsLoaderListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chats, menu);

        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        MenuItemCompat.setOnActionExpandListener(searchMenuItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                ContactSearchActivity.this.finish();
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                return false;
            }
        });

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        searchView.requestFocus();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                getLoaderManager().restartLoader(SEARCH_BASE_LOADER_ID, null, contactsLoaderListener);
                if(contactsView.getAdapter().getItemCount() <= 0)
                    getLoaderManager().restartLoader(SEARCH_LOADER_ID, null, contactsLoaderListener);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                getLoaderManager().restartLoader(SEARCH_BASE_LOADER_ID, null, contactsLoaderListener);
                if(contactsView.getAdapter().getItemCount() <= 0)
                    getLoaderManager().restartLoader(SEARCH_LOADER_ID, null, contactsLoaderListener);

                return true;
            }
        });

        searchMenuItem.expandActionView();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            // searchView.setIconified(false);
            searchView.requestFocusFromTouch();
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    LoaderManager.LoaderCallbacks<List<Contact>> contactsLoaderListener =
            new LoaderManager.LoaderCallbacks<List<Contact>>() {
                @Override
                public Loader<List<Contact>> onCreateLoader(int id, Bundle args) {
                    ContactsSearchLoader loader = new ContactsSearchLoader(ContactSearchActivity.this);
                    if (searchView != null) {
                        loader.setSearchQuery(searchView.getQuery().toString());
                    } else {
                        loader.setSearchQuery("");
                    }
                    return loader;
                }

                @Override
                public void onLoadFinished(Loader<List<Contact>> loader, List<Contact> data) {
                    if (data != null) {
                        contactsView.setAdapter(new ContactSearchAdapter(data));
                    }
                }

                @Override
                public void onLoaderReset(Loader<List<Contact>> loader) {
                    ContactsSearchLoader searchLoader = (ContactsSearchLoader) loader;
                    searchLoader.setSearchQuery(searchView.getQuery().toString());
                }
            };

    LoaderManager.LoaderCallbacks<List<Contact>> contactsLoaderBaseListener =
            new LoaderManager.LoaderCallbacks<List<Contact>>() {
                @Override
                public Loader<List<Contact>> onCreateLoader(int id, Bundle args) {
                    ContactListDBWhereLoader loader = new ContactListDBWhereLoader(ContactSearchActivity.this);
                    if (searchView != null) {
                        loader.setSearchQuery(searchView.getQuery().toString());
                    } else {
                        loader.setSearchQuery("");
                    }
                    return loader;
                }

                @Override
                public void onLoadFinished(Loader<List<Contact>> loader, List<Contact> data) {
                    if (data != null) {
                        contactsView.setAdapter(new ContactSearchAdapter(data));
                    }
                }

                @Override
                public void onLoaderReset(Loader<List<Contact>> loader) {
                    ContactListDBWhereLoader searchLoader = (ContactListDBWhereLoader) loader;
                    searchLoader.setSearchQuery(searchView.getQuery().toString());
                }
            };
}
