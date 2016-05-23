package ru.mail.park.chat.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.TreeSet;

import ru.mail.park.chat.R;
import ru.mail.park.chat.activities.fragments.ContactsFragment;
import ru.mail.park.chat.database.ContactsToChatsHelper;
import ru.mail.park.chat.models.Chat;
import ru.mail.park.chat.models.Contact;

/**
 * Created by Михаил on 24.04.2016.
 */
public class DialogCreateActivity
        extends AppCompatActivity
        implements ContactsFragment.OnPickEventListener {
    TextView newGroupClickable;
    TextView newP2PClickable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_create);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        newGroupClickable = (TextView) findViewById(R.id.new_group_dialog);
        newGroupClickable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DialogCreateActivity.this, GroupDialogCreateActivity.class);
                startActivity(intent);
                finish();
            }
        });

        newP2PClickable = (TextView) findViewById(R.id.new_p2p_dialog);
        newP2PClickable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View editView = LayoutInflater.from(DialogCreateActivity.this)
                        .inflate(R.layout.dialog_edit_text, null);
                final EditText editText = (EditText) editView.findViewById(R.id.edittext);
                new AlertDialog.Builder(DialogCreateActivity.this)
                        .setTitle("Input address")
                        .setView(editView).setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String input = editText.getText().toString();
                                Intent intent = new Intent(DialogCreateActivity.this, P2PDialogActivity.class);
                                if (!input.equals("")) {
                                    String address = input.substring(0, input.lastIndexOf(':'));
                                    int port = Integer.valueOf(input.substring(input.lastIndexOf(':') + 1, input.length()));
                                    intent.putExtra(P2PDialogActivity.HOST_ARG, address);
                                    intent.putExtra(P2PDialogActivity.PORT_ARG, port);
                                }
                                startActivity(intent);
                            }
                        }).create().show();
            }
        });
    }

    @Override
    public void onContactSetChanged(TreeSet<Contact> chosenContacts) {

    }

    @Override
    public void onContactClicked(Contact contact) {
        Intent intent = new Intent(this, DialogActivity.class);
        ContactsToChatsHelper helper = new ContactsToChatsHelper(DialogCreateActivity.this);
        Chat chat = helper.getChat(contact.getUid());
        if (chat != null) {
            intent.putExtra(DialogActivity.CHAT_ID, chat.getCid());
        } else {
            intent.putExtra(DialogActivity.USER_ID, contact.getUid());
        }
        startActivity(intent);
        finish();
    }
}
