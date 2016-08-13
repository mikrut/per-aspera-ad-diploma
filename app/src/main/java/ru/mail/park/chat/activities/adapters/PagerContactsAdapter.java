package ru.mail.park.chat.activities.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import ru.mail.park.chat.activities.fragments.ContactsCurrentFragment;
import ru.mail.park.chat.activities.fragments.ContactsSubscribersFragment;
import ru.mail.park.chat.loaders.images.ImageDownloadManager;

/**
 * Created by Михаил on 14.05.2016.
 */
public class PagerContactsAdapter extends FragmentPagerAdapter {
    private static final int CURRENT_CONTACTS_POSITION = 0;
    private static final int ADDIBLE_CONTACTS_POSITION = 1;
    private static final int SUBSCRIPTIONS_CONTACTS_POSITION = 2;

    final ContactsCurrentFragment currentFragment = new ContactsCurrentFragment();
    final ContactsSubscribersFragment addibleFragment;
    final ContactsSubscribersFragment subscriptionsFragment;

    private final boolean pickContacts;

    public PagerContactsAdapter(FragmentManager fm, boolean pickContacts) {
        super(fm);
        this.pickContacts = pickContacts;

        if (!pickContacts) {
            Bundle args = new Bundle();

            subscriptionsFragment = new ContactsSubscribersFragment();
            args.putBoolean(ContactsSubscribersFragment.MY_ARG, false);
            subscriptionsFragment.setArguments(args);

            addibleFragment = new ContactsSubscribersFragment();
            args.putBoolean(ContactsSubscribersFragment.MY_ARG, true);
            addibleFragment.setArguments(args);
        } else {
            currentFragment.hideFindFriends(true);
            subscriptionsFragment = null;
            addibleFragment = null;
        }
    }

    public PagerContactsAdapter(FragmentManager fm) {
        this(fm, false);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case CURRENT_CONTACTS_POSITION:
                return currentFragment;
            case ADDIBLE_CONTACTS_POSITION:
                return addibleFragment;
            case SUBSCRIPTIONS_CONTACTS_POSITION:
                return subscriptionsFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return pickContacts ? 1 : 3;
    }

    // FIXME: use resource strings
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case CURRENT_CONTACTS_POSITION:
                return "Friends";
            case ADDIBLE_CONTACTS_POSITION:
                return "Subscribers";
            case SUBSCRIPTIONS_CONTACTS_POSITION:
                return "Subscriptions";
            default:
                return null;
        }
    }
}
