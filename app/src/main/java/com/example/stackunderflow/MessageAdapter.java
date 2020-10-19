package com.example.stackunderflow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.data.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public  static final int MSG_TYPE_LEFT=0;
    public  static final int MSG_TYPE_RIGHT=1;

    private Context mContext;
    private List<Chat> mChats;
    private String imageUrl;

    FirebaseUser fuser;

    public MessageAdapter(Context mContext, List<Chat> mChats,String imageUrl) {
        this.mContext = mContext;
        this.mChats = mChats;
        this.imageUrl=imageUrl;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType==MSG_TYPE_LEFT)   //if msg type is left i.e from the receiver, we return view as layout as left_chat_item
        {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }
        else   //if msg type is right i.e from the sender, we return view as layout of right_chat_item
            {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        Chat chat=mChats.get(position);
        holder.show_message.setText(chat.getMessage());
//        Glide.with(mContext).load(imageUrl).into(holder.profileImage);
//        Picasso.get().load(imageUrl).into(holder.profileImage);

    }

    @Override
    public int getItemCount() {
        return mChats.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView show_message;
        public ImageView profileImage;

        public ViewHolder(View itemView)
        {
            super(itemView);

            show_message=itemView.findViewById(R.id.show_message);   //to show the msg
            profileImage=itemView.findViewById(R.id.chat_profile_image);
        }
    }

    @Override
    public int getItemViewType(int position) {
        fuser= FirebaseAuth.getInstance().getCurrentUser();
        //to return type of msg i.e from receiver or sender
        if(mChats.get(position).getSender().equals(fuser.getUid()))
        {
            return MSG_TYPE_RIGHT;
        }
        else return MSG_TYPE_LEFT;
    }
}
