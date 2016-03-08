package ru.mail.park.chat.activities;

import android.support.annotation.DrawableRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import ru.mail.park.chat.R;

/**
 * Created by Михаил on 08.03.2016.
 */
public class MenuAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private String[] titles;
    private int[] icons;
    private String name;
    private String email;
    private int userPicture;

    private static final int HEADER = 0;
    private static final int ITEM = 1;

    public static class HeaderHolder extends RecyclerView.ViewHolder {
        private TextView userName;
        private TextView userEmail;
        private ImageView userPicture;

        public HeaderHolder(View headerView) {
            super(headerView);
            userName = (TextView) headerView.findViewById(R.id.userName);
            userEmail = (TextView) headerView.findViewById(R.id.userEmail);
            userPicture = (ImageView) headerView.findViewById(R.id.userPicture);

        }

        public void setUserName(String name) {
            userName.setText(name);
        }

        public void setUserEmail(String email) {
            userEmail.setText(email);
        }

        public void setUserPicture(@DrawableRes int picture) {
            userPicture.setImageResource(picture);
        }
    }

    public static class ItemRowHolder extends RecyclerView.ViewHolder {
        ImageView rowIcon;
        TextView rowText;

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
    }

    public MenuAdapter(String name, String email, @DrawableRes int userPicture, String titles[], @DrawableRes int... icons) {
        this.titles = titles;
        this.icons = icons;
        this.name = name;
        this.email = email;
        this.userPicture = userPicture;
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
                headerHolder.setUserPicture(userPicture);
                break;
            case ITEM:
                position--;
                ItemRowHolder itemRowHolder = (ItemRowHolder) holder;
                if (titles.length > position)
                    itemRowHolder.setTitle(titles[position]);
                if (icons.length > position)
                    itemRowHolder.setIcon(icons[position]);
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
