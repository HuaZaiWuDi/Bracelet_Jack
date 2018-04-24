package com.lab.dxy.bracelet.core;

import com.lab.dxy.bracelet.Utils.L;
import com.lab.dxy.bracelet.ble.MyBle;

/**
 * 项目名称：Bracelet
 * 类描述：
 * 创建人：Jack
 * 创建时间：2017/9/19
 */
public class BleLongPackageThread extends Thread {
    String message;
    int itemNumber = 0;
    int sumNumber = 0;
    boolean sendSuccess = false;

    interface OnSendSuccessListener {
        void sendSuccess();
    }

    private OnSendSuccessListener onSendSuccessListener;

    public void setOnSendSuccessListener(OnSendSuccessListener onSendSuccessListener) {
        this.onSendSuccessListener = onSendSuccessListener;
    }

    public BleLongPackageThread(String message) {
        this.message = message;
        itemNumber = 0;
        sumNumber = 0;
        sendSuccess = false;
    }


    @Override
    public void run() {
        super.run();
        int packLength = message.length();
        byte[] data = message.getBytes();
        byte[] msgBytes = new byte[16];
        byte[] endBytes = new byte[packLength % 16];
        MyBle.getInstance().sendFirstPackage(packLength);

        sumNumber = packLength % 16 == 0 ? (packLength / 16) : (packLength / 16 + 1);
        try {
            sleep(200);

            while (true) {

                System.arraycopy(data, itemNumber * 16, msgBytes, 0, 16);
                if (itemNumber < sumNumber) {
                    MyBle.getInstance().sendMessagePackage(itemNumber, msgBytes);

                } else {
                    System.arraycopy(data, itemNumber * 16, endBytes, 0, endBytes.length);
                    MyBle.getInstance().sendEndPackage(endBytes);
                    sendSuccess = true;
                    startReceiver();
                    return;
                }
                itemNumber++;
                sleep(200);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    private void startReceiver() throws InterruptedException {
        while (sendSuccess) {
            MyBle.getInstance().readShake(data -> {
                if (data[1] == 0x05) {
                    byte[] bytes = new byte[15];
                    System.arraycopy(data, 5, bytes, 0, 15);
                    for (int i = 0; i < bytes.length; i++) {
                        for (int j = 0; j < 8; j++) {
                            if (bytes[i] % 2 != 0) {
                                int item = i * 8 + j + 1;
                                L.d("数据包：" + item);

                            }
                            bytes[i] = (byte) (bytes[i] >> 1);
                        }
                    }
                } else if (data[3] == (byte) 0xff && data[4] == (byte) 0xf0) {
                    L.d("包序号未成功添加。");
                }
            });

            sleep(200);
        }

    }

}
