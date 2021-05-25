package com.sbugert.rnadmob;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.UiThreadUtil;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class RNAdMobInterstitialAdModule extends ReactContextBaseJavaModule {

    public static final String REACT_CLASS = "RNAdMobInterstitial";

    public static final String EVENT_AD_LOADED = "interstitialAdLoaded";
    public static final String EVENT_AD_FAILED_TO_LOAD = "interstitialAdFailedToLoad";
    public static final String EVENT_AD_OPENED = "interstitialAdOpened";
    public static final String EVENT_AD_CLOSED = "interstitialAdClosed";
    public static final String EVENT_AD_LEFT_APPLICATION = "interstitialAdLeftApplication";

    InterstitialAd mInterstitialAd;
    FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {

        @Override
        public void onAdShowedFullScreenContent() {
            super.onAdShowedFullScreenContent();
            sendEvent(EVENT_AD_OPENED, null);
        }

        @Override
        public void onAdDismissedFullScreenContent() {
            super.onAdDismissedFullScreenContent();
            sendEvent(EVENT_AD_CLOSED, null);
        }
        //TODO: add events for other methods, TODO: sendEvent(EVENT_AD_LEFT_APPLICATION, null);
    };
    InterstitialAdLoadCallback interstitialAdLoadCallback = new InterstitialAdLoadCallback() {
        @Override
        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
            super.onAdLoaded(interstitialAd);
            interstitialAd.setFullScreenContentCallback(fullScreenContentCallback);
            mInterstitialAd = interstitialAd;
            isLoading = false;
            isLoaded = true;
            sendEvent(EVENT_AD_LOADED, null);
            mRequestAdPromise.resolve(null);
        }

        @Override
        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
            super.onAdFailedToLoad(loadAdError);
            isLoading = false;
            isLoaded = false; //TODO: ?
            WritableMap event = ErrorHandler.getErrorEvent(getReactApplicationContext(),loadAdError);
            sendEvent(EVENT_AD_FAILED_TO_LOAD, event);
            mRequestAdPromise.reject(event.getString("code"), event.getString("message"));
        }
    };


    private Promise mRequestAdPromise;
    private String adUnitID;
    private boolean isLoaded = false;
    private boolean isLoading = false;

    @Override
    public @NonNull String getName() {
        return REACT_CLASS;
    }

    public RNAdMobInterstitialAdModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    private void sendEvent(String eventName, @Nullable WritableMap params) {
        getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
    }

    @ReactMethod
    public void setAdUnitID(String adUnitID) {
        this.adUnitID = adUnitID;
    }

    @ReactMethod
    public void setTestDevices(ReadableArray testDevicesArray) {
        TestDevices.set(testDevicesArray);
    }

    @ReactMethod
    public void requestAd(final Promise promise) {
        UiThreadUtil.runOnUiThread(() -> {
            if (isLoaded || isLoading) {
                promise.reject("E_AD_ALREADY_LOADED", "Ad is already loaded.");
            } else {
                mRequestAdPromise = promise;
                AdRequest adRequest = new AdRequest.Builder().build();
                InterstitialAd.load(getReactApplicationContext(), adUnitID, adRequest, interstitialAdLoadCallback);
            }
        });
    }

    @ReactMethod
    public void showAd(final Promise promise) {
        UiThreadUtil.runOnUiThread(() -> {
            Activity currentActivity = getCurrentActivity();
            if (isLoaded && currentActivity != null) {
                mInterstitialAd.show(currentActivity);
                promise.resolve(null);
                isLoaded = false;
                isLoading = false;
            } else {
                promise.reject("E_AD_NOT_READY", "Ad is not ready.");
            }
        });
    }

    @ReactMethod
    public void isReady(final Callback callback) {
        UiThreadUtil.runOnUiThread(() -> callback.invoke(isLoaded));
    }
}
