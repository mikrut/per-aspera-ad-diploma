package ru.mail.park.chat.activities;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ru.mail.park.chat.R;
import ru.mail.park.chat.models.Chat;

/**
 * Created by Михаил on 07.03.2016.
 */
public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder> {
    private final List<Chat> chats;

    public ChatsAdapter(List<Chat> chats) {
        this.chats = chats;
    }

    @Override
    public ChatsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
       View chatView = LayoutInflater.from(parent.getContext())
               .inflate(R.layout.element_chats, parent, false);
       return new ViewHolder(chatView);
    }

    @Override
    public void onBindViewHolder(ChatsAdapter.ViewHolder holder, int position) {
        holder.initView(chats.get(position));
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public View chatView;

        public ImageView chatPicture;
        public TextView chatName;
        public TextView lastMessageTime;
        public TextView lastMessageText;

        public ViewHolder(View chatView) {
            super(chatView);
            this.chatView = chatView;

            chatPicture = (ImageView) chatView.findViewById(R.id.chatPicture);
            chatName = (TextView) chatView.findViewById(R.id.chatName);
            lastMessageTime = (TextView) chatView.findViewById(R.id.lastMessageTime);
            lastMessageText = (TextView) chatView.findViewById(R.id.lastMessageText);
        }

        public void initView(Chat chat) {
            chatName.setText(chat.getName());
            // FIXME: take values from DB
            lastMessageTime.setText("Sun");
            lastMessageText.setText("Dummy text");
            // TODO: chat pictures
        }
    }
}
