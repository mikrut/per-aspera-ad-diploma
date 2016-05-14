package ru.mail.park.chat.activities.adapters;

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

    ContactsCurrentFragment currentFragment = new ContactsCurrentFragment();
    ContactsSubscribersFragment addibleFragment = new ContactsSubscribersFragment();

    public PagerContactsAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case CURRENT_CONTACTS_POSITION:
                return currentFragment;
            case ADDIBLE_CONTACTS_POSITION:
                return addibleFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case CURRENT_CONTACTS_POSITION:
                return "Friends";
            case ADDIBLE_CONTACTS_POSITION:
                return "Subscribers";
            default:
                return null;
        }
    }
}
