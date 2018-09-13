package com.louisgeek.mymqttserverraw.tool;

import android.os.Build;

/**
 * Created by Classichu on 2017-7-17.
 */

public class DeviceTool {


    /**
     * 获取设备的系统版本号
     */
    public static int getSDK_INT() {
        int sdk = Build.VERSION.SDK_INT;
        return sdk;
    }

    /**
     * 获取设备的厂商 e.g.Xiaomi
     */
    public static String getManufacturer() {
        String manufacturer = Build.MANUFACTURER;
        return manufacturer;
    }

    /**
     * 获取设备的型号  e.g.MI2SC
     */
    public static String getModel() {
        String model = Build.MODEL;
        return model;
    }


    public static String getSerial() {
        String result;
        if (Build.VERSION.SDK_INT >= 26) {
            result = Build.getSerial();
        } else {
            result = Build.SERIAL;
        }
        return result;
    }

}
