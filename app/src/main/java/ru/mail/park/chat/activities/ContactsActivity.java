package ru.mail.park.chat.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import ru.mail.park.chat.R;
import ru.mail.park.chat.models.Contact;

public class ContactsActivity extends AppCompatActivity {
    private RecyclerView contactsView;
    private LinearLayout findFriends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        findFriends = (LinearLayout) findViewById(R.id.findFriends);
        findFriends.setOnClickListener(findFriendsListener);

        // TODO: loader from DB
        List<Contact> contactList = new ArrayList<>();
        contactList.add(new Contact());
        contactList.add(new Contact());

        contactsView = (RecyclerView) findViewById(R.id.contactsView);
        contactsView.setAdapter(new ContactAdapter(contactList));
        contactsView.setLayoutManager(new LinearLayoutManager(this));
    }

    View.OnClickListener findFriendsListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(ContactsActivity.this, ContactSearchActivity.class);
            startActivity(intent);
        }
    };
}
