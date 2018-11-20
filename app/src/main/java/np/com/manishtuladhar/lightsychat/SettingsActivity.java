package np.com.manishtuladhar.lightsychat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";

    //Firebase
    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mUserRef;
    private FirebaseAuth mAuth;

    //widgets
    private CircleImageView mDisplayImage;
    private TextView mDisplayName;
    private TextView mStatus;
    private Button mImageBtn;
    private Button mStatusBtn;

    private Toolbar mToolbar;

    private static  final int GALLERY_PICK = 1;

    //Storage Firebase
    private StorageReference mImageStorage;

    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Log.d(TAG, "onCreate: SettingsActivity started");

        //widgets
        mDisplayImage = (CircleImageView)findViewById(R.id.settings_image);
        mDisplayName = (TextView)findViewById(R.id.settings_displayname);
        mStatus = (TextView)findViewById(R.id.settings_status);
        mImageBtn = (Button)findViewById(R.id.settings_image_btn);
        mStatusBtn = (Button)findViewById(R.id.settings_status_btn);

        //Toolbar
        mToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.settings_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //storage firebase
        mImageStorage = FirebaseStorage.getInstance().getReference();

        //firebase
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mUserDatabase.keepSynced(true);
        mUserRef.keepSynced(true);

        //used to retrieve data of database
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").toString();

                mDisplayName.setText(name);
                mStatus.setText(status);

                //loads default image if no image
                if(!image.equals("default"))
                {
                    //retrieve offline image
                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.default_user).into(mDisplayImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            //get the image online if not saved offline
                            Picasso.get().load(image).placeholder(R.drawable.default_user).into(mDisplayImage);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //change status
        mStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String status_value = mStatus.getText().toString();

                Intent statusIntent = new Intent(SettingsActivity.this,StatusActivity.class);
                //sending status to status activity
                statusIntent.putExtra("status_value",status_value);

                startActivity(statusIntent);
            }
        });

     /*
     ------------------ Changing Image and Crop Image---------------------------
     */

    mImageBtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Intent galleryIntent = new Intent();
            //creating a image only type intent
            galleryIntent.setType("image/*");
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
            //opens gallerydocument chooser
            startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"),GALLERY_PICK);

            }
        });
    }

    /*
    -------------------------------Crop image-------------------------------------------------------
    */

    //process this result after you get selcet image from the image gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //if the data is correctfully recieved
        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
            //saves the postion to a string
            Uri imageUri = data.getData();
            //start crop activity
            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .start(this);

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK)
            {
                mProgressDialog = new ProgressDialog(SettingsActivity.this);
                mProgressDialog.setTitle("Uploading image");
                mProgressDialog.setMessage("Please wait while we upload and process the image");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                //cropped data uri
                Uri resultUri = result.getUri();

                File thumb_filePath = new File(resultUri.getPath());
                String current_userid = mCurrentUser.getUid();

                //compressing the file into bitmap

                Bitmap thumb_bitmap = null;
                try
                {
                    thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_filePath);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

                //convert bitmap to byte to upload to firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos);
                    final byte[] thumb_byte = baos.toByteArray();


                final StorageReference filepath = mImageStorage.child("profile_images").child(current_userid + ".jpg");
                final StorageReference thumb_filepath = mImageStorage.child("profile_images").child("thumbs").child(current_userid + ".jpg");

                //put the image in firebase
                filepath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                final String download_url = uri.toString();

                                UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);
                                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                        thumb_filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {

                                                final String thumb_downloadurl = uri.toString();

                                                Map update_hashMap = new HashMap();
                                                update_hashMap.put("image",download_url);
                                                update_hashMap.put("thumb_image",thumb_downloadurl);

                                                mUserDatabase.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task)
                                                    {
                                                        if(task.isSuccessful())
                                                        {
                                                            mProgressDialog.dismiss();
                                                            Toast.makeText(SettingsActivity.this, "Uploaded successfully", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                mProgressDialog.dismiss();
                                                Toast.makeText(SettingsActivity.this, "Error in uploading thumbnail", Toast.LENGTH_LONG).show();

                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mProgressDialog.dismiss();
                            Toast.makeText(SettingsActivity.this, "Error in uploading", Toast.LENGTH_LONG).show();

                        }
                    });
            }

            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {

                Exception error = result.getError();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
            mUserDatabase.child("online").setValue(true);
    }
}
