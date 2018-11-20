package np.com.manishtuladhar.lightsychat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout mLoginEmail;
    private TextInputLayout mLoginPassword;
    private Button mLoginBtn;

    private android.support.v7.widget.Toolbar mToolbar;

    private static final String TAG = "LoginActivity";

    //ProgressDialog
    private ProgressDialog mLoginProgress;

    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d(TAG, "onCreate: LoginActivity started");

        //Toolbar Set
        mToolbar = (android.support.v7.widget.Toolbar)findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mLoginProgress = new ProgressDialog(this);

        //Firebase
        mAuth = FirebaseAuth.getInstance();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        //Android Fields

        mLoginEmail = (TextInputLayout)findViewById(R.id.login_email);
        mLoginPassword =(TextInputLayout)findViewById(R.id.login_password);
        mLoginBtn = (Button)findViewById(R.id.reg_create_btn);

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = mLoginEmail.getEditText().getText().toString();
                String password = mLoginPassword.getEditText().getText().toString();

                if(!TextUtils.isEmpty(email)|| !TextUtils.isEmpty(password))
                {
                    mLoginProgress.setTitle("Logining");
                    mLoginProgress.setMessage("Please wait while we login in");
                    mLoginProgress.setCanceledOnTouchOutside(false);
                    mLoginProgress.show();
                    loginUser(email, password);
                }
            }
        });

    }
  /*
   --------------------- Logining to the account ----------------------------------
     */

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {

                            mLoginProgress.dismiss();

                            String current_user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();

                            mUserDatabase.child(current_user_id).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "signInWithEmail:success");
                                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }
                        else {
                            mLoginProgress.hide();
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Cannot sign in. Please check the login form and try again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
