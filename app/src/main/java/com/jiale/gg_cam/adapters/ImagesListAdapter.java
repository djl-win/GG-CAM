package com.jiale.gg_cam.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.jiale.gg_cam.R;
import com.jiale.gg_cam.entities.CamItem;

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

    // 回调接口，获取单机事件的回调
    public interface ItemClickListener {
        void onItemClick(CamItem camItem);
    }

    private ItemClickListener itemClickListener;
    Context context;
    List<CamItem> images;

    public ImagesListAdapter(Context context, List<CamItem> images) {
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
        holder.imageViewPlay.setVisibility(View.GONE);
        // 加载用户头像到xml
        Glide.with(context)
                .load(images.get(position).getUri())
                .centerCrop()
                .error(R.drawable.ic_logo)
                .into(holder.imageViewPic);
        if(images.get(position).getType() == CamItem.MediaType.VIDEO){
            holder.imageViewPlay.setVisibility(View.VISIBLE);
        }
        // bind onclick call back
        holder.frameLayoutMain.setOnClickListener(view -> {
            // use call back to images activity
            if (itemClickListener != null) {
                itemClickListener.onItemClick(images.get(position));
            }
        });
    }


    @Override
    public int getItemCount() {
        return images.size();
    }

    /**
     * set on click listener for images list item
     *
     * @param itemClickListener content of father container
     */
    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }



    static class ImageListViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageViewPic;
        private ImageView imageViewPlay;
        private FrameLayout frameLayoutMain;

        public ImageListViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewPic = itemView.findViewById(R.id.images_list_item_image_view_pic);
            imageViewPlay = itemView.findViewById(R.id.images_list_item_image_view_play);
            frameLayoutMain = itemView.findViewById(R.id.images_list_item_frame_layout_main);
        }

    }
}

