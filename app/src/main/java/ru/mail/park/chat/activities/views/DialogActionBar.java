package ru.mail.park.chat.activities.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.net.MalformedURLException;
import java.net.URL;

import ru.mail.park.chat.R;
import ru.mail.park.chat.activities.ProfileViewActivity;
import ru.mail.park.chat.api.ApiSection;
import ru.mail.park.chat.loaders.images.IImageSettable;
import ru.mail.park.chat.loaders.images.ImageDownloadManager;
import ru.mail.park.chat.models.Chat;
import ru.mail.park.chat.models.Contact;

/**
 * Created by Михаил on 03.06.2016.
 */
public class DialogActionBar
        extends RelativeLayout
        implements IImageSettable {
    private ImageButton backImageButton;
    private RelativeLayout imageLayout;
    private ImageView dialogImageView;
    private TextView imageTitle;
    private ProgressBar progressBar;
    private TextView titleTextView;
    private TextView subtitleTextView;

    private URL oldUrl = null;

    public DialogActionBar(Context context) {
        super(context);
        initializeViews();
    }

    public DialogActionBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews();
        initAttrs(attrs);
    }

    public DialogActionBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeViews();
        initAttrs(attrs);
    }

    public void initAttrs(AttributeSet attrs) {
        // TODO: something ?
    }

    private void initializeViews() {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.actionbar_dialog, this);

        backImageButton = (ImageButton) findViewById(R.id.small_dialog_user_icon);
        imageLayout = (RelativeLayout) findViewById(R.id.imageLayout);
        dialogImageView = (ImageView) findViewById(R.id.image);
        imageTitle = (TextView) findViewById(R.id.imageText);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        titleTextView = (TextView) findViewById(R.id.dialog_title);
        subtitleTextView = (TextView) findViewById(R.id.dialog_last_seen);

        backImageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getContext();
                if (context instanceof Activity) {
                    ((Activity) context).finish();
                }
            }
        });

        progressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    public void setImage(Bitmap image) {
        dialogImageView.setImageBitmap(image);
    }

    @Override
    public int getImageWidth() {
        return dialogImageView.getWidth();
    }

    @Override
    public int getImageHeight() {
        return dialogImageView.getHeight();
    }

    public void setChatData(@NonNull Chat chat, @Nullable final ImageDownloadManager imageDownloadManager) {
        setTitle(chat.getName());

        final URL imageURL = chat.getImagePath();
        if (oldUrl == null || (imageURL != null && !oldUrl.equals(imageURL))) {
            if (imageDownloadManager != null && imageURL != null) {
                dialogImageView.post(new Runnable() {
                    @Override
                    public void run() {
                        imageDownloadManager.setImage(DialogActionBar.this, imageURL);
                    }
                });
                imageTitle.setVisibility(View.GONE);
                oldUrl = imageURL;
            }
        }

        switch (chat.getType()) {
            case Chat.INDIVIDUAL_TYPE:
                String companionID = chat.getCompanionId(getContext());
                if (companionID != null) {
                    Intent intent = new Intent(getContext(), ProfileViewActivity.class);
                    intent.putExtra(ProfileViewActivity.UID_EXTRA, companionID);
                    setTransitionIntent(intent);
                }
                break;
            case Chat.GROUP_TYPE:
                // TODO: Edit chat activity
                break;
            default:
                break;
        }
    }

    public void setUserData(@NonNull Contact user, @Nullable final ImageDownloadManager imageDownloadManager) {
        setTitle(user.getContactTitle());

        String userImg = user.getImg();
        if (userImg != null) {
            try {
                final URL imageURL = new URL(ApiSection.SERVER_URL + user.getImg());
                if ((oldUrl == null || !oldUrl.equals(imageURL)) && imageDownloadManager != null) {
                    dialogImageView.post(new Runnable() {
                        @Override
                        public void run() {
                            imageDownloadManager.setImage(DialogActionBar.this, imageURL);
                            imageTitle.setVisibility(View.GONE);
                        }
                    });
                    oldUrl = imageURL;
                }
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            }
        }

        Intent intent = new Intent(getContext(), ProfileViewActivity.class);
        intent.putExtra(ProfileViewActivity.UID_EXTRA, user.getUid());
        setTransitionIntent(intent);
    }

    public void setTransitionIntent(final Intent intent) {
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getContext().startActivity(intent);
            }
        });
    }

    public void setTitle(String title) {
        titleTextView.setText(title);

        if (oldUrl == null) {
            imageTitle.setVisibility(View.VISIBLE);
            TitledPicturedViewHolder.setTitle(dialogImageView, imageTitle, title);
        } else {
            imageTitle.setVisibility(View.GONE);
        }
    }

    public void setSubtitle(CharSequence subtitle) {
        subtitleTextView.setText(subtitle);
    }

    public void setProgress(boolean inProgress) {
        progressBar.setVisibility(inProgress ? View.VISIBLE : View.GONE);
        imageLayout.setVisibility(inProgress ? View.GONE : View.VISIBLE);
    }
}

