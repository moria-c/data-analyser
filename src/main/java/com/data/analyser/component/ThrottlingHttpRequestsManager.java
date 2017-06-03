package com.data.analyser.component;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.nio.client.HttpAsyncClient;

public class ThrottlingHttpRequestsManager {
    int counter;
    private final int maxMessages;
    private Object lock = new Object();

    HttpAsyncClient httpClient;

    public ThrottlingHttpRequestsManager(int maxMessages, HttpAsyncClient httpClient) {
        this.maxMessages = maxMessages;
        this.httpClient = httpClient;
        this.counter = 0;
    }

    public void sendRequest(HttpHost host, HttpRequest request, FutureCallback callback){

        synchronized (lock) {
            if (counter >= maxMessages) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            counter++;
        }
        sendHttpClientAsync(host,request,callback);

    }

    private void sendHttpClientAsync(HttpHost host, HttpRequest request, FutureCallback<HttpResponse> callback) {
        httpClient.execute(host, request, new FutureCallback<HttpResponse> (){

            @Override
            public void completed(HttpResponse httpResponse) {
                synchronized (lock){
                    counter--;
                    lock.notify();
                }
                callback.completed(httpResponse);
            }

            @Override
            public void failed(Exception e) {

                callback.failed(e);
            }

            @Override
            public void cancelled() {
                callback.cancelled();
            }
        });
    }
}