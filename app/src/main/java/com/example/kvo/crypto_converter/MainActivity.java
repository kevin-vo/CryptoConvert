package com.example.kvo.crypto_converter;

import android.os.AsyncTask;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Spinner spinner = (Spinner) findViewById(R.id.spinner);
        final Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
        final EditText amount1 = (EditText) findViewById(R.id.editText);
        final EditText amount2 = (EditText) findViewById(R.id.editText2);


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.currencies_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(1);

        adapter = ArrayAdapter.createFromResource(this,
                R.array.currencies_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(adapter);
        spinner2.setSelection(0);




        btcToUsdRate = 11038.00;
        ethToUsdRate = 1054.83;
        ripToUsdRate = 1.29;
        carToUsdRate = 0.67;
        steToUsdRate = 0.63;
        ltcToUsdRate = 187.35;
        dogToUsdRate = 0.01;

        currencyToUsd = new HashMap<String, Double>();
        currencyToUsd.put("USD", 1.0);
        currencyToUsd.put("Bitcoin", btcToUsdRate);
        currencyToUsd.put("Ethereum", ethToUsdRate);
        currencyToUsd.put("Ripple", ripToUsdRate);
        currencyToUsd.put("Cardano", carToUsdRate);
        currencyToUsd.put("Stellar", steToUsdRate);
        currencyToUsd.put("Litecoin", ltcToUsdRate);
        currencyToUsd.put("Dogecoin", dogToUsdRate);

        Log.d("Starting", "this");
        updateWithApi();

        Log.d("hello", "onCreate: everythinggood");


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (amount1.getText().length() != 0) {
                    double from = Double.parseDouble(amount1.getText().toString());
                    String curr1 = spinner.getSelectedItem().toString();
                    String curr2 = spinner2.getSelectedItem().toString();
                    double to = convert(from, curr1, curr2);
                    amount2.setText(moneyFormat(to));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (amount1.getText().length() != 0) {
                    double from = Double.parseDouble(amount1.getText().toString());
                    String curr1 = spinner.getSelectedItem().toString();
                    String curr2 = spinner2.getSelectedItem().toString();
                    double to = convert(from, curr1, curr2);
                    amount2.setText(moneyFormat(to));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        amount1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (getCurrentFocus() == amount1) {
                    if (amount1.getText().length() == 0 || amount1.getText().toString().equals(".")) {
                        amount2.setText("");
                    } else {
                        double from = Double.parseDouble(amount1.getText().toString());
                        String curr1 = spinner.getSelectedItem().toString();
                        String curr2 = spinner2.getSelectedItem().toString();
                        double to = convert(from, curr1, curr2);
                        amount2.setText(moneyFormat(to));
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        amount2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (getCurrentFocus() == amount2) {
                    if (amount2.getText().length() == 0 || amount2.getText().toString().equals(".")) {
                        amount1.setText("");
                    } else {
                        double from = Double.parseDouble(amount2.getText().toString());
                        String curr1 = spinner.getSelectedItem().toString();
                        String curr2 = spinner2.getSelectedItem().toString();
                        double to = convert(from, curr2, curr1);
                        amount1.setText(moneyFormat(to));
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    public void updateWithApi() {
        final HashSet<String> compatibleCurrencies = new HashSet<String>();
        String[] x = {"Bitcoin", "Ethereum", "Ripple", "Cardano", "Stellar", "Litecoin", "Dogecoin"};
        for (String i : x) {
            compatibleCurrencies.add(i);
        }
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    URL coinsApi = new URL("https://api.coinmarketcap.com/v1/ticker/?limit=41");
                    HttpsURLConnection myConnection =
                            (HttpsURLConnection) coinsApi.openConnection();
                    myConnection.setRequestProperty("User-Agent", "crypto_app");

                    currencyToUsd = new HashMap<String, Double>();

                    if (myConnection.getResponseCode() == 200) {
                        Log.d("YESSS", "SUCCESSSSSS");
                        InputStream responseBody = myConnection.getInputStream();
                        InputStreamReader responseBodyReader =
                                new InputStreamReader(responseBody, "UTF-8");
                        JsonReader jsonReader = new JsonReader(responseBodyReader);

                        List<Message> messages = new ArrayList<Message>();
                        jsonReader.beginArray();
                        double btcVal = 0;
                        double ethVal = 0;

                        while (jsonReader.hasNext()) {
                            HashMap<String, Double> mp = readMessage(jsonReader);
                            Iterator<String> it = mp.keySet().iterator();
                            String key = it.next();
                            if (compatibleCurrencies.contains(key)) {
                                currencyToUsd.put(key, mp.get(key));
                                Log.d(key, Double.toString(mp.get(key)));
                            }

                        }

                    } else {
                        Log.d("AHHHHHHHHHH", "FAILLLLLLLL");
                    }
                } catch (IOException e) {
                    Log.d("MISERABLE", "FAIL");
                    e.printStackTrace();
                    currencyToUsd = new HashMap<String, Double>();
                    currencyToUsd.put("USD", 1.0);
                    currencyToUsd.put("Bitcoin", btcToUsdRate);
                    currencyToUsd.put("Ethereum", ethToUsdRate);
                    currencyToUsd.put("Ripple", ripToUsdRate);
                    currencyToUsd.put("Cardano", carToUsdRate);
                    currencyToUsd.put("Stellar", steToUsdRate);
                    currencyToUsd.put("Litecoin", ltcToUsdRate);
                    currencyToUsd.put("Dogecoin", dogToUsdRate);
                }
            }
        });
    }

    public void refreshExchange(View view) {
        updateWithApi();
    }

    public HashMap<String, Double> readMessage(JsonReader reader) throws IOException {
        String id = "";
        Double price_usd = null;
        HashMap<String, Double> mp = new HashMap<String, Double>();
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("name")) {
                id = reader.nextString();
            } else if (name.equals("price_usd")) {
                price_usd = reader.nextDouble();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        mp.put(id, price_usd);
        return mp;
    }

    private String moneyFormat(double val) {
        DecimalFormat df = new DecimalFormat("#0.00");
        return df.format(val);
    }

    private double convert(Double from, String curr1, String curr2) {
        if (curr1.equals(curr2)) {
            return from;
        }
        if (curr1.equals("USD") && curr2.equals("USD")) {
            return from;
        }
        if (curr1.equals("USD")) {
            return from / currencyToUsd.get(curr2);
        }
        if (curr2.equals("USD")) {
            return from * currencyToUsd.get(curr1);
        }
        return convert(from * currencyToUsd.get(curr1), "USD", curr2);

    }

    private double btcToUsdRate;
    private double ethToUsdRate;
    private double ripToUsdRate;
    private double carToUsdRate;
    private double steToUsdRate;
    private double ltcToUsdRate;
    private double dogToUsdRate;

    private HashMap<String, Double> currencyToUsd;

}
