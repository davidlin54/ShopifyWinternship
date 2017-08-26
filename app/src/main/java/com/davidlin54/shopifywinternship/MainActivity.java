package com.davidlin54.shopifywinternship;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String ENDPOINT = "https://shopicruit.myshopify.com/admin/orders.json?page=1&access_token=c32313df0d0ef512ca64d5b336a0d7c6";
    private static OkHttpClient mClient;
    private static Request mRequest;
    private ProgressBar mProgressBar;
    private LinearLayout mLayout;
    private TextView mSpentTextView, mSoldTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mLayout = (LinearLayout) findViewById(R.id.layout);
        mSpentTextView = (TextView) findViewById(R.id.tvSpent);
        mSoldTextView = (TextView) findViewById(R.id.tvSold);

        mClient = new OkHttpClient();
        mRequest = new Request.Builder()
                .url(ENDPOINT)
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLayout.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);


        mClient.newCall(mRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                double spent = 0;
                int bags = 0;
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray ordersArray = jsonObject.getJSONArray("orders");

                    for (int i = 0; i < ordersArray.length(); i++) {
                        JSONObject orderObject = ordersArray.getJSONObject(i);

                        JSONObject customersObject = orderObject.optJSONObject("customer");
                        if (customersObject != null) {
                            // check if Napoleon Batz purchased item
                            String first_name = customersObject.getString("first_name");
                            String last_name = customersObject.getString("last_name");

                            if ("Napoleon".equals(first_name) && "Batz".equals(last_name)) {
                                spent += ordersArray.getJSONObject(i).getDouble("total_price");
                            }
                        }

                        JSONArray itemsArray = orderObject.optJSONArray("line_items");
                        if (itemsArray != null) {
                            // check if any items are "Awesome Bronze Bag"
                            for (int j = 0; j < itemsArray.length(); j++) {
                                JSONObject itemObject = itemsArray.getJSONObject(j);
                                if ("Awesome Bronze Bag".equals(itemObject.getString("title"))) {
                                    bags += itemObject.getInt("quantity");
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                final double finalSpent = spent;
                final int finalBags = bags;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressBar.setVisibility(View.GONE);
                        mLayout.setVisibility(View.VISIBLE);
                        mSpentTextView.setText("$"+ finalSpent);
                        mSoldTextView.setText(finalBags +"");
                    }
                });
            }
        });
    }
}
