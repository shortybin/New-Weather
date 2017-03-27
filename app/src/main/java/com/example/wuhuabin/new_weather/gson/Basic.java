package com.example.wuhuabin.new_weather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by wuhuabin on 2017/3/23.
 */

public class Basic {

    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    @SerializedName("update")
    public Update mUpdate;

    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }
}
