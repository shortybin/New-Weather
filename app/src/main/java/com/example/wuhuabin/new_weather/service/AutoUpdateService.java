package com.example.wuhuabin.new_weather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.wuhuabin.new_weather.gson.Weather;
import com.example.wuhuabin.new_weather.util.HttpUtil;
import com.example.wuhuabin.new_weather.util.Utility;
import com.example.wuhuabin.new_weather.view.WeatherActivity;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.content.ContentValues.TAG;
import static android.os.SystemClock.elapsedRealtime;

public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        updateImage();
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 8 * 60 * 60 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent intent1 = new Intent(this, AutoUpdateService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent1, 0);
        alarmManager.cancel(pendingIntent);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pendingIntent);
        return super.onStartCommand(intent, flags, startId);
    }


    private void updateWeather() {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weather = defaultSharedPreferences.getString("weather", null);
        if (weather != null) {
            Weather weather1 = Utility.hanlderWeatherResponse(weather);
            String weatherId = weather1.mBasic.weatherId;
            String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=4f4aa2171522428285237c1b1f3a550c";
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String s = response.body().string();
                    Log.d(TAG, "onResponse: " + s);
                    final Weather weather = Utility.hanlderWeatherResponse(s);
                    if (weather != null && "ok".equals(weather.status)) {
                        SharedPreferences defaultSharedPreferences = PreferenceManager.
                                getDefaultSharedPreferences(AutoUpdateService.this);
                        SharedPreferences.Editor edit = defaultSharedPreferences.edit();
                        edit.putString("weather", s);
                        edit.apply();
                    }
                }
            });
        }

    }

    private void updateImage() {
        String imageUrl = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(imageUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String string = response.body().string();
                SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this);
                SharedPreferences.Editor edit = defaultSharedPreferences.edit();
                edit.putString("back_image", string);
                edit.apply();
            }
        });
    }

}
