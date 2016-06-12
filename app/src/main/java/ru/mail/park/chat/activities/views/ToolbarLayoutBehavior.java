package ru.mail.park.chat.activities.views;

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
public class ToolbarLayoutBehavior extends CoordinatorLayout.Behavior<RelativeLayout> {
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
    public boolean layoutDependsOn(CoordinatorLayout parent, RelativeLayout child, View dependency) {
        return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, RelativeLayout child, View dependency) {
        final ImageView image = (ImageView) child.findViewById(R.id.toolbar_image);
        final TextView title = (TextView) child.findViewById(R.id.toolbar_title);
        final TextView subtitle = (TextView) child.findViewById(R.id.toolbar_subtitle);

        initLayoutProperties(child, dependency);
        initImageProperties(image, dependency);

        final float maxScrollDistance = Math.abs(imageFinalYPosition - imageStartYPosition);
        final float expandedPercentageFactor = Math.abs(dependency.getY() + dependency.getHeight() - finalBottom) / Math.abs(startBottom - finalBottom);

        animateLayout(child, dependency);
        animateImage(image, expandedPercentageFactor);
        animateTitle(title, image);
        animateSubtitle(subtitle, title);

        return true;
    }

    private void animateLayout(RelativeLayout child, View dependency) {
        child.setX(dependency.getX());
        child.setY(0);

        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
        lp.height = (int) Math.max(dependency.getBottom(), topOffset + actionBarSize);
        lp.width  = dependency.getWidth();
        child.setLayoutParams(lp);
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
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) image.getLayoutParams();
        lp.height = (int) h;
        lp.width = (int) h;
        image.setLayoutParams(lp);
    }

    private void animateTitle(TextView title, ImageView image) {
        title.setX(image.getX() + image.getWidth());
        title.setY(image.getY());
    }

    private void animateSubtitle(TextView subtitle, TextView title) {
        subtitle.setX(title.getX());
        subtitle.setY(title.getY() + title.getHeight());
    }

    private void initLayoutProperties(RelativeLayout child, View dependency) {
        if (startBottom == 0)
            startBottom = dependency.getY() + dependency.getHeight();

        if (finalBottom == 0) {
           finalBottom = dependency.getY() + actionBarSize + topOffset;
        }
    }

    private void initImageProperties(ImageView image, View dependency) {
        if (imageStartSize == 0)
            imageStartSize = image.getHeight();

        if (imageFinalSize == 0)
            imageFinalSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, dm);

        if (imageStartXPosition == 0)
            imageStartXPosition = image.getX();//child.getX();

        if (imageStartYPosition == 0)
            imageStartYPosition = image.getY();//child.getY();

        if (imageFinalXPosition == 0)
            imageFinalXPosition = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 74, dm);

        if (imageFinalYPosition == 0)
            imageFinalYPosition = dependency.getY() + topOffset + (actionBarSize - imageFinalSize) / 2;
    }



}
