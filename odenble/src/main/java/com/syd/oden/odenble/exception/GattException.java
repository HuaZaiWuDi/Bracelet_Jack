package com.syd.oden.odenble.exception;

/**
 * 项目名称：OdenDemo
 * 类描述：
 * 创建人：oden
 * 创建时间：2016/6/12 16:58
 */
public class GattException extends BleException {
    private int gattStatus;

    public GattException(int gattStatus) {
        super(ERROR_CODE_GATT, "Gatt Exception Occurred! ");
        this.gattStatus = gattStatus;
    }


    public int getGattStatus() {
        return gattStatus;
    }

    public GattException setGattStatus(int gattStatus) {
        this.gattStatus = gattStatus;
        return this;
    }

    @Override
    public String toString() {
        return "GattException{" +
                "gattStatus=" + gattStatus +
                "} " + super.toString();
    }
}
