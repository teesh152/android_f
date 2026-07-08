package com.mob2.tubeexplorer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mob2.tubeexplorer.R;
import com.mob2.tubeexplorer.model.VideoItem;

import java.util.ArrayList;
import java.util.List;

/**
 * VideoAdapter
 * ------------
 * RecyclerView.Adapter (RecyclerView is REQUIRED by the project) that binds
 * the list of VideoItem objects to CardView rows in Tab 1.
 */
public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    /** Click listener so the Fragment can pass the tapped item to the ViewModel. */
    public interface OnVideoClickListener {
        void onVideoClick(VideoItem item);
    }

    private final List<VideoItem> videos = new ArrayList<>();
    private final OnVideoClickListener listener;

    public VideoAdapter(OnVideoClickListener listener) {
        this.listener = listener;
    }

    /** Replace the whole data set (used after each API fetch / refresh). */
    public void setVideos(List<VideoItem> newVideos) {
        videos.clear();
        if (newVideos != null) videos.addAll(newVideos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        VideoItem item = videos.get(position);

        holder.textTitle.setText(item.getTitle());
        holder.textChannel.setText(item.getChannelTitle());
        holder.textDate.setText(item.getPublishedAt());

        Glide.with(holder.itemView.getContext())
                .load(item.getThumbnailUrl())
                .placeholder(R.drawable.thumbnail_placeholder)
                .centerCrop()
                .into(holder.imageThumbnail);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onVideoClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    /** ViewHolder holding references to the row views. */
    static class VideoViewHolder extends RecyclerView.ViewHolder {
        final ImageView imageThumbnail;
        final TextView textTitle;
        final TextView textChannel;
        final TextView textDate;

        VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageThumbnail = itemView.findViewById(R.id.imageThumbnail);
            textTitle = itemView.findViewById(R.id.textTitle);
            textChannel = itemView.findViewById(R.id.textChannel);
            textDate = itemView.findViewById(R.id.textDate);
        }
    }
}
