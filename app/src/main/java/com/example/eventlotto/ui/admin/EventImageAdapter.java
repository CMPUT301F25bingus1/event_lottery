package com.example.eventlotto.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eventlotto.R;

import java.util.List;

public class EventImageAdapter extends RecyclerView.Adapter<EventImageAdapter.ImageViewHolder> {

    private final List<AdmImagesFragment.EventImageItem> images;
    private final OnImageClickListener listener;

    public interface OnImageClickListener {
        void onImageClick(AdmImagesFragment.EventImageItem item);
    }

    public EventImageAdapter(List<AdmImagesFragment.EventImageItem> images,
                             OnImageClickListener listener) {
        this.images = images;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        AdmImagesFragment.EventImageItem item = images.get(position);

        Glide.with(holder.itemView.getContext())
                .load(item.getImageUrl())
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .centerCrop()
                .into(holder.imageView);

        holder.eventTitle.setText(item.getEventTitle());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onImageClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return images != null ? images.size() : 0;
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView eventTitle;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            eventTitle = itemView.findViewById(R.id.event_title);
        }
    }
}

