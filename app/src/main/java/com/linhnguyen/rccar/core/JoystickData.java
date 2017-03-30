package com.linhnguyen.rccar.core;

/**
 * Created by linhn on 3/28/17.
 */
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