package com.example.kapture.fragments;

import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.kapture.fragments.HistoryHelper.CustomAdapter;
import com.example.kapture.fragments.HistoryHelper.DataModel;
import com.example.kapture.fragments.HistoryHelper.DatabaseHelper;
import com.example.kapture.R;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class HistoryFragment extends Fragment {

    ArrayList<DataModel> dataModels;
    private ListView mListView;
    private static CustomAdapter adapter;

    private Button deleteAllBtn;
    private static final String TAG = "HistoryFragment";
    DatabaseHelper mDatabaseHelper;
    Fragment frg;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        frg = this;

        mListView = (ListView) getActivity().findViewById(R.id.listViewHistory);
        mDatabaseHelper = new DatabaseHelper(this.getActivity());

        deleteAllBtn = getActivity().findViewById(R.id.deleteAllBtn);
        deleteAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatabaseHelper.deleteAll();
                refreshFragment();
            }
        });

        populateHistory();
    }

    private void populateHistory(){
        Log.d(TAG, "populateHistory: Displaying data in the ListView.");

        Cursor data = mDatabaseHelper.getData();
        dataModels = new ArrayList<>();
        while (data.moveToNext()){
            dataModels.add(new DataModel(data.getString(1), data.getString(2), data.getString(3))); //COL1
        }

        for (DataModel model:dataModels) {
            System.out.println(model);
        }

        //ArrayAdapter adapter = new ArrayAdapter<DatabaseHelper>(this.getActivity(), R.layout.adapter_history, listData);
        //ArrayAdapter<String> adapter = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_list_item_1, dataModels);

        adapter = new CustomAdapter(dataModels, getContext());
        mListView.setAdapter(adapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                DataModel dataModel= dataModels.get(position);

                Snackbar.make(view, dataModel.getEvent() + "\n"+dataModel.getDate() + "\t" + dataModel.getTime(), Snackbar.LENGTH_LONG)
                        .setAction("No action", null).show();
            }
        });

    }

    private void refreshFragment(){
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(frg).attach(frg).commit();
    }

    private void toastMessage(String message){
        Toast.makeText(this.getActivity(), message, Toast.LENGTH_SHORT).show();
    }
}