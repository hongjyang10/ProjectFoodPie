package com.lanou.dllo.myfoodpie.potting;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/*
         |              |
         | \            | \
         |   | | | | | |    | | | | |||||\
         |                          |||||||\
         |         ( )              ||||||||
         |                           |||||/
         |                  | | | | | |||/
         |    |             |          |
         |    |             |          |
       / |   | |            |          |\
      |      |/             |          \|
       \ |                  |
         |                  |
           \ | | | | | | | /
             |       |            <-----弱鸡
             |       |
             |       |
*/
public class OkTool implements NetInterface {
    private OkHttpClient okHttpClient;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Gson gson;

    public OkTool() {
        gson = new Gson();
        okHttpClient = new OkHttpClient.Builder().
                retryOnConnectionFailure(true).
                connectTimeout(5, TimeUnit.SECONDS).cache
                (new Cache(Environment.getExternalStorageDirectory(),
                        10 * 1024 * 1024)).build();
    }

    @Override
    public void startRequest(String url, final CallBack<String> callBack) {
        Request request = new Request.Builder().url(url).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callBack.onError(e);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String string = response.body().string();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callBack.onSuccess(string);
                    }
                });
            }
        });
    }

    @Override
    public <T> void startRequest(String url, final Class<T> tClass, final CallBack<T> callback) {
        final Request request = new Request.Builder().url(url).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError(e);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                final T result = gson.fromJson(string, tClass);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess(result);
                    }
                });
            }
        });
    }
}
