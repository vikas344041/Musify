package com.example.vikas.musify;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.sqrt;

public class ActivityRecognizedService extends IntentService {
 
    public ActivityRecognizedService() {
        super("ActivityRecognizedService");
    }
 
    public ActivityRecognizedService(String name) {
        super(name);
    }

    private Constants.Activities State;
    private float prevVal = 0.0f;
    private ArrayList<Float> heartBeatChange = new ArrayList<Float>();

    @Override
    protected void onHandleIntent(Intent intent) {
        if(ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            DetectedActivity mostProbableActivity
                    = result.getMostProbableActivity();

            // Get the confidence % (probability)
            int confidence = mostProbableActivity.getConfidence();
            Log.e("ActivityRecogition", "In confidence: " + confidence);
            // Get the type
            int activityType = mostProbableActivity.getType();

            switch(activityType){
                case DetectedActivity.IN_VEHICLE: {
                    if (confidence > 75) {
                        Log.e("ActivityRecogition", "In Vehicle: " + confidence);
                        State = Constants.Activities.DRIVING;
                    } else {
                        Log.e("ActivityRecogition", "In Vehicle: " + confidence);
                        State = Constants.Activities.WALKING;
                    }
                    break;
                }
                case DetectedActivity.ON_BICYCLE: {
                    Log.e( "ActivityRecogition", "On Bicycle: " + confidence );
                    State = Constants.Activities.BICYCLE_RIDING;
                    break;
                }
                case DetectedActivity.ON_FOOT: {
                    Log.e( "ActivityRecogition", "On Foot: " + confidence );
                    State = Constants.Activities.WALKING;
                    break;
                }
                case DetectedActivity.RUNNING: {
                    Log.e( "ActivityRecogition", "Running: " + confidence );
                    State = Constants.Activities.RUNNING;
                    break;
                }
                case DetectedActivity.STILL: {
                    Log.e( "ActivityRecogition", "Still: " + confidence );
                    State = Constants.Activities.RELAXING;
                    break;
                }
                case DetectedActivity.TILTING: {
                    Log.e( "ActivityRecogition", "Tilting: " + confidence );
                    State = Constants.Activities.RELAXING;
                    break;
                }
                case DetectedActivity.WALKING: {
                    Log.e( "ActivityRecogition", "Walking: " + confidence );
                    if( confidence >= 75 ) {
                        State = Constants.Activities.WALKING;
                    }

                    break;
                }
                case DetectedActivity.UNKNOWN: {
                    Log.e( "ActivityRecogition", "Unknown: " + confidence );
                    State = Constants.Activities.HAPPY;
                    break;
                }
            }
            if (confidence > 75){
                sendResultBackToActivity();
            }


        }
    }

    private void sendResultBackToActivity(){
        isHappyOrSad(85.0f);
        boolean check = processHeartBeat(85.0f);
        if(!check){
            State = Constants.Activities.RELAXING;
        }
        Intent intent = new Intent ("message"); //put the same message as in the filter you used in the activity when registering the receiver
        intent.putExtra("success", State.toString());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /*
    This method checks the heart beat and keep a track on the user whether the heartbeat is within the desired range or not
    it returns true if everything is fine else false
     */
    private boolean processHeartBeat(float beat){
        if (State.equals(Constants.Activities.HAPPY) || State.equals(Constants.Activities.RELAXING) || State.equals(Constants.Activities.DRIVING)){
            if (beat >  70 && beat < 119){
                return true;
            }
        }
        else if(State.equals(Constants.Activities.RUNNING)){
            if(beat > 180){
                return false;
            }
        }
        else if(State.equals(Constants.Activities.WALKING)){
            if(beat > 130){
               return false;
            }
        }

        return true;
    }

    private boolean isHappyOrSad(float beat){
        if(prevVal == 0.0f){
            prevVal = beat;
            return false;
        }
        float delValue = prevVal - beat;
        heartBeatChange.add(delValue);
        if (heartBeatChange.size() == 10){
            float avg = 0.0f;
            for (float diff : heartBeatChange ) {
                avg = avg + diff;
            }
            avg = avg/10f;
            if(avg < 0.11){
                State = Constants.Activities.HAPPY;
                return true;
            }
            if (avg < 0.05){
                State = Constants.Activities.SAD;
                return true;
            }
        }
        return false;
    }
}