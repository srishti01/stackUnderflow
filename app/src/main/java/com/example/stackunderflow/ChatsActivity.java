package com.example.stackunderflow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<Contacts> mUsers;

    FirebaseUser fuser;
    DatabaseReference reference;

    private List<String> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);

        recyclerView=findViewById(R.id.chats_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        fuser= FirebaseAuth.getInstance().getCurrentUser();
        userList=new ArrayList<>();

        reference= FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();

                for(DataSnapshot snapshot1 : snapshot.getChildren())
                {
                    Chat chat=snapshot1.getValue(Chat.class);

                    if(chat.getSender().equals(fuser.getUid()))
                    {
                        userList.add(chat.getReceiver());
                    }
                    if(chat.getReceiver().equals(fuser.getUid()))
                    {
                        userList.add(chat.getSender());
                    }
                }
                readChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void readChats()
    {
        mUsers=new ArrayList<>();
        reference=FirebaseDatabase.getInstance().getReference("User");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUsers.clear();

                for(DataSnapshot snapshot1 : snapshot.getChildren())
                {
                    Contacts user=snapshot1.getValue(Contacts.class);

                    for(String id : userList)
                    {
                        if(user.getUid().equals(id))
                        {
                            if(mUsers.size()!=0)
                            {
                                for(Contacts user1 : mUsers)
                                {
                                    if(!user.getUid().equals(user1.getUid()))
                                    {
                                        mUsers.add(user);
                                    }
                                }
                            } else mUsers.add(user);
                        }
                    }
                }

                userAdapter=new UserAdapter(getApplicationContext(),mUsers);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}