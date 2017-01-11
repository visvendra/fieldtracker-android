package com.oppo.sfamanagement.fragment;

import android.app.LoaderManager;
import android.content.DialogInterface;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.oppo.sfamanagement.MainActivity;
import com.oppo.sfamanagement.R;
import com.oppo.sfamanagement.database.AppsConstant;
import com.oppo.sfamanagement.database.CustomBuilder;
import com.oppo.sfamanagement.model.Leave;
import com.oppo.sfamanagement.model.LeaveReason;
import com.oppo.sfamanagement.model.LeaveReasonApply;
import com.oppo.sfamanagement.model.LeaveType;
import com.oppo.sfamanagement.webmethods.LoaderConstant;
import com.oppo.sfamanagement.webmethods.LoaderMethod;
import com.oppo.sfamanagement.webmethods.LoaderServices;
import com.oppo.sfamanagement.webmethods.ParameterBuilder;
import com.oppo.sfamanagement.webmethods.Services;
import com.oppo.sfamanagement.webmethods.UrlBuilder;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by allsmartlt218 on 09-01-2017.
 */

public class EditLeaveFragment  extends Fragment implements View.OnClickListener, LoaderManager.LoaderCallbacks<Object>, DatePickerDialog.OnDateSetListener {

    private TextView etStart,etEnd,etType,tvReasonType,tvDays;
    private ImageView ivStart,ivEnd;
    private Button submit,cancel;
    private String enumTypeId="";
    private String enumReasonId="";
    private EditText etReason;
    private String leaveReasonId = "" ;
    private ArrayList<LeaveType> leaveTypeList;
    private ArrayList<LeaveReason> leaveReasonList;
    private boolean isFrom = false, isTo = false;
    private int fromyear = 0,frommonth = 0,fromDay = 0;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_leave_fragment,container,false);
        etStart = (TextView) view.findViewById(R.id.etStartDate);
        etEnd = (TextView) view.findViewById(R.id.etEndDate);
        etReason = (EditText) view.findViewById(R.id.etReason);
        etType = (TextView) view.findViewById(R.id.etType);
        ivStart = (ImageView) view.findViewById(R.id.ivDatePicker);
        ivEnd = (ImageView) view.findViewById(R.id.ivDatePicker2);
        submit = (Button) view.findViewById(R.id.btSubmit);
        cancel = (Button) view.findViewById(R.id.btnCancel);
        tvDays = (TextView) view.findViewById(R.id.tvLeaveDays);
        tvReasonType = (TextView) view.findViewById(R.id.etTypeReason);

        final Leave leave = getArguments().getParcelable("leave_key");
        if(null != leave) {
            etStart.setText(leave.getFromDate());
            etEnd.setText(leave.getToDate());
            tvDays.setText(leave.getDays().replaceAll("[^0-9]", ""));
            etReason.setText(leave.getReason());
        }

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fromDate = etStart.getText().toString();
                String thruDate = etEnd.getText().toString();



                if (!TextUtils.isEmpty(enumReasonId) && !TextUtils.isEmpty(enumTypeId) && !TextUtils.isEmpty(leaveReasonId) && !TextUtils.isEmpty(leave.getPartyRelationShipId())) {
                    Bundle bundle = new Bundle();
                    bundle.putString(AppsConstant.URL, UrlBuilder.getUrl(Services.APPLY_LEAVES));
                    bundle.putString(AppsConstant.METHOD, AppsConstant.PUT);
                    bundle.putString(AppsConstant.PARAMS, ParameterBuilder.getApplyLeave(enumTypeId,enumReasonId,leaveReasonId,fromDate,thruDate,leave.getPartyRelationShipId()));
                    getActivity().getLoaderManager().initLoader(LoaderConstant.APPLY_LEAVE,bundle,EditLeaveFragment.this).forceLoad();
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                fm.popBackStackImmediate();
            }
        });
        etType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomBuilder builder = new CustomBuilder(getContext(),"Select Leave Type",true);
                builder.setSingleChoiceItems(leaveTypeList,etType.getTag(), new CustomBuilder.OnClickListener() {
                    @Override
                    public void onClick(CustomBuilder builder, Object selectedObject) {
                        etType.setTag(selectedObject);
                        etType.setText(((LeaveType) selectedObject).getTypeDescription());
                        enumTypeId = ((LeaveType) selectedObject).getEnumType();
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
        etStart.setOnClickListener(this);
        etEnd.setOnClickListener(this);
        ivStart.setOnClickListener(this);
        ivEnd.setOnClickListener(this);
        etType.setTag(new LeaveType());
        tvReasonType.setTag(new LeaveReason());
        tvReasonType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomBuilder builder = new CustomBuilder(getContext(),"Select Reason",true);
                builder.setSingleChoiceItems(leaveReasonList,tvReasonType.getTag(), new CustomBuilder.OnClickListener() {
                    @Override
                    public void onClick(CustomBuilder builder, Object selectedObject) {
                        tvReasonType.setTag(selectedObject);
                        tvReasonType.setText(((LeaveReason) selectedObject).getReasonDescription());
                        enumReasonId = ((LeaveReason) selectedObject).getEnumTypeReason();
                        leaveReasonId = tvReasonType.getText().toString();
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
        Bundle b = new Bundle();
        b.putString(AppsConstant.URL, UrlBuilder.getUrl(Services.LEAVE_TYPES));
        b.putString(AppsConstant.METHOD,AppsConstant.GET);
        getActivity().getLoaderManager().initLoader(LoaderConstant.LEAVE_TYPES,b,EditLeaveFragment.this).forceLoad();

        return view;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.etStartDate || v.getId() == R.id.ivDatePicker) {
            isFrom = true;
            Calendar now = Calendar.getInstance();
            DatePickerDialog dpd = DatePickerDialog.newInstance(
                    this,
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH) - 1
            );
            dpd.setAccentColor(getActivity().getResources().getColor(R.color.colorPrimaryDark));
            dpd.vibrate(true);
            dpd.show(getActivity().getFragmentManager(), "Datepickerdialog");
            dpd.setTitle("Select From Date");
            dpd.setMinDate(now);
        }
        if (v.getId() == R.id.etEndDate || v.getId() == R.id.ivDatePicker2) {
            if (isFrom) {
                if(!TextUtils.isEmpty(etEnd.getText().toString())) {
                    isFrom=false;
                    isTo = true;
                    Calendar now = Calendar.getInstance();
                    now.set(Calendar.YEAR,fromyear);
                    now.set(Calendar.MONTH,frommonth);
                    now.set(Calendar.DAY_OF_MONTH,fromDay);
                    DatePickerDialog dpd = DatePickerDialog.newInstance(
                            this,
                            now.get(Calendar.YEAR),
                            now.get(Calendar.MONTH),
                            now.get(Calendar.DAY_OF_MONTH) - 1
                    );
                    dpd.setAccentColor(getActivity().getResources().getColor(R.color.colorPrimaryDark));
                    dpd.vibrate(true);
                    dpd.show(getActivity().getFragmentManager(), "Datepickerdialog");
                    dpd.setTitle("Select To Date");
                    dpd.setMinDate(now);
                }
            } else {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog.setMessage("Please select from Date.");
                dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        // TODO Auto-generated method stub
                    }
                });
                dialog.show();
            }
        }
    }

    @Override
    public Loader<Object> onCreateLoader(int id, Bundle args) {
        ((MainActivity)getActivity()).showHideProgressForLoder(false);
        switch (id) {
            case LoaderConstant.LEAVE_TYPES:
                return new LoaderServices(getContext(), LoaderMethod.LEAVE_TYPES,args);
            case LoaderConstant.APPLY_LEAVE:
                return new LoaderServices(getContext(), LoaderMethod.APLLY_LEAVES,args);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Object> loader, Object data) {
        ((MainActivity)getActivity()).showHideProgressForLoder(true);

        switch (loader.getId()) {
            case LoaderConstant.LEAVE_TYPES:
                if(data != null && data instanceof ArrayList) {

                } else {

                }
                ArrayList<LeaveReasonApply> arrayList = (ArrayList<LeaveReasonApply>) data;
                LeaveReasonApply reasonApply = arrayList.get(0);
                leaveTypeList = reasonApply.getTypeList();
                leaveReasonList = reasonApply.getReasonList();
                break;
            case LoaderConstant.APPLY_LEAVE:
                if(data != null && data instanceof String) {
                    if (data!=null && data.equals("success")) {

                    }
                } else {

                }


        }
        getActivity().getLoaderManager().destroyLoader(loader.getId());
    }

    @Override
    public void onLoaderReset(Loader<Object> loader) {

    }

    public String getDays(String fromDate,String thruDate) {
        if(fromDate.length() == thruDate.length() && !TextUtils.isEmpty(fromDate) && !TextUtils.isEmpty(thruDate) && fromDate.length() == 10) {
            int thru = Integer.parseInt(thruDate.substring(8,10));
            int from = Integer.parseInt(fromDate.substring(8,10));
            if (from == thru) {
                return  "1";
            } else if((thru-from) < 0) {
                return "-";
            } else {
                return ((thru-from) + 1) + "";
            }

        } else {
            return "-";
        }
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        if(isFrom){
            fromyear =year;
            frommonth =monthOfYear;
            fromDay =dayOfMonth;
            monthOfYear = monthOfYear + 1;
            String selDate = year + "-" + (monthOfYear < 10 ? "0" + (monthOfYear) : monthOfYear) + "-" + (dayOfMonth < 10 ? "0" + dayOfMonth : "" + dayOfMonth);
            etStart.setText(selDate);
            //etEnd.setText("");
        }else{
            monthOfYear = monthOfYear + 1;
            String selDate = year + "-" + (monthOfYear < 10 ? "0" + (monthOfYear) : monthOfYear) + "-" + (dayOfMonth < 10 ? "0" + dayOfMonth : "" + dayOfMonth);
            etEnd.setText(selDate);
        }
        if(isTo) {
            tvDays.setText(getDays(etStart.getText().toString(),etEnd.getText().toString()));
        }
    }
}