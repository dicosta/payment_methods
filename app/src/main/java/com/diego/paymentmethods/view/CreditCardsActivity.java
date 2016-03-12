package com.diego.paymentmethods.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.diego.paymentmethods.R;
import com.diego.paymentmethods.domain.PaymentMethod;
import com.diego.paymentmethods.service.CreditCardService;
import com.diego.paymentmethods.util.CreditCardStore;
import com.google.common.collect.Lists;

import java.util.List;

public class CreditCardsActivity extends AppCompatActivity {

    enum CreditCardsActivityState { INITIAL, RUNNING, COMPLETED }

    private static final String ACTIVITY_STATE_KEY = "CREDIT_CARDS_ACTIVITY_STATE";
    private static final String ADAPTER_VALUES = "ADAPTER_VALUES";

    private Button loadButton;
    private ProgressBar loadingProgress;
    private ListView paymentMethodListView;
    private CreditCardsActivityState creditCardsActivityState;
    private PaymentMethodArrayAdapter creditCardsAdapter;

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(CreditCardService.EXTRA_SHOW_CREDIT_CARDS)) {
                Parcelable[] values = intent.getParcelableArrayExtra(CreditCardService.EXTRA_SHOW_CREDIT_CARDS);
                PaymentMethod[] paymentMethods = new PaymentMethod[values.length];
                System.arraycopy(values, 0, paymentMethods, 0, values.length);
                loadCreditCardsListView(Lists.newArrayList(paymentMethods));

                changeCreditCardsActivityState(CreditCardsActivityState.COMPLETED);

            } else if (intent.hasExtra(CreditCardService.EXTRA_SHOW_ERROR)) {
                changeCreditCardsActivityState(CreditCardsActivityState.INITIAL);

                String errorMessage = intent.getStringExtra(CreditCardService.EXTRA_SHOW_ERROR);
                Toast.makeText(CreditCardsActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_credit_cards);

        loadingProgress = (ProgressBar)findViewById(R.id.loadingProgress);
        paymentMethodListView = (ListView)findViewById(R.id.paymentMethodsListView);
        loadButton = (Button)findViewById(R.id.loadButton);
        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreditCardStore.clearCreditCards(getApplicationContext());
                CreditCardStore.clearCreditCardFetchError(getApplicationContext());

                Intent intent = new Intent(CreditCardsActivity.this, CreditCardService.class);
                startService(intent);

                changeCreditCardsActivityState(CreditCardsActivityState.RUNNING);
            }
        });

        if (savedInstanceState != null) {
            //restore the persisted state.
            changeCreditCardsActivityState((CreditCardsActivityState) savedInstanceState.get(ACTIVITY_STATE_KEY));

            //if we already got the credit cards data, recreate the adapter
            if (CreditCardsActivityState.COMPLETED.equals(creditCardsActivityState)) {
                Parcelable[] values = savedInstanceState.getParcelableArray(ADAPTER_VALUES);
                PaymentMethod[] paymentMethods = new PaymentMethod[values.length];
                System.arraycopy(values, 0, paymentMethods, 0, values.length);

                loadCreditCardsListView(Lists.newArrayList(paymentMethods));

            } else if (CreditCardsActivityState.RUNNING.equals(creditCardsActivityState)) {
                //in case activity destroyed when sent to background: if it was running, we have to check on store to see
                // if the data or error arrived when the activity in background.
                handleResultsArrivedInBackground();
            }
        } else {
            changeCreditCardsActivityState(CreditCardsActivityState.INITIAL);
        }
    }

    private void handleResultsArrivedInBackground() {
        String fetchError = CreditCardStore.getCreditCardFetchError(getApplicationContext());

        if (!TextUtils.isEmpty(fetchError)) {
            changeCreditCardsActivityState(CreditCardsActivityState.INITIAL);
            Toast.makeText(CreditCardsActivity.this, fetchError, Toast.LENGTH_SHORT).show();
        } else {
            List<PaymentMethod> storedCreditCards = CreditCardStore.getStoredCreditCards(getApplicationContext());
            if (storedCreditCards != null) {
                changeCreditCardsActivityState(CreditCardsActivityState.COMPLETED);
                loadCreditCardsListView(storedCreditCards);
            }
        }
    }

    private void loadCreditCardsListView(List<PaymentMethod> creditCardList) {
        creditCardsAdapter = new PaymentMethodArrayAdapter(this, creditCardList);
        paymentMethodListView.setAdapter(creditCardsAdapter);
    }

    private void changeCreditCardsActivityState(CreditCardsActivityState newState) {
        creditCardsActivityState = newState;

        switch (creditCardsActivityState) {
            case INITIAL: {
                loadButton.setVisibility(View.VISIBLE);
                loadingProgress.setVisibility(View.GONE);
                paymentMethodListView.setVisibility(View.GONE);
                break;
            }
            case RUNNING: {
                loadButton.setVisibility(View.GONE);
                loadingProgress.setVisibility(View.VISIBLE);
                paymentMethodListView.setVisibility(View.GONE);
                break;
            }
            case COMPLETED: {
                loadButton.setVisibility(View.GONE);
                loadingProgress.setVisibility(View.GONE);
                paymentMethodListView.setVisibility(View.VISIBLE);
                break;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(ACTIVITY_STATE_KEY, creditCardsActivityState);

        //if it was already completed, store the data to reload listview.
        if (CreditCardsActivityState.COMPLETED.equals(creditCardsActivityState)) {
            outState.putParcelableArray(ADAPTER_VALUES, creditCardsAdapter.getValues().toArray(new PaymentMethod[creditCardsAdapter.getValues().size()]));
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(CreditCardService.PROCESS_RESPONSE));

        //if it was running, we have to check on store to see if the data or an error arrived when the activity
        //was paused.
        if (CreditCardsActivityState.RUNNING.equals(creditCardsActivityState)) {
            handleResultsArrivedInBackground();
        }
    }
}
