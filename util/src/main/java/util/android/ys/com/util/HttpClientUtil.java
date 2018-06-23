package util.android.ys.com.util;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.config.RequestConfig;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.conn.ConnectTimeoutException;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.util.EntityUtils;

/**
 * compile 'com.loopj.android:android-async-http:1.4.9'
 * Created by 18758 on 2018/5/4.
 */

public class HttpClientUtil {

    public interface HttpClientEventListener {
        void onSuccess(int status_code, byte[] bytes);

        void onFailure(int status_code, byte[] bytes);

        void onTimeout();
    }

    //使用异步Http发送get请求
    static public void get(String url, Map<String, String> params, final HttpClientEventListener httpClientEventListener) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams rp = new RequestParams(params);
        client.get(url, rp, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                if (httpClientEventListener != null) {
                    httpClientEventListener.onSuccess(i, bytes);
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                if (httpClientEventListener != null) {
                    if (throwable instanceof ConnectTimeoutException) {
                        httpClientEventListener.onTimeout();
                    } else if (throwable instanceof SocketTimeoutException) {
                        httpClientEventListener.onTimeout();
                    } else {
                        httpClientEventListener.onFailure(i, bytes);
                    }
                }
            }
        });
    }

    //使用异步Http发送post请求
    static public void post(String url, Map<String, String> params, final HttpClientEventListener httpClientEventListener, int timeout) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams rp = new RequestParams(params);
        client.setTimeout(timeout);
        client.post(url, rp, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                if (httpClientEventListener != null) {
                    httpClientEventListener.onSuccess(i, bytes);
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                if (httpClientEventListener != null) {
                    if (throwable instanceof ConnectTimeoutException) {
                        httpClientEventListener.onTimeout();
                    } else if (throwable instanceof SocketTimeoutException) {
                        httpClientEventListener.onTimeout();
                    } else {
                        httpClientEventListener.onFailure(i, bytes);
                    }
                }
            }
        });
    }

    // 同步的方式发送post请求
//    static public void postSync(String url, Map<String, String> params, final HttpClientEventListener httpClientEventListener, int timeout) {
//
//        SyncHttpClient client = new SyncHttpClient();
//        RequestParams rp = new RequestParams(params);
//
//        rp.setUseJsonStreamer(false);
//        rp.setHttpEntityIsRepeatable(true);
//        client.setTimeout(timeout);
//        client.post(Table_Data.getContext(), url, rp, new AsyncHttpResponseHandler() {
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
//                Log.e("success", "postSync->成功");
//                if (httpClientEventListener != null) {
//                    httpClientEventListener.onSuccess(statusCode, responseBody);
//                }
//            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
//                if (httpClientEventListener != null) {
//                    if (error instanceof ConnectTimeoutException) {
//                        httpClientEventListener.onTimeout();
//                    } else if (error instanceof SocketTimeoutException) {
//                        httpClientEventListener.onTimeout();
//                    } else {
//                        httpClientEventListener.onFailure(statusCode, responseBody);
//                    }
//                }
//            }
//        });
//    }


    // 附件上传
    static public void testPostJsonSync(Context context, String url, Map<String, Object> params, final HttpClientEventListener httpClientEventListener, int timeout) {

        SyncHttpClient client = new SyncHttpClient();
        RequestParams rp = new RequestParams();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            Object obj = entry.getValue();
            if (obj instanceof File) {
                try {
                    rp.put(entry.getKey(), (File) obj, RequestParams.APPLICATION_OCTET_STREAM);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                rp.put(entry.getKey(), obj);
            }
        }

        rp.setUseJsonStreamer(false);
        rp.setHttpEntityIsRepeatable(true);
        client.setTimeout(timeout);
        client.post(context, url, rp, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.e("success", "postSync->成功");
                if (httpClientEventListener != null) {
                    httpClientEventListener.onSuccess(statusCode, responseBody);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                if (httpClientEventListener != null) {
                    if (error instanceof ConnectTimeoutException) {
                        httpClientEventListener.onTimeout();
                    } else if (error instanceof SocketTimeoutException) {
                        httpClientEventListener.onTimeout();
                    } else {
                        httpClientEventListener.onFailure(statusCode, responseBody);
                    }
                }
            }
        });
    }


    /*
    *   以下为 android 自带 http
    * */

    static class PostSync2Task implements Runnable {

        private String url;
        private Map<String, String> params;
        private int timeout;
        private HttpClientEventListener httpClientEventListener;

        PostSync2Task(String url, Map<String, String> params, final HttpClientEventListener httpClientEventListener, int timeout) {

            this.url = url;
            this.params = params;
            this.timeout = timeout;
            this.httpClientEventListener = httpClientEventListener;

        }

        @Override
        public void run() {
            postSync2(url , params ,  httpClientEventListener , timeout);
        }

    }

    // 异步 andriod 自带
    static public void post2(String url, Map<String, String> params, final HttpClientEventListener httpClientEventListener, int timeout) {
        PostSync2Task postSync2Task = new PostSync2Task( url , params , httpClientEventListener , timeout);
        new Thread(postSync2Task).start();
    }


    // 同步 andriod 自带 post
    static public void postSync2(String url, Map<String, String> params, final HttpClientEventListener httpClientEventListener, int timeout) {
        try
        {
            List<NameValuePair> pairList = new ArrayList<NameValuePair>();
            for (Map.Entry<String, String> param: params.entrySet()) {
                pairList.add(new BasicNameValuePair(param.getKey(),param.getValue()));
            }

            HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList);
            // URL使用基本URL即可，其中不需要加参数
            HttpPost httpPost = new HttpPost(url);
            // 将请求体内容加入请求中
            httpPost.setEntity(requestHttpEntity);

            RequestConfig config=RequestConfig.custom()
                    .setConnectTimeout(timeout)       // 设置连接超时时间 5秒钟
                    .setSocketTimeout(timeout)        // 设置读取超时时间5秒钟
                    .build();
            httpPost.setConfig(config);

            // 需要客户端对象来发送请求
            HttpClient httpClient = new DefaultHttpClient();
            // 发送请求
            HttpResponse response = httpClient.execute(httpPost);

            String data = EntityUtils.toString(response.getEntity());

            httpClientEventListener.onSuccess(200, data.getBytes());

            Log.d("response" , response.toString());

        } catch (Exception error) {

            if (error instanceof SocketException) {
                httpClientEventListener.onTimeout();
            } else if(error instanceof ConnectException){
                httpClientEventListener.onTimeout();
            } else {
                httpClientEventListener.onFailure(100, null);
            }

        }

    }

}
