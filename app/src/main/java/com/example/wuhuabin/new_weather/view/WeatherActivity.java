package com.example.wuhuabin.new_weather.view;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.wuhuabin.new_weather.R;
import com.example.wuhuabin.new_weather.gson.Forecast;
import com.example.wuhuabin.new_weather.gson.Weather;
import com.example.wuhuabin.new_weather.util.HttpUtil;
import com.example.wuhuabin.new_weather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private static final String TAG = "WeatherActivity";

    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;

    private ImageView mBackImage;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String weather_id;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_weather);

        initView();

        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        String back_image = defaultSharedPreferences.getString("back_image", null);
        if (back_image != null) {
            Glide.with(this).load(back_image).into(mBackImage);
        } else {
            loadImage();
        }


        String weather = defaultSharedPreferences.getString("weather", null);
        if (weather != null) {
            Weather weather1 = Utility.hanlderWeatherResponse(weather);
            weather_id=weather1.mBasic.weatherId;
            showWeatherInfo(weather1);
        } else {
            String weather_id = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.VISIBLE);
            weather_id=weather_id;
            requsetWeather(weather_id);
        }

    }

    private void loadImage() {
        String imageUrl = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(imageUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String string = response.body().string();
                SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
                SharedPreferences.Editor edit = defaultSharedPreferences.edit();
                edit.putString("back_image", string);
                edit.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(string).into(mBackImage);
                    }
                });
            }
        });
    }

    private void initView() {
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);
        mBackImage = (ImageView) findViewById(R.id.weather_back);
        mSwipeRefreshLayout= (SwipeRefreshLayout) findViewById(R.id.swipe_regresh);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //mSwipeRefreshLayout.setRefreshing(true);
                requsetWeather(weather_id);
            }
        });
    }

    private void showWeatherInfo(Weather weather) {
        String cityName = weather.mBasic.cityName;
        String updateTime = weather.mBasic.mUpdate.updateTime.split(" ")[1];
        String degree = weather.mNow.temperature + "°C";
        String info = weather.mNow.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(info);
        forecastLayout.removeAllViews();
        for (Forecast forecast : weather.mForecastList) {
            View inflate = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = (TextView) inflate.findViewById(R.id.date_text);
            TextView infoText = (TextView) inflate.findViewById(R.id.info_text);
            TextView maxText = (TextView) inflate.findViewById(R.id.max_text);
            TextView minText = (TextView) inflate.findViewById(R.id.min_text);

            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);

            forecastLayout.addView(inflate);
        }

        if (weather.mAQI != null) {
            aqiText.setText(weather.mAQI.city.aqi);
            pm25Text.setText(weather.mAQI.city.pm25);
        }

        String comfort = "舒适度" + weather.mSuggestion.carWash.info;
        String carWash = "洗车指数" + weather.mSuggestion.carWash.info;
        String sport = "运动建议" + weather.mSuggestion.sport.info;

        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);

        weatherLayout.setVisibility(View.VISIBLE);

    }


    private void requsetWeather(String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=4f4aa2171522428285237c1b1f3a550c";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_LONG).show();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String s = response.body().string();
                Log.d(TAG, "onResponse: " + s);
                final Weather weather = Utility.hanlderWeatherResponse(s);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences defaultSharedPreferences = PreferenceManager.
                                    getDefaultSharedPreferences(WeatherActivity.this);
                            SharedPreferences.Editor edit = defaultSharedPreferences.edit();
                            edit.putString("weather", s);
                            edit.apply();
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_LONG).show();
                        }

                        mSwipeRefreshLayout.setRefreshing(false);

                    }
                });
            }
        });
    }

}
