package ru.mail.park.chat.activities.views;

import android.content.Context;
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
    int mStartYPosition;
    int mFinalYPosition;
    int mStartHeight;
    int mStartXPosition;
    int mFinalXPosition;
    float mStartToolbarPosition;
    float mChangeBehaviorPoint;

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
        return dependency instanceof Toolbar;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        initProperties(child, dependency);

        final int maxScrollDistance = (int) mStartToolbarPosition;
        float expandedPercentageFactor = dependency.getY() / maxScrollDistance;

        if (expandedPercentageFactor < mChangeBehaviorPoint) {
            // Never
            Log.e(ToolbarImageBehavior.class.getSimpleName(), "Never");
        } else {
            Log.d(ToolbarImageBehavior.class.getSimpleName(), "Always");

            float distanceYToSubtract = ((mStartYPosition - mFinalYPosition)
                    * (1f - expandedPercentageFactor)) + mStartHeight / 2;

            child.setX(mStartXPosition - child.getWidth()/2);
            child.setY(mStartYPosition - distanceYToSubtract);

            CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
            lp.width = (int) mStartHeight;
            lp.height = (int) mStartHeight;
            child.setLayoutParams(lp);
        }

        return true;
    }

    private void initProperties(View child, View dependency) {
        if (mStartYPosition == 0)
            mStartYPosition = (int) dependency.getY();

        if (mStartXPosition == 0)
            mFinalYPosition = dependency.getHeight() / 2;

        if (mStartHeight == 0)
            mStartHeight = child.getHeight();

        if (mStartXPosition == 0)
            mStartXPosition = (int) child.getX() + (child.getWidth() / 2);

        if (mFinalXPosition == 0)
            mFinalXPosition = mContext.getResources().getDimensionPixelOffset(android.support.design.R.dimen.abc_action_bar_content_inset_material) + (int) mStartHeight / 2;

        if (mStartToolbarPosition == 0)
            mStartToolbarPosition = dependency.getY();

        if (mChangeBehaviorPoint == 0)
            mChangeBehaviorPoint = (child.getHeight() - mStartHeight) / (2f * (mStartYPosition - mFinalYPosition));
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = mContext.getResources().getIdentifier("status_bar_height", "dimen", "android");

        if (resourceId > 0) {
            result = mContext.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
