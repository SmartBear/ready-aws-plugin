package com.smartbear.rapisupport;

import com.eviware.soapui.SoapUI;

//wrapper around deprecated SoapUI class
public final class RapiLogger {
    public static void log(String message) {
        SoapUI.log(message);
    }

    public static void logError(Throwable e) {
        SoapUI.logError(e);
    }

    public static void logError(Throwable e, String message) {
        SoapUI.logError(e, message);
    }
}
