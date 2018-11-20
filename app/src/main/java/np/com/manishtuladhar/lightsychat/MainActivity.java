package np.com.manishtuladhar.lightsychat;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    //firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;

    private Toolbar mToolbar;

    //viewpager
    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: MainActivity started");

        //firebase
        mAuth = FirebaseAuth.getInstance();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        //toolbar
        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Lightsy Chat");

        //tab adapter
        mViewPager = (ViewPager)findViewById(R.id.main_tabPager);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(mSectionsPagerAdapter);

        //tab layout
        mTabLayout = (TabLayout)findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            sendToStart();
        }
        else{
            mUserRef.child(currentUser.getUid()).child("online").setValue(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null)
        {
            mUserRef.child(currentUser.getUid()).child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

    private void sendToStart() {
        Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    //tab layout options
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.main_logout_btn)
        {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MainActivity.this,LoginActivity.class);
            startActivity(intent);
        }

        if(item.getItemId() == R.id.main_settings_btn)
        {
            Intent settingsIntent = new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(settingsIntent);
        }
        if(item.getItemId() == R.id.main_all_users_btn)
        {
            Intent settingsIntent = new Intent(MainActivity.this,UsersActivity.class);
            startActivity(settingsIntent);
        }

        return true;
    }
}
