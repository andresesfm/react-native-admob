package com.sbugert.rnadmob;

import android.content.Context;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;

class ErrorHandler {
    private ErrorHandler() {
    }

    public static WritableMap getErrorEvent(Context context, LoadAdError loadAdError) {
        String errorString = "ERROR_UNKNOWN";
        String errorMessage =context.getString(R.string.admob_unknown_error);
        switch (loadAdError.getCode()) {
            case AdRequest.ERROR_CODE_INTERNAL_ERROR:
                errorString = "ERROR_CODE_INTERNAL_ERROR";
                errorMessage = context.getString(R.string.admob_internal_error);
                break;
            case AdRequest.ERROR_CODE_INVALID_REQUEST:
                errorString = "ERROR_CODE_INVALID_REQUEST";
                errorMessage = context.getString(R.string.admob_invalid_error);
                break;
            case AdRequest.ERROR_CODE_NETWORK_ERROR:
                errorString = "ERROR_CODE_NETWORK_ERROR";
                errorMessage = context.getString(R.string.admob_network_error);
                break;
            case AdRequest.ERROR_CODE_NO_FILL:
                errorString = "ERROR_CODE_NO_FILL";
                errorMessage = context.getString(R.string.admob_no_fill);
                break;
        }
        WritableMap event = Arguments.createMap();
        WritableMap error = Arguments.createMap();
        error.putString("message", errorMessage);
        event.putString("code", errorString);
        event.putMap("error", error);
        return event;
    }
}
