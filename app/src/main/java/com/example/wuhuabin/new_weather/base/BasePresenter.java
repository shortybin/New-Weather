package com.example.wuhuabin.new_weather.base;

/**
 * Created by wuhuabin on 2017/6/1.
 */

public abstract class BasePresenter<T,E> {

    public T mView;
    public E mModel;

    public void onStart(T v,E m){
        mView=v;
        mModel=m;
    }

    public void onDestory(){
        mView=null;
        mModel=null;
    }


}
