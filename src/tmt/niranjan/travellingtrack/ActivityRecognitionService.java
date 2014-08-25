package tmt.niranjan.travellingtrack;


import android.app.IntentService;
import android.content.Intent;


import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

public class ActivityRecognitionService extends IntentService	 {

	public final String KEY_PREVIOUS_ACTIVITY_TYPE="KEY";
	
	
	public ActivityRecognitionService() {
		super("My Activity Recognition Service");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
	
		ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
		
		 DetectedActivity mostProbableActivity = result.getMostProbableActivity();
           int activityType = mostProbableActivity.getType();
           int confidence=mostProbableActivity.getConfidence();
           String Activity=getType(activityType);
           
      
		if(ActivityRecognitionResult.hasResult(intent)){ 
	
			if(confidence>=50){
				
				Intent i = new Intent("tmt.niranjan.myactivityrecognition.ACTIVITY_RECOGNITION_DATA");
				i.putExtra("Activity", Activity );
				i.putExtra("Confidence", confidence);
				i.putExtra("activityType",activityType );
				sendBroadcast(i);
			}
		}
	
	}

	private String getType(int type){
		if(type == DetectedActivity.UNKNOWN)
			return "Unknown";
		else if(type == DetectedActivity.IN_VEHICLE)
			return "In Vehicle";
		else if(type == DetectedActivity.ON_BICYCLE)
			return "On Bicycle";
		else if(type == DetectedActivity.ON_FOOT)
			return "On Foot";
		else if(type == DetectedActivity.STILL)
			return "Still";
		else if(type == DetectedActivity.TILTING)
			return "Tilting";
		else if(type == DetectedActivity.WALKING)
			return "Walking";
		else if(type == DetectedActivity.RUNNING)
			return "Running";
		else
			return "Unknown";
	}
	

}
