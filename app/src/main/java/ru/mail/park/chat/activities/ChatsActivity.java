package ru.mail.park.chat.activities;

import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Loader;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import java.util.List;

import ru.mail.park.chat.NetcipherTester;
import ru.mail.park.chat.R;
import ru.mail.park.chat.loaders.ChatLoader;
import ru.mail.park.chat.models.Chat;

public class ChatsActivity extends AppCompatActivity {
    protected FloatingActionButton fab;
    private RecyclerView chatsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);

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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chats, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

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

}

