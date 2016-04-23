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
import ru.mail.park.chat.api.Messages;
import ru.mail.park.chat.message_income.IMessageReaction;
import ru.mail.park.chat.models.Chat;
import ru.mail.park.chat.models.Contact;
import ru.mail.park.chat.models.Message;

/**
 * Created by mikrut on 01.04.16.
 */
public class GroupDialogCreateActivity
        extends AppCompatActivity
        implements IMessageReaction, ContactsFragment.OnPickEventListener {
    private EditText chosenContactsList;

    private Messages messages;
    private EditText groupChat;
    private List<String> uids;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_dialog_create);

        try {
            messages = new Messages(this, this);
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
        fragmentTransaction.add(R.id.contacts_fragment_container, fragment);

        fragmentTransaction.commit();

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
                        messages = new Messages(this, this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (messages != null) {
                    messages.createGroupChat(groupChat.getText().toString(), uids);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    // FIXME: normal interfaces etc.
    @Override
    public void onIncomeMessage(JSONObject message) {

    }

    @Override
    public void onActionSendMessage(JSONObject message) {

    }

    @Override
    public void onActionDeleteMessage(int mid) {

    }

    @Override
    public void onGetHistoryMessages(ArrayList<Message> msg_list) {

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
