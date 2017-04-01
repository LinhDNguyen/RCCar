package com.linhnguyen.rccar.fragments;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.linhnguyen.rccar.R;
import com.linhnguyen.rccar.activity.MainActivity;
import com.linhnguyen.rccar.core.BleDeviceHolder;
import com.linhnguyen.rccar.core.BleSpinnerAdapter;
import com.linhnguyen.rccar.core.IOnListChanged;
import com.linhnguyen.rccar.core.IOnScrollEnable;
import com.linhnguyen.rccar.core.OnJoystickListener;
import com.linhnguyen.rccar.service.BluetoothLeService;
import com.zerokol.views.JoystickView;

import java.util.ArrayList;


public class OneFragment extends Fragment{
    TextView txtAngle;
    TextView txtPower;
    Button btnScan;
    Button btnConnect;
    private BleSpinnerAdapter adapter;
    private Spinner bleSpinner;
    BluetoothDevice connectedBle = null;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private boolean mConnected = false;

    private JoystickView joystick;

    private IOnScrollEnable iOnScrollEnable;
    private int prevAngle = 0;
    private int prevPower = 0;

    // Stops scanning after 20 seconds.
    private static final long SCAN_PERIOD = 20000;

    public OneFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_one, container, false);
        joystick = (JoystickView) view.findViewById(R.id.view_joystick);

        txtAngle = (TextView)view.findViewById(R.id.txtAngle);
        txtPower = (TextView)view.findViewById(R.id.txtPower);
        btnScan = (Button) view.findViewById(R.id.btnScan);
        btnConnect = (Button) view.findViewById(R.id.btnConnect);
        bleSpinner = (Spinner) view.findViewById(R.id.spnBle);

        //Event listener that always returns the variation of the angle in degrees, motion power in percentage and direction of movement
        joystick.setOnJoystickMoveListener(new JoystickView.OnJoystickMoveListener() {

            @Override
            public void onValueChanged(int angle, int power, int direction) {
                txtAngle.setText(String.valueOf(angle));
                txtPower.setText(String.valueOf(power));
            }
        }, JoystickView.DEFAULT_LOOP_INTERVAL);

        if (scanListener != null) btnScan.setOnClickListener(scanListener);
        if (connectListener != null) btnConnect.setOnClickListener(connectListener);

        adapter = new BleSpinnerAdapter(getActivity().getApplicationContext(),
                android.R.layout.simple_spinner_item,
                new ArrayList<BleDeviceHolder>());
        bleSpinner.setAdapter(adapter);

        joystick.setOnJoystickUpListener(iOnJoystickListener);

        adapter.setOnChangedListener(listChanged);

        joystick.setOnJoystickMoveListener(mJoystickListener, JoystickView.DEFAULT_LOOP_INTERVAL);

        return view;
    }

    private JoystickView.OnJoystickMoveListener mJoystickListener = new JoystickView.OnJoystickMoveListener() {
        @Override
        public void onValueChanged(int angle, int power, int direction) {
            int fAngle = 0;
            int fPower = 0;

            if (power > 40) {
                if ((angle >=45) && (angle < 135)) {
                    fAngle = 90;
                } else if ((angle >= 135) && (angle < 225)) {
                    fAngle = 180;
                } else if ((angle >= 225) && (angle < 315)) {
                    fAngle = 270;
                } else {
                    fAngle = 0;
                }
                if (power > 90) {
                    fPower = 100;
                } else if (power > 80) {
                    fPower = 90;
                } else if (power > 70) {
                    fPower = 80;
                } else {
                    fPower = 70;
                }
            }

            if ((fAngle != prevAngle) || (fPower != prevPower)) {
                prevAngle = fAngle;
                prevPower = fPower;
            } else {
                // No change, return
                return;
            }

            byte[] data = new byte[4];
            data[1] = (byte)((fAngle & 0xff00) >> 8);
            data[0] = (byte)(fAngle & 0xff);
            data[3] = 0;
            data[2] = (byte)(fPower);
            MainActivity avt = (MainActivity)getActivity();
            avt.broadcastUpdate(BluetoothLeService.RCCAR_MOVE_DATA, data);
            Log.i("OneFragment", "joystick changed " + String.valueOf(fAngle) + ":" + String.valueOf(fPower));
        }
    };

    public void display(int angle, int power) {
        if (txtAngle == null) return;
        txtAngle.setText(String.valueOf(angle));
        txtPower.setText(String.valueOf(power));
    }

    public void setDefault() {
        JoystickView joystick = (JoystickView)getView().findViewById(R.id.view_joystick);
        joystick.setDefault();
    }

    public Button getBtnScan() {return btnScan;}
    public Button getBtnConnect() {return btnConnect;}

    public void setScanListener(View.OnClickListener listener) {scanListener = listener;}
    public void setConnectListener(View.OnClickListener listener) {connectListener = listener;}

    public void bleListClear() {
        adapter.clear();
    }

    public void setBleList(ArrayList<BleDeviceHolder> devices) {
        if (adapter == null) { return; }

        adapter.clear();
        for (BleDeviceHolder dev:devices) {
            adapter.add(dev);
        }
    }

    public BluetoothDevice getSelectedDevice() {
        BluetoothDevice res = null;

        if ((adapter == null) || (adapter.getCount() == 0)) {
            return res;
        }

        res = ((BleDeviceHolder)bleSpinner.getSelectedItem()).getDevice();

        return res;
    }

    public BluetoothDevice getConnectedDevice() {
        return connectedBle;
    }

    public void addBleToList(BleDeviceHolder dev) {
        if (adapter == null) {return;}

        adapter.add(dev);
    }

    private View.OnClickListener connectListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Button btn = (Button)v;
            MainActivity atv = (MainActivity)getActivity();
            BluetoothLeService bleSrv = atv.getBleService();
            // Start scan
            Log.i("MainActivity", btn.getText() + " clicked, current device: " + getSelectedDevice().getAddress());

            if (btn.getText().equals("Connect")) {
                // Connect BLE device
                bleSrv.connect(getSelectedDevice().getAddress());

                // Done, change text
                onBleConnected();
            } else {
                // Disconnect Ble device

                bleSrv.disconnect();

                // Done, change text
                onBleDisconnected();
            }
        }
    };

    public void onBleConnected() {
        btnConnect.setText("Disconnect");
        setScanState(false);
        btnScan.setText("Scan");
        bleSpinner.setEnabled(false);
        final BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();
        scanner.stopScan(mLeScanCallback);
        mHandler.removeCallbacksAndMessages(null);
        joystick.setEnabled(true);
        mConnected = true;
    }

    public void onBleDisconnected() {
        btnConnect.setText("Connect");
        setScanState(true);
        bleSpinner.setEnabled(true);
        joystick.setEnabled(false);
        mConnected = false;
    }

    private void setScanState(boolean isIdle) {
        btnScan.setEnabled(isIdle);
        if (!isIdle) {
            btnScan.setText("Scanning...");
        } else {
            btnScan.setText("Scan");
        }
    }

    public void scanLeDevice(final boolean enable) {
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        final BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();
        if (enable) {
            setScanState(false);
            adapter.clear();
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanner.stopScan(mLeScanCallback);
                    setScanState(true);
                }
            }, SCAN_PERIOD);

            scanner.startScan(mLeScanCallback);
        } else {
            setScanState(true);
            scanner.stopScan(mLeScanCallback);
        }
    }

    private ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.i("OneFragment", "Scan failed " + String.valueOf(errorCode));
        }

        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            super.onScanResult(callbackType, result);

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("OneFragment", "run: lescan got device: " + result.getDevice().toString());
                    BleDeviceHolder dev = new BleDeviceHolder(result.getDevice());
                    addBleToList(dev);
                }
            });
        }
    };

    private View.OnClickListener scanListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Start scan
            Log.i("MainActivity", "Scan clicked");
            scanLeDevice(true);
        }
    };

    public boolean isConnected() {
        return mConnected;
    }

    private Handler mTouchHandler = new Handler();
    private OnJoystickListener iOnJoystickListener = new OnJoystickListener() {
        @Override
        public void onTouchUp(boolean isTouch) {
            if (iOnScrollEnable == null) {
                return;
            }

            if (isTouch) {
                iOnScrollEnable.onEnable(true);
            } else {
                mTouchHandler.removeCallbacksAndMessages(null);
                mTouchHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        iOnScrollEnable.onEnable(false);
                    }
                }, 300);
            }
        }
    };

    public void setScrollEnable(IOnScrollEnable listener) {
        iOnScrollEnable = listener;
    }

    private IOnListChanged listChanged = new IOnListChanged() {
        @Override
        public void onListChanged(int itemCount) {
            Log.d("OneFragment", "list changed to " + String.valueOf(itemCount) + " items");
            if (itemCount > 0) {
                btnConnect.setEnabled(true);
            } else {
                btnConnect.setEnabled(false);
            }
        }
    };
}
