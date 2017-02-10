package com.allsmart.fieldtracker.fragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.app.LoaderManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.allsmart.fieldtracker.activity.MainActivity;
import com.allsmart.fieldtracker.R;
import com.allsmart.fieldtracker.constants.AppsConstant;
import com.allsmart.fieldtracker.storage.Preferences;
import com.allsmart.fieldtracker.model.Store;
import com.allsmart.fieldtracker.constants.LoaderConstant;
import com.allsmart.fieldtracker.constants.LoaderMethod;
import com.allsmart.fieldtracker.service.LoaderServices;
import com.allsmart.fieldtracker.utils.NetworkUtils;
import com.allsmart.fieldtracker.utils.ParameterBuilder;
import com.allsmart.fieldtracker.constants.Services;
import com.allsmart.fieldtracker.utils.UrlBuilder;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * Created by allsmartlt218 on 02-12-2016.
 */

public class EditStoreFragment extends Fragment implements View.OnClickListener, LoaderManager.LoaderCallbacks, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    protected EditText storeName,address,siteRadius;
    protected TextView lattitude,longitude;
    protected Button btEdit,btCancel,getLocation;
    private int storeId;
    protected ProgressDialog pd;
    private Location location;
    private String lat = "";
    private final static int REQ_PERMISSION = 1001;
    private String lon = "";
    private GoogleApiClient googleApiClient;
    private boolean isGetLocationClicked = false;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(googleApiClient == null || !googleApiClient.isConnected()) {
            googleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_store,container,false);
        storeName = (EditText) view.findViewById(R.id.etStName);
        address = (EditText) view.findViewById(R.id.etStAddress);
        lattitude = (TextView) view.findViewById(R.id.tvLattitude);
        longitude = (TextView) view.findViewById(R.id.tvLongitude);
        btEdit = (Button) view.findViewById(R.id.btEdit);
        siteRadius = (EditText) view.findViewById(R.id.etSiteRadius);
        getLocation = (Button) view.findViewById(R.id.getLocation);
        btCancel = (Button) view.findViewById(R.id.btCancel);
        if(googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(getContext()).
                    addConnectionCallbacks(this).
                    addOnConnectionFailedListener(this).
                    addApi(LocationServices.API).build();
        }
        pd = new ProgressDialog(getContext());
        btEdit.setOnClickListener(this);
        btCancel.setOnClickListener(this);
        getLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkPermission()) {
                        if (googleApiClient.isConnected()) {
                            Location loc = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                            if(loc != null) {
                                lattitude.setText(loc.getLatitude() + "");
                                longitude.setText(loc.getLongitude() + "");
                                ((MainActivity) getActivity()).displayMessage("Accurate to " + location.getAccuracy() + " m");
                                isGetLocationClicked = true;
                            } else {
                                ((MainActivity) getActivity()).displayMessage("Unable to get your location");
                            }
                        } else {
                            Log.d(MainActivity.TAG,"Google API is not connected");
                        }
                    }else {
                        requestPermission();
                    }
                }else {
                    // if (NetworkUtils.isNetworkConnectionAvailable(getContext())) {
                    final LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                    if (locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if(loc != null){
                            lattitude.setText(loc.getLatitude()+"");
                            longitude.setText(loc.getLongitude()+"");
                            ((MainActivity) getActivity()).displayMessage("Accurate to " + loc.getAccuracy() + " m");
                            isGetLocationClicked = true;
                        }else {
                            ((MainActivity) getActivity()).displayMessage("Unable to get your location");
                        }
                    } else {
                        ((MainActivity) getActivity()).displayMessage("GPS is not available");
                    }
                }
            }
        });
        Store b = getArguments().getParcelable("Store");
        storeId = b.getStoreId();
        storeName.setText(b.getStoreName());
        address.setText(b.getAddress());
        siteRadius.setText(b.getSiteRadius());
        lattitude.setText(b.getLattitude());
        longitude.setText(b.getLongitude());
        return view;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQ_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (googleApiClient.isConnected()) {
                        if(checkPermission()) {
                            Location loc = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                            if (loc != null) {
                                lattitude.setText(loc.getLatitude() + "");
                                longitude.setText(loc.getLongitude() + "");
                                isGetLocationClicked = true;
                            } else {
                                ((MainActivity) getActivity()).displayMessage("Unable to get your location");
                            }
                            Log.d(MainActivity.TAG,"Permision granted");
                        } else {
                            requestPermission();
                        }

                    }
                } else {
                    ((MainActivity)getActivity()).displayMessage("Permission denied");
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        if(shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)){
                            showMessageOKCancel("You need to allow access to get location", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(new String[]{ACCESS_FINE_LOCATION},
                                                REQ_PERMISSION);
                                    }
                                }
                            });
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(getActivity())
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private boolean checkPermission() {
        // Ask for permission if it wasn't granted yet
        int result = ContextCompat.checkSelfPermission(getContext(),ACCESS_FINE_LOCATION);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(getActivity(),new String[]{ACCESS_FINE_LOCATION},REQ_PERMISSION);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btEdit:
                String sName = storeName.getText().toString();
                String sAddress = address.getText().toString();
                String sLattitude = lattitude.getText().toString();
                String sLongitude = longitude.getText().toString();
                String proximity = siteRadius.getText().toString();
                if(!TextUtils.isEmpty(sName) && !TextUtils.isEmpty(sAddress) && !TextUtils.isEmpty(proximity) ) {
                    Bundle b = new Bundle();
                    b.putString(AppsConstant.URL, UrlBuilder.getStoreUpdate(Services.STORE_UPDATE, String.valueOf(storeId)));
                    b.putString(AppsConstant.METHOD, AppsConstant.PUT);
                    b.putString(AppsConstant.PARAMS, ParameterBuilder.getAddStore(sName, sAddress, sLattitude, sLongitude, proximity));
                    getActivity().getLoaderManager().initLoader(LoaderConstant.STORE_UPDATE, b, EditStoreFragment.this).forceLoad();
                } else {
                    Toast.makeText(getContext(),"Fields cannot be empty",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btCancel:
                FragmentManager fm = getFragmentManager();
                fm.popBackStackImmediate();
                break;
        }
    }


    @Override
    public android.content.Loader onCreateLoader(int id, Bundle args) {
        ((MainActivity)getActivity()).showHideProgressForLoder(false);
        switch (id) {
            case LoaderConstant.STORE_UPDATE:
                return new LoaderServices(getContext(),LoaderMethod.STORE_UPDATE,args);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(android.content.Loader loader, Object data) {
     //   if(getActivity() != null && getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showHideProgressForLoder(true);
       // }
        if(data != null && data instanceof String) {

        } else {

        }

        if(data.equals("success")) {
            Toast.makeText(getContext(),
                    "Store Added Successfully",
                    Toast.LENGTH_SHORT).show();
            FragmentManager fm = getFragmentManager();
            fm.popBackStack();
        } else {
            Toast.makeText(getContext(),
                    "Failed to Upload",
                    Toast.LENGTH_SHORT).show();
        }

        if(getActivity() != null  && getActivity() instanceof  MainActivity) {
            getActivity().getLoaderManager().destroyLoader(loader.getId());
        }

    }

    @Override
    public void onLoaderReset(android.content.Loader loader) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (googleApiClient.isConnected()) {

        } else {
            Log.d(MainActivity.TAG,"Google API not connected");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
