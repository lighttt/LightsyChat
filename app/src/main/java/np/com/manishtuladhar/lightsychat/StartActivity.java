package np.com.manishtuladhar.lightsychat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {

    private static final String TAG = "StartActivity";
    
    private Button mLoginBtn;
    private Button mRegBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Log.d(TAG, "onCreate: StartActivity started");

        mLoginBtn = (Button) findViewById(R.id.start_login_btn);

        mRegBtn = (Button) findViewById(R.id.start_reg_btn);

        mRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent regIntent = new Intent(StartActivity.this,RegisterActivity.class);
                startActivity(regIntent);
            }
        });


        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(StartActivity.this, LoginActivity.class);
                startActivity(loginIntent);
            }
        });
    }
}
