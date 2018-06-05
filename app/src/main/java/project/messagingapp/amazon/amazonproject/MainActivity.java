package project.messagingapp.amazon.amazonproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;


public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    final static String TAG = "MainActivity";
    private String url;
    private String uid;
    private int tabposition=0;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.e(TAG,"Created");
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));


        SharedPreferences sharedPreferences = getSharedPreferences("Cust", Context.MODE_PRIVATE);
        String type = sharedPreferences.getString("type","notfoundtype");
        String deviceid = FirebaseInstanceId.getInstance().getToken();

        Log.e(TAG,type+"Heyyy");
        if(FirebaseAuth.getInstance().getCurrentUser()!=null) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            database = FirebaseDatabase.getInstance();
            if (type.equals("BusinessOwner")) {
                myRef = database.getReference("BusinessOwner");
                myRef.child(uid).child("status").setValue("connected");
                myRef.child(uid).child("deviceid").setValue(deviceid);
            } else {
                myRef = database.getReference("Clients");
                myRef.child(uid).child("status").setValue("connected");
                myRef.child(uid).child("deviceid").setValue(deviceid);
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    tabposition = 1;
                    Tab1ChatsFragment tab1 = new Tab1ChatsFragment();
                    return tab1;
                case 1:
                    tabposition = 2;
                    Tab2ContactsFragment tab2 = new Tab2ContactsFragment();
                    return tab2;
                case 2:
                    tabposition = 3;
                    Tab3ProfileFragment tab3 = new Tab3ProfileFragment();
                    return tab3;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position){
            switch (position){
                case 0:
                    return "CHATS";
                case 1:
                    return "PROFILES";
                case 2:
                    return "SUBSCRIPTIONS";
                default:
                    return null;
            }
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        myRef.child(uid).child("status").setValue("connected");
    }

    @Override
    protected void onPause(){
        super.onPause();
        myRef.child(uid).child("status").setValue("not connected");
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        myRef.child(uid).child("status").setValue("not connected");
    }
    @Override
    public void onBackPressed(){
        if(tabposition==1) {
            if (doubleBackToExitPressedOnce) {
                moveTaskToBack(true);
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }

            }, 2000);
        }
        else{
            super.onBackPressed();
        }
    }
}
