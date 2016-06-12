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

import ru.mail.park.chat.R;

/**
 * Created by Михаил on 13.06.2016.
 */
public class ToolbarImageBehavior extends CoordinatorLayout.Behavior<ImageView> {
    float startBottom;
    float finalBottom;

    float imageStartXPosition;
    float imageFinalXPosition;

    float imageStartYPosition;
    float imageFinalYPosition;

    float imageStartSize;
    float imageFinalSize;

    Context mContext;
    final DisplayMetrics dm;

    float actionBarSize;
    float topOffset;

    public ToolbarImageBehavior(Context context, AttributeSet attrs) {
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
    public boolean layoutDependsOn(CoordinatorLayout parent, ImageView child, View dependency) {
        return dependency.getId() == R.id.toolbar_background;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, ImageView child, View dependency) {
        final ImageView image = child;

        Log.d("image", "behavior");

        initImageProperties(image, dependency);

        final float maxScrollDistance = Math.abs(imageFinalYPosition - imageStartYPosition);
        final float expandedPercentageFactor = Math.abs(dependency.getY() + dependency.getLayoutParams().height - finalBottom) / Math.abs(startBottom - finalBottom);

        animateImage(image, expandedPercentageFactor);

        return true;
    }

    private void animateImage(ImageView image, float percentage) {
        float x = (imageStartXPosition - imageFinalXPosition) * percentage + imageFinalXPosition;
        float y = (imageStartYPosition - imageFinalYPosition) * percentage + imageFinalYPosition;
        float h = (imageStartSize - imageFinalSize) * percentage + imageFinalSize;

        Log.d("yf", String.valueOf(imageFinalYPosition));
        Log.d("ys", String.valueOf(imageStartYPosition));
        Log.d("pe", String.valueOf(percentage));
        Log.d("y ", String.valueOf(y));

        image.setX(x);
        image.setY(y);
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) image.getLayoutParams();
        lp.height = (int) h;
        lp.width = (int) h;
        image.setLayoutParams(lp);
    }

    private void initImageProperties(ImageView image, View dependency) {
        if (imageStartSize == 0)
            imageStartSize = image.getHeight();

        if (imageFinalSize == 0)
            imageFinalSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, dm);

        if (imageStartXPosition == 0)
            imageStartXPosition = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, dm);

        if (imageStartYPosition == 0)
            imageStartYPosition = dependency.getY() + dependency.getHeight() - image.getHeight() - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, dm);

        if (imageFinalXPosition == 0)
            imageFinalXPosition = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 74, dm);

        if (imageFinalYPosition == 0)
            imageFinalYPosition = dependency.getY() + topOffset + (actionBarSize - imageFinalSize) / 2;

        if (startBottom == 0)
            startBottom = dependency.getY() + dependency.getHeight();

        if (finalBottom == 0) {
            finalBottom = dependency.getY() + actionBarSize + topOffset;
        }
    }

}
