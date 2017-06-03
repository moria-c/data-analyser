package com.data.analyser.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by moria.cohen on 5/15/16.
 */

public class ObjectMapperUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}