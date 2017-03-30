package com.linhnguyen.rccar.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.linhnguyen.rccar.R;
import com.zerokol.views.JoystickView;


public class OneFragment extends Fragment{
    TextView txtAngle;
    TextView txtPower;

    private JoystickView joystick;

    public OneFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_one, container, false);
        joystick = (JoystickView) view.findViewById(R.id.view_joystick);

        txtAngle = (TextView)view.findViewById(R.id.txtAngle);
        txtPower = (TextView)view.findViewById(R.id.txtPower);

        //Event listener that always returns the variation of the angle in degrees, motion power in percentage and direction of movement
        joystick.setOnJoystickMoveListener(new JoystickView.OnJoystickMoveListener() {

            @Override
            public void onValueChanged(int angle, int power, int direction) {
                txtAngle.setText(String.valueOf(angle));
                txtPower.setText(String.valueOf(power));
            }
        }, JoystickView.DEFAULT_LOOP_INTERVAL);

        return view;
    }

    public void display(int angle, int power) {
        if (txtAngle == null) return;
        txtAngle.setText(String.valueOf(angle));
        txtPower.setText(String.valueOf(power));
    }

    public void setDefault() {
        JoystickView joystick = (JoystickView)getView().findViewById(R.id.view_joystick);
        joystick.setDefault();
    }

}
