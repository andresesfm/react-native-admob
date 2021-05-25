package com.sbugert.rnadmob;

import android.content.Context;

import androidx.annotation.NonNull;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;
import com.google.android.gms.ads.MobileAds;

import java.util.Arrays;
import java.util.List;

public class RNAdMobPackage implements ReactPackage {

    public RNAdMobPackage(@NonNull Context reactContext) {
        super();
        MobileAds.initialize(reactContext, initializationStatus -> {
            //TODO: set a flag to be accessed later?
            //TODO: Move to a @reactMethod?
        });
    }

    @Override
    public @NonNull List<NativeModule> createNativeModules(@NonNull ReactApplicationContext reactContext) {
        return Arrays.asList(
            new RNAdMobInterstitialAdModule(reactContext),
            new RNAdMobRewardedVideoAdModule(reactContext)
        );
    }

    @Override
    public @NonNull List<ViewManager> createViewManagers(@NonNull ReactApplicationContext reactContext) {
        return Arrays.asList(
            new RNAdMobBannerViewManager(),
            new RNPublisherBannerViewManager()
        );
    }
}
