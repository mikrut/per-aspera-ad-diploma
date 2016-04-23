package ru.mail.park.chat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import java.util.TreeSet;

import ru.mail.park.chat.R;
import ru.mail.park.chat.activities.fragments.ContactsFragment;
import ru.mail.park.chat.models.Contact;

/**
 * Created by Михаил on 24.04.2016.
 */
public class DialogCreateActivity
        extends AppCompatActivity
        implements ContactsFragment.OnPickEventListener {
    TextView newGroupClickable;

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
    }

    @Override
    public void onContactSetChanged(TreeSet<Contact> chosenContacts) {

    }

    @Override
    public void onContactClicked(Contact contact) {
        Intent intent = new Intent(this, DialogActivity.class);
        intent.putExtra(DialogActivity.USER_ID, contact.getUid());
        startActivity(intent);
        finish();
    }
}
