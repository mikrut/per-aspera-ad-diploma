package ru.mail.park.chat.activities.views.behaviors;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
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
        animateSubtitle(child, dependency);
        return true;
    }

    private void animateSubtitle(TextView child, View dependency) {
        child.setX(dependency.getX());
        child.setY(dependency.getY() + dependency.getHeight());
    }
}
