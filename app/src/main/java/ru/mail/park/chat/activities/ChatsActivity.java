package ru.mail.park.chat.activities;

import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;

import java.util.List;

import ru.mail.park.chat.NetcipherTester;
import ru.mail.park.chat.R;
import ru.mail.park.chat.activities.adapters.ChatsAdapter;
import ru.mail.park.chat.activities.adapters.MenuAdapter;
import ru.mail.park.chat.loaders.ChatLoader;
import ru.mail.park.chat.models.Chat;
import ru.mail.park.chat.models.OwnerProfile;

public class ChatsActivity extends AppCompatActivity {
    protected FloatingActionButton fab;
    private RecyclerView chatsList;
    private SearchView searchView;
    private SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                toolbar,
                R.string.open,
                R.string.close
        );
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Query queryer = new Query();
                queryer.execute((String[]) null);
            }
        });

        chatsList = (RecyclerView) findViewById(R.id.chatsList);
        chatsList.setLayoutManager(new LinearLayoutManager(this));

        getLoaderManager().initLoader(0, null, messagesLoaderListener);

        // TODO: real menu options
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.left_drawer);
        mRecyclerView.setHasFixedSize(true);
        String[] titles = {"Edit profile","Show profile","Contacts", "Help"};
        View.OnClickListener[] listeners = {new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatsActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        },new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatsActivity.this, UserProfileActivity.class);
                startActivity(intent);
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatsActivity.this, ContactsActivity.class);
                startActivity(intent);
            }
        }, null};
        int[] pictures = {
                R.drawable.ic_edit_black_24dp,
                R.drawable.ic_person_black_48dp,
                R.drawable.ic_group_black_24dp,
                R.drawable.ic_help_black_24dp
            };
        OwnerProfile owner = new OwnerProfile(this);
        RecyclerView.Adapter mAdapter = new MenuAdapter(
                owner.getLogin(),
                owner.getEmail(),
                R.drawable.ic_user_picture,
                titles,
                listeners,
                pictures);
        mRecyclerView.setAdapter(mAdapter);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeContainer.setOnRefreshListener(refreshListener);
    }

    class Query extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return NetcipherTester.testNetcipher(ChatsActivity.this);
        }

        @Override
        protected void onPostExecute(String s) {
            Snackbar.make(fab, s, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chats, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);

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


    LoaderManager.LoaderCallbacks<List<Chat>> messagesLoaderListener =
            new LoaderManager.LoaderCallbacks<List<Chat>>() {
                @Override
                public Loader<List<Chat>> onCreateLoader(int id, Bundle args) {
                    return new ChatLoader(ChatsActivity.this);
                }

                @Override
                public void onLoadFinished(Loader<List<Chat>> loader, List<Chat> data) {
                    chatsList.setAdapter(new ChatsAdapter(data));
                }

                @Override
                public void onLoaderReset(Loader<List<Chat>> loader) {
                    // TODO: something...
                }
            };

    SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {

        }
    };

}

