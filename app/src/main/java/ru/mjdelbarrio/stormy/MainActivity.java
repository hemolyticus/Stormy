package ru.mjdelbarrio.stormy;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
         public static final String TAG = MainActivity.class.getSimpleName();
    private CurrentWeather  mCurrentWeather;
    @BindView(R.id.lblTime) TextView mTimeLabel;
    @BindView(R.id.lblTemperature) TextView mTemperatureLabel;
    @BindView(R.id.valHumidity) TextView mHumidityValue;
    @BindView(R.id.lblPrecipValue) TextView mPrecipValue;
    @BindView(R.id.lblSummary) TextView mSummaryLabel;
    @BindView(R.id.imgViewIcon) ImageView mIconImageView;
    @BindView(R.id.imgViewRefresh)  ImageView mRefreshView;
    @BindView(R.id.progressBar)ProgressBar mProgressBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mProgressBar.setVisibility(View.INVISIBLE);

        final double latitude =-41.28664;
        final double longitude = 174.7757;
        mRefreshView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getForecast(latitude,longitude);
            }


        });
        getForecast(latitude,longitude);
        Log.d(TAG, "Main UI is running");
}

    private void getForecast(double latitude, double longitude) {
        String apiKey="365a313be6edbcb984a3a0613cd11ec5";

        String forecastUrl="https://api.forecast.io/forecast/" + apiKey +"/" + latitude + "," + longitude;
        if (isNetworkAvailable()) {
            toggleRefresh();
      OkHttpClient client =new OkHttpClient();
      Request request = new Request.Builder()
              .url(forecastUrl)
              .build();
      Call call =client.newCall(request);
      call.enqueue(new Callback() {
          @Override
          public void onFailure(Call call, IOException e) {
              runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                      toggleRefresh();
                  }
              });

              alertUserAboutError();

          }

          @Override
          public void onResponse(Call call, Response response) throws IOException {
              runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                      toggleRefresh();
                  }
              });

              try {
                  String jsonData =response.body().string();
                       Log.v(TAG,jsonData);
                  if (response.isSuccessful()) {
                      mCurrentWeather=getCurrentDetails(jsonData);
                      runOnUiThread(new Runnable() {
                          @Override
                          public void run() {
                              updateDisplay();
                          }
                      });

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
    }

    private void toggleRefresh() {
       if(mProgressBar.getVisibility()==View.INVISIBLE) {
           mProgressBar.setVisibility(View.VISIBLE);
           mRefreshView.setVisibility(View.INVISIBLE);
       }else
       {
           mProgressBar.setVisibility(View.INVISIBLE);
           mRefreshView.setVisibility(View.VISIBLE);
       }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void updateDisplay() {
        mTemperatureLabel.setText(mCurrentWeather.getmTemperature() + "");
        mTimeLabel.setText("At " + mCurrentWeather.getFormattedTime() + " it will be ");
        mHumidityValue.setText(mCurrentWeather.getmHumidity() + "");
        mPrecipValue.setText(mCurrentWeather.getmPrecipChance() + "%");
        mSummaryLabel.setText(mCurrentWeather.getmSummary());
        Drawable drawable = getResources().getDrawable(mCurrentWeather.getIconId());


        mIconImageView.setImageDrawable(drawable);
    }

    private CurrentWeather getCurrentDetails(String jsonData) throws JSONException {
        JSONObject forecast =new JSONObject(jsonData);
        String timeZone = forecast.getString("timezone");
        Log.i(TAG, "From JSON: " + timeZone);

        JSONObject currently =forecast.getJSONObject("currently");
        CurrentWeather currentWeather =new CurrentWeather();
        currentWeather.setmHumidity(currently.getDouble("humidity"));
        currentWeather.setmTime(currently.getLong("time"));
        currentWeather.setmIcon(currently.getString("icon"));
        currentWeather.setmPrecipChance(currently.getDouble("precipProbability"));
        currentWeather.setmSummary(currently.getString("summary"));
        currentWeather.setmTemperature(currently.getDouble("temperature"));
        currentWeather.setmTimeZone(timeZone);
        Log.d(TAG,currentWeather.getFormattedTime());
        return currentWeather;
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