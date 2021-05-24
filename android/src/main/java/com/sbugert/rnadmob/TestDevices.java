package com.sbugert.rnadmob;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableNativeArray;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;

import java.util.ArrayList;

public class TestDevices {
    static void set(ReadableArray testDevicesArray) {
        ReadableNativeArray nativeArray = (ReadableNativeArray) testDevicesArray;
        ArrayList<String> testDevices = new ArrayList<>();
        for (int i = 0; i < nativeArray.size(); i++) {
            String testDevice = testDevicesArray.getString(i);
            if (testDevice.equals("EMULATOR") || testDevice.equals("SIMULATOR")) {
                testDevice = AdRequest.DEVICE_ID_EMULATOR;
            }
            testDevices.add(testDevice);
        }

        RequestConfiguration configuration =
            new RequestConfiguration.Builder().setTestDeviceIds(testDevices).build();
        MobileAds.setRequestConfiguration(configuration);
    }
}
