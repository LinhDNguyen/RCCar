package com.example.linh.rccar;

import com.zerokol.views.JoystickView;
import com.zerokol.views.JoystickView.OnJoystickMoveListener;
import android.annotation.SuppressLint;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Build;
import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.widget.Toast;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.UUID;

import android.util.Log;
import android.content.pm.PackageManager;
import android.content.Context;
import android.content.Intent;

class JoystickData {
    protected int power;
    protected int angle;
    protected int direction;
    protected boolean is_resent;

    public JoystickData(int power, int angle, int direction) {
        this.power = power;
        this.angle = angle;
        this.direction = direction;
        this.is_resent = false;
    }

    public void setIs_resent(boolean is_resent) {
        this.is_resent = is_resent;
    }

    public boolean is_resent() {
        return is_resent;
    }

    public int getAngle() {
        return angle;
    }

    public int getDirection() {
        return direction;
    }

    public int getPower() {
        return power;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public void setPower(int power) {
        this.power = power;
    }
}

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class JoystickActivity extends AppCompatActivity {
    // Importing also other views
    private JoystickView joystick;
    private TextView angleTextView;
    private TextView powerTextView;
    private TextView directionTextView;

    private Spinner spnBleList;
    private Button btnConnect;

    private BluetoothAdapter mBluetoothAdapter;
    private int REQUEST_ENABLE_BT = 1;
    private Handler mHandler;
    private static final long SCAN_PERIOD = 10000;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private BluetoothGatt mGatt;
    private BluetoothGattService mGattService;
    private BluetoothGattCharacteristic mGattChar;

    private List<BluetoothDevice> bleDevices;

    private JoystickData lastFailed = null;

    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            if ((lastFailed != null) && (!lastFailed.is_resent())) {
                lastFailed.setIs_resent(true);
                bleWrite(0, 0);
            }
        }
    };

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.bleDevices = new ArrayList<BluetoothDevice>();
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        // Init BLE
        spnBleList = (Spinner) findViewById(R.id.bleList);
        mHandler = new Handler();
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE Not Supported",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Init joystick
        mContentView = findViewById(R.id.fullscreen_content);
        joystick = (JoystickView) mContentView;
        angleTextView = (TextView) findViewById(R.id.txtAngle);
        powerTextView = (TextView) findViewById(R.id.txtPower);
        directionTextView = (TextView) findViewById(R.id.txtDirection);
        this.btnConnect = (Button) findViewById(R.id.btnConnect);

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
                Button btn = (Button)v;
                Log.i("btnConnect:onClick", "Clicked, txt: " + btn.getText());
                if (btn.getText().equals("Connect")) {
                    // connect BLE
                    bleConnect();
                } else {
                    // Disconnect BLE
                    bleDisconnect();
                }
            }
        });

        //Event listener that always returns the variation of the angle in degrees, motion power in percentage and direction of movement
        joystick.setOnJoystickMoveListener(new OnJoystickMoveListener() {

            @Override
            public void onValueChanged(int angle, int power, int direction) {
                if (mGatt == null) {
                    return;
                }
                angleTextView.setText(" Angle: " + String.valueOf(angle) + "°");
                powerTextView.setText(" Power: " + String.valueOf(power) + "%");
                String directionStr = "Direction: ";
                switch (direction) {
                    case JoystickView.FRONT:
                        directionStr += "Front";
                        break;
                    case JoystickView.FRONT_RIGHT:
                        directionStr += "Front Right";
                        break;
                    case JoystickView.RIGHT:
                        directionStr += "Right";
                        break;
                    case JoystickView.RIGHT_BOTTOM:
                        directionStr += "Right Bottom";
                        break;
                    case JoystickView.BOTTOM:
                        directionStr += "Bottom";
                        break;
                    case JoystickView.BOTTOM_LEFT:
                        directionStr += "Bottom Left";
                        break;
                    case JoystickView.LEFT:
                        directionStr += "Left";
                        break;
                    case JoystickView.LEFT_FRONT:
                        directionStr += "Left Front";
                        break;
                    default:
                        directionStr += "CENTER";
                }
                directionTextView.setText(directionStr);
                bleWrite(angle, power);
            }
        }, JoystickView.DEFAULT_LOOP_INTERVAL);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            if (Build.VERSION.SDK_INT >= 21) {
                mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build();
                filters = new ArrayList<ScanFilter>();
            }
            scanLeDevice(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            scanLeDevice(false);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mGatt == null) {
            super.onDestroy();
            return;
        }
        mGatt.close();
        mGatt = null;
        mGattChar = null;
        mGattService = null;

        super.onDestroy();
    }

    private void bleWrite(int angle, int power) {
        if (mGattChar == null) {
            return;
        }

        // Write characteristic
        short a = (short)angle;
        short p = (short)power;

        byte[] value = new byte[4];
        value[1] = (byte)(a >> 8);
        value[0] = (byte)(a & 0xff);
        value[3] = (byte)(p >> 8);
        value[2] = (byte)(p & 0xff);
        mGattChar.setValue(value);
        boolean res = mGatt.writeCharacteristic(mGattChar);
        if (!res) {
            Log.e("bleWrite", String.format("write a: %d, p: %d failed", a, p));
            this.lastFailed = new JoystickData(power, angle, 0);
            if ((power == 0) && (angle == 0)) {
                timerHandler.removeCallbacks(timerRunnable);
                timerHandler.postDelayed(timerRunnable, 5);
            }
            return;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                //Bluetooth not enabled.
                finish();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void scanLeDevice(final boolean enable) {

        if (enable) {
            this.bleDevices.clear();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT < 21) {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    } else {
                        mLEScanner.stopScan(mScanCallback);

                    }
                }
            }, SCAN_PERIOD);
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            } else {
                mLEScanner.startScan(filters, settings, mScanCallback);
            }
        } else {
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            } else {
                mLEScanner.stopScan(mScanCallback);
            }
        }
    }


    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i("callbackType", String.valueOf(callbackType));
            Log.i("result", result.toString());
            BluetoothDevice btDevice = result.getDevice();
            addBle(btDevice);
            // Disable scan
            scanLeDevice(false);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.i("ScanResult - Results", sr.toString());
                BluetoothDevice btDevice = sr.getDevice();
                addBle(btDevice);
            }
            // Disable scan
            scanLeDevice(false);
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    };

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i("onLeScan", device.toString());
                            addBle(device);
                            // Disable scan
                            scanLeDevice(false);
                        }
                    });
                }
            };

    public void bleConnect() {
        BluetoothDevice device = this.bleDevices.get(this.spnBleList.getSelectedItemPosition());
        if (mGatt == null) {
            mGatt = device.connectGatt(this, false, gattCallback);
            scanLeDevice(false);// will stop after first device detection
        }
    }
    public void bleDisconnect() {
        if (mGatt != null) {
            mGatt.disconnect();
            mGatt = null;
        }
    }

    public void updateButton(final boolean val) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(val) {
                    btnConnect.setText("Disconnect");
                } else {
                    btnConnect.setText("Connect");
                }
            }
        });
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback", "STATE_CONNECTED");
                    updateButton(true);
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    updateButton(false);
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> services = gatt.getServices();
            Log.i("onServicesDiscovered", services.toString());

            mGattService = gatt.getService(UUID.fromString("12345678-1234-5678-1234-56789abcdef0"));
            if (mGattService == null) {
                Log.e("onServicesDiscovered", "RC CAR service not found!!!");
                return;
            }
            Log.i("onServicesDiscovered", "RC CAR service: " + mGattService.toString());

            mGattChar = mGattService.getCharacteristic(UUID.fromString("12345678-1234-5678-1234-56789abcdef1"));
            if (mGattChar == null) {
                Log.e("onServicesDiscovered", "RC CAR characteristic not found!!!");
                return;
            }
            Log.i("onServicesDiscovered", "RC CAR characteristic: " + mGattChar.toString());

            boolean res = gatt.readCharacteristic(mGattChar);
            Log.i("onServicesDiscovered", String.format("Read characteristic: %s", res));
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {
            Log.i("onCharacteristicRead", characteristic.getValue().toString());
        }
    };

    public void updateBleList() {
        List<String> list = new ArrayList<String>();

        for (Iterator<BluetoothDevice> iter = this.bleDevices.iterator(); iter.hasNext(); ) {
            list.add(iter.next().getName());
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnBleList.setAdapter(dataAdapter);
    }

    public void addBle(BluetoothDevice dev) {
        boolean isExist = false;
        for (Iterator<BluetoothDevice> iter = this.bleDevices.iterator(); iter.hasNext(); ) {
            BluetoothDevice btdev = iter.next();
            Log.d("addBle", dev.getName() + " - " + btdev.getName());
            if (dev.getName().equals(btdev.getName())) {
                isExist = true;
                break;
            }
        }

        if (!isExist) {
            Log.d("addBle - add", dev.getName());
            this.bleDevices.add(dev);
        }
        updateBleList();
    }
}
