package com.diego.paymentmethods.view;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.diego.paymentmethods.R;
import com.diego.paymentmethods.domain.PaymentMethod;

import java.util.List;

public class PaymentMethodArrayAdapter extends ArrayAdapter<PaymentMethod> {
    private final List<PaymentMethod> paymentMethods;
    private final Activity context;

    public PaymentMethodArrayAdapter(Activity context, List<PaymentMethod> paymentMethods) {
        super(context, R.layout.view_payment_method_list_item, paymentMethods);

        this.context = context;
        this.paymentMethods = paymentMethods;
    }

    public List<PaymentMethod> getValues() {
        return this.paymentMethods;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;

        if (convertView == null) {
            view = context.getLayoutInflater().inflate(R.layout.view_payment_method_list_item, null);

            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.text = (TextView) view.findViewById(R.id.payment_method_name);
            view.setTag(viewHolder);
        } else {
            view = convertView;
        }

        ViewHolder holder = (ViewHolder) view.getTag();
        holder.text.setText(paymentMethods.get(position).getName());

        return view;
    }

    private static class ViewHolder {
        protected TextView text;
    }
}
