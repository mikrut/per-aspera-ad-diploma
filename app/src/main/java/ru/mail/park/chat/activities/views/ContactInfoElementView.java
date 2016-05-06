package ru.mail.park.chat.activities.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ru.mail.park.chat.R;

/**
 * Created by Михаил on 06.05.2016.
 */
public class ContactInfoElementView extends RelativeLayout {
    private ImageView mPictureView;
    private TextView mMainTextView;
    private TextView mHelperTextView;

    private Drawable src;
    private CharSequence text;
    private CharSequence hint;

    public ContactInfoElementView(Context context) {
        super(context);
        initializeViews();
    }

    public ContactInfoElementView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews();
        initAttrs(attrs);
    }

    public ContactInfoElementView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeViews();
        initAttrs(attrs);
    }

    public void initAttrs(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs,
                R.styleable.ContactInfoElementView);

        src = a.getDrawable(R.styleable.ContactInfoElementView_android_src);
        text = a.getString(R.styleable.ContactInfoElementView_android_text);
        hint = a.getString(R.styleable.ContactInfoElementView_android_hint);

        a.recycle();
    }

    private void initializeViews() {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_contact_info_element, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mPictureView = (ImageView) findViewById(R.id.picture);
        mMainTextView = (TextView) findViewById(R.id.main_text);
        mHelperTextView = (TextView) findViewById(R.id.helper_text);

        mPictureView.setImageDrawable(src);
        mMainTextView.setText(text != null ? text : "");
        mHelperTextView.setText(hint != null ? hint : "");
    }

    public void setText(CharSequence text) {
        this.text = text;
        mMainTextView.setText(text);
    }
}
