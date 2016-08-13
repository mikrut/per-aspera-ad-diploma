package ru.mail.park.chat.activities.adapters;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

import ru.mail.park.chat.R;
import ru.mail.park.chat.activities.AImageDownloadServiceBindingActivity;
import ru.mail.park.chat.activities.ProfileViewActivity;
import ru.mail.park.chat.activities.views.TitledPicturedViewHolder;
import ru.mail.park.chat.api.ApiSection;
import ru.mail.park.chat.loaders.images.IImageSettable;
import ru.mail.park.chat.loaders.images.ImageDownloadManager;
import ru.mail.park.chat.models.Contact;

/**
 * Created by Михаил on 12.03.2016.
 */
public abstract class AContactAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements AImageDownloadServiceBindingActivity.IImageDownloadManagerBinderSubscriber {
    static final int CONTACT = 1;

    private Drawable contactActionDrawable;
    private ImageDownloadManager imageManager;
    private ContactHolder.OnContactActionListener contactActionListener;

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
        contactHolder.setContact(contact, imageManager, contactActionListener, contactActionDrawable);
    }

    protected abstract Contact getContactForPosition(int position);

    @Override
    public int getItemViewType(int position) {
        return CONTACT;
    }

    public static class ContactHolder extends TitledPicturedViewHolder {
        final ImageView contactImage;
        final ImageView choosenImage;
        final TextView contactName;
        final TextView contactLastSeen;

        final ImageButton contactAction;

        String uid;
        Contact contact;

        public ContactHolder(View itemView) {
            super(itemView);
            contactImage = (ImageView) itemView.findViewById(R.id.image);
            choosenImage = (ImageView) itemView.findViewById(R.id.choosenImage);
            contactName = (TextView) itemView.findViewById(R.id.contactName);
            contactLastSeen = (TextView) itemView.findViewById(R.id.contactLastSeen);
            contactAction = (ImageButton) itemView.findViewById(R.id.contact_action_image_button);

            setOnContactClickListener(new OnContactClickListener() {
                @Override
                public void onContactClick(View v, ContactHolder holder) {
                    Intent intent = new Intent(v.getContext(), ProfileViewActivity.class);
                    intent.putExtra(ProfileViewActivity.UID_EXTRA, uid);
                    v.getContext().startActivity(intent);
                }
            });
        }

        public void setContact(Contact contact, ImageDownloadManager imageManager, OnContactActionListener contactActionListener, Drawable contactActionDrawable) {
            if (contact.getImg() != null && !contact.getImg().equals("false") && imageManager != null) {
                try {
                    URL url = new URL(ApiSection.SERVER_URL + contact.getImg());
                    imageManager.setImage(this, url, ImageDownloadManager.Size.SMALL);
                } catch (MalformedURLException e) {
                    Log.w(ContactAdapter.class.getSimpleName(), e.getLocalizedMessage());
                }
            } else {
                setImage(null);
            }

            setTitle(contact.getContactTitle());
            this.uid = contact.getUid();

            Calendar lastSeen = contact.getLastSeen();
            if (lastSeen != null) {
                Log.d("[TP-diploma]", "got lastSeen field");
                setContactLastSeen(ProfileViewActivity.formatLastSeenTime(lastSeen));
            } else {
                Log.d("[TP-diploma]", "dont have lastSeen");
                setContactLastSeen(contact.isOnline() ? "Online" : "Offline");
            }

            this.contact = contact;

            if (contactActionListener != null && contactActionDrawable != null) {
                setContactAction(contactActionDrawable, contactActionListener);
            }
        }

        public interface OnContactActionListener {
            void onContactAction(Contact contact);
        }

        public void setContactAction(Drawable icon, final OnContactActionListener listener) {
            setContactAction(icon, null, listener);
        }

        public void setContactAction(Drawable icon, CharSequence actionDescription,
                                     final OnContactActionListener listener) {
            if (contactAction != null) {
                contactAction.setImageDrawable(icon);
                contactAction.setContentDescription(actionDescription);
                contactAction.setVisibility(View.VISIBLE);
                contactAction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.v(ContactHolder.class.getSimpleName(), "Contact action");
                        listener.onContactAction(contact);
                    }
                });
            }
        }

        public void setActionEnabled(boolean enabled) {
            contactAction.setVisibility(enabled ? View.VISIBLE : View.GONE);
        }

        public void setChosen(boolean chosen) {
            setChosen(chosen, true);
        }

        public void setChosen(boolean chosen, boolean withAnimation) {
            choosenImage.setVisibility(chosen ? View.VISIBLE : View.GONE);
            if (withAnimation) {
                Animation chosenAnimation = AnimationUtils
                        .loadAnimation(itemView.getContext(),
                            chosen ? R.anim.done_appearence : R.anim.done_disappearence);
                choosenImage.startAnimation(chosenAnimation);
            }
        }

        public Contact getContact() {
            return contact;
        }

        public void setUid(String uid) {this.uid = uid;}

        @Override
        public void setTitle(String name) {
            super.setTitle(name);
            contactName.setText(name);
        }

        public void setContactLastSeen(String lastSeen) {
            contactLastSeen.setText(lastSeen);
        }

        public void setOnContactClickListener(final OnContactClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onContactClick(v, ContactHolder.this);
                }
            });
        }

        public interface OnContactClickListener {
            void onContactClick(View contactView, ContactHolder viewHolder);
        }
    }

    public void setContactAction(Drawable contactActionDrawable,
                                 ContactHolder.OnContactActionListener listener) {
        this.contactActionDrawable = contactActionDrawable;
        this.contactActionListener = listener;
    }

    @Override
    public void onImageDownloadManagerAvailable(@NonNull ImageDownloadManager imageManager) {
        ImageDownloadManager old = this.imageManager;
        this.imageManager = imageManager;
        if (old == null && imageManager != null) {
            notifyDataSetChanged();
        }
    }
}
