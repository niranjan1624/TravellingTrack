package tmt.niranjan.travellingtrack;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.ActivityRecognitionClient;

@SuppressLint("SimpleDateFormat") public class GetLocation extends ActionBarActivity
	implements LocationListener, GooglePlayServicesClient.ConnectionCallbacks,
				GooglePlayServicesClient.OnConnectionFailedListener {
	
	NirDatabaseAdapter NirHelper;
	
	private static final int PROFILE_PIC_SIZE = 200;

	private ActivityRecognitionClient arclient;
	private PendingIntent pIntent;
	private BroadcastReceiver receiver;
	private TextView tvActivity;
	private TextView Location;
	private TextView mAddress;
	   private TextView txtName, txtEmail;
	   private String Name,PhotoUrl;
	   private String date, Email="",Activity="",Time,LatLng="",Address="";
	   
	   private int count_v=0,count_b=0,count_w=0,count_r=0,count_f=0;
	   
	ImageView image;
	MainActivity ma;
	
	  double nlat;
			double nlng;
			double glat;
			double glng;
			
			LocationManager glocManager;
			LocationListener glocListener;
			LocationManager nlocManager;
			LocationListener nlocListener;
			
			private ImageView  imgProfilePic;
			
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_get_location);
	       NirHelper=new NirDatabaseAdapter(this);
		ma=new MainActivity();
		tvActivity = (TextView) findViewById(R.id.Actrec);
		Location = (TextView) findViewById(R.id.Location);
		mAddress=(TextView) findViewById(R.id.Address);
		image = (ImageView) findViewById(R.id.test_image);
		imgProfilePic = (ImageView) findViewById(R.id.test_image1);

	       txtName = (TextView) findViewById(R.id.txtName);
	       txtEmail = (TextView) findViewById(R.id.txtEmail);
	       
	       date = new SimpleDateFormat("MMM dd, yyyy").format(new Date());
	       Time = new SimpleDateFormat("HH:mm:ss").format(new Date());
	       tvActivity.setText(NirHelper.getalldata());	       
	       Bundle extras=getIntent().getExtras();
			
			if (extras!=null)
			{
			 Email=NirHelper.getEmail();
			 Name=NirHelper.getName();
			 Name=Name.toUpperCase(Locale.getDefault());
			PhotoUrl=NirHelper.getphoto();
							
				txtEmail.setText(Email);
				txtName.setText(Name);
				
				PhotoUrl = PhotoUrl.substring(0,
						PhotoUrl.length() - 2)
						+ PROFILE_PIC_SIZE;

				new LoadProfileImage(imgProfilePic).execute(PhotoUrl);
				
				// profile pic retrieval 

			} 
			else
			{
				Use.message(getApplicationContext(), "Person information is null");
			}
        //   new LoadProfileImage(imgProfilePic).execute(photourl);
		
		showLoc();
		int resp =GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if(resp == ConnectionResult.SUCCESS){
			arclient = new ActivityRecognitionClient(this, this, this);
			arclient.connect();			
		}
		else{
			Use.message(this,  "Please install Google Play Service.");
		}

		receiver = new BroadcastReceiver() {
		    @Override
		    public void onReceive(Context context, Intent intent) {
		    	
		    	Activity=intent.getExtras().getString("Activity");
		    	
		    	if(Activity.equals("Still")){
		    		image.setImageResource(R.drawable.still);
		    	}
		    	else if(Activity.equals("In Vehicle")){
		    		image.setImageResource(R.drawable.vehicle);
		    		count_v++;
		    		if(count_v>=5){
		    			UpdateData();
		    			count_v=0;
		    		}
		    	}
		    	else if(Activity.equals("On Foot")){
		    		image.setImageResource(R.drawable.walking);	
		    		count_f++;
		    		if(count_f>=5){
		    			UpdateData();
		    			count_f=0;
		    		}
		    	}
		    	else if(Activity.equals("Walking")){
		    		image.setImageResource(R.drawable.walking);	
		    		count_w++;
		    		if(count_w>=5){
		    			UpdateData();
		    			count_w=0;
		    		}
		    	}
		    	else if(Activity.equals("On Bicycle")){
		    		image.setImageResource(R.drawable.bicycle);
		    		count_b++;
		    		if(count_b>=5){
		    			UpdateData();
		    			count_b=0;
		    		}
		    	}
		    	else if(Activity.equals("Running")){
		    		image.setImageResource(R.drawable.running);
		    		count_r++;
		    		if(count_r>=5){
		    			UpdateData();
		    			count_r=0;
		    		}
		    	}
		    	else if(Activity.equals("Tilting")){
		    		image.setImageResource(R.drawable.tilting);
		    	}
		    	else{
		    		image.setImageResource(R.drawable.unknown);
		    	}
		    	if(!Address.equals(NirHelper.getAddress())){
		    		UpdateData();
		    	}
		    	tvActivity.setText(NirHelper.getalldata());
		    }
		  };
		 IntentFilter filter = new IntentFilter();
		 filter.addAction("tmt.niranjan.myactivityrecognition.ACTIVITY_RECOGNITION_DATA");
		 registerReceiver(receiver, filter);
	}
	@Override
	protected void onStart(){
		super.onStart();
		tvActivity.setText(NirHelper.getalldata());
		
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(arclient!=null){
			arclient.removeActivityUpdates(pIntent);
			arclient.disconnect();
		}

		//Remove GPS location update
		if(glocManager != null){
			glocManager.removeUpdates(glocListener);
		}
		
		//Remove Network location update
		if(nlocManager != null){
			nlocManager.removeUpdates(nlocListener);
		}
		//Toast.makeText(this, "ondestroycalled", Toast.LENGTH_SHORT).show();
		unregisterReceiver(receiver);
	}
	
	
	private boolean isgoogleplayservicesavailable() {

        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            return true;
        } else {
            return false;
        }
    }
	public void UpdateData(){
		
			String name=NirHelper.getName();
			Name=name.toUpperCase(Locale.getDefault());
			Time = new SimpleDateFormat("HH:mm:ss").format(new Date());
			NirHelper.insertData(Activity, date, Time, LatLng,Address);	
		
	}
	public class MyLocationListenerNetWork implements LocationListener	
	{
		@Override
		public void onLocationChanged(Location loc)
		{
			nlat = loc.getLatitude();
			nlng = loc.getLongitude();
			LatLng=nlat+","+nlng;
			Location.setText(LatLng);
			  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO && !Geocoder.isPresent()) {

		            Toast.makeText(getApplicationContext(), R.string.no_geocoder_available, Toast.LENGTH_LONG).show();
		            return;
		        }

		        if (isgoogleplayservicesavailable()) {
		            (new GetLocation.GetAddressTask(getApplicationContext())).execute(loc);
		        }
		}

		@Override
		public void onProviderDisabled(String provider)
		{
		}
		@Override
		public void onProviderEnabled(String provider)
		{
		}
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras)
		{
		}
	}
	
	public class MyLocationListenerGPS implements LocationListener	
	{
		
		@Override
		public void onLocationChanged(Location loc)
		{
			glat = loc.getLatitude();
			glng = loc.getLongitude();
			LatLng=glat+","+glng;
			  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO && !Geocoder.isPresent()) {

		            Toast.makeText(getApplicationContext(), R.string.no_geocoder_available, Toast.LENGTH_LONG).show();
		            return;
		        }

		        if (isgoogleplayservicesavailable()) {
		            (new GetLocation.GetAddressTask(getApplicationContext())).execute(loc);
		        }
		}
		
		@Override
		public void onProviderDisabled(String provider)
		{
		}
		@Override
		public void onProviderEnabled(String provider)
		{
			Use.message(getApplicationContext(), "GPS enabled");
		}
		
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras)
		{
		}
	}
	
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Use.message(this, "Connection Failed");
		
		  if (connectionResult.hasResolution()) {
	            try {

	                // Start an Activity that tries to resolve the error
	                connectionResult.startResolutionForResult(
	                        this,
	                        9000);//CONNECTION_FAILURE_RESOLUTION_REQUEST

	            } catch (IntentSender.SendIntentException e) {
	            	
	                e.printStackTrace();
	            }
	        } 
	}
	@Override
	public void onConnected(Bundle arg0) {
		Intent intent = new Intent(this, ActivityRecognitionService.class);
		pIntent = PendingIntent.getService(this, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT);
		arclient.requestActivityUpdates(15000, pIntent);   //Requests for every 10sec
		
	}
	@Override
	public void onDisconnected() {
	}
	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub
		//Use.message(this, Address);
	}
	
	   protected boolean isRouteDisplayed() {
	        // TODO Auto-generated method stub
	        return false;
	    }
	   public void showLoc() {
		   // 	  Intent i=null;
		    	  
		    	/*  i=new Intent(android.content.Intent.ACTION_VIEW);
		  			i.setData(Uri.parse("geo:17.345,67.33"));
		  			startActivity(i);*/
		  		
		  		//Location access ON or OFF checking
		  		ContentResolver contentResolver = getBaseContext().getContentResolver();
		  		@SuppressWarnings("deprecation")
				boolean gpsStatus = Settings.Secure.isLocationProviderEnabled(contentResolver, LocationManager.GPS_PROVIDER);
		  		@SuppressWarnings("deprecation")
				boolean networkWifiStatus = Settings.Secure.isLocationProviderEnabled(contentResolver, LocationManager.NETWORK_PROVIDER);

		  		//If GPS and Network location is not accessible show an alert and ask user to enable both
		  		if(!gpsStatus || !networkWifiStatus)
		  		{
		  			AlertDialog.Builder alertDialog = new AlertDialog.Builder(GetLocation.this);
		  			
		  			alertDialog.setTitle("Make your location accessible ...");
		  			alertDialog.setMessage("Your Location is not accessible to us.To show location you have to enable it.");
		  		

		  			alertDialog.setNegativeButton("Enable", new DialogInterface.OnClickListener() {
		  				public void onClick(DialogInterface dialog, int which) {
		  					startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
		  				}
		  			});

		  			alertDialog.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
		  				public void onClick(DialogInterface dialog,int which) {
		  					Use.message(getApplicationContext(), "Remember to show location you have to eanable it !");
		  					dialog.cancel();
		  				}
		  			});

		  			alertDialog.show();
		  		}
		  		//IF GPS and Network location is accessible
		  		else 
		  		{
		  			nlocManager   = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		  			nlocListener = new MyLocationListenerNetWork();
		  			nlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
		  					1000 * 1,  // 1 Sec        
		  			        0,         // 0 meter   
		  			        nlocListener);
		  			
		  			
		  			glocManager  = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		  			glocListener = new MyLocationListenerGPS();
		  			glocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
		  					1000 * 1,  // 1 Sec        
		  			        0,         // 0 meter           
		  			        glocListener);
		  		}
		  	}
	   
	   protected class GetAddressTask extends AsyncTask<Location, Void, String> {

	        // Store the context passed to the AsyncTask when the system instantiates it.
	        Context localContext;

	        // Constructor called by the system to instantiate the task
	        public GetAddressTask(Context context) {

	            // Required by the semantics of AsyncTask
	            super();
	            localContext = context;
	        }

	        @Override
	        protected String doInBackground(Location... params) {

	            Geocoder geocoder = new Geocoder(localContext, Locale.getDefault());

	            Location location = params[0];

	            // Create a list to contain the result address
	            List <Address> addresses = null;
	          //  mLatLng.setText(location.getLatitude()+","+location.getLongitude());

	            try {

	                addresses = geocoder.getFromLocation(location.getLatitude(),
	                    location.getLongitude(), 1
	                );

	                // Catch network or other I/O problems.
	                } catch (IOException exception1) {

	                    exception1.printStackTrace();

	                    return (getString(R.string.IO_Exception_getFromLocation));

	                } catch (IllegalArgumentException exception2) {

	                    // Construct a message containing the invalid arguments
	                    String errorString = getString(
	                            R.string.illegal_argument_exception,
	                            location.getLatitude(),
	                            location.getLongitude()
	                    );
	                    
	                    exception2.printStackTrace();

	                    //
	                    return errorString;
	                }
	                // If the reverse geocode returned an address
	                if (addresses != null && addresses.size() > 0) {

	                    // Get the first address
	                    Address address = addresses.get(0);

	                    // Format the first line of address
	                    String addressText = getString(R.string.address_output_string,

	                            // If there's a street address, add it
	                            address.getMaxAddressLineIndex() > 0 ?
	                                    address.getAddressLine(0) : "",

	                            // Locality is usually a city
	                           address.getLocality(),

	                            // The country of the address
	                           address.getCountryName()
	                    );

	                    // Return the text
	                    return addressText;

	                // If there aren't any addresses, post a message
	                } else {
	                  return getString(R.string.no_address_found);
	                }
	        }
	        @Override
	        protected void onPostExecute(String address) {    	
	            Address=address;
	            mAddress.setText(address);
	        }
	    }
	  
		private class LoadProfileImage extends AsyncTask<String, Void, Bitmap> {
			ImageView bmImage;

			public LoadProfileImage(ImageView bmImage) {
				this.bmImage = bmImage;
			}

			protected Bitmap doInBackground(String... urls) {
				String urldisplay = urls[0];
				Bitmap mIcon11 = null;
				try {
					InputStream in = new java.net.URL(urldisplay).openStream();
					mIcon11 = BitmapFactory.decodeStream(in);
				} catch (Exception e) {
					Log.e("Error", e.getMessage());
					e.printStackTrace();
				}
				return mIcon11;
			}

			protected void onPostExecute(Bitmap result) {
				bmImage.setImageBitmap(result);
			}
		}
	   
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		 Use.message(this, "Enabled new provider ");
	}
	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		Use.message(this,"Disabled provider " );
	}
	

}
