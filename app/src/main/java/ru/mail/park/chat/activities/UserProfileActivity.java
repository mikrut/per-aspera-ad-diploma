package ru.mail.park.chat.activities;

import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
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
import android.widget.SearchView;
import android.widget.TextView;

import ru.mail.park.chat.R;
import ru.mail.park.chat.activities.tasks.AddContactTask;
import ru.mail.park.chat.database.ContactHelper;
import ru.mail.park.chat.loaders.ContactListDBLoader;
import ru.mail.park.chat.loaders.ContactListWebLoader;
import ru.mail.park.chat.loaders.ProfileWebLoader;
import ru.mail.park.chat.models.Contact;
import ru.mail.park.chat.models.OwnerProfile;

public class UserProfileActivity extends AppCompatActivity {
    public static final String UID_EXTRA = UserProfileActivity.class.getCanonicalName() + ".UID_EXTRA";
    private final static int DB_LOADER = 0;
    private final static int WEB_LOADER = 1;

    private String uid;

    private CollapsingToolbarLayout toolbarLayout;
    private Toolbar toolbar;
    private FloatingActionButton userAddToContacts;
    private FloatingActionButton userSendMessage;

    private ImageView userPicture;
    private TextView userLogin;
    private TextView userEmail;

    private Contact.Relation relation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        toolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        userAddToContacts = (FloatingActionButton) findViewById(R.id.user_add_to_contacts);
        userSendMessage = (FloatingActionButton) findViewById(R.id.user_send_message);

        userPicture = (ImageView) findViewById(R.id.user_picture);
        userLogin = (TextView) findViewById(R.id.user_login);
        userEmail = (TextView) findViewById(R.id.user_email);

        OwnerProfile owner = new OwnerProfile(this);
        if (getIntent().hasExtra(UID_EXTRA)) {
            uid = getIntent().getStringExtra(UID_EXTRA);
        } else {
            uid = owner.getUid();
        }

        if (uid.equals(owner.getUid())) {
            setUserData(owner, Contact.Relation.SELF);
        } else {
            ContactHelper contactHelper = new ContactHelper(this);
            Contact profile = contactHelper.getContact(uid);
            if (profile != null) {
                setUserData(profile, Contact.Relation.FRIEND);
            } else {
                Bundle args = new Bundle();
                args.putString(ProfileWebLoader.UID_ARG, uid);
                getLoaderManager().initLoader(WEB_LOADER, args, contactsLoaderListener);
            }
        }

        userAddToContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AddContactTask(UserProfileActivity.this).execute(uid);
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setUserData(Contact user, Contact.Relation relation) {
        toolbarLayout.setTitle(user.getContactTitle());
        userLogin.setText(user.getLogin());

        if (user.getEmail() != null) {
            userEmail.setText(user.getEmail());
            userEmail.setVisibility(View.VISIBLE);
        } else {
            userEmail.setVisibility(View.GONE);
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

        invalidateOptionsMenu();
    }

    LoaderManager.LoaderCallbacks<Contact> contactsLoaderListener =
            new LoaderManager.LoaderCallbacks<Contact>() {
                @Override
                public Loader<Contact> onCreateLoader(int id, Bundle args) {
                    return new ProfileWebLoader(UserProfileActivity.this, id, args);
                }

                @Override
                public void onLoadFinished(Loader<Contact> loader, Contact data) {
                    Log.d("loader", "received data");
                    if (data != null) {
                        setUserData(data, Contact.Relation.OTHER);
                    }
                }

                @Override
                public void onLoaderReset(Loader<Contact> loader) {
                    // TODO: something...
                }
            };
}
