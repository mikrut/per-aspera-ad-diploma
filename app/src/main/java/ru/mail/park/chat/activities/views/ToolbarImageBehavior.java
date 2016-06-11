package ru.mail.park.chat.activities.views;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import de.hdodenhof.circleimageview.CircleImageView;
import ru.mail.park.chat.R;

/**
 * Created by Михаил on 11.06.2016.
 */
public class ToolbarImageBehavior extends CoordinatorLayout.Behavior<View> {
    float mStartXPosition;
    float mFinalXPosition;

    float mStartYPosition;
    float mFinalYPosition;

    Context mContext;
    private float mAvatarMaxSize;

    public ToolbarImageBehavior(Context context, AttributeSet attrs) {
        mContext = context;
        init();
    }

    private void init() {
        bindDimensions();
    }

    private void bindDimensions() {
        mAvatarMaxSize = mContext.getResources().getDimension(R.dimen.toolbar_image_max_size);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        initProperties(child, dependency);

        final float maxScrollDistance = Math.abs(mFinalYPosition - mStartYPosition);
        final float expandedPercentageFactor = Math.abs(dependency.getY() + dependency.getHeight() - child.getHeight() - mStartYPosition) / maxScrollDistance;

        float distanceXToAdd = (Math.abs(mStartXPosition - mFinalXPosition)
                * expandedPercentageFactor) + mStartXPosition;

        child.setX(mStartXPosition + distanceXToAdd);
        child.setY(dependency.getY() + dependency.getHeight() - child.getHeight());

        return true;
    }

    private void initProperties(View child, View dependency) {
        if (mStartXPosition == 0)
            mStartXPosition = 0;//child.getX();

        if (mStartYPosition == 0)
            mStartYPosition = dependency.getY() + dependency.getHeight() - child.getHeight();//child.getY();

        if (mFinalXPosition == 0)
            mFinalXPosition = 4*mContext.getResources().getDimensionPixelOffset(android.support.design.R.dimen.abc_action_bar_content_inset_material);

        if (mFinalYPosition == 0)
            mFinalYPosition = dependency.getY() + mContext.getResources().getDimensionPixelOffset(android.support.design.R.dimen.abc_action_bar_content_inset_material);
    }



}
