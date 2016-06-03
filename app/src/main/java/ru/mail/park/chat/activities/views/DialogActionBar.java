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

import java.net.URL;

import ru.mail.park.chat.R;
import ru.mail.park.chat.activities.ProfileViewActivity;
import ru.mail.park.chat.loaders.images.IImageSettable;
import ru.mail.park.chat.loaders.images.ImageDownloadManager;
import ru.mail.park.chat.models.Chat;

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

    public void setChatData(@NonNull Chat chat, @Nullable ImageDownloadManager imageDownloadManager) {
        setTitle(chat.getName());

        URL imageURL = chat.getImagePath();
        if (oldUrl == null || (imageURL != null && !oldUrl.equals(imageURL))) {
            if (imageDownloadManager != null && imageURL != null) {
                imageDownloadManager.setImage(this, imageURL, ImageDownloadManager.Size.SMALL);
                imageTitle.setVisibility(View.GONE);
                oldUrl = imageURL;
            }
        }

        switch (chat.getType()) {
            case Chat.INDIVIDUAL_TYPE:
                String companionID = chat.getCompanionId();
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

