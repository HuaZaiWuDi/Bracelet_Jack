package com.syd.oden.odenble.listener;

/**
 * 项目名称：MeshLed
 * 类描述：
 * 创建人：oden
 * 创建时间：2016/6/24 10:24
 */
public interface BleDisconnectListener {
    void onBleDisconnect(boolean isDiscoverServiceFail);
}
