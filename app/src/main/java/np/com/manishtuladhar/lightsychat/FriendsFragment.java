package np.com.manishtuladhar.lightsychat;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView mFriendsList;

    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUsersDatabase;

    private FirebaseAuth mAuth;

    private String mCurrent_user_id;

    private View mMainView;

    //firebaserecycler
    FirebaseRecyclerAdapter<Friends, FriendsViewHolder> friendsRecyclerAdapter;

    public FriendsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_friends,container,false);

        mFriendsList = (RecyclerView)mMainView.findViewById(R.id.friends_list);

        //firebase
        mAuth = FirebaseAuth.getInstance();
        //database
        mCurrent_user_id = mAuth.getCurrentUser().getUid();
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
        mFriendsDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        //recyclerview
        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        onActivityStarted();

        // Inflate the layout for this fragment
        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        friendsRecyclerAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        friendsRecyclerAdapter.stopListening();
    }

    private void onActivityStarted() {

        FirebaseRecyclerOptions<Friends> friendsOptions = new FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(mFriendsDatabase,Friends.class).build();

        friendsRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(friendsOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull final Friends model) {

                holder.setDate(model.getDate());

                //directing to the users database for values
                final String list_user_id = getRef(position).getKey();

                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String userThumbImage = dataSnapshot.child("thumb_image").getValue().toString();

                        if(dataSnapshot.hasChild("online"))
                        {
                            String userOnline = dataSnapshot.child("online").toString();
                            holder.setUserOnline(userOnline);
                        }

                        holder.setName(userName);
                        holder.setUserImage(userThumbImage);

                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                CharSequence options[] = new CharSequence[]{"Open Profile", "Send Message"};

                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int i) {

                                        //Click Event for each item.
                                        if(i == 0)
                                        {
                                            Intent profileIntent = new Intent(getContext(),ProfileActivity.class);
                                            profileIntent.putExtra("user_id",list_user_id);
                                            startActivity(profileIntent);
                                        }
                                        if(i == 1)
                                        {
                                            Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                                            chatIntent.putExtra("user_id",list_user_id);
                                            chatIntent.putExtra("user_name",userName);
                                            startActivity(chatIntent);
                                        }
                                    }
                                });
                                builder.show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public FriendsFragment.FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_layout,parent,false);
                return new FriendsViewHolder(view);
            }
        };
        mFriendsList.setAdapter(friendsRecyclerAdapter);
    }

    //Viewholder
    public static class FriendsViewHolder extends  RecyclerView.ViewHolder
    {
        View mView;

        public FriendsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setDate(String date) {

            TextView userNameView = (TextView)mView.findViewById(R.id.user_single_status);
            userNameView.setText(date);
        }

        public void setName(String name)
        {
            TextView userNameView = (TextView)mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);
        }

        public void setUserImage(String thumb_image)
        {
            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.user_single_image);
            Picasso.get().load(thumb_image).placeholder(R.drawable.default_user).into(userImageView);
        }

        public void setUserOnline(String online_status)
        {
            ImageView userOnlineView = (ImageView)mView.findViewById(R.id.users_single_online_icon);

            if(online_status.equals(true))
            {
                userOnlineView.setVisibility(View.VISIBLE);
            }
            else
            {
                userOnlineView.setVisibility(View.INVISIBLE);
            }
        }
    }
}
