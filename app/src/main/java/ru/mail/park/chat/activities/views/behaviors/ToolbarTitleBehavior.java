package ru.mail.park.chat.activities.views.behaviors;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ru.mail.park.chat.R;

/**
 * Created by Михаил on 13.06.2016.
 */
public class ToolbarTitleBehavior extends CoordinatorLayout.Behavior<TextView> {
    Context mContext;
    final DisplayMetrics dm;

    float actionBarSize;
    float topOffset;

    public ToolbarTitleBehavior(Context context, AttributeSet attrs) {
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
        return dependency instanceof ImageView && dependency.getId() == R.id.toolbar_image;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, TextView child, View dependency) {
        animateTitle(child, dependency);
        return true;
    }

    private void animateTitle(TextView child, View dependency) {
        child.setX(dependency.getX() + dependency.getWidth());
        child.setY(dependency.getY());
    }
}
