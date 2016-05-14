package ru.mail.park.chat.activities.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.res.ResourcesCompat;
import android.widget.Toast;

import java.util.List;

import ru.mail.park.chat.R;
import ru.mail.park.chat.activities.adapters.AContactAdapter;
import ru.mail.park.chat.loaders.ContactListDBLoader;
import ru.mail.park.chat.loaders.ContactListWebLoader;
import ru.mail.park.chat.models.Contact;

/**
 * Created by Михаил on 14.05.2016.
 */
public class ContactsSubscribersFragment extends ContactsFragment {
    @Override
    protected LoaderManager.LoaderCallbacks<List<Contact>> getLoaderCallbacks() {
        return new SubscribersLoaderCallbacks();
    }

    protected class SubscribersLoaderCallbacks extends ContactsLoaderCallbacks {
        @Override
        public Loader<List<Contact>> onCreateLoader(int id, Bundle args) {
            ContactListWebLoader webLoader = new ContactListWebLoader(getActivity(), WEB_LOADER);
            webLoader.setActivated(false);
            return webLoader;
        }
    }

    @Override
    protected AContactAdapter onCreateContactAdapter(@NonNull List<Contact> data) {
        AContactAdapter adapter = super.onCreateContactAdapter(data);
        Drawable add = ResourcesCompat.getDrawable(getResources(),
                R.drawable.ic_add_black_24dp, null);
        adapter.setContactAction(add,
            new AContactAdapter.ContactHolder.OnContactActionListener() {
                @Override
                public void onContactAction(Contact contact) {
                    
                }
            }
        );
        return adapter;
    }
}
