package np.com.manishtuladhar.lightsychat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class    RegisterActivity extends AppCompatActivity {

    private TextInputLayout mDisplayName;
    private TextInputLayout mEmail;
    private TextInputLayout mPassword;
    private Button mCreateBtn;

    private android.support.v7.widget.Toolbar mToolbar;

    private static final String TAG = "RegisterActivity";

    //ProgressDialog
    private ProgressDialog mRegProgress;

    //Firebase Database
    private DatabaseReference mDatabase;

    //Firebase Auth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Log.d(TAG, "onCreate: RegisterActivity started");

        //Toolbar Set
        mToolbar = (android.support.v7.widget.Toolbar)findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mRegProgress = new ProgressDialog(this);

        //Firebase
        mAuth = FirebaseAuth.getInstance();

        //Android Fields
        mDisplayName = (TextInputLayout) findViewById(R.id.reg_display_name);
        mPassword = (TextInputLayout)findViewById(R.id.reg_password);
        mEmail =(TextInputLayout)findViewById(R.id.reg_email);
        mCreateBtn = (Button)findViewById(R.id.reg_create_btn);

        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String display_name = mDisplayName.getEditText().getText().toString();
                String email = mEmail.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();

                if(!TextUtils.isEmpty(display_name)|| !TextUtils.isEmpty(email)|| !TextUtils.isEmpty(password))
                {
                    mRegProgress.setTitle("Registering User");
                    mRegProgress.setMessage("Please wait while we create your account!");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();
                    register_user(display_name, email, password);
                }


            }
        });

    }

    /*
   --------------------- Registering a new account ----------------------------------
     */

    private void register_user(final String display_name, String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            //Firebase uid
                            FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = current_user.getUid();
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();

                            //Firebase setup with needed nodes
                            mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                            HashMap<String, String> userMap = new HashMap<>();
                            userMap.put("device_token",deviceToken);
                            userMap.put("name",display_name);
                            userMap.put("status","Hi there I'm using Lightsy Chat App.");
                            userMap.put("image","default");
                            userMap.put("thumb_image","default");

                            mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        mRegProgress.dismiss();

                                        Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(mainIntent);
                                        finish();

                                        Log.d(TAG, "createUserWithEmail:success");
                                    }
                                }
                            });

                        }
                        else {

                            // If sign in fails, display a message to the user.
                            mRegProgress.hide();
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Cannot sign in. Please check the form and try again.",
                                    Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });

    }
}
