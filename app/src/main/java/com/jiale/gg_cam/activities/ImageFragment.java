package com.jiale.gg_cam.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jiale.gg_cam.R;
import com.jiale.gg_cam.entities.CamItem;

/**
 * Image Fragment
 * <p>
 * Image Fragment logic
 * </p>
 *
 * @author Jiale Dong
 * @version 1.0
 * @since 2023-09-12
 */
public class ImageFragment extends DialogFragment {

    private View view;
    // Media Item
    private CamItem mediaItemSelf;

    // back button
    private ImageButton imageButtonBack;

    // time
    private TextView textViewTime;

    // city
    private TextView textViewCity;

    // location
    private TextView textViewLocation;

    // image
    private ImageView imageViewPic;

    // video
    private PlayerView videoViewPic;

    // main page
    private RelativeLayout relativeLayoutMain;

    // 初始化播放器
    private ExoPlayer player;

    // 关闭时的视图
    ImageView imageViewClosePicture;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Set the dialog style
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyleAndAnimation);

        // 获取的Dialog实例
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // 将对话框的布局文件转化为页面底层view
        view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_image, null);

        // 设置Dialog的内容视图
        dialog.setContentView(view);

        // 设置软键盘模式，当键盘弹出时调整布局
        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        // init views
        initViews(view);
        // set Listener
        setListener();

        // Returns th Dialog instance
        return dialog;
    }

    /**
     * init views
     *
     * @param view views
     */
    @SuppressLint("SetTextI18n")
    private void initViews(View view) {
        imageButtonBack = view.findViewById(R.id.image_fragment_image_button_back);
        textViewTime = view.findViewById(R.id.image_fragment_text_view_time);
        textViewTime.setText("Time: " + mediaItemSelf.getTime());
        textViewCity = view.findViewById(R.id.image_fragment_text_view_city);
        textViewCity.setText("City: " + mediaItemSelf.getCity());
        textViewLocation = view.findViewById(R.id.image_fragment_text_view_location);
        textViewLocation.setText("Location: " + mediaItemSelf.getLocation());
        imageViewPic = view.findViewById(R.id.image_fragment_image_view_pic);
        videoViewPic = view.findViewById(R.id.image_fragment_video_view_pic);
        initImage();
        relativeLayoutMain = view.findViewById(R.id.image_fragment_relative_layout_main);
    }

    /**
     * init image or videos
     */
    private void initImage() {
        if (mediaItemSelf.getType() == CamItem.MediaType.IMAGE) {
            imageViewPic.setVisibility(View.VISIBLE);
            videoViewPic.setVisibility(View.GONE);
            // 图片
            Glide.with(this)
                    .load(mediaItemSelf.getUri())
                    .centerCrop()
                    .error(R.drawable.ic_logo)
                    .into(imageViewPic);
        } else {
            imageViewPic.setVisibility(View.GONE);
            videoViewPic.setVisibility(View.VISIBLE);

            // 初始化播放器
            player = new ExoPlayer.Builder(getActivity()).build();


            videoViewPic.setPlayer(player);

            // 准备本地视频的播放源
            MediaItem mediaItem = MediaItem.fromUri(mediaItemSelf.getUri());
            player.setMediaItem(mediaItem);

            // 准备并开始播放
            player.prepare();


        }
    }

    /**
     * set Listener
     */
    private void setListener() {
        // imageButtonBack Listener
        imageButtonBackListener();
    }

    /**
     * imageButtonBack Listener
     */
    private void imageButtonBackListener() {
        imageButtonBack.setOnClickListener(view1 -> {
            dismiss();
        });
    }

    public void setMediaItem(CamItem mediaItem01) {
        this.mediaItemSelf = mediaItem01;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        if (player != null) {
            videoViewPic.setPlayer(null);  // Disconnect PlayerView from the player
            player.release();              // 释放播放器资源
            player = null;                 // 将播放器对象设置为null
        }
        super.onDismiss(dialog);
    }


}