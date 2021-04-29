package com.example.kapture.fragments.HistoryHelper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kapture.R;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter<DataModel> implements View.OnClickListener{

    private ArrayList<DataModel> dataSet;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView eventTextView;
        TextView dateTextView;
        TextView timeTextView;
        ImageView capturedImage;
    }

    public CustomAdapter(ArrayList<DataModel> data, Context context) {
        super(context, R.layout.adapter_history, data);
        this.dataSet = data;
        this.mContext=context;

    }

    @Override
    public void onClick(View v) {

        int position=(Integer) v.getTag();
        Object object= getItem(position);
        DataModel dataModel=(DataModel)object;

        switch (v.getId())
        {
            case R.id.capturedImage:
                Snackbar.make(v, "Image " + dataModel.getEvent(), Snackbar.LENGTH_LONG)
                        .setAction("No action", null).show();
                break;
        }
    }

    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        DataModel dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.adapter_history, parent, false);
            viewHolder.eventTextView = (TextView) convertView.findViewById(R.id.eventTextView);
            viewHolder.dateTextView = (TextView) convertView.findViewById(R.id.dateTextView);
            viewHolder.timeTextView = (TextView) convertView.findViewById(R.id.timeTextView);
            viewHolder.capturedImage = (ImageView) convertView.findViewById(R.id.capturedImage);

            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        //Animation animation = AnimationUtils.loadAnimation(mContext, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
        //result.startAnimation(animation);
        lastPosition = position;

        viewHolder.eventTextView.setText(dataModel.getEvent());
        viewHolder.dateTextView.setText(dataModel.getDate());
        viewHolder.timeTextView.setText(dataModel.getTime());
        viewHolder.capturedImage.setOnClickListener(this);
        viewHolder.capturedImage.setTag(position);
        // Return the completed view to render on screen
        return convertView;
    }
}

