package ru.mail.park.chat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import ru.mail.park.chat.R;
import ru.mail.park.chat.activities.fragments.ContactsFragment;
import ru.mail.park.chat.activities.fragments.ContactsSimpleListFragment;
import ru.mail.park.chat.api.Messages;
import ru.mail.park.chat.message_interfaces.IChatListener;
import ru.mail.park.chat.message_interfaces.IGroupCreateListener;
import ru.mail.park.chat.models.Chat;
import ru.mail.park.chat.models.Contact;
import ru.mail.park.chat.models.Message;

/**
 * Created by mikrut on 01.04.16.
 */
public class GroupDialogCreateActivity
        extends AppCompatActivity
        implements IGroupCreateListener, ContactsFragment.OnPickEventListener {
    private EditText chosenContactsList;
    private EditText groupChat;

    private Messages messages;
    private List<String> uids;

    private static final String PICKER_TAG = "PICKER_FRAGMENT";
    private static final String SIMPLE_TAG = "SIMPLE_FRAGMENT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_dialog_create);

        try {
            messages = new Messages(this);
            messages.setGroupCreateListener(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        chosenContactsList = (EditText) findViewById(R.id.choosen_contacts_list);
        groupChat = (EditText) findViewById(R.id.chatName);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        ContactsFragment fragment = new ContactsFragment();
        Bundle args = new Bundle();
        args.putBoolean(ContactsFragment.IS_MULTICHOICE, true);
        fragment.setArguments(args);
        fragmentTransaction.add(R.id.contacts_fragment_container, fragment, PICKER_TAG);

        fragmentTransaction.commit();

        fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                    groupChat.setVisibility(View.GONE);
                    chosenContactsList.setVisibility(View.VISIBLE);
                }
            }
        });

        uids = new ArrayList<>();
    }

    @Override
    public void onContactSetChanged(TreeSet<Contact> chosenContacts) {
        StringBuilder builder = new StringBuilder();
        Iterator<Contact> contactIterator = chosenContacts.iterator();
        for (int i = 0; i < chosenContacts.size(); i++) {
            Contact currentContact = contactIterator.next();
            builder.append(currentContact.getContactTitle());
            if (i != chosenContacts.size() - 1) {
                builder.append(", ");
            }
        }
        chosenContactsList.setText(builder.toString());

        uids.clear();
        for (Contact contact : chosenContacts) {
            uids.add(contact.getUid());
        }
    }

    @Override
    public void onContactClicked(Contact contact) {}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_group_dialog_create, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_create:
                if (messages == null) {
                    try {
                        messages = new Messages(this);
                        messages.setGroupCreateListener(this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (messages != null &&
                        uids.size() > 0 &&
                        groupChat.getVisibility() != View.GONE &&
                        groupChat.getText().length() > 0) {
                    messages.createGroupChat(groupChat.getText().toString(), uids);
                } else {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    ContactsFragment contactsFragment = (ContactsFragment) fragmentManager.findFragmentByTag(PICKER_TAG);
                    if (contactsFragment != null && contactsFragment.isVisible() && uids.size() > 0) {
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        ContactsSimpleListFragment contactsSimpleListFragment = new ContactsSimpleListFragment();
                        Bundle args = new Bundle();
                        args.putSerializable(ContactsFragment.PICKED_CONTACTS, contactsFragment.getChosenContacts());
                        contactsSimpleListFragment.setArguments(args);
                        fragmentTransaction.replace(R.id.contacts_fragment_container, contactsSimpleListFragment, SIMPLE_TAG);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

                        fragmentTransaction.commit();

                        groupChat.setVisibility(View.VISIBLE);
                        chosenContactsList.setVisibility(View.GONE);
                    }
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onChatCreated(Chat chat) {
        Intent intent = new Intent(this, DialogActivity.class);
        intent.putExtra(DialogActivity.CHAT_ID, chat.getCid());
        startActivity(intent);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        messages.disconnect();
    }
}
