package com.mindyapps.android.landly.repositories;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.mindyapps.android.landly.models.Landmark;
import com.mindyapps.android.landly.network.PixabayApi;
import com.mindyapps.android.landly.util.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

@Singleton
public class LandmarkRepository {

    private static final String TAG = "LandmarkRepository";

    private List<String> randomMarks = new ArrayList<>();
    PixabayApi pixabayApi;
    Retrofit retrofit;

    @Inject
    public LandmarkRepository(Retrofit retrofit, PixabayApi pixabayApi){
        this.retrofit = retrofit;
        this.pixabayApi = pixabayApi;
    }

    public MutableLiveData<Landmark> getLandmarksByName(String key, String name){
        final MutableLiveData<Landmark> landmarksData = new MutableLiveData<>();
        pixabayApi.getLandmark(key, name).enqueue(new Callback<Landmark>() {
            @Override
            public void onResponse(Call<Landmark> call,
                                   Response<Landmark> response) {
                if (response.isSuccessful()){
                    landmarksData.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<Landmark> call, Throwable t) {
                landmarksData.setValue(null);
            }
        });
        return landmarksData;
    }

    public MutableLiveData<Landmark> getRandomLandmarks(String key){
        final MutableLiveData<Landmark> landmarksData = new MutableLiveData<>();
        final String landMark = getRandomMark();
        pixabayApi.getLandmark(key, landMark).enqueue(new Callback<Landmark>() {
            @Override
            public void onResponse(Call<Landmark> call,
                                   Response<Landmark> response) {
                if (response.isSuccessful()){
                    response.body().setName(landMark);
                    landmarksData.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<Landmark> call, Throwable t) {
                landmarksData.setValue(null);
            }
        });
        return landmarksData;
    }

    public String getRandomMark() {
        Random rand = new Random();
        randomMarks = Arrays.asList(Constants.LANDMARKS.split("\\s*,\\s*"));
        return randomMarks.get(rand.nextInt(randomMarks.size()));
    }
}