package com.linhnguyen.rccar.core;

import android.bluetooth.BluetoothDevice;

/**
 * Created by linhn on 3/30/17.
 */

public class BleDeviceHolder{
    BluetoothDevice device;
    public BleDeviceHolder(BluetoothDevice device) {
        this.device = device;
    }

    @Override
    public String toString() {
        String s = device.getName();

        if ((s == null) || (s.isEmpty())) {
            s = device.getAddress();
        }
        return s;
    }

    public BluetoothDevice getDevice() {
        return device;
    }
}
