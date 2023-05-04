package com.example.gpt;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

   private List<Object> mData;

   public ChatAdapter(List<Object> data) {
      mData = data;
   }

   private static final int TYPE_MESSAGE = 1;
   private static final int TYPE_IMAGE = 2;

   public void addMessage(String message) {
      mData.add(message);
      notifyDataSetChanged();
   }

   public void addImage(Bitmap image) {
      mData.add(image);
      notifyDataSetChanged();
   }

   public void addImageUrl(String imageUrl) {
      mData.add(imageUrl);
      notifyDataSetChanged();
   }

   @Override
   public int getItemViewType(int position) {
      Object item = mData.get(position);
      if (item instanceof String && ((String) item).startsWith("http")) {
         return TYPE_IMAGE;
      } else if (item instanceof Bitmap) {
         return TYPE_IMAGE;
      } else if (item instanceof String) {
         return TYPE_MESSAGE;
      }
      throw new IllegalArgumentException("Invalid item type");
   }

   @NonNull
   @Override
   public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      LayoutInflater inflater = LayoutInflater.from(parent.getContext());
      switch (viewType) {
         case TYPE_MESSAGE:
            View messageView = inflater.inflate(R.layout.item_chat_message, parent, false);
            return new MessageViewHolder(messageView);
         case TYPE_IMAGE:
            View imageView = inflater.inflate(R.layout.item_chat_image, parent, false);
            return new ImageViewHolder(imageView);
         default:
            throw new IllegalArgumentException("Invalid view type");
      }
   }

   @Override
   public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
      Object item = mData.get(position);
      switch (holder.getItemViewType()) {
         case TYPE_MESSAGE:
            String message = (String) item;
            ((MessageViewHolder) holder).bind(message);
            break;
         case TYPE_IMAGE:
            if (item instanceof Bitmap) {
               Bitmap image = (Bitmap) item;
               ((ImageViewHolder) holder).bind(image);
            } else if (item instanceof String && ((String) item).startsWith("http")) {
               String imageUrl = (String) item;
               ((ImageViewHolder) holder).bind(imageUrl);
            }
            break;
         default:
            throw new IllegalArgumentException("Invalid view type");
      }
   }

   @Override
   public int getItemCount() {
      return mData.size();
   }

   static class MessageViewHolder extends RecyclerView.ViewHolder {

      TextView mMessageTextView;

      MessageViewHolder(@NonNull View itemView) {
         super(itemView);
         mMessageTextView = itemView.findViewById(R.id.text_view_message);
      }

      void bind(String message) {
         mMessageTextView.setText(message);
      }
   }

   static class ImageViewHolder extends RecyclerView.ViewHolder {

      ImageView mImageView;

      ImageViewHolder(@NonNull View itemView) {
         super(itemView);
         mImageView = itemView.findViewById(R.id.image_view);
      }

      void bind(Bitmap image) {
         mImageView.setImageBitmap(image);
      }

      void bind(String imageUrl) {
         Glide.with(itemView.getContext())
                 .load(imageUrl)
                 .into(mImageView);
      }
   }
}
