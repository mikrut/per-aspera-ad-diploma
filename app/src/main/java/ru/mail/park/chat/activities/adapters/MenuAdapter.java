package ru.mail.park.chat.activities.adapters;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
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
import ru.mail.park.chat.activities.interfaces.IUserPicSetupListener;
import ru.mail.park.chat.loaders.images.IImageSettable;

/**
 * Created by Михаил on 08.03.2016.
 */
public class MenuAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final String[] titles;
    private final int[] icons;
    private final String name;
    private final String email;
    private Bitmap bmBlurred;
    private Bitmap bmUserImage;
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


        public void setBluredBackground(Bitmap blurredImage) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                itemView.setBackground(new BitmapDrawable(itemView.getContext().getResources(), blurredImage));
            } else {
                //noinspection deprecation
                itemView.setBackgroundDrawable(new BitmapDrawable(itemView.getContext().getResources(), blurredImage));
            }
        }

        public void setUserName(String name) {
            userName.setText(name);
        }

        public void setUserEmail(String email) {
            userEmail.setText(email);
        }

        public void setUserPicture(Bitmap userImage) {
            if (userImage == null) {
                userPicture.setImageResource(R.drawable.ic_user_picture);
            } else {
                userPicture.setImageBitmap(userImage);
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

    public MenuAdapter(String name, String email, String titles[],
                       @NonNull View.OnClickListener[] listeners, @DrawableRes int... icons) {
        this.titles = titles;
        this.icons = icons;
        this.name = name;
        this.email = email;
        this.listeners = listeners;
    }

    public IImageSettable getBlurSettable() {
        return new IImageSettable() {
            @Override
            public void setImage(Bitmap image) {
                bmBlurred = image;
                notifyItemChanged(0);
            }
        };
    }

    public IImageSettable getUserImageSettable() {
        return new IImageSettable() {
            @Override
            public void setImage(Bitmap image) {
                bmUserImage = image;
                notifyItemChanged(0);
            }
        };
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
                headerHolder.setUserPicture(bmUserImage);
                headerHolder.setBluredBackground(bmBlurred);
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
