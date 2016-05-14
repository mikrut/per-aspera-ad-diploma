package ru.mail.park.chat.activities.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import ru.mail.park.chat.R;
import ru.mail.park.chat.activities.adapters.AContactAdapter;
import ru.mail.park.chat.activities.adapters.ContactAdapter;
import ru.mail.park.chat.loaders.ContactListDBLoader;
import ru.mail.park.chat.loaders.ContactListWebLoader;
import ru.mail.park.chat.models.Contact;

/**
 * Created by Михаил on 23.04.2016.
 */
public class ContactsFragment extends Fragment {
    public static final String IS_MULTICHOICE = ContactsFragment.class.getCanonicalName() + ".IS_MULTICHOICE";
    public static final String PICKED_CONTACTS = ContactsFragment.class.getCanonicalName() + ".PICKED_CONTACTS";

    private ContactAdapter contactAdapter;
    private RecyclerView contactsView;
    private SwipeRefreshLayout swipeRefreshLayout;

    private final static int DB_LOADER = 0;
    private final static int WEB_LOADER = 1;

    private boolean multichoice = false;
    private TreeSet<Contact> chosenContacts = new TreeSet<>();

    public interface OnPickEventListener {
        void onContactSetChanged(TreeSet<Contact> chosenContacts);
        void onContactClicked(Contact contact);
    }

    private OnPickEventListener onPickEventListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            Object chosen = savedInstanceState.get(PICKED_CONTACTS);
            if (chosen != null) {
                chosenContacts = (TreeSet<Contact>) chosen;
            }
        }
        Bundle args = getArguments();
        if (args != null) {
            multichoice = args.getBoolean(IS_MULTICHOICE, false);
        }
        return inflateView(inflater, container);
    }

    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_contacts, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        contactsView = (RecyclerView) view.findViewById(R.id.contactsView);
        contactsView.setLayoutManager(new LinearLayoutManager(getContext()));

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getLoaderManager().restartLoader(WEB_LOADER, null, contactsLoaderListener);
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Context context = getContext();
        if (context instanceof OnPickEventListener) {
            onPickEventListener = (OnPickEventListener) context;
            onPickEventListener.onContactSetChanged(chosenContacts);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(DB_LOADER, null, contactsLoaderListener);
    }

    protected AContactAdapter onCreateContactAdapter(@NonNull List<Contact> data) {
         contactAdapter = new ContactAdapter(data, new AContactAdapter.ContactHolder.OnContactClickListener() {
            @Override
            public void onContactClick(View contactView, AContactAdapter.ContactHolder viewHolder) {
                Contact contact = viewHolder.getContact();
                if (chosenContacts.contains(contact)) {
                    chosenContacts.remove(contact);
                    viewHolder.setChosen(false);
                } else {
                    if (!multichoice) {
                        chosenContacts.clear();
                    } else {
                        viewHolder.setChosen(true);
                    }
                    chosenContacts.add(contact);
                }

                if (onPickEventListener != null) {
                    onPickEventListener.onContactSetChanged(chosenContacts);
                    onPickEventListener.onContactClicked(contact);
                }
            }
        });

        if (multichoice) {
            contactAdapter.setMultichoice(chosenContacts);
        }
        return contactAdapter;
    }

    private final LoaderManager.LoaderCallbacks<List<Contact>> contactsLoaderListener =
            new LoaderManager.LoaderCallbacks<List<Contact>>() {
                @Override
                public Loader<List<Contact>> onCreateLoader(int id, Bundle args) {
                    switch (id) {
                        case DB_LOADER:
                            return new ContactListDBLoader(getActivity(), id);
                        case WEB_LOADER:
                            return new ContactListWebLoader(getActivity(), id);
                        default:
                            return null;
                    }
                }

                @Override
                public void onLoadFinished(Loader<List<Contact>> loader, List<Contact> data) {
                    swipeRefreshLayout.setRefreshing(false);

                    if (data != null) {
                        contactsView.setAdapter(onCreateContactAdapter(data));
                    }

                    switch (loader.getId()) {
                        case DB_LOADER:
                            getLoaderManager().restartLoader(WEB_LOADER, null, contactsLoaderListener);
                            break;
                        case WEB_LOADER:
                            if (data == null)
                                Toast.makeText(getActivity(), "Load error. Check your connection.", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }

                @Override
                public void onLoaderReset(Loader<List<Contact>> loader) {
                    // TODO: something...
                }
            };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(PICKED_CONTACTS, chosenContacts);
    }

    public TreeSet<Contact> getChosenContacts() {
        return chosenContacts;
    }
}
