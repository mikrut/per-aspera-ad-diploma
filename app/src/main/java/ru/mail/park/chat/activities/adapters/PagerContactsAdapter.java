package ru.mail.park.chat.activities.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import ru.mail.park.chat.activities.fragments.ContactsCurrentFragment;
import ru.mail.park.chat.activities.fragments.ContactsSubscribersFragment;

/**
 * Created by Михаил on 14.05.2016.
 */
public class PagerContactsAdapter extends FragmentPagerAdapter {
    private static final int CURRENT_CONTACTS_POSITION = 0;
    private static final int ADDIBLE_CONTACTS_POSITION = 1;
    private static final int SUBSCRIPTIONS_CONTACTS_POSITION = 2;

    ContactsCurrentFragment currentFragment = new ContactsCurrentFragment();
    ContactsSubscribersFragment addibleFragment = new ContactsSubscribersFragment();
    ContactsSubscribersFragment subscriptionsFragment = new ContactsSubscribersFragment();

    public PagerContactsAdapter(FragmentManager fm) {
        super(fm);
        Bundle args = new Bundle();
        args.putBoolean(ContactsSubscribersFragment.MY_ARG, false);
        subscriptionsFragment.setArguments(args);

        args.putBoolean(ContactsSubscribersFragment.MY_ARG, true);
        addibleFragment.setArguments(args);
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
        return 3;
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
