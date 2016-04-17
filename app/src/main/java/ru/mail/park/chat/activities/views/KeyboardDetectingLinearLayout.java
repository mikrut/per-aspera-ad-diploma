package ru.mail.park.chat.activities.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Created by Михаил on 08.04.2016.
 */
public class KeyboardDetectingLinearLayout extends LinearLayout {
    public KeyboardDetectingLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public interface OnKeyboardEventListener {
        void onSoftKeyboardShown();
        void onSoftKeyboardHidden();
    }

    private OnKeyboardEventListener listener;
    public void setOnKeyboardEventListener(OnKeyboardEventListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int proposedHeight = MeasureSpec.getSize(heightMeasureSpec);
        final int actualHeight = getHeight();

        int diff = (actualHeight - proposedHeight);
        if (listener != null) {
            if (diff > 128) { // assume all soft keyboards are at least 128 pixels high
                listener.onSoftKeyboardShown();
            } else {
                listener.onSoftKeyboardHidden();
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
