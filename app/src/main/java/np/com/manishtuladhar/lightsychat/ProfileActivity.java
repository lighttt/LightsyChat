package np.com.manishtuladhar.lightsychat;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    //widgets
    private ImageView mProfileImage;
    private TextView mProfileName, mProfileStatus, mProfileFriendsCount;
    private Button mProfileSendReqBtn,mDeclineBtn;

    private ProgressDialog mProgressDialog;

    //firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;
    private FirebaseUser mCurrent_user;
    private DatabaseReference mNotificationDatabase;
    private DatabaseReference mUserRef;

    private DatabaseReference mRootRef;

    //friend state
    private String mCurrent_state;

    private static final String TAG = "ProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Log.d(TAG, "onCreate: ProfileActivity started");
        //getting extra from users
        final String user_id = getIntent().getStringExtra("user_id");
        //firebase
        mAuth = FirebaseAuth.getInstance();
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();

        //widgets
        mProfileImage = (ImageView)findViewById(R.id.profile_displayImage);
        mProfileName = (TextView)findViewById(R.id.profile_displayName);
        mProfileStatus = (TextView)findViewById(R.id.profile_status);
        mProfileFriendsCount = (TextView)findViewById(R.id.profile_totalFriends);
        mProfileSendReqBtn = (Button)findViewById(R.id.profile_send_req_btn);
        mDeclineBtn = (Button)findViewById(R.id.profile_decline_btn);

        //show dialog on loading profile
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading user data");
        mProgressDialog.setMessage("Please wait while we load the user data");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        //default state
        mCurrent_state = "not_friends";

        mDeclineBtn.setVisibility(View.INVISIBLE);
        mDeclineBtn.setEnabled(false);

        //retreiving data on uid
        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(display_name);
                mProfileStatus.setText(status);

                Picasso.get().load(image).placeholder(R.drawable.default_user).into(mProfileImage);

     //====================== FRIENDS LIST / REQUEST FEATURE =======================================

               //work for single value
                mFriendReqDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(user_id))
                        {
                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();
                            if(req_type.equals("recieved"))
                            {
                                mCurrent_state = "req_recieved";
                                mProfileSendReqBtn.setText("Accept Friend Request");
                                mDeclineBtn.setVisibility(View.VISIBLE);
                                mDeclineBtn.setEnabled(true);
                            }
                            else if(req_type.equals("sent"))
                            {
                                mCurrent_state = "req_sent";
                                mProfileSendReqBtn.setText("Cancel Friend Request");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);
                            }
                            mProgressDialog.dismiss();
                        }

                        else{
                            mFriendDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(user_id))
                                    {
                                        mCurrent_state = "friends";
                                        mProfileSendReqBtn.setText("Unfriend this person");
                                        mDeclineBtn.setVisibility(View.INVISIBLE);
                                        mDeclineBtn.setEnabled(false);

                                    }
                                    mProgressDialog.dismiss();
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    mProgressDialog.dismiss();
                                }
                            });
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        mProgressDialog.dismiss();

                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    /*--------------------------- Sending a Friend Request ---------------------------------------------*/

    mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //button disable after tap
                mProfileSendReqBtn.setEnabled(false);


    // ========================= NOT FRIENDS STATE =================================

                //check if the user is friends
                if(mCurrent_state.equals("not_friends"))
                {
                    //creaating a new notification child to the database
                    DatabaseReference newNotificationref = mRootRef.child("notification").child(user_id).push();
                    String newNotificationId = newNotificationref.getKey();

                    //sending notification as hashMap
                    HashMap<String,String> notificationData = new HashMap<>();
                    notificationData.put("from",mCurrent_user.getUid());
                    notificationData.put("type","request");

                    //creating a request map to add two references for the notification
                    //User A to B and B to A
                    Map requestMap = new HashMap();
                    requestMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id + "/request_type","sent");
                    requestMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid() + "/request_type", "recieved");
                    requestMap.put("notifications/" + user_id + "/" + newNotificationId , notificationData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if(databaseError != null)
                            {
                                Toast.makeText(ProfileActivity.this, "There was some error in sending request", Toast.LENGTH_SHORT).show();

                            }
                            mProfileSendReqBtn.setEnabled(true);
                            mCurrent_state = "req_sent";
                            mProfileSendReqBtn.setText("Cancel Friend Request");

                        }
                    });
                }

  // - ========================= REQUEST CANCEL STATE ======================================

                if(mCurrent_state.equals("req_sent"))
                {

                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mProfileSendReqBtn.setEnabled(true);
                                    mCurrent_state = "not_friends";
                                    mProfileSendReqBtn.setText("Send Friend Request");
                                    mDeclineBtn.setVisibility(View.INVISIBLE);
                                    mDeclineBtn.setEnabled(false);
                                }
                            });
                        }
                    });
                }

  // - ========================= REQUEST RECIEVED STATE ======================================

                if(mCurrent_state.equals("req_recieved"))
                {
                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id + "/date", currentDate);
                    friendsMap.put("Friends/" + user_id + "/" + mCurrent_user.getUid() + "/date", currentDate);

                    friendsMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id, null);
                    friendsMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid(), null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if(databaseError == null)
                            {
                                mProfileSendReqBtn.setEnabled(true);
                                mCurrent_state = "friends";
                                mProfileSendReqBtn.setText("Unfriend this person");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);
                            }

                            else {
                                String error = databaseError.getMessage();

                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

    // - ========================= UNFRIEND STATE ======================================

                if(mCurrent_state.equals("friends"))
                {
                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id, null);
                    unfriendMap.put("Friends/" + user_id + "/" + mCurrent_user.getUid(), null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if(databaseError == null)
                            {

                                mCurrent_state = "not_friends";
                                mProfileSendReqBtn.setText("Send Friend Request");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);
                            }

                            else {
                                String error = databaseError.getMessage();

                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                            }

                            mProfileSendReqBtn.setEnabled(true);
                        }
                    });

                }

    // - ========================= DECLINE FRIEND REQUEST STATE ======================================


            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        String currentUser = mAuth.getCurrentUser().getUid();
        mUserRef.child(currentUser).child("online").setValue(true);
    }
}
