package com.example.stackunderflow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.data.model.User;
import com.squareup.picasso.Picasso;

import java.util.List;

import static androidx.recyclerview.widget.RecyclerView.*;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private Context mContext;
    private List<Contacts> mUsers;

    public UserAdapter(Context mContext, List<Contacts> mUsers) {
        this.mContext = mContext;
        this.mUsers = mUsers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(mContext).inflate(R.layout.user_item,parent,false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Contacts user=mUsers.get(position);
        holder.username.setText(user.getName());
        Glide.with(mContext).load(user.getImage()).into(holder.profileImage);

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView username;
        public ImageView profileImage;

        public ViewHolder(View itemView)
        {
            super(itemView);

            username=itemView.findViewById(R.id.username_chats);
            profileImage=itemView.findViewById(R.id.user_profile_image);
        }
    }
}
