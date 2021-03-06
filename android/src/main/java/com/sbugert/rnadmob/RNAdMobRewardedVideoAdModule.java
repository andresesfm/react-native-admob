package com.sbugert.rnadmob;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.UiThreadUtil;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

public class RNAdMobRewardedVideoAdModule extends ReactContextBaseJavaModule {

    public static final String REACT_CLASS = "RNAdMobRewarded";

    public static final String EVENT_AD_LOADED = "rewardedVideoAdLoaded";
    public static final String EVENT_AD_FAILED_TO_LOAD = "rewardedVideoAdFailedToLoad";
    public static final String EVENT_AD_OPENED = "rewardedVideoAdOpened";
    public static final String EVENT_AD_CLOSED = "rewardedVideoAdClosed";
    public static final String EVENT_AD_LEFT_APPLICATION = "rewardedVideoAdLeftApplication";
    public static final String EVENT_REWARDED = "rewardedVideoAdRewarded";
    public static final String EVENT_VIDEO_STARTED = "rewardedVideoAdVideoStarted";
    public static final String EVENT_VIDEO_COMPLETED = "rewardedVideoAdVideoCompleted";

    RewardedAd mRewardedAd;
    String adUnitID;
    boolean isLoaded = false;
    RewardedAdLoadCallback rewardedAdLoadCallback = new RewardedAdLoadCallback() {
        @Override
        public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
            super.onAdLoaded(rewardedAd);
            isLoaded = true;
            rewardedAd.setFullScreenContentCallback(fullScreenContentCallback);
            mRewardedAd = rewardedAd;
            sendEvent(EVENT_AD_LOADED, null);
            mRequestAdPromise.resolve(null);
        }

        @Override
        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
            super.onAdFailedToLoad(loadAdError);
            isLoaded = false;

            WritableMap event = ErrorHandler.getErrorEvent(getReactApplicationContext(),loadAdError);
            sendEvent(EVENT_AD_FAILED_TO_LOAD, event);
            mRequestAdPromise.reject(event.getString("code"), event.getString("message"));
        }

    };

    FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
        @Override
        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
            super.onAdFailedToShowFullScreenContent(adError);
        }

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

        @Override
        public void onAdImpression() {
            super.onAdImpression();
        }
    };
    //TODO: sendEvent(EVENT_VIDEO_STARTED, null);
    // sendEvent(EVENT_AD_LEFT_APPLICATION, null);
    // sendEvent(EVENT_VIDEO_COMPLETED, null);

    OnUserEarnedRewardListener onUserEarnedRewardListener = rewardItem -> {
        WritableMap reward = Arguments.createMap();

        reward.putInt("amount", rewardItem.getAmount());
        reward.putString("type", rewardItem.getType());

        sendEvent(EVENT_REWARDED, reward);
    };

    private Promise mRequestAdPromise;

    @Override
    public @NonNull String getName() {
        return REACT_CLASS;
    }

    public RNAdMobRewardedVideoAdModule(ReactApplicationContext reactContext) {
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
            if (isLoaded) {
                promise.reject("E_AD_ALREADY_LOADED", "Ad is already loaded.");
            } else {
                mRequestAdPromise = promise;

                AdRequest adRequest = new AdRequest.Builder().build();
                RewardedAd.load(getReactApplicationContext(), adUnitID, adRequest, rewardedAdLoadCallback);
            }
        });
    }

    @ReactMethod
    public void showAd(final Promise promise) {
        UiThreadUtil.runOnUiThread(() -> {
            Activity currentActivity = getCurrentActivity();
            if (currentActivity != null && isLoaded) {
                mRewardedAd.show(currentActivity, onUserEarnedRewardListener);
                promise.resolve(null);
                isLoaded = false;

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
