package ru.mail.park.chat.activities.adapters;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import ru.mail.park.chat.R;
import ru.mail.park.chat.activities.DialogActivity;
import ru.mail.park.chat.activities.views.TitledPicturedViewHolder;
import ru.mail.park.chat.loaders.images.ImageDownloadManager;
import ru.mail.park.chat.models.Chat;

/**
 * Created by Михаил on 07.03.2016.
 */
public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder> {
    private final List<Chat> chats;
    private ImageDownloadManager downloadManager = null;

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

    public class ViewHolder extends TitledPicturedViewHolder {
        public final View chatView;

        public final ImageView groupIndicatorView;
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

            groupIndicatorView = (ImageView) chatView.findViewById(R.id.groupIndicatorImageView);
            chatName = (TextView) chatView.findViewById(R.id.chatName);
            lastMessageTime = (TextView) chatView.findViewById(R.id.lastMessageTime);
            lastMessageText = (TextView) chatView.findViewById(R.id.lastMessageText);
        }

        @Override
        public void setTitle(String title) {
            super.setTitle(title);
            chatName.setText(title);
        }

        public void initView(Chat chat) {
            setTitle(chat.getName());
            // FIXME: take values from DB
            // FIXME: get last message text, not description
            if (chat.getDescription() != null) {
                lastMessageText.setText(chat.getDescription());
            } else {
                lastMessageText.setText("Chat is empty");
            }

            // TODO: chat pictures
            switch (chat.getType()) {
                case Chat.GROUP_TYPE:
                    groupIndicatorView.setVisibility(View.VISIBLE);
                    break;
                case Chat.INDIVIDUAL_TYPE:
                default:
                    groupIndicatorView.setVisibility(View.GONE);
            }
            chatID = chat.getCid();

            String timestring = "";
            Calendar dtime = chat.getDateTime();
            Calendar now = GregorianCalendar.getInstance();
            if (dtime != null) {
                if (dtime.get(Calendar.YEAR) == now.get(Calendar.YEAR)) {
                    if (dtime.get(Calendar.MONTH) == now.get(Calendar.MONTH)) {
                        if (dtime.get(Calendar.WEEK_OF_MONTH) == now.get(Calendar.WEEK_OF_MONTH)) {
                            timestring = capFirstLetter(dtime.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()));
                        } else {
                            timestring = capFirstLetter(dtime.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault())) + " " +
                                String.valueOf(dtime.get(Calendar.DATE));
                        }
                    } else {
                        timestring = capFirstLetter(dtime.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())) + " " +
                                String.valueOf(dtime.get(Calendar.DATE));
                    }
                } else {
                    DateFormat mFormat = new SimpleDateFormat("yy.MM.dd", Locale.getDefault());
                    timestring = mFormat.format(dtime.getTime());
                }
            }

            lastMessageTime.setText(timestring);

            if (chat.getImagePath() != null && downloadManager != null) {
                downloadManager.setImage(getImage(), chat.getImagePath(), ImageDownloadManager.Size.SMALL);
            }
            if (chat.getImagePath() != null) {
                Log.i("ImagePath", chat.getImagePath().toString());
            }
        }
    }

    public void setDownloadManager(ImageDownloadManager downloadManager) {
        ImageDownloadManager old = this.downloadManager;
        this.downloadManager = downloadManager;
        if (old == null && downloadManager != null) {
            notifyDataSetChanged();
        }
    }

    private static String capFirstLetter(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }
}
