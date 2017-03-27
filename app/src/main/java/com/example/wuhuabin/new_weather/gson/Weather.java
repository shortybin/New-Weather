package com.example.wuhuabin.new_weather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by wuhuabin on 2017/3/23.
 */

public class Weather {

    public String status;

    @SerializedName("basic")
    public Basic mBasic;
    @SerializedName("aqi")
    public AQI mAQI;
    @SerializedName("now")
    public Now mNow;
    @SerializedName("suggestion")
    public Suggestion mSuggestion;

    @SerializedName("daily_forecast")
    public List<Forecast> mForecastList;

}
