package ru.mail.park.chat.activities.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.spongycastle.crypto.engines.GOST28147Engine;

import java.util.TreeSet;

import ru.mail.park.chat.R;
import ru.mail.park.chat.activities.ContactSearchActivity;
import ru.mail.park.chat.models.Contact;

/**
 * Created by Михаил on 14.05.2016.
 */
public class ContactsCurrentFragment extends ContactsFragment {
    private LinearLayout findFriends;
    private boolean shouldHide = false;

    @Override
    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_contacts_current, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findFriends = (LinearLayout) view.findViewById(R.id.findFriends);
        findFriends.setOnClickListener(findFriendsListener);
        findFriends.setVisibility(shouldHide ? View.GONE : View.VISIBLE);
    }

    public void hideFindFriends(boolean shouldHide) {
        this.shouldHide = shouldHide;
        if (findFriends != null)
            findFriends.setVisibility(shouldHide ? View.GONE: View.VISIBLE);
    }

    private final View.OnClickListener findFriendsListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), ContactSearchActivity.class);
            startActivity(intent);
        }
    };
}
