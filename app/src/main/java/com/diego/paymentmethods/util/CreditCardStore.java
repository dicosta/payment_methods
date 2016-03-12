package com.diego.paymentmethods.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.diego.paymentmethods.domain.PaymentMethod;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CreditCardStore {

    private final static String CREDIT_CARDS_KEY = "payment_methods";
    private final static String CREDIT_CARDS_FETCH_ERROR = "fetch_error";

    public static void storeCreditCards(Context context, List<PaymentMethod> paymentMethods) {
        String stringData = new Gson().toJson(paymentMethods);

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(CREDIT_CARDS_KEY, stringData);
        editor.apply();
    }

    public static void clearCreditCards(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().remove(CREDIT_CARDS_KEY).commit();
    }

    public static List<PaymentMethod> getStoredCreditCards(Context context) {
        String stringData = PreferenceManager.getDefaultSharedPreferences(context).getString(CREDIT_CARDS_KEY, "");
        if (!TextUtils.isEmpty(stringData)) {
            Type paymentListType = new TypeToken<ArrayList<PaymentMethod>>() {}.getType();
            return new Gson().fromJson(stringData, paymentListType);
        }

        return null;
    }

    public static void storeCreditCardFetchError(Context context, String errorMessage) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(CREDIT_CARDS_FETCH_ERROR, errorMessage);
        editor.apply();
    }

    public static void clearCreditCardFetchError(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().remove(CREDIT_CARDS_FETCH_ERROR).commit();
    }

    public static String getCreditCardFetchError(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(CREDIT_CARDS_FETCH_ERROR, "");
    }
}
