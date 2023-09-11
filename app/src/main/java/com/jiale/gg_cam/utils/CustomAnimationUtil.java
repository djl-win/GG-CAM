package com.jiale.gg_cam.utils;


import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.jiale.gg_cam.R;
import com.google.android.material.card.MaterialCardView;


/**
 * Custom animation classes
 * <p>
 * Load animation
 * </p>
 * @author Jiale Dong
 * @version 1.0
 * @since 2023-09-09
 */
public class CustomAnimationUtil {

    /**
     * show camera
     * @param context  context
     * @param imageView imageView
     */
    public static void imageViewAnim01(Context context, MaterialCardView imageView){
        // Start the slide-out animation after the delay
        Animation slideOutAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_out_bottom);
        imageView.startAnimation(slideOutAnimation);
    }

    /**
     * hide camera
     * @param context  context
     * @param imageView imageView
     */
    public static void imageViewAnim02(Context context, MaterialCardView imageView){
        // Start the slide-out animation after the delay
        Animation slideOutAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_out_top);
        imageView.startAnimation(slideOutAnimation);
    }


}
