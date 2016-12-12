package com.oppo.sfamanagement.webmethods;

import android.net.Uri;

/**
 * Created by AllSmart-LT008 on 8/5/2016.
 */
public class UrlBuilder {


    public static String getStoreDetails(String strServices, String Storeid) {
        Uri.Builder b = Uri.parse(getUrl(strServices)).buildUpon();
        b.appendPath(Storeid);
        b.build();
        return b.toString();
    }

    public static String getUrl(String strServices) {
        StringBuilder b = new StringBuilder();
        b.append(Services.DomainUrl).append(strServices);
        return b.toString();
    }

    public static String getStoreUpdate(String strServices, String productStoreId) {
        StringBuilder b = new StringBuilder();
        b.append(Services.DomainUrl).append(strServices).append("/" + productStoreId);
        return b.toString();
    }

    public static String getUpdatePromoter(String strService, String requestId) {
        StringBuilder b = new StringBuilder();
        b.append(Services.DomainUrl).append(strService).append("/" + requestId);
        return b.toString();
    }

    public static String getImageUrl(String strService) {
        StringBuilder b = new StringBuilder();
        b.append(Services.DomainUrlImage);
        return b.toString();
    }

}