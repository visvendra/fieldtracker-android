package com.allsmart.fieldtracker.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.allsmart.fieldtracker.R;
import com.allsmart.fieldtracker.model.SimpleGeofence;
import com.allsmart.fieldtracker.utils.SimpleGeofenceStore;
import com.allsmart.fieldtracker.activity.CameraActivity;
import com.allsmart.fieldtracker.activity.MainActivity;
import com.allsmart.fieldtracker.service.GeofenceReceiver;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.allsmart.fieldtracker.constants.AppsConstant;
import com.allsmart.fieldtracker.utils.CalenderUtils;
import com.allsmart.fieldtracker.storage.EventDataSource;
import com.allsmart.fieldtracker.utils.Logger;
import com.allsmart.fieldtracker.utils.NetworkUtils;
import com.allsmart.fieldtracker.storage.Preferences;
import com.allsmart.fieldtracker.customviews.ShiftTimeView;
import com.allsmart.fieldtracker.utils.StringUtils;
import com.allsmart.fieldtracker.model.TimeInOutDetails;
import com.allsmart.fieldtracker.service.UploadTransactions;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.allsmart.fieldtracker.R.id.tvSiteName;

public class MapFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback<Status>/*, LoaderManager.LoaderCallbacks*/ {
    protected SupportMapFragment mapFragment;
    protected GoogleMap map;
    private Preferences preferences;
    private LocationRequest mLocationRequest;
    protected Marker myPositionMarker;
    protected EventDataSource dataSource;
    private GoogleApiClient mGoogleApiClient;
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 5;
    private PendingIntent mPendingIntent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        preferences = new Preferences(getContext());
        setRetainInstance(true);
        super.onCreate(savedInstanceState);
    }

    LinearLayout llLogin_Logout, llShiftTime;
    TextView tvTimeInOut, tvTimeInOutLocation;
    ImageView ivCurrentLocation, ivTimeLineExpand;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        ivTimeLineExpand = (ImageView) rootView.findViewById(R.id.ivTimeLineExapand);
        dataSource = new EventDataSource(getContext());

        mapFragment = SupportMapFragment.newInstance();
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.map_container, mapFragment);
//        map.setMyLocationEnabled(true);
        fragmentTransaction.commit();
        buildGoogleApiClient();

        ((MainActivity) getActivity()).preferences.saveString(Preferences.LOCATIONSTATUS, ""); // value to store
//		((MainActivity) getActivity()).preferences.saveBoolean(Preferences.INLOCATION, isUserInLoacation());
        ((MainActivity) getActivity()).preferences.commit();
        tvTimeInOut = (TextView) rootView.findViewById(R.id.tvTimeInOut);
        ivCurrentLocation = (ImageView) rootView.findViewById(R.id.ivCurrentLocation);
        //tvTimeInOut.setText("Time Out");
        tvTimeInOutLocation = (TextView) rootView.findViewById(R.id.tvTimeInOutLocation);
        ShiftTimeView stvShiftTime = (ShiftTimeView) rootView.findViewById(R.id.stvShiftTime);
        llShiftTime = (LinearLayout) rootView.findViewById(R.id.llShiftTime);
        llLogin_Logout = (LinearLayout) rootView.findViewById(R.id.llLogin_Logout);


        ivTimeLineExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment f = new TimeLineFragment();
                FragmentManager fm = getFragmentManager();
                fm.beginTransaction().replace(R.id.flMiddle, f).addToBackStack(null).commit();
                fm.beginTransaction();
            }
        });

        ivCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                boolean gps_enabled = false;
                boolean network_enabled = false;

                try {
                    gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                } catch (Exception e) {
                    Logger.e("Log", e);
                    Crashlytics.log(1, getClass().getName(), "Error in Map Fragment");
                    Crashlytics.logException(e);
                }

                try {
                    network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                } catch (Exception e) {
                    Logger.e("Log", e);
                    Crashlytics.log(1, getClass().getName(), "Error in Map Fragment");
                    Crashlytics.logException(e);
                }

                if (!gps_enabled && !network_enabled) {
                    // notify user
                    AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                    dialog.setMessage("Location is not enabled !");
                    dialog.setPositiveButton("Open setting", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            // TODO Auto-generated method stub
                            Intent myIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(myIntent);
                            //get gps
                        }
                    });
                    dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            // TODO Auto-generated method stub

                        }
                    });
                    dialog.show();
                } else {
                    Fragment f = getFragmentManager().findFragmentById(R.id.flMiddle);
                    if (f instanceof MapFragment) {
                        ((MapFragment) f).moveToCurentLocation();
                    }
                }
            }
        });
        llLogin_Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(NetworkUtils.isNetworkConnectionAvailable(getContext())) {
                    if (preferences == null) {
                        preferences = new Preferences(getContext());
                    }
                    if (!preferences.getString(Preferences.ROLETYPEID, "").equals(null) && preferences.getString(Preferences.ROLETYPEID, "").equalsIgnoreCase("FieldExecutiveOnPremise")) {
                        if (isUserInLoacation()) {

                            if (checkCameraPermission()) {
                                if (checkStoragePermission())
                                    OpenCamera();
                                else
                                    askStoragePermission();
                            } else {
                                askCameraPermission();
                            }
                        } else {
                            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                            dialog.setTitle("Not at store location");
                            dialog.setMessage("Please go to store location and try again!");
                            dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                                }
                            });

                            dialog.show();
                        }
                    } else {
                        if (checkCameraPermission()) {
                            if (checkStoragePermission())
                                OpenCamera();
                            else
                                askStoragePermission();
                        } else {
                            askCameraPermission();
                        }
                    }
                } else {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                    dialog.setTitle("No Internet Connection");
                    dialog.setMessage("Please connect to the internet to " +tvTimeInOut.getText().toString());
                    dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                }
            }
        });
        isUserInLoacation();
        SetLoginLogOut();
        UpdateLocationStatus();

        return rootView;
    }

    private boolean isUserInLoacation() {
        float[] result = new float[1];
        double lat1 = StringUtils.getDouble(((MainActivity) getActivity()).preferences.getString(Preferences.USERLATITUDE, ""));
        double lon1 = StringUtils.getDouble(((MainActivity) getActivity()).preferences.getString(Preferences.USERLONGITUDE, ""));
        double lat2 = StringUtils.getDouble(((MainActivity) getActivity()).preferences.getString(Preferences.LATITUDE, ""));
        double lon2 = StringUtils.getDouble(((MainActivity) getActivity()).preferences.getString(Preferences.LONGITUDE, ""));
        //Double distance = distance(lat1, lon1, lat2, lon2);
        int siteRadius = Integer.parseInt(preferences.getString(Preferences.SITE_RADIUS, AppsConstant.DEFAULTRADIUS));
        Location.distanceBetween(lat1, lon1, lat2, lon2, result);
        float distance = result[0];
        Boolean isInLocation = distance <= siteRadius;

        if (isInLocation) {
            ((MainActivity) getActivity()).preferences.saveBoolean(Preferences.INLOCATION, true);
            ((MainActivity) getActivity()).preferences.saveString(Preferences.LOCATIONSTATUS, "False");
        } else {
            ((MainActivity) getActivity()).preferences.saveBoolean(Preferences.INLOCATION, false);
            ((MainActivity) getActivity()).preferences.saveString(Preferences.LOCATIONSTATUS, "True");
        }
        ((MainActivity) getActivity()).preferences.commit();

        return isInLocation;
    }

    public void moveToCurentLocation() {
        if (checkPermission()) {
            if (map != null) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(new
                        LatLng(StringUtils.getDouble(((MainActivity) getActivity()).preferences.getString(Preferences.USERLATITUDE, ""))
                        , StringUtils.getDouble(((MainActivity) getActivity()).preferences.getString(Preferences.USERLONGITUDE, ""))), 18));
            }
        }
    }

    public void UpdateLocationStatus() {
        if (TextUtils.isEmpty(((MainActivity) getActivity()).preferences.getString(Preferences.LOCATIONSTATUS, ""))) {
            if (((MainActivity) getActivity()).preferences.getBoolean(Preferences.INLOCATION, false)) {
                tvTimeInOutLocation.setText("at " + ((MainActivity) getActivity()).preferences.getString(Preferences.SITENAME, ""));
                ((MainActivity) getActivity()).preferences.saveString(Preferences.LOCATIONSTATUS, "False"); // value to store
                ((MainActivity) getActivity()).preferences.commit();
            } else {

                tvTimeInOutLocation.setText("(Not at location)");
                ((MainActivity) getActivity()).preferences.saveString(Preferences.LOCATIONSTATUS, "True"); // value to store
                ((MainActivity) getActivity()).preferences.commit();
            }
        } else if (((MainActivity) getActivity()).preferences.getBoolean(Preferences.INLOCATION, false) && !((MainActivity) getActivity()).preferences.getString(Preferences.LOCATIONSTATUS, "").equalsIgnoreCase("True")) {
            tvTimeInOutLocation.setText("at " + ((MainActivity) getActivity()).preferences.getString(Preferences.SITENAME, ""));
            ((MainActivity) getActivity()).preferences.saveString(Preferences.LOCATIONSTATUS, "True"); // value to store
            ((MainActivity) getActivity()).preferences.commit();
        } else if (!((MainActivity) getActivity()).preferences.getBoolean(Preferences.INLOCATION, false)
                && !((MainActivity) getActivity()).preferences.getString(Preferences.LOCATIONSTATUS, "").equalsIgnoreCase("False")) {
            tvTimeInOutLocation.setText("(Not at location)");
            ((MainActivity) getActivity()).preferences.saveString(Preferences.LOCATIONSTATUS, "False"); // value to store
            ((MainActivity) getActivity()).preferences.commit();
        }

    }

    public void SetLoginLogOut() {
        String clockDate = CalenderUtils.getCurrentDate(CalenderUtils.DateMonthDashedFormate);
        TimeInOutDetails details = dataSource.getToday();
        String lastDate = CalenderUtils.getDateMethod(details.getClockDate(), CalenderUtils.DateMonthDashedFormate);
        if (!details.equals(null) && clockDate.equalsIgnoreCase(lastDate)) {
            String actionType = details.getComments();
            if (TextUtils.isEmpty(actionType) || actionType.equalsIgnoreCase("TimeOut")) {
                llShiftTime.setVisibility(View.GONE);
                tvTimeInOut.setText("Time In");
                llLogin_Logout.setVisibility(View.VISIBLE);
            } else {
                llShiftTime.setVisibility(View.VISIBLE);
                tvTimeInOut.setText("Time Out");
                llLogin_Logout.setVisibility(View.VISIBLE);
//					tvTimeInOutLocation.setText("Time In at " + getCurrentTime(new Date()));
            }
        } else {
            llShiftTime.setVisibility(View.GONE);
            tvTimeInOut.setText("Time In");
            llLogin_Logout.setVisibility(View.VISIBLE);
        }
    }

    public void onResult(Status status) {
        if (status.isSuccess()) {
            //Toast.makeText(getApplicationContext(),getString(R.string.geofences_added), Toast.LENGTH_SHORT).show();
        } else {
            MainActivity.geofencesAlreadyRegistered = false;
            String errorMessage = getErrorString(getActivity(), status.getStatusCode());
            Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
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

    @Override
    public void onLocationChanged(Location location) {

        location.setAccuracy(1.0f);
        if(isUserInLoacation()) {
            ((MainActivity)getActivity()).changeSiteName(preferences.getString(Preferences.SITENAME,""));
        } else {
            ((MainActivity)getActivity()).changeSiteName(getString(R.string.off_site));
        }
        Log.d(MainActivity.TAG, "new location : " + location.getLatitude() + ", " + location.getLongitude() + ". " + location.getAccuracy());
        broadcastLocationFound(location);

        if (!MainActivity.geofencesAlreadyRegistered) {
            registerGeofences();
        }
    }

    public void broadcastLocationFound(Location location) {
        updateMarker(location.getLatitude(), location.getLongitude());
    }

    protected void registerGeofences() {
        if (MainActivity.geofencesAlreadyRegistered) {
            return;
        }

        Log.d(MainActivity.TAG, "Registering Geofences");

        HashMap<String, SimpleGeofence> geofences = SimpleGeofenceStore
                .getInstance(((MainActivity) getActivity()).preferences.getString(Preferences.SITENAME, ""), StringUtils.getDouble(((MainActivity) getActivity()).preferences.getString(Preferences.LATITUDE, ""))
                        , StringUtils.getDouble(((MainActivity) getActivity()).preferences.getString(Preferences.LONGITUDE, "")), StringUtils.getInt(((MainActivity) getActivity()).preferences.getString(Preferences.SITE_RADIUS, AppsConstant.DEFAULTRADIUS))).getSimpleGeofences();

        GeofencingRequest.Builder geofencingRequestBuilder = new GeofencingRequest.Builder();
        geofencingRequestBuilder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        for (Map.Entry<String, SimpleGeofence> item : geofences.entrySet()) {
            SimpleGeofence sg = item.getValue();

            geofencingRequestBuilder.addGeofence(sg.toGeofence());
        }

        GeofencingRequest geofencingRequest = geofencingRequestBuilder.build();

        mPendingIntent = requestPendingIntent();
        if (checkPermission()) {
            LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, geofencingRequest, mPendingIntent).setResultCallback(this);
        }

        MainActivity.geofencesAlreadyRegistered = true;
    }

    private PendingIntent requestPendingIntent() {

        if (null != mPendingIntent) {
            return mPendingIntent;
        } else {

          //  Intent intent = new Intent(getActivity(), GeofenceReceiver.class);
          //  return PendingIntent.getService(getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            Intent intent = new Intent("com.allsmart.fieldtracker.ACTION_GEOFENCE_RECEIVER");
            return PendingIntent.getBroadcast(getActivity(), 0, intent,PendingIntent.FLAG_UPDATE_CURRENT);

        }
    }

    @Override
    public void onPause() {
        super.onPause();
//		if ( checkPermission())
//			getActivity().unregisterReceiver(receiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {

                @Override
                public void onMapReady(GoogleMap googleMap) {
                    map = googleMap;
                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
          //          map.setMyLocationEnabled(true);
                    map.animateCamera(CameraUpdateFactory.zoomTo(18));

                    displayGeofences();
                }
            });
        }
//		if ( checkPermission())
//			getActivity().registerReceiver(receiver,new IntentFilter("com.oppo.sfamanagement.geolocation.service"));
    }

    // Create GoogleApiClient instance
    protected synchronized void buildGoogleApiClient() {
        Log.i(MainActivity.TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity()).
                addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).
                addApi(LocationServices.API).build();
        createLocationRequest();
    }

    protected void createLocationRequest() {
        if (mLocationRequest == null) {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
            mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // Call GoogleApiClient connection when starting the Activity
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
//		mGoogleApiClient.disconnect();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
//			stopLocationUpdates();
        }
    }

    // Disconnect GoogleApiClient when stopping Activity
    protected void stopLocationUpdates() {
        if (checkPermission()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    private Location lastLocation;

    private void getLastKnownLocation() {
        if (checkPermission()) {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (lastLocation != null) {
                updateMarker(lastLocation.getLatitude(), lastLocation.getLongitude());
                map.animateCamera(CameraUpdateFactory.zoomTo(18));
                startLocationUpdates();
            } else {
                startLocationUpdates();
            }
//			getActivity().registerReceiver(receiver,new IntentFilter("com.oppo.sfamanagement.geolocation.service"));
        } else askPermission();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(MainActivity.TAG, "Connected to GoogleApiClient");
        // Zoom in the Google Map
        map.animateCamera(CameraUpdateFactory.zoomTo(18));
        getLastKnownLocation();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(MainActivity.TAG, "Connection suspended");
        //mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(MainActivity.TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    //	protected LocationRequest mLocationRequest;
    protected void startLocationUpdates() {
        if (checkPermission()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    private boolean checkPermission() {
        // Ask for permission if it wasn't granted yet
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED);
        } else {
            return true;
        }
    }

    private void OpenCamera() {
        Intent i = new Intent(getActivity(), CameraActivity.class);
        i.putExtra("camera_key", AppsConstant.FRONT_CAMREA_OPEN);
        i.putExtra("purpose", "ForPhoto");
        startActivityForResult(i, REQ_CAMERA);
    }

    private final static int REQ_CAMERA = 1003;

    private boolean checkCameraPermission() {
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED);
    }

    private String getCurrentTime(Date date) {
        Calendar mCalendar = Calendar.getInstance();
        mCalendar.setTime(date);

        return (String) DateFormat.format("hh:mm", mCalendar);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQ_CAMERA) {
                final String strFile = data.getStringExtra("response");
                if (!TextUtils.isEmpty(strFile)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                    if (!tvTimeInOut.getText().toString().equals(null) && !TextUtils.isEmpty(tvTimeInOut.getText().toString()) && tvTimeInOut.getText().toString().equalsIgnoreCase("Time In")) {
                        dialog.setTitle("Confirm Time In");
                    } else {
                        dialog.setTitle("Confirm Time Out");
                    }
                    if (isUserInLoacation()) {
                        dialog.setMessage("You are currently at " + preferences.getString(Preferences.SITENAME, ""));
                    } else {
                        dialog.setMessage("You are currently Off Site");
                    }
                    dialog.setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            UploadTimeInOut(strFile);
                        }
                    });
                    dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    dialog.show();
                }
            }
        }
    }


    private void UploadTimeInOut(String strFile) {
        String strDate = CalenderUtils.getCurrentDate(CalenderUtils.DateMonthDashedFormate);
        String strActionType = "";
        String strComments = "";
        String clockDate = CalenderUtils.getCurrentDate(CalenderUtils.DateMonthDashedFormate);
        TimeInOutDetails details = dataSource.getToday();
        String lastDate = CalenderUtils.getDateMethod(details.getClockDate(), CalenderUtils.DateMonthDashedFormate);
        if (!details.equals(null) && clockDate.equalsIgnoreCase(lastDate)) {
            String actionType = details.getComments();
            if (TextUtils.isEmpty(actionType) || actionType.equalsIgnoreCase("TimeOut")) {
                strActionType = "clockIn";
                strComments = "TimeIn";
            } else {
                strActionType = "clockOut";
                strComments = "TimeOut";
            }
        } else {
            strActionType = "clockIn";
            strComments = "TimeIn";
        }
        dataSource.insertTimeInOutDetails(getTimeInOutDetails(strComments, strActionType, strFile, "false"));
        if (NetworkUtils.isNetworkConnectionAvailable(getContext())) {
            Intent uploadTraIntent = new Intent(getContext(), UploadTransactions.class);
            getActivity().startService(uploadTraIntent);
        }
        preferences.saveString(Preferences.TIMEINTIME, CalenderUtils.getCurrentDate("dd/MM/yyyy HH:mm:ss"));
        preferences.commit();

        SetLoginLogOut();
    }

    private TimeInOutDetails getTimeInOutDetails(String strComments, String strType, String strImage, String isPushed) {
        String clockDate = CalenderUtils.getCurrentDate(CalenderUtils.DateFormate);
        TimeInOutDetails details = new TimeInOutDetails();
        details.setUsername(preferences.getString(Preferences.USERNAME, ""));
        details.setClockDate(clockDate);
        details.setActionType(strType);
        details.setComments(strComments);
        details.setLatitude(preferences.getString(Preferences.USERLATITUDE, ""));
        details.setLongitude(preferences.getString(Preferences.USERLONGITUDE, ""));
        details.setActionImage(strImage);
        details.setIsPushed(isPushed);
        return details;
    }

    private boolean checkStoragePermission() {
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED);
    }

    private void askStoragePermission() {
        this.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQ_Storage_PERMISSION);
    }

    private void askCameraPermission() {
        this.requestPermissions(new String[]{Manifest.permission.CAMERA}, REQ_CAMERA_PERMISSION);
    }

    private final static int REQ_PERMISSION = 1001;
    private final static int REQ_CAMERA_PERMISSION = 1002;
    private final static int REQ_Storage_PERMISSION = 1000;

    // Asks for permission
    private void askPermission() {
        this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_PERMISSION);
    }

    // Verify user's response of the permission requested
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    getLastKnownLocation();

                } else {
                    // Permission denied
                    permissionsDenied();
                }
                break;
            }
            case REQ_CAMERA_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    if (checkStoragePermission())
                        OpenCamera();
                    else
                        askStoragePermission();

                } else {
                    // Permission denied
                    permissionsDenied();
                }
                break;
            }
            case REQ_Storage_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    OpenCamera();

                } else {
                    // Permission denied
                    permissionsDenied();
                }
                break;
            }
        }

    }

    // App cannot work without the permissions
    private void permissionsDenied() {
//		Log.w(TAG, "permissionsDenied()");
    }

    protected void displayGeofences() {
        HashMap<String, SimpleGeofence> geofences = SimpleGeofenceStore.getInstance(((MainActivity) getActivity()).preferences.getString(Preferences.SITENAME, ""), StringUtils.getDouble(((MainActivity) getActivity()).preferences.getString(Preferences.LATITUDE, ""))
                , StringUtils.getDouble(((MainActivity) getActivity()).preferences.getString(Preferences.LONGITUDE, "")), StringUtils.getInt(((MainActivity) getActivity()).preferences.getString(Preferences.SITE_RADIUS, AppsConstant.DEFAULTRADIUS))).getSimpleGeofences();
        for (Map.Entry<String, SimpleGeofence> item : geofences.entrySet()) {
            SimpleGeofence sg = item.getValue();
            CircleOptions circleOptions = new CircleOptions()
                    .center(new LatLng(sg.getLatitude(), sg.getLongitude()))
                    .strokeColor(Color.argb(50, 0, 127, 255))
                    .fillColor(Color.argb(100, 137, 207, 240))
                    .radius(sg.getRadius());
            map.addCircle(circleOptions);
        }
    }

    protected void createMarker(Double latitude, Double longitude) {
        LatLng latLng = new LatLng(latitude, longitude);
        if(myPositionMarker != null) {
            myPositionMarker.remove();
            myPositionMarker = map.addMarker((new MarkerOptions().position(latLng)).icon(BitmapDescriptorFactory.fromResource(R.drawable.user)));
        } else {
            myPositionMarker = map.addMarker((new MarkerOptions().position(latLng)).icon(BitmapDescriptorFactory.fromResource(R.drawable.user)));
        }
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    protected void updateMarker(Double latitude, Double longitude) {
//        LatLng latLng = new LatLng(latitude, longitude);
        //       System.out.println("This is at updateMarker  "+latitude + "      " + longitude);
//        map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
   //     if (myPositionMarker == null) {
            createMarker(latitude, longitude);
   //     }
		((MainActivity)getActivity()).preferences.saveString(Preferences.USERLATITUDE, latitude+""); // value to store
		((MainActivity)getActivity()).preferences.commit();
		((MainActivity)getActivity()).preferences.saveString(Preferences.USERLONGITUDE, longitude+""); // value to store
		((MainActivity)getActivity()).preferences.commit();
        UpdateLocationStatus();
    }
}