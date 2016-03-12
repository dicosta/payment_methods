package com.diego.paymentmethods.service;

import android.app.IntentService;
import android.content.Intent;

import android.support.v4.content.LocalBroadcastManager;

import com.diego.paymentmethods.R;
import com.diego.paymentmethods.api.PaymentMethodAPI;
import com.diego.paymentmethods.api.PaymentMethodAPIListener;
import com.diego.paymentmethods.domain.PaymentMethod;
import com.diego.paymentmethods.util.CreditCardStore;
import com.diego.paymentmethods.util.NetworkUtils;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;


import java.util.List;

public class CreditCardService extends IntentService {

    public static final String PROCESS_RESPONSE = "com.diego.paymentmethods.intent.action.PROCESS_RESPONSE";

    public static final String EXTRA_SHOW_CREDIT_CARDS = "com.diego.paymentmethods.intent.extra.show_creditCards";
    public static final String EXTRA_SHOW_ERROR = "com.diego.paymentmethods.intent.extra.show_errors";

    public CreditCardService() {
        super("CreditCardService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            if (NetworkUtils.isNetworkAvailable(getApplicationContext())) {
                final PaymentMethodAPI paymentMethodAPI = new PaymentMethodAPI();

                paymentMethodAPI.setListener(new PaymentMethodAPIListener() {

                    @Override
                    public void onPaymentMethodsReceived(List<PaymentMethod> paymentMethods) {
                        List<PaymentMethod> creditCards = null;

                        if (paymentMethods != null) {
                            creditCards = Lists.newArrayList(Collections2.filter(paymentMethods, new Predicate<PaymentMethod>() {
                                @Override
                                public boolean apply(PaymentMethod input) {
                                    return input.isCreditCard();
                                }
                            }));
                        }

                        if (creditCards != null && creditCards.size() > 0) {
                            CreditCardStore.storeCreditCards(getApplicationContext(), creditCards);

                            Intent intent = new Intent();
                            intent.setAction(PROCESS_RESPONSE);
                            intent.putExtra(EXTRA_SHOW_CREDIT_CARDS, creditCards.toArray(new PaymentMethod[creditCards.size()]));
                            LocalBroadcastManager.getInstance(CreditCardService.this).sendBroadcast(intent);
                        } else {
                            onError(getString(R.string.no_credit_cards));
                        }
                    }

                    @Override
                    public void onError(String error) {
                        CreditCardStore.storeCreditCardFetchError(getApplicationContext(), error);

                        Intent intent = new Intent();
                        intent.setAction(PROCESS_RESPONSE);
                        intent.putExtra(EXTRA_SHOW_ERROR, getString(R.string.service_error, error));

                        LocalBroadcastManager.getInstance(CreditCardService.this).sendBroadcast(intent);
                    }
                });

                paymentMethodAPI.fetchPaymentMethods(
                        getString(R.string.base_url),
                        getString(R.string.uri),
                        getString(R.string.public_key));
            } else {
                CreditCardStore.storeCreditCardFetchError(getApplicationContext(), getString(R.string.no_internet));

                Intent responseIntent = new Intent();
                responseIntent.setAction(PROCESS_RESPONSE);
                responseIntent.putExtra(EXTRA_SHOW_ERROR, getString(R.string.service_error, getString(R.string.no_internet)));

                LocalBroadcastManager.getInstance(this).sendBroadcast(responseIntent);
            }
        }
    }
}
