package com.sbugert.rnadmob;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableNativeArray;
import com.facebook.react.bridge.ReadableNativeMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.PixelUtil;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.facebook.react.views.view.ReactViewGroup;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;
import com.google.android.gms.ads.admanager.AppEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import timber.log.Timber;

class ReactPublisherAdView extends ReactViewGroup {

    protected AdManagerAdView adView;

    AdSize[] validAdSizes;
    String adUnitID;
    AdSize adSize;
    Map<String, Object> customTargeting;
    AppEventListener appEventListener = (name, info) -> {
        WritableMap event = Arguments.createMap();
        event.putString("name", name);
        event.putString("info", info);
        sendEvent(RNPublisherBannerViewManager.EVENT_APP_EVENT, event);
    };

    public ReactPublisherAdView(final Context context) {
        super(context);
        this.createAdView();
    }

    private void createAdView() {
        if (this.adView != null) this.adView.destroy();

        final Context context = getContext();
        this.adView = new AdManagerAdView(context);

        this.adView.setAppEventListener(appEventListener);
        this.adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                AdSize adSize = adView.getAdSize();
                int width = adSize.getWidthInPixels(context);
                int height = adSize.getHeightInPixels(context);
                int left = adView.getLeft();
                int top = adView.getTop();
                adView.measure(width, height);
                adView.layout(left, top, left + width, top + height);
                sendOnSizeChangeEvent();
                sendEvent(RNPublisherBannerViewManager.EVENT_AD_LOADED, null);
            }

            @Override
            public void onAdFailedToLoad(@NonNull @NotNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                String errorMessage = "Unknown error";
                switch (loadAdError.getCode()) {
                    case AdManagerAdRequest.ERROR_CODE_INTERNAL_ERROR:
                        errorMessage = "Internal error, an invalid response was received from the ad server.";
                        break;
                    case AdManagerAdRequest.ERROR_CODE_INVALID_REQUEST:
                        errorMessage = "Invalid ad request, possibly an incorrect ad unit ID was given.";
                        break;
                    case AdManagerAdRequest.ERROR_CODE_NETWORK_ERROR:
                        errorMessage = "The ad request was unsuccessful due to network connectivity.";
                        break;
                    case AdManagerAdRequest.ERROR_CODE_NO_FILL:
                        errorMessage = "The ad request was successful, but no ad was returned due to lack of ad inventory.";
                        break;
                }
                WritableMap event = Arguments.createMap();
                WritableMap error = Arguments.createMap();
                error.putString("message", errorMessage);
                event.putMap("error", error);
                sendEvent(RNPublisherBannerViewManager.EVENT_AD_FAILED_TO_LOAD, event);
            }

            @Override
            public void onAdOpened() {
                sendEvent(RNPublisherBannerViewManager.EVENT_AD_OPENED, null);
            }

            @Override
            public void onAdClosed() {
                sendEvent(RNPublisherBannerViewManager.EVENT_AD_CLOSED, null);
            }

            //sendEvent(RNPublisherBannerViewManager.EVENT_AD_LEFT_APPLICATION, null);

        });
        this.addView(this.adView);
    }

    private void sendOnSizeChangeEvent() {
        int width;
        int height;
        ReactContext reactContext = (ReactContext) getContext();
        WritableMap event = Arguments.createMap();
        AdSize adSize = this.adView.getAdSize();
        if (adSize == AdSize.SMART_BANNER) {
            width = (int) PixelUtil.toDIPFromPixel(adSize.getWidthInPixels(reactContext));
            height = (int) PixelUtil.toDIPFromPixel(adSize.getHeightInPixels(reactContext));
        } else {
            width = adSize.getWidth();
            height = adSize.getHeight();
        }
        event.putDouble("width", width);
        event.putDouble("height", height);
        sendEvent(RNPublisherBannerViewManager.EVENT_SIZE_CHANGE, event);
    }

    private void sendEvent(String name, @Nullable WritableMap event) {
        ReactContext reactContext = (ReactContext) getContext();
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                getId(),
                name,
                event);
    }

    public void loadBanner() {
        ArrayList<AdSize> adSizes = new ArrayList<AdSize>();
        if (this.adSize != null) {
            adSizes.add(this.adSize);
        }
        if (this.validAdSizes != null) {
            adSizes.addAll(Arrays.asList(this.validAdSizes));
        }

        String slot = null;
        if (customTargeting != null) {
            Object slotObject = customTargeting.get("slot");
            if (slotObject != null) {
                slot = slotObject.toString();
            }
        }

        if (adSizes.size() == 0) {
            adSizes.add(AdSize.BANNER);
        }

        AdSize[] adSizesArray = adSizes.toArray(new AdSize[0]);
        this.adView.setAdSizes(adSizesArray);

        AdManagerAdRequest.Builder adRequestBuilder = new AdManagerAdRequest.Builder();

        if (customTargeting != null) {
            for (String key : customTargeting.keySet()) {
                adRequestBuilder.addCustomTargeting(key, String.valueOf(customTargeting.get(key)));
            }
        }
        AdManagerAdRequest adRequest = adRequestBuilder.build();
        this.adView.loadAd(adRequest);
    }

    public void setAdUnitID(String adUnitID) {
        if (this.adUnitID != null) {
            // We can only set adUnitID once, so when it was previously set we have
            // to recreate the view
            this.createAdView();
        }
        this.adUnitID = adUnitID;
        this.adView.setAdUnitId(adUnitID);
    }

    public void setAdSize(AdSize adSize) {
        this.adSize = adSize;
    }

    public void setValidAdSizes(AdSize[] adSizes) {
        this.validAdSizes = adSizes;
    }

    public void setCustomTargeting(Map<String, Object> customTargeting) {
        this.customTargeting = customTargeting;
    }

}

public class RNPublisherBannerViewManager extends ViewGroupManager<ReactPublisherAdView> {

    public static final String REACT_CLASS = "RNDFPBannerView";

    public static final String PROP_AD_SIZE = "adSize";
    public static final String PROP_VALID_AD_SIZES = "validAdSizes";
    public static final String PROP_AD_UNIT_ID = "adUnitID";
    public static final String PROP_TEST_DEVICES = "testDevices";
    public static final String PROP_CUSTOM_TARGETING = "customTargeting";

    public static final String EVENT_SIZE_CHANGE = "onSizeChange";
    public static final String EVENT_AD_LOADED = "onAdLoaded";
    public static final String EVENT_AD_FAILED_TO_LOAD = "onAdFailedToLoad";
    public static final String EVENT_AD_OPENED = "onAdOpened";
    public static final String EVENT_AD_CLOSED = "onAdClosed";
    public static final String EVENT_AD_LEFT_APPLICATION = "onAdLeftApplication";
    public static final String EVENT_APP_EVENT = "onAppEvent";

    public static final int COMMAND_LOAD_BANNER = 1;

    @Override
    public @NotNull String getName() {
        return REACT_CLASS;
    }

    @Override
    protected @NotNull ReactPublisherAdView createViewInstance(@NotNull ThemedReactContext themedReactContext) {
        return new ReactPublisherAdView(themedReactContext);
    }

    @Override
    public void addView(ReactPublisherAdView parent, View child, int index) {
        throw new RuntimeException("RNPublisherBannerView cannot have subviews");
    }

    @Override
    @Nullable
    public Map<String, Object> getExportedCustomDirectEventTypeConstants() {
        MapBuilder.Builder<String, Object> builder = MapBuilder.builder();
        String[] events = {
                EVENT_SIZE_CHANGE,
                EVENT_AD_LOADED,
                EVENT_AD_FAILED_TO_LOAD,
                EVENT_AD_OPENED,
                EVENT_AD_CLOSED,
                EVENT_AD_LEFT_APPLICATION,
                EVENT_APP_EVENT
        };
        for (String event : events) {
            builder.put(event, MapBuilder.of("registrationName", event));
        }
        return builder.build();
    }

    @ReactProp(name = PROP_AD_SIZE)
    public void setPropAdSize(final ReactPublisherAdView view, final String sizeString) {
        Timber.v("setPropAdSize for view " + view.hashCode() + ": " + sizeString);
        AdSize adSize = getAdSizeFromString(sizeString);
        view.setAdSize(adSize);
    }

    @ReactProp(name = PROP_VALID_AD_SIZES)
    public void setPropValidAdSizes(final ReactPublisherAdView view, final ReadableArray adSizeStrings) {
        Timber.v("setPropValidAdSizes for view " + view.hashCode() + ": " + adSizeStrings);
        ReadableNativeArray nativeArray = (ReadableNativeArray) adSizeStrings;
        ArrayList<Object> list = nativeArray.toArrayList();
        String[] adSizeStringsArray = list.toArray(new String[list.size()]);
        AdSize[] adSizes = new AdSize[list.size()];

        for (int i = 0; i < adSizeStringsArray.length; i++) {
            String adSizeString = adSizeStringsArray[i];
            adSizes[i] = getAdSizeFromString(adSizeString);
        }
        view.setValidAdSizes(adSizes);
    }

    @ReactProp(name = PROP_AD_UNIT_ID)
    public void setPropAdUnitID(final ReactPublisherAdView view, final String adUnitID) {
        Timber.v("setPropAdUnitID for view " + view.hashCode() + ": " + adUnitID);
        view.setAdUnitID(adUnitID);
    }

    @ReactProp(name = PROP_TEST_DEVICES)
    public void setPropTestDevices(final ReactPublisherAdView view, final ReadableArray testDevices) {
        TestDevices.set(testDevices);
    }

    @ReactProp(name = PROP_CUSTOM_TARGETING)
    public void setPropCustomTargeting(final ReactPublisherAdView view, final ReadableMap customTargeting) {
        Timber.v("setPropCustomTargeting for view " + view.hashCode() + ": " + customTargeting);
        ReadableNativeMap nativeMap = (ReadableNativeMap) customTargeting;
        Map<String, Object> map = nativeMap.toHashMap();
        view.setCustomTargeting(map);
    }

    private AdSize getAdSizeFromString(String adSize) {
        switch (adSize) {
            case "banner":
                return AdSize.BANNER;
            case "largeBanner":
                return AdSize.LARGE_BANNER;
            case "mediumRectangle":
                return AdSize.MEDIUM_RECTANGLE;
            case "fullBanner":
                return AdSize.FULL_BANNER;
            case "leaderBoard":
                return AdSize.LEADERBOARD;
            case "smartBannerPortrait":
                return AdSize.SMART_BANNER;
            case "smartBannerLandscape":
                return AdSize.SMART_BANNER;
            case "smartBanner":
                return AdSize.SMART_BANNER;
            default:
                AdSize customAdSize = RNCustomAdSize.parseCustomAdSize(adSize);
                return customAdSize == null ? AdSize.BANNER : customAdSize;
        }
    }

    @Nullable
    @Override
    public Map<String, Integer> getCommandsMap() {
        return MapBuilder.of("loadBanner", COMMAND_LOAD_BANNER);
    }

    @Override
    public void receiveCommand(ReactPublisherAdView root, int commandId, @javax.annotation.Nullable ReadableArray args) {
        switch (commandId) {
            case COMMAND_LOAD_BANNER:
                root.loadBanner();
                break;
        }
    }
}
