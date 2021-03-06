package com.allsmart.fieldtracker.service;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.allsmart.fieldtracker.R;
import com.allsmart.fieldtracker.model.SimpleGeofence;
import com.allsmart.fieldtracker.utils.SimpleGeofenceStore;
import com.allsmart.fieldtracker.activity.MainActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.allsmart.fieldtracker.constants.AppsConstant;
import com.allsmart.fieldtracker.storage.Preferences;
import com.allsmart.fieldtracker.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class GeolocationService extends Service implements ConnectionCallbacks,
		OnConnectionFailedListener, LocationListener, ResultCallback<Status> {
	public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
	public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 5;
	protected GoogleApiClient mGoogleApiClient;
	protected LocationRequest mLocationRequest;
	private Preferences preferences;
	private PendingIntent mPendingIntent;

	@Override
	public void onStart(Intent intent, int startId) {
		/*buildGoogleApiClient();

		mGoogleApiClient.connect();*/
	}

	@Override
	public void onCreate() {
		preferences = new Preferences(getApplicationContext());
		super.onCreate();
		buildGoogleApiClient();

		mGoogleApiClient.connect();
		Log.d(MainActivity.TAG,"GeolocationService is started");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(MainActivity.TAG,"GeolocationService is destroyed");
		/*if (mGoogleApiClient.isConnected()) {
			mGoogleApiClient.disconnect();
		}*/
		//startService(new Intent(GeolocationService.this,GeolocationService.class));

	}

	protected void registerGeofences() {
		if (MainActivity.geofencesAlreadyRegistered) {
			Log.d(MainActivity.TAG,"Within Geofence Register");
			return;
		}
		Log.d(MainActivity.TAG,"Outside Geofence Register");
		Log.d(MainActivity.TAG, "Registering Geofences");
		Preferences preferences = new Preferences(this);
		HashMap<String, SimpleGeofence> geofences = SimpleGeofenceStore
				.getInstance(preferences.getString(Preferences.SITENAME,""), StringUtils.getDouble(preferences.getString(Preferences.LATITUDE,""))
						, StringUtils.getDouble(preferences.getString(Preferences.LONGITUDE,"")),StringUtils.getInt(preferences.getString(Preferences.SITE_RADIUS, AppsConstant.DEFAULTRADIUS))).getSimpleGeofences();

		GeofencingRequest.Builder geofencingRequestBuilder = new GeofencingRequest.Builder();
		for (Map.Entry<String, SimpleGeofence> item : geofences.entrySet()) {
			SimpleGeofence sg = item.getValue();

			geofencingRequestBuilder.addGeofence(sg.toGeofence());
		}

		GeofencingRequest geofencingRequest = geofencingRequestBuilder.build();

		mPendingIntent = requestPendingIntent();
		if(checkPermission()) {
			LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, geofencingRequest, mPendingIntent).setResultCallback(this);
		}

		MainActivity.geofencesAlreadyRegistered = true;
	}

	private PendingIntent requestPendingIntent() {

		if (null != mPendingIntent) {

			return mPendingIntent;
		} else {

		//	Intent intent = new Intent(this, GeofenceReceiver.class);
			Intent intent = new Intent("com.allsmart.fieldtracker.ACTION_GEOFENCE_RECEIVER");
			return PendingIntent.getBroadcast(this, 0, intent,
					PendingIntent.FLAG_UPDATE_CURRENT);
		}
	}

	private int isUserInLocation(Location location){
		float[] result = new float[1];
		double lat1 = location.getLatitude();
		double lon1 = location.getLongitude();
		double lat2 = StringUtils.getDouble(preferences.getString(Preferences.LATITUDE, ""));
		double lon2 = StringUtils.getDouble(preferences.getString(Preferences.LONGITUDE, ""));
		//Double distance = distance(lat1, lon1, lat2, lon2);
		int siteRadius = Integer.parseInt(preferences.getString(Preferences.SITE_RADIUS, AppsConstant.DEFAULTRADIUS));
		Location.distanceBetween(lat1, lon1, lat2, lon2, result);
		float distance = result[0];
		Boolean isInLocation = distance <= siteRadius;

		if (isInLocation) {
			return 1;
		} else {
			return 2;
		}
	}

	public void broadcastLocationFound(Location location) {
		Intent intent = new Intent("com.allsmart.fieldtracker.geolocation.service");
	//	Intent intent = new Intent("com.allsmart.fieldtracker.ACTION_GEOFENCE_RECEIVER");

		int geoEvent = isUserInLocation(location);
		intent.putExtra(AppsConstant.GEOEVENT,geoEvent);
		sendBroadcast(intent);

	}

	// Check for permission to access Location
	private boolean checkPermission() {
		Log.d(MainActivity.TAG, "checkPermission()");
		// Ask for permission if it wasn't granted yet
		return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED );
	}

	protected void startLocationUpdates() {
		if ( checkPermission() ) {
			LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
		}
	}

	protected void stopLocationUpdates() {
		if ( checkPermission() ) {
			LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		Log.i(MainActivity.TAG, "Connected to GoogleApiClient");
		if(mGoogleApiClient.isConnected()) {
			startLocationUpdates();
		} else {
			Log.d(MainActivity.TAG,"google api client not connected");
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		Log.d(MainActivity.TAG,"GeolocationService new location : " + location.getLatitude() + ", "+ location.getLongitude() + ". "+ location.getAccuracy());
		Intent intent = new Intent(AppsConstant.LOCATION_UPDATE);
		if(isUserInLocation(location) == 1) {
			intent.putExtra(Preferences.SITENAME,preferences.getString(Preferences.SITENAME,""));
		} else {
			intent.putExtra(Preferences.SITENAME,getString(R.string.off_site));
		}
		/*preferences.saveString(Preferences.USERLATITUDE,location.getLatitude()+"");
		preferences.saveString(Preferences.USERLONGITUDE,location.getLongitude()+"");
		preferences.commit();*/
		sendBroadcast(intent);

		if (!MainActivity.geofencesAlreadyRegistered) {
			Log.d(MainActivity.TAG,"Before Geofence Register");
			registerGeofences();
		}
		broadcastLocationFound(location);
	}

	@Override
	public void onConnectionSuspended(int cause) {
		Log.i(MainActivity.TAG, "Connection suspended");
		mGoogleApiClient.connect();
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.i(MainActivity.TAG,"Connection failed: ConnectionResult.getErrorCode() = "+ result.getErrorCode());
	}

	protected synchronized void buildGoogleApiClient() {
		Log.i(MainActivity.TAG, "Building GoogleApiClient");
		mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
		createLocationRequest();
	}

	protected void createLocationRequest() {
		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
		mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void onResult(Status status) {
		if (status.isSuccess()) {
			//Toast.makeText(getApplicationContext(),getString(R.string.geofences_added), Toast.LENGTH_SHORT).show();
		} else {
			MainActivity.geofencesAlreadyRegistered = false;
			String errorMessage = getErrorString(this, status.getStatusCode());
			Toast.makeText(getApplicationContext(), errorMessage,Toast.LENGTH_LONG).show();
		}
	}

	public static String getErrorString(Context context, int errorCode) {
		Resources mResources = context.getResources();
		switch (errorCode) {
		case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
			return mResources.getString(R.string.geofence_not_available);
		case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
			return mResources.getString(R.string.geofence_too_many_geofences);
		case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
			return mResources
					.getString(R.string.geofence_too_many_pending_intents);
		default:
			return mResources.getString(R.string.unknown_geofence_error);
		}
	}

}
