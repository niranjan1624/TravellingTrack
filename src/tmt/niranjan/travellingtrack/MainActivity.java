package tmt.niranjan.travellingtrack;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.Plus.PlusOptions;
import com.google.android.gms.plus.model.people.Person;

public class MainActivity extends Activity implements OnClickListener,
       ConnectionCallbacks, OnConnectionFailedListener {

   private static final int RC_SIGN_IN = 0;
   // Logcat tag
   NirDatabaseAdapter NirHelper;

   // Profile pic image size in pixels
   private static final int PROFILE_PIC_SIZE = 400;

   // Google client to interact with Google API
   private GoogleApiClient mGoogleApiClient;
   
   private boolean mIntentInProgress;

   private boolean mSignInClicked;

   private ConnectionResult mConnectionResult;

   private SignInButton btnSignIn;
   
   private String email,personPhotoUrl,personName,Person;
   private String Name="";
   
   EditText username;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_main);
       username=(EditText) findViewById(R.id.name);
       btnSignIn = (SignInButton) findViewById(R.id.btn_sign_in);
       btnSignIn.setOnClickListener(this);
       mGoogleApiClient = new GoogleApiClient.Builder(this)
               .addConnectionCallbacks(this)
               .addOnConnectionFailedListener(this).addApi(Plus.API,PlusOptions.builder().build())
               .addScope(Plus.SCOPE_PLUS_LOGIN).build();
       
       NirHelper=new NirDatabaseAdapter(this);   
   }

   protected void onStart() {
       super.onStart();
       mGoogleApiClient.connect();
   }

   protected void onStop() {
       super.onStop();
       if (mGoogleApiClient.isConnected()) {
           mGoogleApiClient.disconnect();
       }
   }

   /**
    * Method to resolve any signin errors
    * */
   private void resolveSignInError() {
       if (mConnectionResult.hasResolution()) {
           try {
               mIntentInProgress = true;
               mConnectionResult.startResolutionForResult(this, RC_SIGN_IN);
           } catch (SendIntentException e) {
               mIntentInProgress = false;
               mGoogleApiClient.connect();
           }
       }
   }

   @Override
   public void onConnectionFailed(ConnectionResult result) {
       if (!result.hasResolution()) {
           GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this,
                   0).show();
           return;
       }

       if (!mIntentInProgress) {
           // Store the ConnectionResult for later usage
           mConnectionResult = result;

           if (mSignInClicked) {
               // The user has already clicked 'sign-in' so we attempt to
               // resolve all
               // errors until the user is signed in, or they cancel.
               resolveSignInError();
           }
       }

   }

   @Override
   protected void onActivityResult(int requestCode, int responseCode,
           Intent intent) {
       if (requestCode == RC_SIGN_IN) {
           if (responseCode != RESULT_OK) {
               mSignInClicked = false;
           }

           mIntentInProgress = false;

           if (!mGoogleApiClient.isConnecting()) {
               mGoogleApiClient.connect();
           }
       }
   }
   public void userName(View v){
	Name=username.getText().toString();
		 if(Name.equals("")||Name==null){
			 Use.message(this, "Your name will be taken from google account");
	       }else{
	    	   Use.message(this, "Thank you");; 
	       }
   }
   @Override
   public void onConnected(Bundle arg0) {
       mSignInClicked = false;
     //  Use.message(this, "User is connected!");
       getProfileInformation();
       if(Name.equals("")||Name==null){
    	   Person=personName;
       }else{
    	   Person=Name; 
       }     
       NirHelper.insertDetails(Person, email, personPhotoUrl);
       
       
       Intent myIntent=new Intent(this,GetLocation.class);
       myIntent.putExtra("email",email);
		myIntent.putExtra("name",Person);
		myIntent.putExtra("photo",personPhotoUrl);
       startActivity(myIntent);
       // Get user's information
     
	finish();
	
       // Update the UI after signin
       updateUI(true);

   }

   /**
    * Updating the UI, showing/hiding buttons and profile layout
    * */
   private void updateUI(boolean isSignedIn) {
       if (isSignedIn) {
           btnSignIn.setVisibility(View.GONE);
       } else {
           btnSignIn.setVisibility(View.VISIBLE);
       }
   }

   /**
    * Fetching user's information name, email, profile pic
    * */
   private void getProfileInformation() {
       try {
           if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
               Person currentPerson = Plus.PeopleApi
                       .getCurrentPerson(mGoogleApiClient);
               personName = currentPerson.getDisplayName();
               personPhotoUrl = currentPerson.getImage().getUrl();
            //   personGooglePlusProfile = currentPerson.getUrl();
               email = Plus.AccountApi.getAccountName(mGoogleApiClient);


               // by default the profile url gives 50x50 px image only
               // we can replace the value with whatever dimension we want by
               // replacing sz=X
               personPhotoUrl = personPhotoUrl.substring(0,
                       personPhotoUrl.length() - 2)
                       + PROFILE_PIC_SIZE;


           } else {
               Use.message(getApplicationContext(), "Person information is null");
           }
       } catch (Exception e) {
           e.printStackTrace();
       }
   }

   @Override
   public void onConnectionSuspended(int arg0) {
       mGoogleApiClient.connect();
       updateUI(false);
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
       // Inflate the menu; this adds items to the action bar if it is present.
       getMenuInflater().inflate(R.menu.main, menu);
       return true;
   }

   /**
    * Button on click listener
    * */
   @Override
   public void onClick(View v) {
	   if(Name==null){
			 Use.message(this, "Your name will be taken from google account");
	       }
           // Signin button clicked
           signInWithGplus();
           
   }

   /**
    * Sign-in into google
    * */
   private void signInWithGplus() {
       if (!mGoogleApiClient.isConnecting()) {
           mSignInClicked = true;
           resolveSignInError();
       }
   }

   /**
    * Sign-out from google
    * */
   /*
   private void signOutFromGplus() {
       if (mGoogleApiClient.isConnected()) {
           Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
           mGoogleApiClient.disconnect();
           mGoogleApiClient.connect();
           updateUI(false);
       }
   }*/

}