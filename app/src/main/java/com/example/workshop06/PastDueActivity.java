package com.example.workshop06;

import android.content.Intent;
import android.os.Bundle;

public class PastDueActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Redirect to InvoiceListActivity with pastDueFilter flag
        Intent intent = new Intent(this, InvoiceListActivity.class);
        intent.putExtra("pastDueFilter", "true");
        startActivity(intent);
        finish();
    }

    @Override
    protected void onRefresh() {}
}