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
import ru.mail.park.chat.activities.tasks.ActivateContactTask;
import ru.mail.park.chat.loaders.ContactListDBLoader;
import ru.mail.park.chat.loaders.ContactListWebLoader;
import ru.mail.park.chat.models.Contact;

/**
 * Created by Михаил on 14.05.2016.
 */
public class ContactsSubscribersFragment extends ContactsFragment {
    public static final String MY_ARG = ContactsSubscribersFragment.class.getSimpleName() + ".MY_ARG";
    private boolean my;

    @NonNull
    @Override
    protected LoaderManager.LoaderCallbacks<List<Contact>> getLoaderCallbacks() {
        return new SubscribersLoaderCallbacks();
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        my = args.getBoolean(MY_ARG, true);
    }

    protected class SubscribersLoaderCallbacks extends ContactsLoaderCallbacks {
        @Override
        public Loader<List<Contact>> onCreateLoader(int id, Bundle args) {
            ContactListWebLoader webLoader = new ContactListWebLoader(getActivity(), WEB_LOADER);
            webLoader.setActivated(false);
            webLoader.setMy(my);
            return webLoader;
        }
    }

    @Override
    protected AContactAdapter onCreateContactAdapter(@NonNull List<Contact> data) {
        AContactAdapter adapter = super.onCreateContactAdapter(data);
        if (my) {
            Drawable add = ResourcesCompat.getDrawable(getResources(),
                    R.drawable.ic_add_black_24dp, null);
            adapter.setContactAction(add,
                    new AContactAdapter.ContactHolder.OnContactActionListener() {
                        @Override
                        public void onContactAction(Contact contact) {
                            ActivateContactTask task = new ActivateContactTask(getContext());
                            task.execute(contact.getUid());
                        }
                    }
            );
        }
        return adapter;
    }
}
