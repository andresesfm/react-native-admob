package com.sbugert.rnadmob;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableNativeArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

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
        public void onAdLoaded(@NonNull @NotNull InterstitialAd interstitialAd) {
            super.onAdLoaded(interstitialAd);
            interstitialAd.setFullScreenContentCallback(fullScreenContentCallback);
            mInterstitialAd = interstitialAd;
            isLoading=false;
            isLoaded = true;
            sendEvent(EVENT_AD_LOADED, null);
            mRequestAdPromise.resolve(null);
        }

        @Override
        public void onAdFailedToLoad(@NonNull @NotNull LoadAdError loadAdError) {
            super.onAdFailedToLoad(loadAdError);
            isLoading=false;
            isLoaded = false; //TODO
            String errorString = "ERROR_UNKNOWN";
            String errorMessage = "Unknown error";
            switch (loadAdError.getCode()) {
                case AdRequest.ERROR_CODE_INTERNAL_ERROR:
                    errorString = "ERROR_CODE_INTERNAL_ERROR";
                    errorMessage = "Internal error, an invalid response was received from the ad server.";
                    break;
                case AdRequest.ERROR_CODE_INVALID_REQUEST:
                    errorString = "ERROR_CODE_INVALID_REQUEST";
                    errorMessage = "Invalid ad request, possibly an incorrect ad unit ID was given.";
                    break;
                case AdRequest.ERROR_CODE_NETWORK_ERROR:
                    errorString = "ERROR_CODE_NETWORK_ERROR";
                    errorMessage = "The ad request was unsuccessful due to network connectivity.";
                    break;
                case AdRequest.ERROR_CODE_NO_FILL:
                    errorString = "ERROR_CODE_NO_FILL";
                    errorMessage = "The ad request was successful, but no ad was returned due to lack of ad inventory.";
                    break;
            }
            WritableMap event = Arguments.createMap();
            WritableMap error = Arguments.createMap();
            event.putString("message", errorMessage);
            sendEvent(EVENT_AD_FAILED_TO_LOAD, event);
            mRequestAdPromise.reject(errorString, errorMessage);
        }
    };


    private Promise mRequestAdPromise;
    private String adUnitID;
    private boolean isLoaded=false;
    private boolean isLoading=false;

    @Override
    public String getName() {
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
        ReadableNativeArray nativeArray = (ReadableNativeArray)testDevicesArray;
        ArrayList<String> testDevices  =new ArrayList<>();
        for (int i = 0; i < nativeArray.size(); i++) {
            String testDevice = testDevicesArray.getString(i);;
            if (testDevice.equals("EMULATOR") || testDevice.equals("SIMULATOR")) {
                testDevice = AdRequest.DEVICE_ID_EMULATOR;
            }
            testDevices.add(testDevice);
        }

        RequestConfiguration configuration =
            new RequestConfiguration.Builder().setTestDeviceIds(testDevices).build();
        MobileAds.setRequestConfiguration(configuration);
    }

    @ReactMethod
    public void requestAd(final Promise promise) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (isLoaded || isLoading) {
                promise.reject("E_AD_ALREADY_LOADED", "Ad is already loaded.");
            } else {
                mRequestAdPromise = promise;
                AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
                AdRequest adRequest = adRequestBuilder.build();
                InterstitialAd.load(getReactApplicationContext(),adUnitID,adRequest,interstitialAdLoadCallback);
            }
        });
    }

    @ReactMethod
    public void showAd(final Promise promise) {
        new Handler(Looper.getMainLooper()).post(() -> {
            Activity currentActivity = getCurrentActivity();
            if (isLoaded && currentActivity!= null) {
                mInterstitialAd.show(currentActivity);
                promise.resolve(null);
            } else {
                promise.reject("E_AD_NOT_READY", "Ad is not ready.");
            }
        });
    }

    @ReactMethod
    public void isReady(final Callback callback) {
        new Handler(Looper.getMainLooper()).post(() -> callback.invoke(isLoaded));
    }
}
