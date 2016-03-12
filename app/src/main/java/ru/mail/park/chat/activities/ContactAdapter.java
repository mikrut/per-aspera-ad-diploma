package ru.mail.park.chat.activities;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
public class ContactAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Map<Character, List<Contact>> contactGroups;
    private int itemCount;

    private static final int LETTER = 0;
    private static final int CONTACT = 1;

    public ContactAdapter(List<Contact> contactList) {
        contactGroups = new TreeMap<>();
        Collections.sort(contactList);
        itemCount = contactList.size();

        if (contactList.size() > 0) {
            Character lastChar = contactList.get(0).getLogin().charAt(0);
            List<Contact> currentGroup = new LinkedList<>();
            contactGroups.put(lastChar, currentGroup);

            for (Contact contact : contactList) {
                Character currentChar = contact.getLogin().charAt(0);
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
                View letterView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.element_letter, parent, false);
                return new LetterHolder(letterView);
            case CONTACT:
                View contactView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.element_contact, parent, false);
                return new ContactHolder(contactView);
            default:
                return  null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int currentPosition = 0;

        Set<Character> keys = contactGroups.keySet();
        Iterator<Character> iKey;
        char key = '\0';
        iKey = keys.iterator();
        while (iKey.hasNext()) {
            key = iKey.next();
            if (currentPosition + contactGroups.get(key).size() >= position) {
                break;
            }
        }

        switch (holder.getItemViewType()) {
            case LETTER:
                LetterHolder letterHolder = (LetterHolder) holder;
                letterHolder.setLetter(key);
                break;
            case CONTACT:
                ContactHolder contactHolder = (ContactHolder) holder;
                List<Contact> contactGroup = contactGroups.get(key);
                Contact contact = contactGroup.get(position - currentPosition - 1);
                contactHolder.setContactName(contact.getLogin());
                contactHolder.setContactLastSeen(contact.getLastSeen().getTime().toGMTString());
                break;
        }
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

    public static class ContactHolder extends RecyclerView.ViewHolder {
        private ImageView contactImage;
        private TextView contactName;
        private TextView contactLastSeen;

        public ContactHolder(View itemView) {
            super(itemView);
            contactImage = (ImageView) itemView.findViewById(R.id.contactImage);
            contactName = (TextView) itemView.findViewById(R.id.contactName);
            contactLastSeen = (TextView) itemView.findViewById(R.id.contactLastSeen);
        }

        public void setContactImage(Bitmap bitmap) {
            contactImage.setImageBitmap(bitmap);
        }

        public void setContactName(String name) {
            contactName.setText(name);
        }

        public void setContactLastSeen(String lastSeen) {
            contactLastSeen.setText(lastSeen);
        }
    }

    public static class LetterHolder extends RecyclerView.ViewHolder {
        private TextView letter;

        public LetterHolder(View itemView) {
            super(itemView);
            letter = (TextView) itemView.findViewById(R.id.letter);
        }

        public void setLetter(char letter) {
            this.letter.setText(String.valueOf(letter));
        }
    }
}
