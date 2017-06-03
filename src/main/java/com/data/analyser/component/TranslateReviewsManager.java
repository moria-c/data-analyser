package com.data.analyser.component;

import com.data.analyser.dto.Review;
import com.data.analyser.dto.TranslateRequest;
import com.data.analyser.utils.ObjectMapperUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * This class managed buffer for google translate request
 * the request sent when it reached the capacity per requests (1000 characters)
 * current implementation doing nothing with translation response
 */
public class TranslateReviewsManager {

    private static final Logger log = LoggerFactory.getLogger(TranslateReviewsManager.class);

    private final int CAPACITY_PER_REQUEST = 1000;
    private final String GOOGLE_HOST_NAME = "https://api.google.com";
    private final String GOOGLE_TRANSLATE_URI = GOOGLE_HOST_NAME + "/translate";
    private ThrottlingHttpRequestsManager requestHandler;
    private StringBuilder stringBuilder;

    public TranslateReviewsManager(ThrottlingHttpRequestsManager requestHandler) {
        this.requestHandler = requestHandler;
        stringBuilder = new StringBuilder(CAPACITY_PER_REQUEST);
    }

    public void translate(Review review) {
            String text = getTextToAppend(review);
            if (stringBuilder.length() + text.length() < CAPACITY_PER_REQUEST) {
                stringBuilder.append(text);
            } else {
                sendRequest(stringBuilder);
                stringBuilder = new StringBuilder(CAPACITY_PER_REQUEST).append(text);
            }
    }

    public void submitLastRequest(){
        if (stringBuilder.length() > 0){
            sendRequest(stringBuilder);
        }
    }

    private String getTextToAppend(Review review) {
        //i didn't handle the option that text is more than 1000 character,
        //current implementation just doing subString
        String text = review.getId() + '\n' + review.getText() + '\n';
        if (text.length() > CAPACITY_PER_REQUEST){
            text = text.substring(0, CAPACITY_PER_REQUEST - 1);
        }
        return text;
    }

    private void sendRequest(StringBuilder stringBuilder) {
        final HttpPost httpPost = new HttpPost();
        final String requestBody;
        try {
            final TranslateRequest request = TranslateRequest
                    .createEnToFrTranslateRequest(stringBuilder.toString());
            requestBody = ObjectMapperUtil.getObjectMapper().writeValueAsString(request);
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException while trying to writh request as json string", e);
            //todo handle retry mechanisms
            return;
        }
        HttpEntity httpEntity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
        httpPost.setEntity(httpEntity);

        httpPost.setURI(URI.create(GOOGLE_TRANSLATE_URI));
        final HttpHost httpHost = new HttpHost(GOOGLE_HOST_NAME);
        requestHandler.sendRequest(httpHost, httpPost, new FutureCallback() {
            @Override
            public void completed(Object o) {
                //todo implement what to do with the translated response - save it somewhere
            }

            @Override
            public void failed(Exception e) {
                //todo retry mechanism
            }

            @Override
            public void cancelled() {

            }
        });
    }
}
