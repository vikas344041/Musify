package com.example.vikas.musify;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.List;

public class ActivityRecognizedService extends IntentService {
 
    public ActivityRecognizedService() {
        super("ActivityRecognizedService");
    }
 
    public ActivityRecognizedService(String name) {
        super(name);
    }

    private Constants.Activities State;

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
                    State = Constants.Activities.WALKING;
                    break;
                }
                case DetectedActivity.WALKING: {
                    Log.e( "ActivityRecogition", "Walking: " + confidence );
                    if( confidence >= 75 ) {
                        State = Constants.Activities.WALKING;
                    }
                    else{
                        State = Constants.Activities.RELAXING;
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
        Intent intent = new Intent ("message"); //put the same message as in the filter you used in the activity when registering the receiver
        intent.putExtra("success", State.toString());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}