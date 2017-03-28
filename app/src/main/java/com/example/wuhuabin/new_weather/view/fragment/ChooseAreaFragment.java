package com.example.wuhuabin.new_weather.view.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wuhuabin.new_weather.R;
import com.example.wuhuabin.new_weather.db.City;
import com.example.wuhuabin.new_weather.db.County;
import com.example.wuhuabin.new_weather.db.Province;
import com.example.wuhuabin.new_weather.gson.Weather;
import com.example.wuhuabin.new_weather.util.HttpUtil;
import com.example.wuhuabin.new_weather.util.Utility;
import com.example.wuhuabin.new_weather.view.MainActivity;
import com.example.wuhuabin.new_weather.view.WeatherActivity;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


/**
 * Created by wuhuabin on 2017/3/22.
 */

public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE = 0;

    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog mProgressDialog;

    private TextView mTextView;
    private Button mBackButton;
    private ListView mListView;
    private ArrayAdapter<String> mStringArrayAdapter;
    private List<String> mDataList = new ArrayList<>();
    private List<Province> mProvinces;
    private List<City> mCitys;
    private List<County> mCountys;
    private Province mSelectProvince;
    private City mSelectCity;
    private County mSelectCounty;

    private int currentLevel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        mTextView = (TextView) view.findViewById(R.id.title_text);
        mBackButton = (Button) view.findViewById(R.id.back_button);
        mListView = (ListView) view.findViewById(R.id.list_view);
        mStringArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, mDataList);
        mListView.setAdapter(mStringArrayAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    mSelectProvince = mProvinces.get(position);
                    queryCity();
                } else if (currentLevel == LEVEL_CITY) {
                    mSelectCity = mCitys.get(position);
                    queryCounty();
                }else if (currentLevel==LEVEL_COUNTY){
                    String weatherId = mCountys.get(position).getWeatherId();

                    if (getActivity()!=null){
                        FragmentActivity activity = getActivity();
                        if (activity instanceof MainActivity){
                            Intent intent=new Intent(getActivity(), WeatherActivity.class);
                            intent.putExtra("weather_id",weatherId);
                            startActivity(intent);
                            activity.finish();
                        }else if (activity instanceof WeatherActivity){
                            WeatherActivity weatherActivity= (WeatherActivity) activity;
                            weatherActivity.mDrawerLayout.closeDrawers();
                            weatherActivity.weather_id=weatherId;
                            weatherActivity.mSwipeRefreshLayout.setRefreshing(true);
                            weatherActivity.requsetWeather(weatherId);
                        }
                    }
                }
            }
        });

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_COUNTY) {
                    queryCity();
                } else if (currentLevel == LEVEL_CITY) {
                    queryProvince();
                }
            }
        });

        queryProvince();
    }

    private void queryProvince() {
        mTextView.setText("中国");
        mBackButton.setVisibility(View.GONE);
        mProvinces = DataSupport.findAll(Province.class);
        if (mProvinces.size() > 0) {
            mDataList.clear();
            for (Province province : mProvinces) {
                mDataList.add(province.getProvinceName());
            }
            mStringArrayAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel=LEVEL_PROVINCE;
        }else{
            String address="http://guolin.tech/api/china";
            querFormService(address,"province");
        }
    }


    private void queryCity() {
        mTextView.setText(mSelectProvince.getProvinceName());
        mBackButton.setVisibility(View.VISIBLE);
        mCitys = DataSupport.where("provinceid=?",String.valueOf(mSelectProvince.getId())).find(City.class);
        if (mCitys.size() > 0) {
            mDataList.clear();
            for (City city : mCitys) {
                mDataList.add(city.getCityName());
            }
            mStringArrayAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel=LEVEL_CITY;
        }else{
            int id = mSelectProvince.getProvinceCode();
            String address="http://guolin.tech/api/china/"+id;
            querFormService(address,"city");
        }
    }


    private void queryCounty() {
        mTextView.setText(mSelectCity.getCityName());
        mBackButton.setVisibility(View.VISIBLE);
        mCountys = DataSupport.where("cityid=?",String.valueOf(mSelectCity.getId())).find(County.class);
        if (mCountys.size() > 0) {
            mDataList.clear();
            for (County county : mCountys) {
                mDataList.add(county.getCountyName());
            }
            mStringArrayAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel=LEVEL_COUNTY;
        }else{
            int province=mSelectProvince.getProvinceCode();
            int id = mSelectCity.getCityCode();
            String address="http://guolin.tech/api/china/"+province+"/"+id;
            querFormService(address,"county");
        }
    }


    private void querFormService(String address, final String type) {
        showDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                boolean resule = false;
                if ("province".endsWith(type)) {
                    resule = Utility.handleProvinceResponse(string);
                } else if ("city".equals(type)) {
                    resule = Utility.hanlderCityResponse(string, mSelectProvince.getId());
                } else if ("county".endsWith(type)) {
                    resule = Utility.hanlderCountyResponse(string, mSelectCity.getId());
                }

                if (resule) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeDialog();
                            if ("province".endsWith(type)) {
                                queryProvince();
                            } else if ("city".equals(type)) {
                                queryCity();
                            } else if ("county".endsWith(type)) {
                                queryCounty();
                            }
                        }
                    });
                }

            }
        });
    }

    private void showDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setTitle("正在加载...");
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }

    private void closeDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

}
