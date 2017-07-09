package com.example.vikas.musify;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private MediaPlayer mediaPlayer;
    private double startTime = 0;
    private double finalTime = 0;

    private Handler myHandler = new Handler();;
    private int forwardTime = 5000;
    private int backwardTime = 5000;
    private SeekBar seekbar;
    private TextView tx2,tx3,tx4;
    private ImageView image_art;
    private ImageView btnPlayPause,btnPrevious,btnNext;
    Boolean playing=false;
    private String[] songArray;
    private String[] activityArray;
    private List<Integer> songIDs = new ArrayList<Integer>();
    private Random random = new Random();
    private Integer length=0;

    public static int oneTimeOnly = 0;
    public GoogleApiClient mApiClient;
    public String lastActivity;

    @Override
    protected void onStart() {
        super.onStart();

        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mApiClient.connect();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        seekbar = (SeekBar) findViewById(R.id.seekBar);
        btnPlayPause = (ImageView) findViewById(R.id.button_play_pause);
        btnNext = (ImageView) findViewById(R.id.button_next);
        btnPrevious = (ImageView) findViewById(R.id.button_previous);
        tx2 = (TextView) findViewById(R.id.textView2);
        tx3 = (TextView) findViewById(R.id.textView3);
        tx4 = (TextView) findViewById(R.id.textView4);
        image_art = (ImageView) findViewById(R.id.image_art);

        lastActivity = "relaxing";
        setSongArray(lastActivity);




        seekbar.setClickable(false);
       // btnNext.setEnabled(false);
        btnPrevious.setEnabled(false);

        btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playing == false) {
                    if(!length.equals(0)){
                        mediaPlayer.seekTo(length);
                        mediaPlayer.start();
                        length=0;
                    }
                    else{
                        setCurrentUIandPlay();
                    }
                    seekbar.setProgress((int) startTime);
                    myHandler.postDelayed(UpdateSongTime, 100);
                    btnNext.setEnabled(true);
                    btnPrevious.setEnabled(false);
                    playing = true;
                    btnPlayPause.setImageResource(R.drawable.ic_action_playback_pause);
                    btnPlayPause.setPadding(0, 0, 8, 0);

                } else {
                    playing = false;
                    mediaPlayer.pause();
                    length=mediaPlayer.getCurrentPosition();
                    btnPrevious.setEnabled(false);
                    btnNext.setEnabled(false);
                    btnPlayPause.setImageResource(R.drawable.ic_action_playback_play);
                    btnPlayPause.setPadding(8, 0, 0, 0);
                }
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    setCurrentUIandPlay();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

       mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                try {
                    setCurrentUIandPlay();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void setSongArray(String state) {

        int stateID = getResources().getIdentifier(state.toLowerCase(), "array", getPackageName());
        activityArray = getResources().getStringArray(stateID);

        for (int i = 0; i < activityArray.length; i++) {
            String genre = activityArray[i];
            int rawID = getResources().getIdentifier(genre, "array", getPackageName());
            songArray = getResources().getStringArray(rawID);
            for (int j = 0; j < songArray.length; j++) {
                songIDs.add(getResources().getIdentifier(songArray[j], "raw", getPackageName()));
            }
        }

        int val = random.nextInt(activityArray.length);
        String genre = activityArray[val];
        int rawID = getResources().getIdentifier(genre, "array", getPackageName());
        songArray = getResources().getStringArray(rawID);

        val = random.nextInt(songArray.length);
        final String song = songArray[val];
        rawID = getResources().getIdentifier(song, "raw", getPackageName());
        if ( mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                btnPlayPause.setImageResource(R.drawable.ic_action_playback_play);
            }
        }
        mediaPlayer = MediaPlayer.create(this, rawID);
        mediaPlayer.start();
        btnPlayPause.setImageResource(R.drawable.ic_action_playback_pause);

        setCurrentUI(rawID);
        playing = true;

    }

    @Override
    public void onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(BReceiver, new IntentFilter("message"));
    }

    @Override
    protected void onPause() {
        // Unregister the broadcast receiver that was registered during onResume().
        LocalBroadcastManager.getInstance(this).unregisterReceiver(BReceiver);
        super.onPause();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Intent intent = new Intent( this, ActivityRecognizedService.class );
        PendingIntent pendingIntent = PendingIntent.getService( this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates( mApiClient, 3000, pendingIntent );
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i("MainActivity", "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i("MainActivity", "Connection suspended");
        mApiClient.connect();
    }


    private BroadcastReceiver  BReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            //put here whaterver you want your activity to do with the intent received
            Toast.makeText(getApplicationContext(),"here "+intent.getStringExtra("success"),Toast.LENGTH_LONG).show();
            if(!lastActivity.equalsIgnoreCase(intent.getStringExtra("success"))){
                lastActivity = intent.getStringExtra("success");
                setSongArray(intent.getStringExtra("success").toLowerCase());

            }
        }

    };

        @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_heart) {
            // Handle the camera action
        } else if (id == R.id.nav_steps) {

        } else if (id == R.id.nav_location) {

        } else if (id == R.id.nav_weather) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            startTime = mediaPlayer.getCurrentPosition();
            tx2.setText(String.format("%d min, %d sec",
                    TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                    toMinutes((long) startTime)))
            );
            seekbar.setProgress((int)startTime);
            myHandler.postDelayed(this, 100);
        }
    };

    private void setCurrentUIandPlay(){
        mediaPlayer.stop();
        btnPlayPause.setImageResource(R.drawable.ic_action_playback_play);
        mediaPlayer.reset();
        int rawId = songIDs.get(random.nextInt(songIDs.size()));
        mediaPlayer = MediaPlayer.create(getApplicationContext(),rawId);
        mediaPlayer.start();
        btnPlayPause.setImageResource(R.drawable.ic_action_playback_pause);
        setCurrentUI(rawId);
    }

    private void setCurrentUI(int rawId){
        myHandler.postDelayed(UpdateSongTime, 100);
        finalTime = mediaPlayer.getDuration();
        startTime = mediaPlayer.getCurrentPosition();
        if (oneTimeOnly == 0) {
            seekbar.setMax((int) finalTime);
            oneTimeOnly = 1;
        }
        tx3.setText(String.format("%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                finalTime)))
        );

        tx2.setText(String.format("%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                startTime)))
        );


        Uri mediaPath = Uri.parse("android.resource://" + getPackageName() + "/" + rawId);
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(this, mediaPath);
        String text = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

        tx4.setText(text);

        byte[] art = mmr.getEmbeddedPicture();
        if (art != null) {
            final Bitmap songImg = BitmapFactory.decodeByteArray(art, 0, art.length);
            image_art.setImageBitmap(songImg);
        }
        else {
            image_art.setImageResource(R.drawable.songimg);
        }
    }
}
