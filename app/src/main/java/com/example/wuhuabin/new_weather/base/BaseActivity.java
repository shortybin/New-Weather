package com.example.wuhuabin.new_weather.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.SupportActivity;

import com.example.wuhuabin.new_weather.manager.AppManager;
import com.example.wuhuabin.new_weather.util.TUtil;

/**
 * Created by wuhuabin on 2017/6/2.
 */

public abstract class BaseActivity <T extends BasePresenter,E extends BaseModel> extends SupportActivity {

    public T mPresenter;
    public E mModel;
    private Context mContext;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init(savedInstanceState);
    }

    private void init(Bundle savedInstanceState) {
        this.setContentView(this.getLayoutId());
        mContext = this;
        mPresenter = TUtil.getT(this, 0);
        mModel = TUtil.getT(this, 1);
        if (this instanceof BaseView) {
            mPresenter.onStart(this, mModel);
        }
        this.initView(savedInstanceState);
        this.initData();
        AppManager.getAppManager().addActivity(this);
    }



    @Override
    protected void onResume() {
        super.onResume();
        if (mPresenter!=null){
            mPresenter.onStart(this,mModel);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppManager.getAppManager().finishActivity(this);
        if (mPresenter!=null){
            mPresenter.onDestory();
        }
    }

    /**
     * 跳转页面,无extra简易型
     *
     * @param tarActivity 目标页面
     */
    public void startActivity(Class<? extends Activity> tarActivity, Bundle options) {
        Intent intent = new Intent(this, tarActivity);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            startActivity(intent, options);
        } else {
            startActivity(intent);
        }
    }

    public void startActivity(Class<? extends Activity> tarActivity) {
        Intent intent = new Intent(this, tarActivity);
        startActivity(intent);
    }

    public abstract int getLayoutId();

    public abstract void initView(Bundle savedInstanceState);

    public abstract void initData();
}
