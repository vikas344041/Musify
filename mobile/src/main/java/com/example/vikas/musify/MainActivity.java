package com.example.vikas.musify;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener , ServiceCallbacks{
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
    private SharedPreferences sharedPref;
    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String LastActivity = "lastActivity";
    private Boolean hasActivityChanged=true;
    private List<Integer> songIDs = new ArrayList<Integer>();
    private Random random = new Random();

    public static int oneTimeOnly = 0;
    public GoogleApiClient mApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mApiClient.connect();


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        //// TODO: 06/07/2017


        //String state = Constants.STATE.toString();

        //if (mApiClient.isConnected()) {
           // String s = ((MyApplication) this.getApplication()).getSomeVariable();
            Toast.makeText(this, "walking", Toast.LENGTH_LONG).show();
            int stateID = getResources().getIdentifier("walking", "array", getPackageName());
            activityArray = getResources().getStringArray(stateID);

            //get genres and songids
            for (int i = 0; i < activityArray.length; i++) {
                String genre = activityArray[i];
                int rawID = getResources().getIdentifier(genre, "array", getPackageName());
                songArray = getResources().getStringArray(rawID);
                for (int j = 0; j < songArray.length; j++) {
                    songIDs.add(getResources().getIdentifier(songArray[j], "raw", getPackageName()));
                }
            }
            sharedPref = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
            String recentActivity = sharedPref.getString(LastActivity, "");
            if (!recentActivity.equalsIgnoreCase("walking")) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(LastActivity, "walking");
                hasActivityChanged = true;
            } else {
                hasActivityChanged = false;
            }

            int val = random.nextInt(activityArray.length);
            String genre = activityArray[val];
            int rawID = getResources().getIdentifier(genre, "array", getPackageName());
            songArray = getResources().getStringArray(rawID);

            val = random.nextInt(songArray.length);
            final String song = songArray[val];
            rawID = getResources().getIdentifier(song, "raw", getPackageName());
            mediaPlayer = MediaPlayer.create(this, rawID);
            seekbar = (SeekBar) findViewById(R.id.seekBar);
            btnPlayPause = (ImageView) findViewById(R.id.button_play_pause);
            btnNext = (ImageView) findViewById(R.id.button_next);
            btnPrevious = (ImageView) findViewById(R.id.button_previous);
            tx2 = (TextView) findViewById(R.id.textView2);
            tx3 = (TextView) findViewById(R.id.textView3);
            tx4 = (TextView) findViewById(R.id.textView4);
            image_art = (ImageView) findViewById(R.id.image_art);

            seekbar.setClickable(false);
            btnNext.setEnabled(false);
            btnPrevious.setEnabled(false);

            //Toast.makeText(this, genre, Toast.LENGTH_SHORT).show();

            btnPlayPause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (playing == false) {

                        setCurrentUIandPlay();

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


        /*while (s == null ){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            s = ((MyApplication) this.getApplication()).getSomeVariable();
        }*/


            //Intent myIntent = new Intent(MainActivity.this, Main2Activity.class);
            //MainActivity.this.startActivity(myIntent);
       // }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Intent intent = new Intent( this, ActivityRecognizedService.class );
        PendingIntent pendingIntent = PendingIntent.getService( this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates( mApiClient, 3000, pendingIntent );

    }
    Thread t = new Thread(new Runnable() {
        public void run() {
        /*
         * Do something
         */
        }
    });

    t.start();

    /*public void test(String somevariable){
        int stateID = getResources().getIdentifier(somevariable, "array", getPackageName());
        activityArray = getResources().getStringArray(stateID);
    }*/


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public List<String> getSong(String genre)
    {
        XmlPullParserFactory pullParserFactory;
        try {
            InputStream ins = getResources().openRawResource(
                    getResources().getIdentifier("selection",
                            "raw", getPackageName()));
            pullParserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = pullParserFactory.newPullParser();

            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(ins, null);

            List<String> song =parseXML(parser, genre);
            return song;

        } catch (XmlPullParserException e) {

            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    private List<String> parseXML(XmlPullParser parser, String genre) throws XmlPullParserException,IOException {

        ArrayList<ReadMp3File> deserialize = null;
        List<String> songs;
        int eventType = parser.getEventType();
        ReadMp3File file = null;

        while (eventType != XmlPullParser.END_DOCUMENT) {
            String name = null;
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    deserialize = new ArrayList();
                    break;
                case XmlPullParser.START_TAG:
                    name = parser.getName();
                    if (name.equalsIgnoreCase(genre)) {

                        songs = new ArrayList<String>();
                        while(parser.next() != 0){
                            songs.add(parser.nextText());
                        }
                    }/* else if (file != null) {
                        if (name.equalsIgnoreCase("song")) {
                            songs.add(parser.nextText());
                            file.song = parser.nextText();
                            Toast.makeText(this, file.song, Toast.LENGTH_SHORT).show();
                            return file.song;
                        }
                    }*/
                    break;
                /*case XmlPullParser.END_TAG:
                    name = parser.getName();
                    if (name.equalsIgnoreCase("genre") && file != null) {
                        products.add(file);
                    }*/
            }
            eventType = parser.next();
        }
        return null;
    }


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
        mediaPlayer.reset();
        int rawId = songIDs.get(random.nextInt(songIDs.size()));
        mediaPlayer = MediaPlayer.create(getApplicationContext(),rawId);
        mediaPlayer.start();
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

        //tx4.setText(getResources().getResourceEntryName(rawId));

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

    @Override
    public void doSomething() {

    }
}
