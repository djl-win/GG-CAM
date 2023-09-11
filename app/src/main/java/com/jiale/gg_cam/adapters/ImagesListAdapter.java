package com.jiale.gg_cam.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.jiale.gg_cam.R;
import com.jiale.gg_cam.entities.MediaItem;

import java.util.List;

/**
 * Images List Adapter
 * <p>
 * Images List Adapter
 * </p>
 *
 * @author Jiale Dong
 * @version 1.0
 * @since 2023-09-10
 */
public class ImagesListAdapter extends RecyclerView.Adapter<ImagesListAdapter.ImageListViewHolder> {

    Context context;
    List<MediaItem> images;

    public ImagesListAdapter(Context context, List<MediaItem> images) {
        this.context = context;
        this.images = images;
    }

    @NonNull
    @Override
    public ImageListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.images_list_item, viewGroup, false);
        return new ImageListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageListViewHolder holder, int position) {
        Log.d("GlideDebug", "Loading URI: " + images.get(position).getUri());
        // 加载用户头像到xml
        Glide.with(context)
                .load(images.get(position).getUri())
                .error(R.drawable.ic_logo)
                .into(holder.imageViewPic);
    }


    @Override
    public int getItemCount() {
        return images.size();
    }



    static class ImageListViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageViewPic;

        public ImageListViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewPic = itemView.findViewById(R.id.images_list_item_image_view_pic);
        }

    }
}

