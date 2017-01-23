package com.allsmart.fieldtracker.fragment;

import android.Manifest;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.allsmart.fieldtracker.activity.MainActivity;
import com.allsmart.fieldtracker.R;
import com.allsmart.fieldtracker.constants.AppsConstant;
import com.allsmart.fieldtracker.constants.LoaderConstant;
import com.allsmart.fieldtracker.constants.LoaderMethod;
import com.allsmart.fieldtracker.service.LoaderServices;
import com.allsmart.fieldtracker.utils.ParameterBuilder;
import com.allsmart.fieldtracker.constants.Services;
import com.allsmart.fieldtracker.utils.UrlBuilder;

/**
 * Created by allsmartlt218 on 02-12-2016.
 */

public class AddStoreFragment extends Fragment implements View.OnClickListener, LoaderManager.LoaderCallbacks<Object>, LocationListener {

    EditText storeName, storeAddress, siteRadius;
    Button getCurrentLocation, btAdd, btCancel;
    TextView latitude, longitude;
    String lat = "";
    String lon = "";
    private boolean isClicked = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_store_fragment, container, false);
        storeName = (EditText) view.findViewById(R.id.etStoreName);
        storeAddress = (EditText) view.findViewById(R.id.etAddress);
        siteRadius = (EditText) view.findViewById(R.id.etStoreRadius);
        getCurrentLocation = (Button) view.findViewById(R.id.btGetLocation);
        btAdd = (Button) view.findViewById(R.id.btAdd);
        btAdd.setOnClickListener(this);
       // btAdd.setEnabled(false);
        btCancel = (Button) view.findViewById(R.id.btCancel);
        latitude = (TextView) view.findViewById(R.id.latitude);
        longitude = (TextView) view.findViewById(R.id.longitude);
        getCurrentLocation.setOnClickListener(this);
        storeName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) {
                    storeAddress.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            if (s.length() == 0) {
                          //      btAdd.setEnabled(false);
                            } else {
                                /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    btAdd.setBackground(getResources().getDrawable(R.drawable.editstore_edit_button));
                                }*/
                                btAdd.setEnabled(true);
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable s) {

                        }
                    });
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        btCancel.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btGetLocation:
                LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
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
                Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                lat = String.valueOf(location.getLatitude());
                lon = String.valueOf(location.getLongitude());
                latitude.setText(lat);
                longitude.setText(lon);
                isClicked = true;
                Log.d("LAT",String.valueOf(location.getLatitude()));
                break;
            case R.id.btAdd:
                String sN = storeName.getText().toString();
                String sA = storeAddress.getText().toString();
                String sRadius = siteRadius.getText().toString();

                    if(!TextUtils.isEmpty(sN) && !TextUtils.isEmpty(sA) && !TextUtils.isEmpty(sRadius) ) {
                        Bundle b = new Bundle();
                        b.putString(AppsConstant.URL, UrlBuilder.getUrl(Services.ADD_STORE));
                        b.putString(AppsConstant.METHOD,AppsConstant.POST);
                        if(isClicked) {
                            b.putString(AppsConstant.PARAMS, ParameterBuilder.getAddStore(sN, sA, lat, lon, sRadius));
                            getActivity().getLoaderManager().initLoader(LoaderConstant.ADD_STORE,b,AddStoreFragment.this).forceLoad();
                        }else {
                            Toast.makeText(getContext(),"Lat Long empty, Click get Location Button",Toast.LENGTH_SHORT).show();
                        }
                        Log.d("ADD", sN + "  " + sA + "  " + lat + "  " + lon);

                    } else {
                        Toast.makeText(getContext(),"Fields cannot be empty",Toast.LENGTH_SHORT).show();
                    }
                break;
            case R.id.btCancel:
                Fragment fragment = new StoreListFragment();
                FragmentManager fm = getFragmentManager();
                fm.beginTransaction().replace(R.id.flMiddle,fragment).commit();
                fm.executePendingTransactions();
                break;
        }
    }

    @Override
    public Loader<Object> onCreateLoader(int id, Bundle args) {
        if(getActivity() != null && getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showHideProgressForLoder(false);
        }switch (id) {
            case LoaderConstant.ADD_STORE:
                return new LoaderServices(getActivity(), LoaderMethod.ADD_STORE,args);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Object> loader, Object data) {
        if(getActivity() != null && getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showHideProgressForLoder(true);
        }if (data != null && data instanceof String) {

        } else {
            Toast.makeText(getContext(),
                    "Error in response. Please try again.",
                    Toast.LENGTH_SHORT).show();
        }
        if(data.equals("success")) {
            Toast.makeText(getContext(),
                    "Store Added Successfully",
                    Toast.LENGTH_SHORT).show();
            Fragment fragment = new StoreListFragment();
            FragmentManager fm = getFragmentManager();
            fm.beginTransaction().replace(R.id.flMiddle,fragment).commit();
            fm.executePendingTransactions();

        } else {
            Toast.makeText(getContext(),
                    "Failed To Add",
                    Toast.LENGTH_SHORT).show();
        }if(getActivity() != null && getActivity() instanceof MainActivity) {
            getActivity().getLoaderManager().destroyLoader(loader.getId());
        }
    }

    @Override
    public void onLoaderReset(Loader<Object> loader) {

    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        lat = String.valueOf(location.getLatitude());
        lon = String.valueOf(location.getLongitude());
        latitude.setText(lat);
        longitude.setText(lon);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}