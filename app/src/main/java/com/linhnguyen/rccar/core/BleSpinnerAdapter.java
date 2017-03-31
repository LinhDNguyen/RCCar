package com.linhnguyen.rccar.core;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Created by linhn on 3/30/17.
 */

public class BleSpinnerAdapter extends ArrayAdapter<BleDeviceHolder> {

    // Your sent context
    private Context context;
    // Your custom values for the spinner (User)
    private boolean hasData = false;

    public BleSpinnerAdapter(Context context, int textViewResourceId,
                             List<BleDeviceHolder> values) {
        super(context, textViewResourceId, values);
        this.context = context;
        if (values.size() <= 0) {
            hasData = true;
        }
    }

    @Override
    public void add(@Nullable BleDeviceHolder object) {
        boolean isExisted = false;
        for (int i = 0; i < getCount(); ++i) {
            BleDeviceHolder dev = getItem(i);
            if (dev.device.getAddress().equalsIgnoreCase(object.device.getAddress())) {
                isExisted = true;
                Log.d("BleSpinnerAdapter", "Ble device " + object.device.getAddress() + " existed.");
                break;
            }
        }

        if (!isExisted) {
            super.add(object);
            Log.d("BleSpinnerAdapter", "Add " + object.device.getName() + "[" + object.device.getAddress() + "] added");
        }
        hasData = true;
    }

    @Override
    public void clear() {
        super.clear();
        hasData = false;
    }
}