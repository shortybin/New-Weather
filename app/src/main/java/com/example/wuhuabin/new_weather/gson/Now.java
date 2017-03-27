package com.example.wuhuabin.new_weather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by wuhuabin on 2017/3/23.
 */

public class Now {

    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    public class More{
        @SerializedName("txt")
        public String info;
    }

}
