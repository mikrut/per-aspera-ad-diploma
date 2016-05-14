package ru.mail.park.chat.activities.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.TreeSet;

import ru.mail.park.chat.R;
import ru.mail.park.chat.activities.ContactSearchActivity;
import ru.mail.park.chat.models.Contact;

/**
 * Created by Михаил on 14.05.2016.
 */
public class ContactsCurrentFragment extends ContactsFragment {
    private LinearLayout findFriends;

    @Override
    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_contacts_current, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findFriends = (LinearLayout) view.findViewById(R.id.findFriends);
        findFriends.setOnClickListener(findFriendsListener);
    }

    private final View.OnClickListener findFriendsListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), ContactSearchActivity.class);
            startActivity(intent);
        }
    };
}
