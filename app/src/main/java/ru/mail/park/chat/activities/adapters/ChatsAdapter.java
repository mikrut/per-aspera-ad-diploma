package ru.mail.park.chat.activities.adapters;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ru.mail.park.chat.R;
import ru.mail.park.chat.activities.DialogActivity;
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
        public final View chatView;

        public final ImageView chatPicture;
        public final TextView chatName;
        public final TextView lastMessageTime;
        public final TextView lastMessageText;

        private String chatID;

        public ViewHolder(final View chatView) {
            super(chatView);
            this.chatView = chatView;

            chatView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(chatView.getContext(), DialogActivity.class);
                    intent.putExtra(DialogActivity.CHAT_ID, chatID);
                    chatView.getContext().startActivity(intent);
                }
            });

            chatPicture = (ImageView) chatView.findViewById(R.id.chatPicture);
            chatName = (TextView) chatView.findViewById(R.id.chatName);
            lastMessageTime = (TextView) chatView.findViewById(R.id.lastMessageTime);
            lastMessageText = (TextView) chatView.findViewById(R.id.lastMessageText);
        }

        public void initView(Chat chat) {
            chatName.setText(chat.getName());
            // FIXME: take values from DB
            // FIXME: get last message text, not description
            lastMessageText.setText(chat.getDescription());
            // TODO: chat pictures

            chatID = chat.getCid();
        }
    }
}
