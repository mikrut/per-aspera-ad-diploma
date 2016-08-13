package ru.mail.park.chat.activities.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import ru.mail.park.chat.R;
import ru.mail.park.chat.activities.adapters.ContactAdapter;
import ru.mail.park.chat.activities.adapters.ContactSearchAdapter;
import ru.mail.park.chat.loaders.images.ImageDownloadManager;
import ru.mail.park.chat.models.Contact;

/**
 * Created by Михаил on 24.04.2016.
 */
public class ContactsSimpleListFragment extends Fragment {
    private RecyclerView contactsView;
    private TreeSet<Contact> chosenContacts;
    private ContactSearchAdapter contactAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Object chosen = getArguments().get(ContactsFragment.PICKED_CONTACTS);
        if (chosen != null) {
            chosenContacts = (TreeSet<Contact>) chosen;
        }

        return inflater.inflate(R.layout.fragment_contact_simple_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        List<Contact> contacts = new ArrayList<>();
        Iterator<Contact> contactIterator = chosenContacts.iterator();
        for (int i = 0; i < chosenContacts.size(); i++) {
            Contact currentContact = contactIterator.next();
            contacts.add(currentContact);
        }

        contactsView = (RecyclerView) view.findViewById(R.id.contactsView);
        contactsView.setLayoutManager(new LinearLayoutManager(getContext()));
        contactAdapter = new ContactSearchAdapter(contacts);
        contactAdapter.setAddible(false);
        contactsView.setAdapter(contactAdapter);
        if (manager != null)
            contactAdapter.onImageDownloadManagerAvailable(manager);
    }

    private ImageDownloadManager manager;

    public void onImageDownloadManagerAvailable(@NonNull ImageDownloadManager manager) {
        if (contactAdapter != null)
            contactAdapter.onImageDownloadManagerAvailable(manager);
        this.manager = manager;
    }
}
