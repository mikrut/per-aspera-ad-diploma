package ru.mail.park.chat.activities;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;

import ru.mail.park.chat.R;
import ru.mail.park.chat.models.Contact;

/**
 * Created by Михаил on 12.03.2016.
 */
public class ContactSearchAdapter extends AContactAdapter {
    List<Contact> contactList;

    public ContactSearchAdapter(@NonNull List<Contact> contactList) {
        this.contactList = contactList;
        Collections.sort(contactList);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View contactView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.element_contacts_search, parent, false);
        return new ContactHolder(contactView);
    }

    @Override
    protected Contact getContactForPosition(int position) {
        return contactList.get(position);
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }
}
