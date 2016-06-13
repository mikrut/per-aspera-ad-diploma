package ru.mail.park.chat.activities.views.behaviors;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import ru.mail.park.chat.R;

/**
 * Created by Михаил on 13.06.2016.
 */
public class ToolbarSubtitleBehavior  extends CoordinatorLayout.Behavior<TextView> {
    Context mContext;
    final DisplayMetrics dm;

    float startFontSize;
    float finalFontSize;

    float startYPos;
    float finalYPos;

    float actionBarSize;
    float topOffset;

    public ToolbarSubtitleBehavior(Context context, AttributeSet attrs) {
        mContext = context;
        dm = mContext.getResources().getDisplayMetrics();
        init();
    }

    private void init() {
        TypedArray arr = mContext.getTheme().obtainStyledAttributes(
                new int[] { android.R.attr.actionBarSize });
        actionBarSize = arr.getDimension(0, 0);
        topOffset = mContext.getResources().getDimensionPixelOffset(android.support.design.R.dimen.abc_action_bar_content_inset_material);
        arr.recycle();
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, TextView child, View dependency) {
        return dependency instanceof TextView && dependency.getId() == R.id.toolbar_title;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, TextView child, View dependency) {
        initProperties(child, dependency);
        float percentage = Math.abs(dependency.getY() - finalYPos) / Math.abs(startYPos - finalYPos);
        animateSubtitle(child, dependency, percentage);
        return true;
    }

    private void initProperties(TextView child, View dependency) {
        if (startFontSize == 0)
            startFontSize = child.getTextSize();

        if (finalFontSize == 0)
            finalFontSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, dm);

        if (startYPos == 0 || startYPos < dependency.getY())
            startYPos = dependency.getY();

        float imageFinalSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, dm);
        if (finalYPos == 0)
            finalYPos = topOffset + (actionBarSize - imageFinalSize) / 2;
    }

    private void animateSubtitle(TextView child, View dependency, float percentage) {
        child.setX(dependency.getX());
        child.setY(dependency.getY() + dependency.getHeight());

        child.setTextSize(TypedValue.COMPLEX_UNIT_PX, (startFontSize - finalFontSize) * percentage + finalFontSize);
    }
}
