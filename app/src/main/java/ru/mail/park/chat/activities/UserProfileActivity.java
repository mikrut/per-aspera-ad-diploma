package ru.mail.park.chat.activities;

import android.app.LoaderManager;
import android.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import ru.mail.park.chat.R;
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

    private ImageView userPicture;
    private TextView userLogin;
    private TextView userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

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
            setUserData(owner);
        } else {
            ContactHelper contactHelper = new ContactHelper(this);
            Contact profile = contactHelper.getContact(uid);
            if (profile != null) {
                setUserData(profile);
            } else {
                Bundle args = new Bundle();
                args.putString(ProfileWebLoader.UID_ARG, uid);
                getLoaderManager().initLoader(WEB_LOADER, args, contactsLoaderListener);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_user_profile, menu);
        return true;
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

    public void setUserData(Contact user) {
        userLogin.setText(user.getLogin());
        if (user.getEmail() != null) {
            userEmail.setText(user.getEmail());
            userEmail.setVisibility(View.VISIBLE);
        } else {
            userEmail.setVisibility(View.GONE);
        }
    }

    LoaderManager.LoaderCallbacks<Contact> contactsLoaderListener =
            new LoaderManager.LoaderCallbacks<Contact>() {
                @Override
                public Loader<Contact> onCreateLoader(int id, Bundle args) {
                    return new ProfileWebLoader(UserProfileActivity.this, id, args);
                }

                @Override
                public void onLoadFinished(Loader<Contact> loader, Contact data) {
                    if (data != null) {
                        setUserData(data);
                    }
                }

                @Override
                public void onLoaderReset(Loader<Contact> loader) {
                    // TODO: something...
                }
            };
}
