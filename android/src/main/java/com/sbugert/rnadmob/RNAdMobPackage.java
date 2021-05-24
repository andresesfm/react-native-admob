package com.sbugert.rnadmob;

import android.content.Context;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;
import com.google.android.gms.ads.MobileAds;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class RNAdMobPackage implements ReactPackage {

    public RNAdMobPackage(@NotNull Context reactContext) {
        super();
        MobileAds.initialize(reactContext, initializationStatus -> {
            //TODO: set a flag to be accessed later?
            //TODO: Move to a @reactMethod?
        });
    }

    @Override
    public @NotNull List<NativeModule> createNativeModules(@NotNull ReactApplicationContext reactContext) {
        return Arrays.asList(
            new RNAdMobInterstitialAdModule(reactContext),
            new RNAdMobRewardedVideoAdModule(reactContext)
        );
    }

    @Override
    public @NotNull List<ViewManager> createViewManagers(@NotNull ReactApplicationContext reactContext) {
        return Arrays.asList(
            new RNAdMobBannerViewManager(),
            new RNPublisherBannerViewManager()
        );
    }
}
