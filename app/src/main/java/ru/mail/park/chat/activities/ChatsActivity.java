package ru.mail.park.chat.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import ru.mail.park.chat.AnalyticsApplication;
import ru.mail.park.chat.R;
import ru.mail.park.chat.activities.adapters.ChatsAdapter;
import ru.mail.park.chat.activities.adapters.MenuAdapter;
import ru.mail.park.chat.activities.interfaces.IUserPicSetupListener;
import ru.mail.park.chat.api.BlurBuilder;
import ru.mail.park.chat.database.ChatsHelper;
import ru.mail.park.chat.helpers.ScrollEndlessPagination;
import ru.mail.park.chat.loaders.ChatLoader;
import ru.mail.park.chat.loaders.ChatSearchLoader;
import ru.mail.park.chat.loaders.ChatWebLoader;
import ru.mail.park.chat.loaders.images.ImageDownloadManager;
import ru.mail.park.chat.models.Chat;
import ru.mail.park.chat.models.OwnerProfile;

public class ChatsActivity
        extends AImageDownloadServiceBindingActivity {
    private FloatingActionButton fab;
    private RecyclerView chatsList;
    private SearchView searchView;
    private SwipeRefreshLayout swipeContainer;
    private ProgressBar pbChats;

    private LinearLayoutManager liman;
    private ScrollEndlessPagination pagination;

    private MaterialMenuDrawable mToolbarMorphDrawable;
    private MaterialMenuDrawable mSearchViewMorphDrawable;
    private Toolbar toolbar;
    private Bitmap bmBlurred;

    private String uid;

    public static final int CHAT_WEB_LOADER = 0;
    public static final int CHAT_SEARCH_LOADER = 1;
    public static final int CHAT_DB_LOADER = 2;

    private static final String CHATS_DATA = ChatsActivity.class.getCanonicalName() + ".CHATS_DATA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_drawer);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        pbChats = (ProgressBar) findViewById(R.id.pb_chats);
        pbChats.getIndeterminateDrawable().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
        pbChats.setVisibility(View.VISIBLE);
        setSupportActionBar(toolbar);
        setupActionBar();

        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                toolbar,
                R.string.open,
                R.string.close
        );
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatsActivity.this, DialogCreateActivity.class);
                startActivity(intent);
            }
        });

        chatsList = (RecyclerView) findViewById(R.id.chatsList);
        liman = new LinearLayoutManager(this);
        liman.setReverseLayout(true);
        liman.setStackFromEnd(true);
        chatsList.setLayoutManager(liman);
        pagination = new ScrollEndlessPagination<>(liman, chatsLoaderListener, CHAT_WEB_LOADER, getLoaderManager());
        pagination.setPageSize(4);
        chatsList.addOnScrollListener(pagination);

        // TODO: real menu options
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.left_drawer);
        mRecyclerView.setHasFixedSize(true);

        String[] titles = {
                getString(R.string.action_show_profile),
                getString(R.string.action_group_chats),
                getString(R.string.contacts),
                getString(R.string.action_settings),
                getString(R.string.action_log_out)
        };
        View.OnClickListener[] listeners = {new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatsActivity.this, ProfileViewActivity.class);
                startActivity(intent);
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatsActivity.this, GroupDialogCreateActivity.class);
                startActivity(intent);
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatsActivity.this, ContactsActivity.class);
                startActivity(intent);
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatsActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        },
           new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    OwnerProfile ownerProfile = new OwnerProfile(ChatsActivity.this);
                    ownerProfile.logout(ChatsActivity.this);
                    ChatsActivity.this.finish();
                }
            }
        };

        int[] pictures = {
                R.drawable.ic_person_black_48dp,
                R.drawable.ic_group_black_24dp,
                R.drawable.ic_contacts_black_24dp,
                R.drawable.ic_settings_black_24dp,
                R.drawable.ic_lock_black_24dp
            };
        OwnerProfile owner = new OwnerProfile(this);

        uid = owner.getUid();
        Log.d("[TP-diploma]", "URL to load from: http://p30480.lab1.stud.tech-mail.ru/" + owner.getImg());

        String filePath = Environment.getExternalStorageDirectory() + "/torchat/avatars/users/" + uid + ".bmp";

        RecyclerView.Adapter mAdapter = new MenuAdapter(
                owner.getLogin(),
                owner.getEmail(),
                filePath,
                bmBlurred,
                titles,
                listeners,
                pictures);
        mRecyclerView.setAdapter(mAdapter);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        new DownloadAndBlurImageTask(bmBlurred, (IUserPicSetupListener)mAdapter).execute("http://p30480.lab1.stud.tech-mail.ru/" + owner.getImg());

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeContainer.setOnRefreshListener(refreshListener);

        File folder = new File(Environment.getExternalStorageDirectory() + "/torchat");
        boolean success = false;
        if (!folder.exists()) {
            success = folder.mkdir();
        }
        if (success) {
            folder = new File(Environment.getExternalStorageDirectory() + "/torchat/avatars");
            folder.mkdir();

            folder = new File(Environment.getExternalStorageDirectory() + "/torchat/avatars/users");
            folder.mkdir();

            folder = new File(Environment.getExternalStorageDirectory() + "/torchat/avatars/chats");
            folder.mkdir();
        } else {
            // Do something else on failure
        }

    }

    @Override
    protected void onSetImageManager(ImageDownloadManager mgr) {
        if (chatsList != null && chatsList.getAdapter() != null) {
            ((ChatsAdapter) chatsList.getAdapter()).setDownloadManager(mgr);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.v("GAv4", "Resume");
        Tracker t = ((AnalyticsApplication) getApplication()).getDefaultTracker();
        t.setScreenName("hello");
        t.send(new HitBuilders.AppViewBuilder().build());
        GoogleAnalytics.getInstance(this).reportActivityStart(this);

        CircleImageView civ = null;
        getLoaderManager().restartLoader(CHAT_DB_LOADER, null, chatsLoaderListener);
        if(uid != null) {
            String localPath = Environment.getExternalStorageDirectory() + "/torchat/avatars/users/" + uid + ".bmp";
            String localBlurPath = Environment.getExternalStorageDirectory() + "/torchat/avatars/users/" + uid + "_blur.bmp";

            RelativeLayout rl = (RelativeLayout)ChatsActivity.this.findViewById(R.id.left_drawer_header);
            try {
                civ = (CircleImageView) rl.findViewById(R.id.userPicture);
            } catch (NullPointerException e) {

            }

            File localFile = new File(localPath);
            if(localFile.exists() && civ != null)
                civ.setImageBitmap(BitmapFactory.decodeFile(localPath));

            File localBlurFile = new File(localBlurPath);

            if(localBlurFile.exists() && rl != null)
                rl.setBackground(new BitmapDrawable(getResources(), BitmapFactory.decodeFile(localBlurPath)));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chats, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);

        int searchPlateId = searchView.getContext().getResources()
                .getIdentifier("android:id/search_plate", null, null);
        View searchPlate = searchView.findViewById(searchPlateId);
        if (searchPlate != null) {
            searchPlate.setBackgroundResource(R.color.colorPrimary);
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                getLoaderManager().restartLoader(CHAT_SEARCH_LOADER, null, chatsLoaderListener);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                getLoaderManager().restartLoader(CHAT_SEARCH_LOADER, null, chatsLoaderListener);
                return true;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                getLoaderManager().restartLoader(CHAT_DB_LOADER, null, chatsLoaderListener);
                return true;
            }
        });

        MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.action_search),
            new MenuItemCompat.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    mToolbarMorphDrawable.animateIconState(MaterialMenuDrawable
                            .IconState.BURGER);
                    mSearchViewMorphDrawable.animateIconState(MaterialMenuDrawable
                            .IconState.BURGER);
                    return true;
                }

                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    mToolbarMorphDrawable.animateIconState(MaterialMenuDrawable
                            .IconState.ARROW);
                    mSearchViewMorphDrawable.animateIconState(MaterialMenuDrawable
                            .IconState.ARROW);
                    return true;
                }
            });

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

    private final EnlessLoader chatsLoaderListener = new EnlessLoader();
    class EnlessLoader implements ScrollEndlessPagination.EndlessLoaderListener<Chat> {
        boolean listEndReached = false;
        private ArrayList<Chat> chatsData = new ArrayList<>();

        @Override
        public boolean isEndReached() {
            return listEndReached;
        }

        @Override
        public Bundle getBundle() {
            return new Bundle();
        }

        @Override
        public Loader<List<Chat>> onCreateLoader(int id, Bundle args) {
            switch (id) {
                case CHAT_SEARCH_LOADER:
                    ChatSearchLoader searchLoader = new ChatSearchLoader(ChatsActivity.this);
                    searchLoader.setQueryString(searchView.getQuery().toString());
                    return searchLoader;
                case CHAT_WEB_LOADER:
                    return new ChatWebLoader(ChatsActivity.this, args);
                case CHAT_DB_LOADER:
                default:
                    return new ChatLoader(ChatsActivity.this);
            }
        }

        @Override
        public void onLoadFinished(Loader<List<Chat>> loader, List<Chat> data) {
            if (loader.getId() == CHAT_SEARCH_LOADER) {
                Log.d("tag", "chat_serach_loader");
                ChatsAdapter adapter = (ChatsAdapter) chatsList.getAdapter();
                chatsData.clear();
                chatsData.addAll(data);

                if (adapter == null) {
                    adapter = new ChatsAdapter(chatsData);
                    adapter.setDownloadManager(getImageDownloadManager());
                    chatsList.setAdapter(adapter);
                } else {
                    adapter.notifyDataSetChanged();
                }
                return;
            }

            ChatsHelper chatsHelper = new ChatsHelper(ChatsActivity.this);

            if (loader.getId() == CHAT_DB_LOADER) {
                getLoaderManager().restartLoader(CHAT_WEB_LOADER, null, chatsLoaderListener);
            } else if (data != null && loader.getId() == CHAT_WEB_LOADER) {
                chatsHelper.updateChatList(data);
            }

            listEndReached = false;
            boolean changes = false;
            if (data != null) {
                for (Chat chat : data) {
                    boolean contains = false;
                    int indexOfCoincidence;
                    for (indexOfCoincidence = 0; indexOfCoincidence < chatsData.size() && !contains;) {
                        Chat existingChat = chatsData.get(indexOfCoincidence);
                        contains = existingChat.getCid().equals(chat.getCid());
                        if (!contains)
                            indexOfCoincidence++;
                    }

                    if (!contains) {
                        chatsData.add(chatsData.size(), chat);
                        changes = true;
                    } else {
                        if (!chat.equals(chatsData.get(indexOfCoincidence))) {
                            chatsData.set(indexOfCoincidence, chat);
                            changes = true;
                        }
                    }
                }
                ChatsAdapter adapter = (ChatsAdapter) chatsList.getAdapter();
                if (adapter == null) {
                    adapter = new ChatsAdapter(chatsData);
                    adapter.setDownloadManager(getImageDownloadManager());
                    chatsList.setAdapter(adapter);
                } else if (data.size() > 0 && changes) {
                    adapter.notifyDataSetChanged();
                }

                if (loader.getId() == CHAT_WEB_LOADER) {
                    if (data.size() < pagination.getPageSize()) {
                        listEndReached = true;
                    } else {
                        Bundle args = new Bundle();
                        args.putInt(ChatWebLoader.ARG_PAGE, chatsData.size() / pagination.getPageSize() + 1);
                        getLoaderManager().restartLoader(CHAT_WEB_LOADER, args, this).forceLoad();
                    }
                }
            } else {
                Toast.makeText(ChatsActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
            }
            swipeContainer.setRefreshing(false);
            pbChats.setVisibility(View.GONE);

            chatsHelper.close();
        }

        @Override
        public void onLoaderReset(Loader<List<Chat>> loader) {
            if (loader instanceof ChatSearchLoader) {
                ((ChatSearchLoader) loader).setQueryString(searchView.getQuery().toString());
            }
        }
    };

    private final SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            getLoaderManager().restartLoader(CHAT_WEB_LOADER, null, chatsLoaderListener).forceLoad();
        }
    };

    private void setupActionBar() {
        int colorPrimary = getResources().getColor(R.color.actionBarTextColor);
        mToolbarMorphDrawable = new MaterialMenuDrawable(this,
                colorPrimary,
                MaterialMenuDrawable.Stroke.THIN);

        mToolbarMorphDrawable.setIconState(MaterialMenuDrawable.IconState.BURGER);

        mSearchViewMorphDrawable = new MaterialMenuDrawable(this, colorPrimary,
                MaterialMenuDrawable.Stroke.THIN);

        mSearchViewMorphDrawable.setIconState(MaterialMenuDrawable.IconState.BURGER);
        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setHomeAsUpIndicator(mToolbarMorphDrawable);
        }

        try {
            Method ensureCollapseButtonView = android.support.v7.widget.Toolbar.class
                    .getDeclaredMethod("ensureCollapseButtonView", null);

            ensureCollapseButtonView.setAccessible(true);
            ensureCollapseButtonView.invoke(toolbar, null);

            Field collapseButtonViewField = android.support.v7.widget.Toolbar.class.
                    getDeclaredField("mCollapseButtonView");

            collapseButtonViewField.setAccessible(true);

            ImageButton imageButtonCollapse = (ImageButton) collapseButtonViewField
                    .get(toolbar);

            imageButtonCollapse.setImageDrawable(mSearchViewMorphDrawable);
        }
        catch (Exception e) {
            // Something went wrong, let the app work without morphing the buttons :)
            e.printStackTrace();
        }
    }

    private class DownloadAndBlurImageTask extends AsyncTask<String, Void, Bitmap> {
        Bitmap bmImage;
        IUserPicSetupListener listener;

        public DownloadAndBlurImageTask(Bitmap bmImage, IUserPicSetupListener listener) {
            this.bmImage = bmImage;
            this.listener = listener;
        }

        protected Bitmap doInBackground(String... urls) {
            OwnerProfile own = new OwnerProfile(ChatsActivity.this);
            Log.d("[TP-diploma]", "task is working");
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            String smallFilePath = "http://p30480.lab1.stud.tech-mail.ru" + own.getImg();

            String localPath =  Environment.getExternalStorageDirectory() + "/torchat/avatars/users/" + uid + ".bmp";
            String localBlurPath =  Environment.getExternalStorageDirectory() + "/torchat/avatars/users/" + uid + "_blur.bmp";

            File fileLocal = new File(localPath);
            File fileLocalBlur = new File(localBlurPath);

            if(fileLocal.exists()) {

            }

            if(fileLocalBlur.exists()) {

            }

            try {
                Log.d("[TP-diploma]", "Requesting smallUserPic: " + smallFilePath);
                InputStream in = new java.net.URL(smallFilePath).openStream();
                Bitmap smallUserPic = BitmapFactory.decodeStream(in);
                File file = new File(Environment.getExternalStorageDirectory() + "/torchat/avatars/users/" + own.getUid() + ".bmp");
                FileOutputStream fos = new FileOutputStream(file);
                if(smallUserPic != null)
                    smallUserPic.compress(Bitmap.CompressFormat.PNG, 90, fos);
                else
                    Log.d("[TP-diploma]", "DownloadAndBlurImageTask: smallUserPic = null");

                fos.close();
            } catch (IOException e) {

            }

            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
                bmImage = BlurBuilder.blur(ChatsActivity.this, mIcon11);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bmImage;
        }

        protected void onPostExecute(Bitmap result) {
            String filePath = Environment.getExternalStorageDirectory() + "/torchat/avatars/users/" + uid + "_blur.bmp";
            File file = new File(filePath);
            RelativeLayout rl = (RelativeLayout) ChatsActivity.this.findViewById(R.id.left_drawer_header);

            if (result != null && rl != null) {
                Log.d("[TP-diploma]", "DownloadImageTask result not null");
                bmImage = result;
                rl.setBackground(new BitmapDrawable(getResources(), result));
                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    result.compress(Bitmap.CompressFormat.PNG, 90, fos);
                    fos.close();
                } catch (FileNotFoundException e) {
                    Log.d("[TP-diploma]", "File not found: " + e.getMessage());
                } catch (IOException e) {
                    Log.d("[TP-diploma]", "Error accessing file: " + e.getMessage());
                }
            } else if (rl != null) {
                Log.d("[TP-diploma]", "DownloadImageTask result NULL");
                if(file.exists()) {
                    Log.d("[TP-diploma]", "DownloadImageTask file exists: " + file.getAbsolutePath());
                    bmImage = BitmapFactory.decodeFile(filePath);
                    rl.setBackground(new BitmapDrawable(getResources(), bmImage));
                }
                else {
                    Log.d("[TP-diploma]", "DownloadImageTask file do not exist");
                    bmImage = BitmapFactory.decodeResource(ChatsActivity.this.getResources(), R.drawable.ic_user_picture_blur);
                    rl.setBackground(new BitmapDrawable(getResources(), bmImage));
                }
            }

            listener.onUserPicUploadComplete(Environment.getExternalStorageDirectory() + "/torchat/avatars/users/" + uid + ".bmp");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(CHATS_DATA, (Serializable) chatsLoaderListener.chatsData);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        chatsLoaderListener.chatsData = (ArrayList<Chat>) savedInstanceState.getSerializable(CHATS_DATA);
        if (chatsLoaderListener.chatsData != null) {
            ChatsAdapter adapter = new ChatsAdapter(chatsLoaderListener.chatsData);
            adapter.setDownloadManager(getImageDownloadManager());
            chatsList.setAdapter(adapter);
        }
    }

}

