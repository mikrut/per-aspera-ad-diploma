package ru.mail.park.chat.activities.adapters;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import ru.mail.park.chat.R;
import ru.mail.park.chat.models.Contact;

/**
 * Created by Михаил on 08.03.2016.
 */
public class ContactAdapter extends AContactAdapter {
    private final Map<Character, List<Contact>> contactGroups;
    private ContactHolder.OnContactClickListener contactClickListener;
    private int itemCount;

    private static final int LETTER = 0;

    public ContactAdapter(@NonNull List<Contact> contactList,
                          @Nullable ContactHolder.OnContactClickListener contactClickListener) {
        this(contactList);
        this.contactClickListener = contactClickListener;
    }

    public ContactAdapter(@NonNull List<Contact> contactList) {
        contactGroups = new TreeMap<>();
        Collections.sort(contactList);
        itemCount = contactList.size();

        if (contactList.size() > 0) {
            Character lastChar = contactList.get(0).getContactTitle().charAt(0);
            List<Contact> currentGroup = new LinkedList<>();
            contactGroups.put(lastChar, currentGroup);

            for (Contact contact : contactList) {
                Character currentChar = contact.getContactTitle().charAt(0);
                if (!lastChar.equals(currentChar)) {
                    lastChar = currentChar;
                    currentGroup = new LinkedList<>();
                    contactGroups.put(lastChar, currentGroup);
                }
                currentGroup.add(contact);
            }
        }
        itemCount += contactGroups.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case LETTER:
                return createLetterHolder(parent);
            case CONTACT:
                return createContactHolder(parent);
            default:
                return  null;
        }
    }

    private RecyclerView.ViewHolder createLetterHolder(ViewGroup parent) {
        View letterView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.element_letter, parent, false);
        return new LetterHolder(letterView);
    }

    private RecyclerView.ViewHolder createContactHolder(ViewGroup parent) {
        View contactView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.element_contact, parent, false);
        return new ContactHolder(contactView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case LETTER:
                LetterHolder letterHolder = (LetterHolder) holder;
                letterHolder.setLetter(getLetterForPosition(position));
                break;
            case CONTACT:
                super.onBindViewHolder(holder, position);
                if (contactClickListener != null)
                    ((ContactHolder) holder).setOnContactClickListener(contactClickListener);
                break;
        }
    }

    private Pair<Character, Integer> getKeyForPosition(int position) {
        int currentPosition = 0;

        Set<Character> keys = contactGroups.keySet();
        Iterator<Character> iKey;
        char key = '\0';
        iKey = keys.iterator();
        while (iKey.hasNext()) {
            key = iKey.next();
            if (currentPosition + contactGroups.get(key).size() >= position) {
                break;
            } else {
                currentPosition += contactGroups.get(key).size() + 1;
            }
        }
        return new Pair<>(key, currentPosition);
    }

    private char getLetterForPosition(int position) {
        return getKeyForPosition(position).first;
    }

    @Override
    protected Contact getContactForPosition(int position) {
        Pair<Character, Integer> result = getKeyForPosition(position);
        List<Contact> contactGroup = contactGroups.get(result.first);
        return contactGroup.get(position - result.second - 1);
    }

    @Override
    public int getItemCount() {
        return itemCount;
    }

    @Override
    public int getItemViewType(int position) {
        int currentPosition = 0;

        for(Character key : contactGroups.keySet()) {
            if (currentPosition == position) {
                return LETTER;
            } else if (currentPosition + contactGroups.get(key).size() >= position) {
                return CONTACT;
            } else {
                currentPosition += contactGroups.get(key).size() + 1;
            }
        }
        return LETTER;
    }

    public static class LetterHolder extends RecyclerView.ViewHolder {
        private final TextView letter;

        public LetterHolder(View itemView) {
            super(itemView);
            letter = (TextView) itemView.findViewById(R.id.letter);
        }

        public void setLetter(char letter) {
            this.letter.setText(String.valueOf(letter));
        }
    }
}
