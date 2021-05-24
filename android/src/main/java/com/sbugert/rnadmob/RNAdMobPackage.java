package com.sbugert.rnadmob;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RNAdMobPackage implements ReactPackage {

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
