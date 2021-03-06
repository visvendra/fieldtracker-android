package com.allsmart.fieldtracker.fragment;

import android.app.Dialog;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.allsmart.fieldtracker.activity.CameraActivity;
import com.allsmart.fieldtracker.activity.MainActivity;
import com.allsmart.fieldtracker.R;
import com.allsmart.fieldtracker.constants.AppsConstant;
import com.allsmart.fieldtracker.customviews.CustomBuilder;
import com.allsmart.fieldtracker.storage.Preferences;
import com.allsmart.fieldtracker.model.Promoter;
import com.allsmart.fieldtracker.model.Store;
import com.allsmart.fieldtracker.constants.LoaderConstant;
import com.allsmart.fieldtracker.constants.LoaderMethod;
import com.allsmart.fieldtracker.service.LoaderServices;
import com.allsmart.fieldtracker.utils.ParameterBuilder;
import com.allsmart.fieldtracker.constants.Services;
import com.allsmart.fieldtracker.utils.UrlBuilder;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;
import static com.allsmart.fieldtracker.constants.AppsConstant.BACK_CAMREA_OPEN;

/**
 * Created by allsmartlt218 on 10-12-2016.
 */

public class EditPromoterFragment extends Fragment implements LoaderManager.LoaderCallbacks<Object>, View.OnClickListener {

    private Button edit,cancel;
    private EditText firstName,lastName,phone,emailAddress,Address;
    private CheckBox sun,mon,tue,wed,thu,fri,sat;
    private FragmentManager fragmentManager;
    ImageView ivPhoto,ivAadhar,ivAddress;
    private TextView tvStore,tvSE;
    private ArrayList<Store> list;
    private String[] image = new String[3];
    private int i = 0;
    private Promoter promoter;
    private Preferences preferences;
    private int storeId;
    private LinearLayout llWeeklyOff;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        promoter = getArguments().getParcelable("promoter");
        preferences = new Preferences(getContext());

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_promoter_fragment,container,false);
        edit = (Button) view.findViewById(R.id.btEPEdit);
        llWeeklyOff = (LinearLayout) view.findViewById(R.id.llWeeklyOff);
        cancel = (Button) view.findViewById(R.id.btEAddPCancel);
        firstName = (EditText) view.findViewById(R.id.etPromoterFN);
        lastName = (EditText) view.findViewById(R.id.etPromoterLN);
        phone = (EditText) view.findViewById(R.id.etPromoterPh);
        emailAddress = (EditText) view.findViewById(R.id.etPromoterEA);
        tvStore = (TextView) view.findViewById(R.id.tvStoreAssignment);
        tvSE = (TextView) view.findViewById(R.id.tvSEAssignment);
        Address = (EditText) view.findViewById(R.id.etPromoterAdd);
        ivPhoto = (ImageView) view.findViewById(R.id.ivPhoto);
        ivAddress = (ImageView) view.findViewById(R.id.ivAddressProof);
        ivAadhar = (ImageView) view.findViewById(R.id.ivAadhar);
        sun = (CheckBox) view.findViewById(R.id.checkboxSun);
        mon = (CheckBox) view.findViewById(R.id.checkboxM);
        tue = (CheckBox) view.findViewById(R.id.checkboxTue);
        wed = (CheckBox) view.findViewById(R.id.checkboxW);
        thu = (CheckBox) view.findViewById(R.id.checkboxThu);
        fri = (CheckBox) view.findViewById(R.id.checkboxF);
        sat = (CheckBox) view.findViewById(R.id.checkboxSat);
        fragmentManager = getFragmentManager();



        final String photo  = promoter.getUserPhoto();
        final String adhar  = promoter.getAadharIdPath();
        final String address = promoter.getAddressIdPath();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).displayMessage("Cancel Button Clicked");
                fragmentManager.popBackStackImmediate();
            }
        });
        firstName.setText(promoter.getFirstName());
        lastName.setText(promoter.getLastName());
        phone.setText(promoter.getPhoneNum());
        emailAddress.setText(promoter.getEmailAddress());
        Address.setText(promoter.getAddress());
        tvStore.setText(preferences.getString(Preferences.SITENAME,""));
        tvSE.setText(preferences.getString(Preferences.USERFULLNAME,""));
        if (isApproved()) {
            firstName.setEnabled(false);
            lastName.setEnabled(false);
            phone.setEnabled(false);
            emailAddress.setEnabled(false);
            Address.setEnabled(false);
            tvStore.setEnabled(false);
            tvSE.setEnabled(false);

            sun.setEnabled(false);
            mon.setEnabled(false);
            tue.setEnabled(false);
            wed.setEnabled(false);
            thu.setEnabled(false);
            fri.setEnabled(false);
            sat.setEnabled(false);

            edit.setVisibility(View.GONE);
            cancel.setVisibility(View.GONE);
        }



        System.out.println(photo + "     " + adhar + "           " + address);
        Picasso.with(getContext()).load(UrlBuilder.getServerImage(photo)).placeholder(R.drawable.photo).fit().into(ivPhoto);
        Picasso.with(getContext()).load(UrlBuilder.getServerImage(adhar)).placeholder(R.drawable.aadhar).fit().into(ivAadhar);
        Picasso.with(getContext()).load(UrlBuilder.getServerImage(address)).placeholder(R.drawable.id_card).fit().into(ivAddress);
        ivPhoto.setOnClickListener(this);
        ivAadhar.setOnClickListener(this);
        ivAddress.setOnClickListener(this);
        tvStore.setTag(new Store());
        if (!isApproved()) {
            Bundle b = new Bundle();
            b.putString(AppsConstant.URL, UrlBuilder.getUrl(Services.STORE_LIST));
            b.putString(AppsConstant.METHOD, AppsConstant.GET);
            getActivity().getLoaderManager().initLoader(LoaderConstant.STORE_LIST, b, EditPromoterFragment.this).forceLoad();
        } else {
            Bundle b = new Bundle();
            b.putString(AppsConstant.URL, UrlBuilder.getStoreDetails(Services.STORE_DETAIL,promoter.getProductStoreId()));
            b.putString(AppsConstant.METHOD, AppsConstant.GET);
            getActivity().getLoaderManager().initLoader(LoaderConstant.STORE_DETAIL, b, EditPromoterFragment.this).forceLoad();
        }
        tvStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CustomBuilder builder = new CustomBuilder(getContext(),"Select Store",true);
                builder.setSingleChoiceItems(list,tvStore.getTag(), new CustomBuilder.OnClickListener() {
                    @Override
                    public void onClick(CustomBuilder builder, Object selectedObject) {
                        tvStore.setTag(selectedObject);
                        tvStore.setText(((Store) selectedObject).getStoreName());
                        storeId = ((Store) selectedObject).getStoreId();
                        tvSE.setText(preferences.getString(Preferences.USERFULLNAME,"Full Name"));
                        builder.dismiss();
                    }
                });
                builder.setCancelListener(new CustomBuilder.OnCancelListener() {
                    @Override
                    public void onCancel() {

                    }
                });
                builder.show();
            }
        });
      //  if (!promoter.getStatusId().equals(null) && !promoter.getStatusId().equalsIgnoreCase("ReqCompleted")) {
            edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isNotNull(firstName.getText().toString(),lastName.getText().toString(),phone.getText().toString(),emailAddress.getText().toString(),
                        Address.getText().toString(),tvStore.getText().toString(),tvSE.getText().toString())) {
                    Bundle b = new Bundle();
                    b.putString(AppsConstant.URL,UrlBuilder.getUpdatePromoter(Services.UPDATE_PROMOTER,promoter.getRequestId()));
                    b.putString(AppsConstant.METHOD,AppsConstant.PUT);
                    if(!TextUtils.isEmpty(image[0]) && !TextUtils.isEmpty(image[1]) && !TextUtils.isEmpty(image[2])) {
                        if(storeId == 0) {
                            b.putString(AppsConstant.PARAMS,ParameterBuilder.getPromoterUpdate(promoter.getRequestId(),promoter.getRequestType(),
                                    firstName.getText().toString(),lastName.getText().toString(),phone.getText().toString(), Address.getText().toString(),emailAddress.getText().toString(),
                                    promoter.getProductStoreId(),promoter.getStatusId(),"RqtAddPromoter","description after updation",image[1],image[0],image[2]));
                        } else {
                            b.putString(AppsConstant.PARAMS,ParameterBuilder.getPromoterUpdate(promoter.getRequestId(),promoter.getRequestType(),
                                    firstName.getText().toString(),lastName.getText().toString(),phone.getText().toString(), Address.getText().toString(),emailAddress.getText().toString(),
                                    storeId+"",promoter.getStatusId(),"RqtAddPromoter","description after updation",adhar,photo,address));
                        }
                    } else {

                        if(storeId == 0) {
                            b.putString(AppsConstant.PARAMS,ParameterBuilder.getPromoterUpdate(promoter.getRequestId(),promoter.getRequestType(),
                                    firstName.getText().toString(),lastName.getText().toString(),phone.getText().toString(), Address.getText().toString(),emailAddress.getText().toString(),
                                    promoter.getProductStoreId(),promoter.getStatusId(),"RqtAddPromoter","description after updation",adhar,photo,address));
                        } else {
                            b.putString(AppsConstant.PARAMS,ParameterBuilder.getPromoterUpdate(promoter.getRequestId(),promoter.getRequestType(),
                                    firstName.getText().toString(),lastName.getText().toString(),phone.getText().toString(), Address.getText().toString(),emailAddress.getText().toString(),
                                    storeId+"",promoter.getStatusId(),"RqtAddPromoter","description after updation",adhar,photo,address));
                        }
                    }
                    getActivity().getLoaderManager().initLoader(LoaderConstant.UPDATE_PROMOTER,b,EditPromoterFragment.this).forceLoad();
                } else {
                    ((MainActivity)getActivity()).displayMessage("Fields cannot be empty");
                }
                    }
        });

        return view;
    }

    @Override
    public Loader<Object> onCreateLoader(int id, Bundle args) {
        if(getActivity() != null && getActivity() instanceof MainActivity) {
            ((MainActivity)getActivity()).showHideProgressForLoder(false);
        }
        switch(id) {
            case  LoaderConstant.IMAGE_UPLOAD:
                return new LoaderServices(getContext(),LoaderMethod.IMAGE_UPLOAD,args);
            case LoaderConstant.UPDATE_PROMOTER:
                return new LoaderServices(getContext(), LoaderMethod.UPDATE_PROMOTER,args);
            case LoaderConstant.STORE_LIST:
                return new LoaderServices(getContext(),LoaderMethod.STORE_LIST,args);
            case LoaderConstant.STORE_DETAIL:
                return new LoaderServices(getContext(),LoaderMethod.STORE_DETAIL,args);
            default:
                return null;
        }
    }

    private boolean isFieldExecutive() {
        if(preferences.getString(Preferences.ROLETYPEID,"").equalsIgnoreCase("FieldExecutiveOnPremise") ||
                preferences.getString(Preferences.ROLETYPEID,"").equalsIgnoreCase("FieldExecutiveOffPremise") ) {
           return true;
        } else {
            return false;
        }


    }

    @Override
    public void onLoadFinished(Loader<Object> loader, Object data) {
        if(getActivity() != null  && getActivity() instanceof  MainActivity) {
            ((MainActivity) getActivity()).showHideProgressForLoder(true);
        }
        switch (loader.getId()) {
            case LoaderConstant.STORE_LIST:
            if (data != null && data instanceof ArrayList) {
                list = (ArrayList<Store>) data;
            } else {
                Toast.makeText(getContext(),
                        "Error in response. Please try again.",
                        Toast.LENGTH_SHORT).show();
            } if(getActivity() != null  && getActivity() instanceof  MainActivity) {
                  getActivity().getLoaderManager().destroyLoader(loader.getId());
            }
                break;
            case LoaderConstant.STORE_DETAIL:
                if (data != null && data instanceof String) {
                    String storeName = (String) data;
                    if(!TextUtils.isEmpty(storeName) && !storeName.equalsIgnoreCase("error")) {
                        tvStore.setText(storeName);
                        tvSE.setText(preferences.getString(Preferences.USERFULLNAME,""));
                    } else {
                        Toast.makeText(getContext(),
                                "Error in response. Please try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(),
                            "Error in response. Please try again.",
                            Toast.LENGTH_SHORT).show();
                }
                if(getActivity() != null  && getActivity() instanceof  MainActivity) {
                    getActivity().getLoaderManager().destroyLoader(loader.getId());
                }
                break;
            case LoaderConstant.IMAGE_UPLOAD:
                if (data != null && data instanceof String) {
                    String result = (String) data;
                    if(!TextUtils.isEmpty(result) && !result.equalsIgnoreCase("error")) {
                        if(i == 1) {
                            //photo
                            image[0] = (String) data;

                            ivPhoto.setImageResource(R.drawable.photo_tick);
                            ivPhoto.setEnabled(false);
                        }else if(i == 2) {
                            //aadhar
                            image[1] = (String) data;
                            ivAadhar.setImageResource(R.drawable.aadhartick);
                            ivAadhar.setEnabled(false);
                        } else if(i == 3) {
                            //address
                            image[2] = (String) data;
                            ivAddress.setImageResource(R.drawable.id_card_tick);
                            ivAddress.setEnabled(false);
                        } else {
                            //error
                            Toast.makeText(getContext(),
                                    "Failed to upload. Please try again.",
                                    Toast.LENGTH_SHORT).show();
                            i = 0;
                        }
                    } else {
                        Toast.makeText(getContext(),
                                "Error in response. Please try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                    //  image[i] = (String) data;
                    Log.d("IMAGE", (String) data);
                    // i++;

                    if(getActivity() != null  && getActivity() instanceof  MainActivity) {
                        getActivity().getLoaderManager().destroyLoader(loader.getId());
                    }
                }
                break;
            case LoaderConstant.UPDATE_PROMOTER:
                if(data != null && data instanceof String) {
                    String result = (String) data;
                    if (!TextUtils.isEmpty(result) && !result.equalsIgnoreCase("success") && !result.equalsIgnoreCase("error")) {
                        Toast.makeText(getContext(),
                                result,
                                Toast.LENGTH_SHORT).show();

                    } else if(!TextUtils.isEmpty(result) && result.equalsIgnoreCase("success")) {
                        Toast.makeText(getContext(),
                                "Updated Successfully",
                                Toast.LENGTH_SHORT).show();
                        FragmentManager fm = getFragmentManager();
                        fm.popBackStack();
                    }
                    else {
                        Toast.makeText(getContext(),
                                "Failed to Update",
                                Toast.LENGTH_SHORT).show();
                    }

                }
                if(getActivity() != null  && getActivity() instanceof  MainActivity) {
                    getActivity().getLoaderManager().destroyLoader(loader.getId());
                }
                break;
        }
       // if(getActivity() != null  && getActivity() instanceof  MainActivity) {

       // }
    }

    @Override
    public void onLoaderReset(Loader<Object> loader) {

    }
    private boolean isApproved() {
        String statusId = promoter.getStatusId();
        if(!TextUtils.isEmpty(statusId) && statusId.equalsIgnoreCase("ReqCompleted")) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == AppsConstant.IMAGE_PHOTO) {
                String responseValue = data.getStringExtra("response");
                String purpose = data.getStringExtra("image_purpose");
                if (!responseValue.equals(null)) {
                    /*Toast.makeText(getContext(), responseValue, Toast.LENGTH_SHORT).show();
                    Log.d("path", responseValue);*/
                    i = 1;
                    Bundle bundle = new Bundle();
                    bundle.putString(AppsConstant.URL, Services.DomainUrlImage);
                    bundle.putString(AppsConstant.FILE, responseValue);
                    bundle.putString(AppsConstant.FILEPURPOSE,purpose);
                    getActivity().getLoaderManager().initLoader(LoaderConstant.IMAGE_UPLOAD,bundle,EditPromoterFragment.this);
                    /*ivPhoto.setImageResource(R.drawable.photo_tick);
                    ivPhoto.setEnabled(false);*/
                }
            } else if (requestCode == AppsConstant.IMAGE_AADHAR) {
                final String responseValue = data.getStringExtra("response");
                final String purpose = data.getStringExtra("image_purpose");
                if (!responseValue.equals(null)) {
                    /*Toast.makeText(getContext(), responseValue, Toast.LENGTH_SHORT).show();
                    Log.d("path", responseValue);*/
                    i = 2;
                    Bundle bundle = new Bundle();
                    bundle.putString(AppsConstant.URL, Services.DomainUrlImage);
                    bundle.putString(AppsConstant.FILE, responseValue);
                    bundle.putString(AppsConstant.FILEPURPOSE,purpose);
                    getActivity().getLoaderManager().initLoader(LoaderConstant.IMAGE_UPLOAD,bundle,EditPromoterFragment.this).forceLoad();
                    /*ivAadhar.setImageResource(R.drawable.aadhartick);
                    ivAadhar.setEnabled(false);*/
                }
            } else if (requestCode == AppsConstant.IMAGE_ADDRESS_PROOF) {
                final String responseValue = data.getStringExtra("response");
                final String purpose = data.getStringExtra("image_purpose");
                if (!responseValue.equals(null)) {
                    /*Toast.makeText(getContext(), responseValue, Toast.LENGTH_SHORT).show();
                    Log.d("path", responseValue);*/

                            Bundle bundle = new Bundle();
                            bundle.putString(AppsConstant.URL, Services.DomainUrlImage);
                            bundle.putString(AppsConstant.FILE, responseValue);
                            bundle.putString(AppsConstant.FILEPURPOSE,purpose);
                            getActivity().getLoaderManager().initLoader(LoaderConstant.IMAGE_UPLOAD,bundle,EditPromoterFragment.this).forceLoad();
                    i = 3;
                    //  image[2] = responseValue;
                    /*ivAddress.setImageResource(R.drawable.id_card_tick);
                    ivAddress.setEnabled(false);*/
                }
            }
        }
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ivPhoto:
                if (!promoter.getStatusId().equals(null) && !promoter.getStatusId().equalsIgnoreCase("ReqCompleted")) {
                    Intent i = new Intent(getActivity(), CameraActivity.class);
                    i.putExtra("camera_key",AppsConstant.FRONT_CAMREA_OPEN);
                    i.putExtra("purpose","ForPhoto");
                    startActivityForResult(i,AppsConstant.IMAGE_PHOTO);
                } else {
                    // just show previous image in new pop up
                    Dialog dialog = new Dialog(getContext());
                    dialog.setContentView(R.layout.promoter_image_fragment);
                    ImageView imageView = (ImageView) dialog.findViewById(R.id.ivPromoterImage);
                    Picasso.with(getContext()).load(UrlBuilder.getServerImage(promoter.getUserPhoto())).fit().into(imageView);
                }


                break;
            case R.id.ivAadhar:
                if (!promoter.getStatusId().equals(null) && !promoter.getStatusId().equalsIgnoreCase("ReqCompleted")) {
                    Intent i2 = new Intent(getActivity(),CameraActivity.class);
                    i2.putExtra("camera_key",BACK_CAMREA_OPEN);
                    i2.putExtra("purpose","ForAadhar");
                    startActivityForResult(i2,AppsConstant.IMAGE_AADHAR);
                } else {
                    Dialog dialog = new Dialog(getContext());
                    dialog.setContentView(R.layout.promoter_image_fragment);
                    ImageView imageView = (ImageView) dialog.findViewById(R.id.ivPromoterImage);
                    Picasso.with(getContext()).load(UrlBuilder.getServerImage(promoter.getAadharIdPath())).fit().into(imageView);
                }

                break;
            case R.id.ivAddressProof:
                if(!promoter.getStatusId().equals(null) && !promoter.getStatusId().equalsIgnoreCase("ReqCompleted")) {
                    Intent i3 = new Intent(getActivity(),CameraActivity.class);
                    i3.putExtra("camera_key",BACK_CAMREA_OPEN);
                    i3.putExtra("purpose","ForAddressProof");
                    startActivityForResult(i3,AppsConstant.IMAGE_ADDRESS_PROOF);
                } else {
                    Dialog dialog = new Dialog(getContext());
                    dialog.setContentView(R.layout.promoter_image_fragment);
                    ImageView imageView = (ImageView) dialog.findViewById(R.id.ivPromoterImage);
                    Picasso.with(getContext()).load(UrlBuilder.getServerImage(promoter.getAddressIdPath())).fit().into(imageView);
                }
                break;
        }
    }

    public boolean isNotNull(String firstName,String lastName, String phone,String email,String address,String storeAssign,String sEassign) {
        if(!TextUtils.isEmpty(firstName) && !TextUtils.isEmpty(lastName) &&
                !TextUtils.isEmpty(phone) && !TextUtils.isEmpty(email) &&
                !TextUtils.isEmpty(address) && !TextUtils.isEmpty(storeAssign) &&
                !TextUtils.isEmpty(sEassign)){
            return true;
        } else {
            return false;
        }
    }
}
