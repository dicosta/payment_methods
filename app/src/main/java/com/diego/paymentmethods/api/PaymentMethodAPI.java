package com.diego.paymentmethods.api;

import android.net.Uri;

import com.diego.paymentmethods.domain.PaymentMethod;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PaymentMethodAPI {

    private final static String PUBLIC_KEY_PARAM = "public_key";

    private PaymentMethodAPIListener listener;

    public PaymentMethodAPI() {
    }

    public void setListener(PaymentMethodAPIListener listener) {
        this.listener = listener;
    }

    public void fetchPaymentMethods(String baseURL, String uri, String publicKey) {
        URL url = null;
        try {
            Uri paymentMethodsUri = Uri.parse(baseURL).buildUpon()
                    .appendPath(uri)
                    .appendQueryParameter(PUBLIC_KEY_PARAM, publicKey)
                    .build();

            url = new URL(paymentMethodsUri.toString());
        }
        catch (MalformedURLException e) {
            if (listener != null) {
                listener.onError(e.getMessage());
            }
            return;
        }

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            InputStream inputStream = new BufferedInputStream(connection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Type paymentListType = new TypeToken<ArrayList<PaymentMethod>>() {}.getType();
                List<PaymentMethod> paymentMethods = new Gson().fromJson(reader, paymentListType);

                if (listener != null) {
                    listener.onPaymentMethodsReceived(paymentMethods);
                }
            } else {
                if (listener != null) {
                    listener.onError("Unexpected Response Code: " + responseCode);
                }
            }
        }
        catch (Exception e) {
            if (listener != null) {
                listener.onError(e.getMessage());
            }
        }
        finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}


