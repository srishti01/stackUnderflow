package com.example.stackunderflow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.data.model.User;
import com.squareup.picasso.Picasso;

import java.util.List;

import static androidx.recyclerview.widget.RecyclerView.*;

public class UserAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private Context mContext;
    private List<Contacts> mUsers;

    public UserAdapter(Context mContext, List<Contacts> mUsers) {
        this.mContext = mContext;
        this.mUsers = mUsers;
    }


    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item,parent,false);
        return new MessageAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {
        final Contacts user= mUsers.get(position);
        holder.username.setText(user.getName());
        if(user.getImageURL().equals("default"))
        {
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        }
        else
        {
            Picasso.get().load(profile_image).placeholder(R.drawable.profile_image).into(msg_Profile_Image);
        }

        //holder.itemView.setOnClickListene
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView username;
        public ImageView profile_image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.name);
            profile_image = itemView.findViewById(R.id.profile_image);
        }

    }
}
