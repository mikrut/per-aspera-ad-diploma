package ru.mail.park.chat.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ru.mail.park.chat.R;
import ru.mail.park.chat.models.Contact;

public class ContactsActivity extends AppCompatActivity {
    private RecyclerView contactsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        // TODO: loader from DB
        List<Contact> contactList = new ArrayList<Contact>();
        contactList.add(new Contact());
        contactList.add(new Contact());

        contactsView = (RecyclerView) findViewById(R.id.contactsView);
        contactsView.setAdapter(new ContactAdapter(contactList));
        contactsView.setLayoutManager(new LinearLayoutManager(this));
    }
}
