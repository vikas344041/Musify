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

    @Override
    protected void onHandleIntent(Intent intent) {
        if(ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            DetectedActivity mostProbableActivity
                    = result.getMostProbableActivity();

            // Get the confidence % (probability)
            int confidence = mostProbableActivity.getConfidence();
            // Get the type
            int activityType = mostProbableActivity.getType();

            switch(activityType){
                case DetectedActivity.IN_VEHICLE: {
                    if (confidence > 75) {
                        Log.e("ActivityRecogition", "In Vehicle: " + confidence);
                        Constants.STATE = Constants.Activities.DRIVING;
                    } else {
                        Log.e("ActivityRecogition", "In Vehicle: " + confidence);
                        Constants.STATE = Constants.Activities.WALKING;
                    }
                    break;
                }
                case DetectedActivity.ON_BICYCLE: {
                    Log.e( "ActivityRecogition", "On Bicycle: " + confidence );
                    Constants.STATE=Constants.Activities.BICYCLE_RIDING;
                    break;
                }
                case DetectedActivity.ON_FOOT: {
                    Log.e( "ActivityRecogition", "On Foot: " + confidence );
                    Constants.STATE=Constants.Activities.WALKING;
                    break;
                }
                case DetectedActivity.RUNNING: {
                    Log.e( "ActivityRecogition", "Running: " + confidence );
                    Constants.STATE = Constants.Activities.RUNNING;
                    break;
                }
                case DetectedActivity.STILL: {
                    Log.e( "ActivityRecogition", "Still: " + confidence );
                    Constants.STATE = Constants.Activities.RELAXING;
                    break;
                }
                case DetectedActivity.TILTING: {
                    Log.e( "ActivityRecogition", "Tilting: " + confidence );
                    Constants.STATE = Constants.Activities.RELAXING;
                    break;
                }
                case DetectedActivity.WALKING: {
                    Log.e( "ActivityRecogition", "Walking: " + confidence );
                    if( confidence >= 75 ) {
                        Constants.STATE = Constants.Activities.WALKING;
                    }
                    else{
                        Constants.STATE = Constants.Activities.RELAXING;
                    }

                    break;
                }
                case DetectedActivity.UNKNOWN: {
                    Log.e( "ActivityRecogition", "Unknown: " + confidence );
                    Constants.STATE = Constants.Activities.HAPPY;
                    break;
                }
            }
            senResultBackToActivity();

        }
    }

    private void senResultBackToActivity(){
        Intent intent = new Intent ("message"); //put the same message as in the filter you used in the activity when registering the receiver
        intent.putExtra("success", Constants.STATE.toString());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void handleDetectedActivities(List<DetectedActivity> probableActivities) {
        for( DetectedActivity activity : probableActivities ) {
            switch( activity.getType() ) {
                case DetectedActivity.IN_VEHICLE: {
                    Log.e( "ActivityRecogition", "In Vehicle: " + activity.getConfidence() );
                    Constants.STATE=Constants.Activities.DRIVING;
                    break;
                }
                case DetectedActivity.ON_BICYCLE: {
                    Log.e( "ActivityRecogition", "On Bicycle: " + activity.getConfidence() );
                    Constants.STATE=Constants.Activities.BICYCLE_RIDING;
                    break;
                }
                case DetectedActivity.ON_FOOT: {
                    Log.e( "ActivityRecogition", "On Foot: " + activity.getConfidence() );
                    Constants.STATE=Constants.Activities.WALKING;
                    break;
                }
                case DetectedActivity.RUNNING: {
                    Log.e( "ActivityRecogition", "Running: " + activity.getConfidence() );
                    Constants.STATE = Constants.Activities.RUNNING;
                    break;
                }
                case DetectedActivity.STILL: {
                    Log.e( "ActivityRecogition", "Still: " + activity.getConfidence() );
                    Constants.STATE = Constants.Activities.RELAXING;
                    break;
                }
                case DetectedActivity.TILTING: {
                    Log.e( "ActivityRecogition", "Tilting: " + activity.getConfidence() );
                    Constants.STATE = Constants.Activities.RELAXING;
                    break;
                }
                case DetectedActivity.WALKING: {
                    Log.e( "ActivityRecogition", "Walking: " + activity.getConfidence() );
                    if( activity.getConfidence() >= 75 ) {
                        Constants.STATE = Constants.Activities.WALKING_WITH_CONFIDENCE;
                        ((MyApplication) this.getApplication()).setSomeVariable("walking");
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                        builder.setContentText( "Are you walking?" );
                        builder.setSmallIcon( R.mipmap.ic_launcher );
                        builder.setContentTitle( getString( R.string.app_name ) );
                        NotificationManagerCompat.from(this).notify(0, builder.build());
                    }
                    Constants.STATE = Constants.Activities.RELAXING;
                    break;
                }
                case DetectedActivity.UNKNOWN: {
                    Log.e( "ActivityRecogition", "Unknown: " + activity.getConfidence() );
                    Constants.STATE = Constants.Activities.HAPPY;
                    break;
                }

            }
            Intent intent = new Intent ("message"); //put the same message as in the filter you used in the activity when registering the receiver
            intent.putExtra("success", Constants.STATE.toString());
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }
}