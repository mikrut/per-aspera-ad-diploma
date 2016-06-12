package ru.mail.park.chat.activities.views.behaviors;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import ru.mail.park.chat.R;

/**
 * Created by Михаил on 11.06.2016.
 */
public class ToolbarLayoutBehavior extends CoordinatorLayout.Behavior<View> {
    Context mContext;
    final DisplayMetrics dm;

    float actionBarSize;
    float topOffset;

    public ToolbarLayoutBehavior(Context context, AttributeSet attrs) {
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
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        animateLayout(child, dependency);
        return true;
    }

    private void animateLayout(View child, View dependency) {
        child.setX(dependency.getX());
        child.setY(0);

        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
        lp.height = (int) Math.max(dependency.getBottom(), topOffset + actionBarSize);
        lp.width  = dependency.getWidth();
        child.setLayoutParams(lp);
    }

}
