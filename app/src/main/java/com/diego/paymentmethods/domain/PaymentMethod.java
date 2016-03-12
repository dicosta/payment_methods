package com.diego.paymentmethods.domain;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by diego on 09/03/16.
 */

public class PaymentMethod implements Parcelable{

    @SerializedName("name")
    private String name;

    @SerializedName("payment_type_id")
    private PaymentType paymentType;

    public boolean isCreditCard() {
        return PaymentType.CREDIT_CARD.equals(paymentType);
    }

    public String getName() {
        return name;
    }

    protected PaymentMethod(Parcel in) {
        name = in.readString();
        paymentType = (PaymentType) in.readValue(PaymentType.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeValue(paymentType);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<PaymentMethod> CREATOR = new Parcelable.Creator<PaymentMethod>() {
        @Override
        public PaymentMethod createFromParcel(Parcel in) {
            return new PaymentMethod(in);
        }

        @Override
        public PaymentMethod[] newArray(int size) {
            return new PaymentMethod[size];
        }
    };
}
