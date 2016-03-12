package com.diego.paymentmethods.domain;

import com.google.gson.annotations.SerializedName;

/**
 * Created by diego on 09/03/16.
 */
public enum PaymentType {
    @SerializedName("credit_card")
    CREDIT_CARD,
    @SerializedName("ticket")
    TICKET,
    @SerializedName("atm")
    ATM
}
