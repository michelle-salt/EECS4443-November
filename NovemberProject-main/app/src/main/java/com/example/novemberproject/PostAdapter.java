package com.example.novemberproject;

import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private ArrayList<Post> posts;
    private OnLikeClickListener likeClickListener;

    public interface OnLikeClickListener {
        void onLikeClicked(Post post);
        void onUnlikeClicked(Post post);
    }

    public PostAdapter(ArrayList<Post> posts, OnLikeClickListener listener) {
        this.posts = posts;
        this.likeClickListener = listener;
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivPostImage;
        public TextView tvSummary;
        public ImageButton btnLike;

        public PostViewHolder(View itemView) {
            super(itemView);
            ivPostImage = itemView.findViewById(R.id.ivPostImage);
            tvSummary = itemView.findViewById(R.id.tvSummary);
            btnLike = itemView.findViewById(R.id.btnLike);

            // set to clickable to detect double tap likes
            ivPostImage.setClickable(true);
            ivPostImage.setFocusable(true);
        }
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        int pos = holder.getAdapterPosition();
        if (pos == RecyclerView.NO_POSITION) {
            return;
        }
        Post currentPost = posts.get(pos);
        holder.tvSummary.setText(currentPost.getSummary());

        // Load the post image
        if (currentPost.getImageUrl() != null && !currentPost.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(currentPost.getImageUrl())
                    .into(holder.ivPostImage);
        } else {
            holder.ivPostImage.setImageResource(android.R.color.transparent);
        }

        // Set the heart icon based on the like state
        if (currentPost.isLiked()) {
            holder.btnLike.setImageResource(R.drawable.heart_filled);
        } else {
            holder.btnLike.setImageResource(R.drawable.heart_outline);
        }

        // Single tap on the heart button to like
        holder.btnLike.setOnClickListener(v -> {
            int posClick = holder.getAdapterPosition();
            if (posClick == RecyclerView.NO_POSITION) return;
            Post post = posts.get(posClick);
            if (!post.isLiked()) {
                post.setLiked(true);
                if (likeClickListener != null) {
                    likeClickListener.onLikeClicked(post);
                }
            } else {
                post.setLiked(false);
                if (likeClickListener != null) {
                    likeClickListener.onUnlikeClicked(post);
                }
            }
            notifyItemChanged(posClick);
        });

        // Double tap image of post to like
        GestureDetector gestureDetector = new GestureDetector(holder.itemView.getContext(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDown(MotionEvent e) {
                        return true;
                    }
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        int posDouble = holder.getAdapterPosition();
                        if (posDouble == RecyclerView.NO_POSITION) return false;
                        Post post = posts.get(posDouble);
                        if (!post.isLiked()) {
                            post.setLiked(true);
                            if (likeClickListener != null) {
                                likeClickListener.onLikeClicked(post);
                            }
                        } else {
                            post.setLiked(false);
                            if (likeClickListener != null) {
                                likeClickListener.onUnlikeClicked(post);
                            }
                        }
                        notifyItemChanged(posDouble);
                        return true;
                    }
                });
        holder.ivPostImage.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }
}
