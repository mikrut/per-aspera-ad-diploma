package ru.mail.park.chat.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;

import java.util.TreeSet;

import ru.mail.park.chat.R;
import ru.mail.park.chat.activities.fragments.ContactsFragment;
import ru.mail.park.chat.models.Contact;

public class ContactsActivity extends AppCompatActivity implements ContactsFragment.OnPickEventListener {
    private LinearLayout findFriends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        findFriends = (LinearLayout) findViewById(R.id.findFriends);
        findFriends.setOnClickListener(findFriendsListener);
    }

    @Override
    public void onContactSetChanged(TreeSet<Contact> chosenContacts) {

    }

    @Override
    public void onContactClicked(Contact contact) {
        Intent intent = new Intent(this, ProfileViewActivity.class);
        intent.putExtra(ProfileViewActivity.UID_EXTRA, contact.getUid());
        startActivity(intent);
    }

    private final View.OnClickListener findFriendsListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(ContactsActivity.this, ContactSearchActivity.class);
            startActivity(intent);
        }
    };
}
