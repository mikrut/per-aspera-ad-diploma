package ru.mail.park.chat.activities.views;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import ru.mail.park.chat.R;

/**
 * Created by Михаил on 12.06.2016.
 */
public class ToolbarBackgroundBehavior extends CoordinatorLayout.Behavior<View> {
    Context mContext;

    public ToolbarBackgroundBehavior(Context context, AttributeSet attrs) {
        mContext = context;
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
        params.height = dependency.getBottom();
        child.setLayoutParams(params);
        child.setY(0);
        return true;
    }



}
