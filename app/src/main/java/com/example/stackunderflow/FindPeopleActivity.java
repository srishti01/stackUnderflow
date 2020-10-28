package com.example.stackunderflow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.content.Context;

import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


public class FindPeopleActivity extends AppCompatActivity {

    private RecyclerView findPeopleList;

    private EditText searchET;
    private String str="";
    private DatabaseReference usersRef;

    private String userName, profileImage ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_people);


        searchET = findViewById(R.id.search_user_text);         //receives the name to search
        findPeopleList = findViewById(R.id.find_people_list);
        findPeopleList.setLayoutManager( new LinearLayoutManager(getApplicationContext()));

        usersRef = FirebaseDatabase.getInstance().getReference().child("User");

        searchET.addTextChangedListener(new TextWatcher() {        //inbuilt method to access and implement functions when text is added in searchbar
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(searchET.getText().toString().equals(""))     //when search bar is left empty
                    Toast.makeText(FindPeopleActivity.this, "Please write a name to search",Toast.LENGTH_SHORT).show();
                else
                {
                    str=s.toString();   //the text to be searched is stored in str
                    onStart();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options=null;   //options is a local variable accessing the four details from database parameters in contacts activity
        if(str.equals(""))
        {
            options = new FirebaseRecyclerOptions.Builder<Contacts>()
                    .setQuery(usersRef, Contacts.class)
                    .build();     //displaying only suggested users when nothing is typed in search bar
        }
        else
        {
            options =
                    new FirebaseRecyclerOptions.Builder<Contacts>()
                            .setQuery(usersRef.orderByChild("Name").startAt(str).endAt(str + "\uf8ff")
                                    , Contacts.class)
                            .build();  //search database when str is not null
        }

        FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, final int position, @NonNull final Contacts model)
            {
                final String listUserID=getRef(position).getKey();

                usersRef.child(listUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {

                            userName = snapshot.child("Name").getValue().toString();
                            profileImage = snapshot.child("image").getValue().toString();

                            holder.userNameTxt.setText(userName);
                            Picasso.get().load(profileImage).into(holder.profileImageView);

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //when some user from the search suggestions
                                    String visit_user_id = getRef(position).getKey();  //visit_user_id is storing the reference to the user that has been clicked upon

                                    Intent intent = new Intent(FindPeopleActivity.this, ProfileActivity.class);
                                    intent.putExtra("visit_user_id", visit_user_id);
                                    intent.putExtra("profile_image", model.getImage());
                                    intent.putExtra("profile_name", model.getName());

                                    startActivity(intent); //profile activity is being started by giving information of user_id,image and name
                                }
                            });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
//                holder.itemView.setOnClickListener(new View.OnClickListener()
//                {
//                    @Override
//                    public void onClick(View v)
//                    {
//                        //when some user from the search suggestions
//                        String visit_user_id = getRef(position).getKey();  //visit_user_id is storing the reference to the user that has been clicked upon
//
//                        Intent intent = new Intent(FindPeopleActivity.this, ProfileActivity.class);
//                        intent.putExtra("visit_user_id", visit_user_id );
//                        intent.putExtra("profile_image",model.getImage());
//                        intent.putExtra("profile_name", model.getName());
//
//                        startActivity(intent); //profile activity is being started by giving information of user_id,image and name
//                    }
//                });
            }
                    @NonNull
            @Override
            public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_design,parent,false);
                FindFriendsViewHolder viewHolder = new FindFriendsViewHolder(view);
                return viewHolder;
            }
        };

        findPeopleList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening(); //this will enable displaying the suggested users whenever we change text in searchbar
    }

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder
    {
        TextView userNameTxt;
        Button videoCallBtn,msgBtn;
        ImageView profileImageView;
        RelativeLayout cardView1;

             public FindFriendsViewHolder(@NonNull View itemView)
             {
                 super(itemView);

                 userNameTxt = itemView.findViewById(R.id.name_contact);
                 videoCallBtn = itemView.findViewById(R.id.call_btn);
                 profileImageView = itemView.findViewById(R.id.image_contact);
                 cardView1 = itemView.findViewById(R.id.card_view1);
                 msgBtn=itemView.findViewById(R.id.msg_button);

                 videoCallBtn.setVisibility(View.GONE); //videocall option wont be shown when the user is being displayed as a result of search
                 msgBtn.setVisibility(View.GONE);
             }
    }
}