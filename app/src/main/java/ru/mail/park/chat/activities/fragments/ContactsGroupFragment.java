package ru.mail.park.chat.activities.fragments;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

import ru.mail.park.chat.R;
import ru.mail.park.chat.loaders.GroupContactsLoader;
import ru.mail.park.chat.models.Chat;
import ru.mail.park.chat.models.Contact;

/**
 * Created by Михаил on 13.06.2016.
 */
public class ContactsGroupFragment extends ContactsFragment {
    public static final String ARG_CID =
            ContactsGroupFragment.class.getCanonicalName() + "ARG_CID";
    private String cid;

    @Override
    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_contacts_no_swipe, container, false);
    }

    @Override
    protected LoaderManager.LoaderCallbacks<List<Contact>> getLoaderCallbacks() {
        return new GroupContactsLoaderCallbacks();
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        cid = args.getString(ARG_CID, null);
    }

    private class GroupContactsLoaderCallbacks extends ContactsLoaderCallbacks {
        @Override
        public Loader<List<Contact>> onCreateLoader(int id, Bundle args) {
            if (cid != null)
                return new GroupContactsLoader(getContext(), id, cid);
            else
                return null;
        }
    }
}
