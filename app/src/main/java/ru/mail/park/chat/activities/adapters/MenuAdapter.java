package ru.mail.park.chat.activities.adapters;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

import ru.mail.park.chat.R;
import ru.mail.park.chat.activities.ProfileViewActivity;

/**
 * Created by Михаил on 08.03.2016.
 */
public class MenuAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final String[] titles;
    private final int[] icons;
    private final String name;
    private final String email;
    private final String filePath;
    private final Bitmap bmBlurred;
    private final View.OnClickListener[] listeners;

    private static final int HEADER = 0;
    private static final int ITEM = 1;

    public static class HeaderHolder extends RecyclerView.ViewHolder {
        private final TextView userName;
        private final TextView userEmail;
        private final ImageView userPicture;

        public HeaderHolder(final View headerView) {
            super(headerView);
            userName = (TextView) headerView.findViewById(R.id.userName);
            userEmail = (TextView) headerView.findViewById(R.id.userEmail);
            userPicture = (ImageView) headerView.findViewById(R.id.userPicture);

            headerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(headerView.getContext(), ProfileViewActivity.class);
                    headerView.getContext().startActivity(intent);
                }
            });
        }

        public void setUserName(String name) {
            userName.setText(name);
        }

        public void setUserEmail(String email) {
            userEmail.setText(email);
        }

        public void setUserPicture(String filePath) {
            File file = new File(filePath);

            if(file.exists())
                userPicture.setImageBitmap(BitmapFactory.decodeFile(filePath));
            else {
                userPicture.setImageResource(R.drawable.ic_user_picture);
            }
        }
    }

    public static class ItemRowHolder extends RecyclerView.ViewHolder {
        final ImageView rowIcon;
        final TextView rowText;

        public ItemRowHolder(View itemView) {
            super(itemView);
            rowIcon = (ImageView) itemView.findViewById(R.id.rowIcon);
            rowText = (TextView) itemView.findViewById(R.id.rowText);
        }

        public void setTitle(String title) {
            rowText.setText(title);
        }

        public void setIcon(@DrawableRes int icon) {
            rowIcon.setImageResource(icon);
        }

        public void setOnClickListener(@Nullable View.OnClickListener listener) {
            if (listener != null)
                itemView.setOnClickListener(listener);
        }
    }

    public MenuAdapter(String name, String email, String filePath,  Bitmap bmBlurred, String titles[],
                       @NonNull View.OnClickListener[] listeners, @DrawableRes int... icons) {
        this.titles = titles;
        this.icons = icons;
        this.name = name;
        this.email = email;
        this.filePath = filePath;
        this.bmBlurred = bmBlurred;
        this.listeners = listeners;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case HEADER:
                View headerView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.header, parent, false);
                return new HeaderHolder(headerView);
            case ITEM:
                View rowView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_row, parent, false);
                return new ItemRowHolder(rowView);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch(holder.getItemViewType()) {
            case HEADER:
                HeaderHolder headerHolder = (HeaderHolder) holder;
                headerHolder.setUserEmail(email);
                headerHolder.setUserName(name);
                headerHolder.setUserPicture(filePath);
                break;
            case ITEM:
                position--;
                ItemRowHolder itemRowHolder = (ItemRowHolder) holder;
                if (titles.length > position)
                    itemRowHolder.setTitle(titles[position]);
                if (icons.length > position)
                    itemRowHolder.setIcon(icons[position]);
                itemRowHolder.setOnClickListener(listeners[position]);
        }
    }

    @Override
    public int getItemCount() {
        return Math.max(titles.length, icons.length) + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return HEADER;
        return ITEM;
    }
}
