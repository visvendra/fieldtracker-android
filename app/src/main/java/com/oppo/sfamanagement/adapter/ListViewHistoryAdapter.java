package com.oppo.sfamanagement.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.oppo.sfamanagement.R;
import com.oppo.sfamanagement.model.History;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by allsmartlt218 on 01-12-2016.
 */

public class ListViewHistoryAdapter extends ArrayAdapter<History>{

    protected ArrayList<History> list;
    protected int resourceId;
    protected Activity activity;

    public ListViewHistoryAdapter(Activity activity, int resource, ArrayList<History> list) {
        super(activity, resource, list);
        this.activity = activity;
        this.resourceId = resource;
        this.list = list;
    }

    @Nullable
    public History getItem(History position) {
        return position;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public long getItemId(int position) {

        return super.getItemId(position);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = convertView;
        if (rowView == null) {
            rowView = inflater.inflate(resourceId,parent,false);
        }

        History history = list.get(position);

        TextView tvDate = (TextView) rowView.findViewById(R.id.tvHistoryDate);
        TextView tvTimeIn = (TextView) rowView.findViewById(R.id.tvHistoryTimeIn);
        TextView tvTimeOut = (TextView) rowView.findViewById(R.id.tvHistoryTimeOut);
        TextView tvTime = (TextView) rowView.findViewById(R.id.tvHistoryTime);
        tvDate.setText(history.getDate());
        tvTimeIn.setText(history.getTimeIn());
        tvTimeOut.setText(history.getTimeOut());
        tvTime.setText(history.getTime());
        return rowView;
    }
}