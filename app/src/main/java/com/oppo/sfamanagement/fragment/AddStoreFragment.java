package com.oppo.sfamanagement.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.oppo.sfamanagement.R;
import com.oppo.sfamanagement.adapter.ListViewPromoterListAdapter;

import java.util.ArrayList;

/**
 * Created by allsmartlt218 on 02-12-2016.
 */

public class AddStoreFragment extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_store_fragment,container,false);
        return view;
    }
}