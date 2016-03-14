package ru.mail.park.chat.activities;

import android.content.Intent;
import android.graphics.Bitmap;
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
 * Created by Михаил on 12.03.2016.
 */
public abstract class AContactAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    protected static final int CONTACT = 1;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View contactView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.element_contact, parent, false);
        return new ContactHolder(contactView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ContactHolder contactHolder = (ContactHolder) holder;
        Contact contact = getContactForPosition(position);
        contactHolder.setContactName(contact.getLogin());
        contactHolder.setContactLastSeen(contact.getLastSeen().getTime().toGMTString());
    }

    protected abstract Contact getContactForPosition(int position);

    @Override
    public int getItemViewType(int position) {
        return CONTACT;
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

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), UserProfileActivity.class);
                    v.getContext().startActivity(intent);
                }
            });
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
}
