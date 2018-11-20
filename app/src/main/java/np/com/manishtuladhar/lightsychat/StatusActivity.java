package np.com.manishtuladhar.lightsychat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private static final String TAG = "StatusActivity";

    private android.support.v7.widget.Toolbar mToolbar;

    private TextInputLayout mStatus;
    private Button mSaveBtn;

    //Firebase
    private DatabaseReference mStatusDatabase;
    private FirebaseUser mCurrentUser;

    //Progress
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        Log.d(TAG, "onCreate: StatusActivity started");

        //Firebase
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();

        mStatusDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        //Toolbar
        mToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.status_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //widgets
        mStatus = (TextInputLayout)findViewById(R.id.status_input);
        mSaveBtn = (Button)findViewById(R.id.status_save_btn);

        //intent
        String status_value = getIntent().getStringExtra("status_value");
        mStatus.getEditText().setText(status_value);

        //changing status
        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Progress
                mProgress = new ProgressDialog(StatusActivity.this);//runs again when saved
                mProgress.setTitle("Saving Changes");
                mProgress.setMessage("Please wait while we save the changes");
                mProgress.show();

                String status = mStatus.getEditText().getText().toString();

                //write status to database
                mStatusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful())
                        {
                            mProgress.dismiss();
                            Intent statusIntent = new Intent(StatusActivity.this,SettingsActivity.class);
                            startActivity(statusIntent);
                        }
                        else{

                            Toast.makeText(StatusActivity.this, "There was some error in saving changes", Toast.LENGTH_LONG).show();

                        }
                    }
                });
            }
        });
    }
}
