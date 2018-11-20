package np.com.manishtuladhar.lightsychat;

import android.content.Context;
import android.content.Intent;
import android.os.TransactionTooLargeException;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mUsersList;

    private FirebaseAuth mAuth;
    private DatabaseReference mUsersDatabase;
    private FirebaseRecyclerAdapter<Users,UsersViewHolder> firebaseRecyclerAdapter;

    private static final String TAG = "UsersActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        Log.d(TAG, "onCreate: UsersActivity started");

        mAuth = FirebaseAuth.getInstance();
        //Toolbar
        mToolbar = (Toolbar) findViewById(R.id.users_appBar);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //RecyclerView
        mUsersList = (RecyclerView)findViewById(R.id.users_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));

        //FirebaseRecyclerViewAdapter
        onActivityStarted();

    }

    //retrieve data when start
    @Override
    protected void onStart() {
        super.onStart();
        firebaseRecyclerAdapter.startListening();
        String currentUser = mAuth.getCurrentUser().getUid();
        mUsersDatabase.child(currentUser).child("online").setValue(true);
    }


    public void onActivityStarted()
    {
        //database
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        Query usersQuery = mUsersDatabase.orderByKey();

        FirebaseRecyclerOptions usersOptions = new FirebaseRecyclerOptions.Builder<Users>().setQuery(usersQuery,Users.class).build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(usersOptions) {
            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_layout,parent,false);
                return new UsersViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull Users model){

                holder.setName(model.getName());
                holder.setUserStatus(model.getStatus());
                holder.setUserImage(model.getThumb_image());

                //get key from where the user has touched the holder
                final String user_id = getRef(position).getKey();

                //when click on the viewholder
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent profileIntent = new Intent(UsersActivity.this,ProfileActivity.class);
                        profileIntent.putExtra("user_id",user_id);
                        startActivity(profileIntent);
                    }
                });
            }
        };

        mUsersList.setAdapter(firebaseRecyclerAdapter);
    }

    //Viewholder

    public static class UsersViewHolder extends  RecyclerView.ViewHolder
    {
        View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String name)
        {
            TextView userNameView = (TextView) mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);
        }

        public void setUserStatus (String status)
        {
            TextView userStatusView = (TextView) mView.findViewById(R.id.user_single_status);
            userStatusView.setText(status);
        }

        public void setUserImage(String thumb_image)
        {
            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.user_single_image);
            Picasso.get().load(thumb_image).placeholder(R.drawable.default_user).into(userImageView);
        }
    }
}
