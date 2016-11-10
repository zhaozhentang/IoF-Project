package com.example.ioffalerts;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;

/**
 * Adapter to bind a ToDoItem List to a view
 */
public class ToDoItemAdapter extends ArrayAdapter<ToDoItem> {

    /**
     * Adapter context
     */
    Context mContext;

    /**
     * Adapter View layout
     */
    int mLayoutResourceId;

    public ToDoItemAdapter(Context context, int layoutResourceId) {
        super(context, layoutResourceId);

        mContext = context;
        mLayoutResourceId = layoutResourceId;
    }

    /**
     * Returns the view for a specific item on the list
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        final ToDoItem currentItem = getItem(position);

        if (row == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            row = inflater.inflate(mLayoutResourceId, parent, false);
        }

        row.setTag(currentItem);
        final TextView view = (TextView) row.findViewById(R.id.failist);
        final TextView view2 = (TextView)row.findViewById(R.id.devicename);
        final TextView view3 = (TextView)row.findViewById(R.id.dateTime);
        view.setText(currentItem.getText());
        view.setTag(String.valueOf(currentItem.getId()));
        view2.setText(currentItem.getDeviceName());
        view3.setText(getTimeNow(currentItem.getDateTime()));


        return row;
    }

    public String getTimeNow(Date currentTime) {

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy, hh:mm a");
        return formatter.format(currentTime);

    }

    }