package ru.mjdelbarrio.stormy;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
         public static final String TAG = MainActivity.class.getSimpleName();
    private CurrentWeather  mCurrentWeather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String apiKey="365a313be6edbcb984a3a0613cd11ec5";
        double latitude =37.8267;
        double longitude = -122.423;
        String forecastUrl="https://api.forecast.io/forecast/" + apiKey +"/" + latitude + "," + longitude;
          if (isNetworkAvailable()) {
        OkHttpClient client =new OkHttpClient();
        Request request = new Request.Builder()
                .url(forecastUrl)
                .build();
        Call call =client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String jsonData =response.body().string();
                         Log.v(TAG,jsonData);
                    if (response.isSuccessful()) {
                        mCurrentWeather=getCurrentDetails(jsonData);
                    }else
                    {
                         alertUserAboutError();
                    }

                } catch (IOException e) {
                    Log.e(TAG, "Exception caught", e);
                }  catch (JSONException e)
                {
                    Log.e(TAG, "Exception caught", e);
                }

            }

    });
          }
        else {
              Toast.makeText(this,getString(R.string.network_unavailable), Toast.LENGTH_LONG).show();
          }
        Log.d(TAG, "Main UI is running");
}

    private CurrentWeather getCurrentDetails(String jsonData) throws JSONException {
        JSONObject forecast =new JSONObject(jsonData);
        String timeZone = forecast.getString("timezone");
       Log.i(TAG, "From JSON: " + timeZone);
        return new CurrentWeather();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo  networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable=false;
        if (networkInfo!=null && networkInfo.isConnected()){
            isAvailable=true;
        }
        return isAvailable;
    }

    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "error_dialog");
    }

}