package ru.mail.park.chat.activities.adapters;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ru.mail.park.chat.R;
import ru.mail.park.chat.activities.views.TitledPicturedViewHolder;
import ru.mail.park.chat.models.Message;

/**
 * Created by Михаил on 27.03.2016.
 */
public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {
    private static final int INCOMING_MESSAGE = 0;
    private static final int OUTGOING_MESSAGE = 1;

    private final List<Message> messagesSet;
    private final String ownerUID;

    public MessagesAdapter(@NonNull List<Message> messagesSet, @NonNull String ownerUID) {
        this.messagesSet = messagesSet;
        this.ownerUID = ownerUID;
    }

    public class ViewHolder extends TitledPicturedViewHolder {
        private final TextView messageText;
        private final ImageView contactPicture;

        public ViewHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.messageText);
            contactPicture = (ImageView) itemView.findViewById(R.id.image);
        }

        public void setMessageText(@NonNull String text) {
            messageText.setText(text);
        }

        public void setContactPicture(@NonNull Bitmap picture) {
            contactPicture.setImageBitmap(picture);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        @android.support.annotation.LayoutRes int resource;
        switch (viewType) {
            case INCOMING_MESSAGE:
                resource = R.layout.element_message_left;
                break;
            case OUTGOING_MESSAGE:
            default:
                resource = R.layout.element_message_right;
                break;
        }
        View messageView = LayoutInflater.from(parent.getContext())
                .inflate(resource, parent, false);
        return new ViewHolder(messageView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Message message = messagesSet.get(position);
        holder.setMessageText(message.getMessageBody());
        holder.setTitle(message.getTitle());
    }

    @Override
    public int getItemCount() {
        return messagesSet.size();
    }

    @Override
    public int getItemViewType(int position) {
        String senderUID = messagesSet.get(position).getUid();
        if (senderUID.equals(ownerUID)) {
            return OUTGOING_MESSAGE;
        } else {
            return INCOMING_MESSAGE;
        }
    }


}
