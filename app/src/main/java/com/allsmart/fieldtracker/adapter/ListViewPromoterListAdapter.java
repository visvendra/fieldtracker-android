package com.allsmart.fieldtracker.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.allsmart.fieldtracker.R;
import com.allsmart.fieldtracker.model.Promoter;

import java.util.ArrayList;

/**
 * Created by allsmartlt218 on 02-12-2016.
 */

public class ListViewPromoterListAdapter extends ArrayAdapter<Promoter> {

    protected Activity activity;
    protected int resourceId;
    protected ArrayList<Promoter> list;

    public ListViewPromoterListAdapter(Activity activity, int resource, ArrayList<Promoter> list) {
        super(activity,resource,list);
        this.activity = activity;
        this.resourceId = resource;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }
    @Override
    public Promoter getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = convertView;
        if (view == null) {
           view = inflater.inflate(resourceId,parent,false);
        }
        TextView tvPromoter = (TextView) view.findViewById(R.id.tvPromoterItem);
        TextView tvStatus = (TextView) view.findViewById(R.id.tvStatusId);
        TextView storeName = (TextView) view.findViewById(R.id.tvStoreName);

        Promoter p = getItem(position);
        storeName.setText(p.getStoreName());
        tvPromoter.setText(p.getFirstName() + " " + p.getLastName());
        if(!p.getStatusId().equals(null)) {
            if(p.getStatusId().equals("ReqCompleted")) {
                tvStatus.setText("Approved");
            } else if (p.getStatusId().equals("ReqRejected")) {
                tvStatus.setText("Rejected");
            } else {
                tvStatus.setText("Pending");
            }
        }
        return view;
    }

    public void refresh(ArrayList<Promoter> list) {
        this.list = list;
        this.notifyDataSetChanged();
    }
}
