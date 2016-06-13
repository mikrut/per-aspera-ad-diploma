package ru.mail.park.chat.activities.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.mail.park.chat.R;

/**
 * Created by Михаил on 13.06.2016.
 */
public class ContactsFragmentNoSwipe extends ContactsFragment {
    @Override
    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_contacts_no_swipe, container, false);
    }
}
