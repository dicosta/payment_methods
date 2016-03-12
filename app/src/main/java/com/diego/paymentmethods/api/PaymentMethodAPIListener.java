package com.diego.paymentmethods.api;

import com.diego.paymentmethods.domain.PaymentMethod;

import java.util.List;

public interface PaymentMethodAPIListener {

    void onPaymentMethodsReceived(List<PaymentMethod> paymentMethods);

    void onError(String error);
}
