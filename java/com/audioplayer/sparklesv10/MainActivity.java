package com.audioplayer.sparklesv10;

import android.Manifest;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.audioplayer.sparklesv10.audioplayer.AudioPlayerService;
import com.audioplayer.sparklesv10.fragments.ControllerFragment;
import com.audioplayer.sparklesv10.fragments.MainFragment;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import static com.audioplayer.sparklesv10.audioplayer.AudioPlayerService.audioRemote;
import static com.audioplayer.sparklesv10.audioplayer.AudioPlayerService.bindToService;
import static com.audioplayer.sparklesv10.fragments.ControllerFragment.top_container;

public class MainActivity extends AppCompatActivity implements ServiceConnection {

    private static final int KEY_PER = 123;
    private SlidingUpPanelLayout slidingUpPanelLayout;
    boolean doubleBackToExitPressedOnce = false;
    boolean up = false;
    public static int currFragment = 0;

    private AudioPlayerService.ServiceToken serviceToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serviceToken = AudioPlayerService.bindToService(MainActivity.this, MainActivity.this);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, KEY_PER);

            return;
        }
        else {
            UiInit();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case KEY_PER:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) UiInit();
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void UiInit() {

        serviceToken = bindToService(this, this);

        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(this));

        slidingUpPanelLayout = findViewById(R.id.slidingUpPanelLayout);

        Fragment fragment = new MainFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.mainContainer, fragment);
        fragmentTransaction.commit();

        slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                if (up) top_container.setVisibility(View.VISIBLE);
                else top_container.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (SlidingUpPanelLayout.PanelState.EXPANDED == newState) {
                    up = true;
                    top_container.setVisibility(View.INVISIBLE);
                }
                else {
                    up = false;
                    top_container.setVisibility(View.VISIBLE);
                }

            }
        });


        new initQuickControls().execute("");
    }


    @Override
    public void onBackPressed() {

        if (currFragment == 0) {
            if (doubleBackToExitPressedOnce) {
                finish();
                System.exit(0 );
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
        else super.onBackPressed();
    }

    @Override
    protected void onResume() {
        if (serviceToken == null) {
            serviceToken = AudioPlayerService.bindToService(this, this);
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceToken != null) {
            AudioPlayerService.unbindToService(serviceToken);
            serviceToken = null;
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        audioRemote = AudioPlayerAIDL.Stub.asInterface(service);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        audioRemote = null;
    }

    public class initQuickControls extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            ControllerFragment fragment1 = new ControllerFragment();
            FragmentManager fragmentManager1 = getSupportFragmentManager();
            fragmentManager1.beginTransaction()
                    .replace(R.id.controlContainer, fragment1).commitAllowingStateLoss();
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
        }

        @Override
        protected void onPreExecute() {
        }
    }
}
