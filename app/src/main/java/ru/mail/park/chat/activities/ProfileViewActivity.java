package ru.mail.park.chat.activities;

import android.app.LoaderManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

import java.util.Locale;

import ru.mail.park.chat.R;
import ru.mail.park.chat.activities.tasks.AddContactTask;
import ru.mail.park.chat.activities.tasks.DeleteContactTask;
import ru.mail.park.chat.activities.views.ContactInfoElementView;
import ru.mail.park.chat.api.ApiSection;
import ru.mail.park.chat.database.ContactsHelper;
import ru.mail.park.chat.database.ContactsToChatsHelper;
import ru.mail.park.chat.loaders.OwnerWebLoader;
import ru.mail.park.chat.loaders.ProfileWebLoader;
import ru.mail.park.chat.loaders.images.ImageDownloadManager;
import ru.mail.park.chat.models.Chat;
import ru.mail.park.chat.models.Contact;
import ru.mail.park.chat.models.OwnerProfile;

public class ProfileViewActivity extends AImageDownloadServiceBindingActivity
        implements DeleteContactTask.DeleteContactCallbacks, AddContactTask.IAddContactListener {
    public static final String UID_EXTRA = ProfileViewActivity.class.getCanonicalName() + ".UID_EXTRA";
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
    private ContactInfoElementView userLogin;
    private ContactInfoElementView userLastName;
    private ContactInfoElementView userEmail;
    private ContactInfoElementView userPhone;
    private ContactInfoElementView aboutUser;
    private TextView onlineIndicator;
    private LinearLayout profileDataLayout;

    private ProgressBar progressBar;

    private Contact contact;
    private Contact.Relation relation = null;

    private DeleteContactTask deleteTask;
    private AddContactTask addTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);

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
        userLogin = (ContactInfoElementView) findViewById(R.id.user_login);
        userEmail = (ContactInfoElementView) findViewById(R.id.user_email);
        userPhone = (ContactInfoElementView) findViewById(R.id.user_phone);
        userLastName = (ContactInfoElementView) findViewById(R.id.user_lastname);
        onlineIndicator = (TextView) findViewById(R.id.online_indicator);
        aboutUser = (ContactInfoElementView) findViewById(R.id.user_about);
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
            ContactsHelper contactsHelper = new ContactsHelper(this);
            Contact profile = contactsHelper.getContact(uid);
            if (profile != null) {
                setUserData(profile, Contact.Relation.FRIEND);
            }
            loaderType = WEB_LOADER;
        }

        final Bundle args = new Bundle();
        args.putString(ProfileWebLoader.UID_ARG, uid);
        getLoaderManager().initLoader(loaderType, args, contactsLoaderListener);

        userAddToContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addTask != null)
                    addTask.cancel(true);
                addTask = new AddContactTask(ProfileViewActivity.this);
                addTask.setListener(ProfileViewActivity.this);
                addTask.execute(uid);
            }
        });

        userSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileViewActivity.this, DialogActivity.class);
                ContactsToChatsHelper helper = new ContactsToChatsHelper(ProfileViewActivity.this);
                Chat chat = helper.getChat(uid);
                if (chat != null) {
                    intent.putExtra(DialogActivity.CHAT_ID, chat.getCid());
                } else {
                    intent.putExtra(DialogActivity.USER_ID, uid);
                }
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // getMenuInflater().inflate(R.menu.menu_profile_view, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if (relation != null) {
            switch (relation) {
                case FRIEND:
                    getMenuInflater().inflate(R.menu.menu_profile_view, menu);
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
    protected void onSetImageManager(ImageDownloadManager mgr) {
        if (contact != null) {
            try {
                URL url = new URL(ApiSection.SERVER_URL + contact.getImg());
                mgr.setImage(userPicture, url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_edit_contact:
                Intent intent = new Intent(this, ProfileEditActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_edit_onion: {
                View editView = LayoutInflater.from(this)
                        .inflate(R.layout.dialog_edit_text, null);
                final EditText editText = (EditText) editView.findViewById(R.id.edittext);
                if (contact.getOnionAddress() != null)
                    editText.setText(contact.getOnionAddress().toString());
                new AlertDialog.Builder(this)
                        .setTitle("Input onion address")
                        .setView(editView)
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String onionAddress = editText.getText().toString();
                                        onionAddress = onionAddress.replaceAll("\\s", "");
                                        contact.setOnionAddress(onionAddress);
                                        ContactsHelper contactsHelper = new ContactsHelper(editText.getContext());
                                        contactsHelper.updateContact(contact);
                                    }
                                }).create().show();
                return true;
            }
            case R.id.action_edit_pubkey: {
                View editView = LayoutInflater.from(this)
                        .inflate(R.layout.dialog_edit_text, null);
                final EditText editText = (EditText) editView.findViewById(R.id.edittext);
                editText.setText(contact.getPubkeyDigestString());
                new AlertDialog.Builder(this)
                        .setTitle("Input public key")
                        .setView(editView)
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String publicKeyDigest = editText.getText().toString();
                                        contact.setPubkeyDigest(publicKeyDigest);
                                        ContactsHelper contactsHelper = new ContactsHelper(editText.getContext());
                                        contactsHelper.updateContact(contact);
                                    }
                                }).create().show();
                return true;
            }
            case R.id.action_delete_contact: {
                if (deleteTask != null) {
                    deleteTask.cancel(true);
                }
                deleteTask = new DeleteContactTask(this, this);
                deleteTask.execute(uid);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDeleted(boolean success) {
        if (success) {
            onBackPressed();
        } else {
            Toast.makeText(this, "Failed to delete contact", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void OnAddContact(String uid) {
        finish();
    }

    @Override
    public void OnAddContactFailed(String uid) {

    }

    private void setUserData(Contact user, Contact.Relation relation) {
        contact = user;

        toolbarLayout.setTitle(user.getLogin()); //toolbar is for login
        userLogin.setText(user.getFirstName()); //userLogin is firstName
        userLastName.setText(user.getLastName());

        if (user.getEmail() != null && !user.getEmail().equals("")) {
            userEmail.setText(user.getEmail());
            userEmail.setVisibility(View.VISIBLE);
        } else {
            userEmail.setVisibility(View.GONE);
        }

        ImageDownloadManager manager = getImageDownloadManager();
        if (manager != null) {
            try {
                manager.setImage(userPicture, new URL(ApiSection.SERVER_URL + user.getImg()));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        Calendar lastSeen = user.getLastSeen();

        if(user.getAbout() != null && !user.getAbout().equals("")) {
            aboutUser.setText(user.getAbout());
            aboutUser.setVisibility(View.VISIBLE);
        } else {
            aboutUser.setVisibility(View.GONE);
        }

        if(relation != Contact.Relation.SELF) {
            if (user.isOnline())
                onlineIndicator.setText("online");
            else if(lastSeen != null)
                onlineIndicator.setText(formatLastSeenTime(lastSeen));
            else
                onlineIndicator.setText("offline");
        }

        if (user.getPhone() != null && !user.getPhone().equals("")) {
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

    public static String formatLastSeenTime(Calendar lastSeen) {
        String lastSeenDate = "";
        String lastSeenTime;
        boolean dateAgoFormat = false;

        Calendar rightNow = Calendar.getInstance();

        int lastSeenDayInYear = lastSeen.get(Calendar.DAY_OF_YEAR);
        int todayDayInYear = rightNow.get(Calendar.DAY_OF_YEAR);

        int lastSeenYear = lastSeen.get(Calendar.YEAR);
        int todayYear = rightNow.get(Calendar.YEAR);

        int lastSeenHour = lastSeen.get(Calendar.HOUR);
        int todayHour = rightNow.get(Calendar.HOUR);

        int lastSeenMin = lastSeen.get(Calendar.MINUTE);

        int dayOfMonth = lastSeen.get(Calendar.DAY_OF_MONTH);
        int month = lastSeen.get(Calendar.MONTH);
        int year = lastSeen.get(Calendar.YEAR);

        if(lastSeenYear == todayYear) {
            switch(todayDayInYear - lastSeenDayInYear) {
                case 0:
                    int diff = todayHour - lastSeenHour;
                    if(diff > 11) {
                        lastSeenDate = "Today";
                    } else {
                        dateAgoFormat = true;
                        if (diff > 1) {
                            lastSeenDate = String.valueOf(diff) + " hours ago";
                        } else if (diff == 1) {
                            lastSeenDate = "an hour ago";
                        }
                    }
                    break;
                case 1:
                    lastSeenDate = "Yesterday";
                    break;
                default:
                    lastSeenDate = String.format(Locale.getDefault(), "%02d %02d %d", dayOfMonth, month, year);
                    break;
            }
        } else {
            lastSeenDate = String.format(Locale.getDefault(), "%02d %02d %d", dayOfMonth, month, year);
        }

        lastSeenTime = String.format(Locale.getDefault(), "%02d:%02d", lastSeenHour, lastSeenMin);

        String result = "Last seen " + lastSeenDate;

        if(!dateAgoFormat)
            result += " at " + lastSeenTime;

        return result;
    }

    private final LoaderManager.LoaderCallbacks<Contact> contactsLoaderListener =
            new LoaderManager.LoaderCallbacks<Contact>() {
                @Override
                public Loader<Contact> onCreateLoader(int id, Bundle args) {
                    switch (id) {
                        case WEB_OWN_LOADER:
                            return new OwnerWebLoader(ProfileViewActivity.this, id, args);
                        case WEB_LOADER:
                        default:
                            return new ProfileWebLoader(ProfileViewActivity.this, id, args);
                    }
                }

                @Override
                public void onLoadFinished(Loader<Contact> loader, Contact data) {
                    if (data != null) {
                        Contact.Relation relation = Contact.Relation.OTHER;
                        String ownerUid = (new OwnerProfile(ProfileViewActivity.this)).getUid();
                        if (data.getUid().equals(ownerUid))
                            relation = Contact.Relation.SELF;
                        else {
                            ContactsHelper contactsHelper = new ContactsHelper(ProfileViewActivity.this);
                            if (contactsHelper.getContact(data.getUid()) != null) {
                                relation = Contact.Relation.FRIEND;
                                contactsHelper.saveContact(data);
                            }
                        }
                        setUserData(data, relation);
                    }
                }

                @Override
                public void onLoaderReset(Loader<Contact> loader) {
                    // TODO: something...
                }
            };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (deleteTask != null)
            deleteTask.cancel(true);
        if (addTask != null)
            addTask.cancel(true);
    }
}
