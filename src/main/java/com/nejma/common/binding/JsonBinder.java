package com.nejma.common.binding;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import play.Logger;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author aminelouati
 */
public class JsonBinder {

    public static final ObjectMapper mapper = new ObjectMapper();

    private static Logger.ALogger logger = Logger.of(JsonBinder.class);

    static {

        //undeclared fields should still work but not mapped
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //allow single quoted json in case the json is not formatted properly {'att':'val'}
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        //write empty array instead of nulls ( to avoid NPE )
        mapper.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);
        //empty string should be output as null values in json
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        // readable POJOs with less annotations; use class fields accessor
        mapper.setVisibilityChecker(mapper.getSerializationConfig().getDefaultVisibilityChecker().withFieldVisibility(JsonAutoDetect.Visibility.ANY).withGetterVisibility(JsonAutoDetect.Visibility.NONE).withSetterVisibility(JsonAutoDetect.Visibility.NONE).withIsGetterVisibility(JsonAutoDetect.Visibility.NONE).withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
    }

    public static <T> String marshal(T dataObject) {
        try {
            return tryMarshal(dataObject);
        } catch (Exception e) {
            logger.debug(e.toString());
            logger.trace("marshal() failed", e);
            return null;
        }
    }

    public static <T> String tryMarshal(T dataObject) throws Exception {
        return mapper.writeValueAsString(dataObject);
    }

    public static <T> void marshal(T dataObject, OutputStream stream) throws Exception {
        mapper.writer().writeValue(stream, dataObject);
    }

    public static <T> T unmarshal(String json, Class<T> classOfT) {
        return unmarshal(json.getBytes(StandardCharsets.UTF_8), classOfT);
    }

    public static <T> T tryUnmarshal(String json, Class<T> classOfT) throws Exception {
        return tryUnmarshal(json.getBytes(StandardCharsets.UTF_8), classOfT);
    }

    public static <T> T tryUnmarshal(byte[] bytes, Class<T> classOfT) throws Exception {
        return mapper.readValue(bytes, classOfT);
    }

    public static <T> T unmarshal(byte[] bytes, Class<T> classOfT) {
        try {
            return tryUnmarshal(bytes, classOfT);
        } catch (Exception e) {
            logger.debug(e.toString());
            logger.trace("marshal() failed", e);
            return null;
        }
    }
}