package ru.mail.park.chat.activities;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Typeface;
import android.support.design.widget.AppBarLayout;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Calendar;

import java.io.InputStream;

import ru.mail.park.chat.R;
import ru.mail.park.chat.activities.tasks.AddContactTask;
import ru.mail.park.chat.database.ContactHelper;
import ru.mail.park.chat.loaders.ProfileWebLoader;
import ru.mail.park.chat.models.Contact;
import ru.mail.park.chat.models.OwnerProfile;

public class UserProfileActivity extends AppCompatActivity {
    public static final String UID_EXTRA = UserProfileActivity.class.getCanonicalName() + ".UID_EXTRA";
    public static final String SERVER_URL = "http://p30480.lab1.stud.tech-mail.ru/";
    private final static int DB_LOADER = 0;
    private final static int WEB_LOADER = 1;
    private final static int WEB_OWN_LOADER = 2;

    private String uid;

    private CollapsingToolbarLayout toolbarLayout;
    private Toolbar toolbar;
    private AppBarLayout appBar;
    private FloatingActionButton userAddToContacts;
    private FloatingActionButton userSendMessage;

    private ImageView userPicture;
    private TextView userLogin;
    private TextView userEmail;
    private TextView userPhone;
    private TextView onlineIndicator;
    private LinearLayout profileDataLayout;

    private ProgressBar progressBar;

    private Contact.Relation relation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        toolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolbarLayout.setTitle("Loading...");

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        appBar = (AppBarLayout) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        userAddToContacts = (FloatingActionButton) findViewById(R.id.user_add_to_contacts);
        userSendMessage = (FloatingActionButton) findViewById(R.id.user_send_message);

        userPicture = (ImageView) findViewById(R.id.user_picture);
        userLogin = (TextView) findViewById(R.id.user_login);
        userEmail = (TextView) findViewById(R.id.user_email);
        userPhone = (TextView) findViewById(R.id.user_phone);
        onlineIndicator = (TextView) findViewById(R.id.online_indicator);
        profileDataLayout = (LinearLayout) findViewById(R.id.profileDataLayout);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        OwnerProfile owner = new OwnerProfile(this);
        if (getIntent().hasExtra(UID_EXTRA)) {
            uid = getIntent().getStringExtra(UID_EXTRA);
        } else {
            uid = owner.getUid();
        }

        int loaderType;
        if (uid.equals(owner.getUid())) {
            setUserData(owner, Contact.Relation.SELF);
            loaderType = WEB_OWN_LOADER;
        } else {
            ContactHelper contactHelper = new ContactHelper(this);
            Contact profile = contactHelper.getContact(uid);
            if (profile != null) {
                setUserData(profile, Contact.Relation.FRIEND);
            }
            loaderType = WEB_LOADER;
        }

        Bundle args = new Bundle();
        args.putString(ProfileWebLoader.UID_ARG, uid);
        getLoaderManager().initLoader(loaderType, args, contactsLoaderListener);

        userAddToContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AddContactTask(UserProfileActivity.this).execute(uid);
            }
        });

        userSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfileActivity.this, DialogActivity.class);
                intent.putExtra(DialogActivity.USER_ID, uid);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // getMenuInflater().inflate(R.menu.menu_user_profile, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if (relation != null) {
            switch (relation) {
                case FRIEND:
                    getMenuInflater().inflate(R.menu.menu_user_profile, menu);
                    break;
                case SELF:
                    getMenuInflater().inflate(R.menu.menu_owner_profile, menu);
                    break;
                case OTHER:
                    break;
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_edit_contact) {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setUserData(Contact user, Contact.Relation relation) {
        toolbarLayout.setTitle(user.getContactTitle());
        userLogin.setText(user.getLogin());

        if (user.getEmail() != null) {
            userEmail.setText(user.getEmail());
            userEmail.setVisibility(View.VISIBLE);
        } else {
            userEmail.setVisibility(View.GONE);
        }

        if(user.getImg() != null){
            Log.d("[TP-diploma]", "starting task");
            new DownloadImageTask(userPicture)
                    .execute(SERVER_URL + user.getImg());
        }

        Calendar lastSeen = user.getLastSeen();

        if(relation != Contact.Relation.SELF) {
            if (user.isOnline())
                onlineIndicator.setText("online");
            else if(lastSeen != null)
                onlineIndicator.setText(lastSeen.getTime().toGMTString());
            else
                onlineIndicator.setText("offline");
        }

        if (user.getPhone() != null) {
            userPhone.setText(user.getPhone());
            userPhone.setVisibility(View.VISIBLE);
        } else {
            userPhone.setVisibility(View.GONE);
        }

        this.relation = relation;

        if (relation != null) {
            switch (relation) {
                case FRIEND:
                    userAddToContacts.setVisibility(View.INVISIBLE);
                    userSendMessage.setVisibility(View.VISIBLE);
                    break;
                case SELF:
                    userAddToContacts.setVisibility(View.INVISIBLE);
                    userSendMessage.setVisibility(View.INVISIBLE);
                    break;
                case OTHER:
                    userAddToContacts.setVisibility(View.VISIBLE);
                    userSendMessage.setVisibility(View.VISIBLE);
                    break;
            }
        }

        profileDataLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        invalidateOptionsMenu();
    }

    private final LoaderManager.LoaderCallbacks<Contact> contactsLoaderListener =
            new LoaderManager.LoaderCallbacks<Contact>() {
                @Override
                public Loader<Contact> onCreateLoader(int id, Bundle args) {
                    return new ProfileWebLoader(UserProfileActivity.this, id, args);
                }

                @Override
                public void onLoadFinished(Loader<Contact> loader, Contact data) {
                    if (data != null) {
                        setUserData(data, (loader.getId() == WEB_OWN_LOADER) ?
                                Contact.Relation.SELF
                                : Contact.Relation.OTHER);
                    }
                }

                @Override
                public void onLoaderReset(Loader<Contact> loader) {
                    // TODO: something...
                }
            };

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            Log.d("[TP-diploma]", "task is working");
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
