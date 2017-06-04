package com.data.analyser.mock;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 * mock for HttpAsyncClient - mock the calls for Google Translate Api
 * implementation of execute - submit request to executor
 * the thread that handling request sleep for 200ms and than return response
 */
public class HttpAsyncClientMock implements HttpAsyncClient {

    private static final Logger log = LoggerFactory.getLogger(HttpAsyncClientMock.class);
    private final int AVERAGE_RESPONSE_TIME = 200;
    private ExecutorService executor;

    public HttpAsyncClientMock(int numberOfThreads) {
        this.executor = Executors.newFixedThreadPool(numberOfThreads);
    }

    @Override
    public <T> Future<T> execute(HttpAsyncRequestProducer httpAsyncRequestProducer,
                                 HttpAsyncResponseConsumer<T> httpAsyncResponseConsumer, HttpContext httpContext,
                                 FutureCallback<T> futureCallback) {
        return null;
    }

    @Override
    public <T> Future<T> execute(HttpAsyncRequestProducer httpAsyncRequestProducer,
                                 HttpAsyncResponseConsumer<T> httpAsyncResponseConsumer,
                                 FutureCallback<T> futureCallback) {
        return null;
    }

    @Override
    public Future<HttpResponse> execute(HttpHost httpHost, HttpRequest httpRequest, HttpContext httpContext,
                                        FutureCallback<HttpResponse> futureCallback) {
        return null;
    }

    @Override
    public Future<HttpResponse> execute(HttpHost httpHost, HttpRequest httpRequest,
                                        FutureCallback<HttpResponse> futureCallback) {
        final Future<HttpResponse> futureResponse = executor.submit(() -> {
            try {
                Thread.sleep(AVERAGE_RESPONSE_TIME);
            } catch (InterruptedException e) {
                futureCallback.failed(e);
            }
            final HttpResponse httpResponse = new HttpResponse() {
                @Override
                public StatusLine getStatusLine() {
                    return null;
                }

                @Override
                public void setStatusLine(StatusLine statusLine) {

                }

                @Override
                public void setStatusLine(ProtocolVersion protocolVersion, int i) {

                }

                @Override
                public void setStatusLine(ProtocolVersion protocolVersion, int i, String s) {

                }

                @Override
                public void setStatusCode(int i) throws IllegalStateException {

                }

                @Override
                public void setReasonPhrase(String s) throws IllegalStateException {

                }

                @Override
                public HttpEntity getEntity() {
                    return null;
                }

                @Override
                public void setEntity(HttpEntity httpEntity) {

                }

                @Override
                public Locale getLocale() {
                    return null;
                }

                @Override
                public void setLocale(Locale locale) {

                }

                @Override
                public ProtocolVersion getProtocolVersion() {
                    return null;
                }

                @Override
                public boolean containsHeader(String s) {
                    return false;
                }

                @Override
                public Header[] getHeaders(String s) {
                    return new Header[0];
                }

                @Override
                public Header getFirstHeader(String s) {
                    return null;
                }

                @Override
                public Header getLastHeader(String s) {
                    return null;
                }

                @Override
                public Header[] getAllHeaders() {
                    return new Header[0];
                }

                @Override
                public void addHeader(Header header) {

                }

                @Override
                public void addHeader(String s, String s1) {

                }

                @Override
                public void setHeader(Header header) {

                }

                @Override
                public void setHeader(String s, String s1) {

                }

                @Override
                public void setHeaders(Header[] headers) {

                }

                @Override
                public void removeHeader(Header header) {

                }

                @Override
                public void removeHeaders(String s) {

                }

                @Override
                public HeaderIterator headerIterator() {
                    return null;
                }

                @Override
                public HeaderIterator headerIterator(String s) {
                    return null;
                }

                @Override
                public HttpParams getParams() {
                    return null;
                }

                @Override
                public void setParams(HttpParams httpParams) {

                }
            };
            futureCallback.completed(httpResponse);
            return httpResponse;
        });
        return futureResponse;
    }



    @Override
    public Future<HttpResponse> execute(HttpUriRequest httpUriRequest, HttpContext httpContext,
                                        FutureCallback<HttpResponse> futureCallback) {
        return null;
    }

    @Override
    public Future<HttpResponse> execute(HttpUriRequest httpUriRequest, FutureCallback<HttpResponse> futureCallback) {
        return null;
    }

    public void shutdown(){
        executor.shutdown();
    }

}
