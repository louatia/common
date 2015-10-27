package com.nejma.common.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.io.ByteStreams;
import com.nejma.common.binding.JsonBinder;
import org.w3c.dom.Document;
import play.Logger;
import play.libs.Json;
import play.libs.XML;
import play.libs.ws.WSCookie;
import play.libs.ws.WSResponse;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Created by aminelouati on 6/23/15.
 * delegate to WSResponse, and utility functions to read the response body
 *
 *
 */
public class WsResponseReader {
    private WSResponse wsResponse;
    private byte[] bytes;

    private static Logger.ALogger logger = Logger.of(WsResponseReader.class);

    /**
     * THIS FUNCTION CAN ONLY BE CALLED ONCE ON A RESPONSE
     *
     * @param wsResponse
     * @return
     */
    public static WsResponseReader read(WSResponse wsResponse) {
        return new WsResponseReader(wsResponse);
    }

    private WsResponseReader(WSResponse wsResponse) {
        this.wsResponse = wsResponse;
        try {
            Object data = null;
            if ((data = wsResponse.getBodyAsStream()) != null) {
                bytes = ByteStreams.toByteArray(((InputStream) data));
            } else if ((data = wsResponse.asByteArray()) != null) {
                bytes = (byte[]) data;
            } else if ((data = wsResponse.getBody()) != null) {
                bytes = data.toString().getBytes(StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            logger.debug("Unable to read response body from " + wsResponse.getUri(), e);
            throw new UnableToReadResponseBodyException("Unable to read response body from " + wsResponse.getUri(), e);
        }
    }

    public Map<String, List<String>> getAllHeaders() {
        return wsResponse.getAllHeaders();
    }

    public URI getUri() {
        return wsResponse.getUri();
    }

    public List<WSCookie> getCookies() {
        return wsResponse.getCookies();
    }

    public String getHeader(String s) {
        return wsResponse.getHeader(s);
    }

    public int getStatus() {
        return wsResponse.getStatus();
    }

    public String getStatusText() {
        return wsResponse.getStatusText();
    }

    public byte[] getBodyAsBytes() {
        return bytes;
    }

    public InputStream getBodyAsStream() {
        return new ByteArrayInputStream(bytes);
    }

    public WSCookie getCookie(String s) {
        return wsResponse.getCookie(s);
    }

    public String getBodyAsString() {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public JsonNode getBodyAsJson() {
        try {
            return Json.parse(new String(bytes, StandardCharsets.UTF_8));
        } catch (RuntimeException e) {
            logger.error("Unable to parse JSON. WsResponseReade.getBodyAsJson() failed.");
            throw new WsResponseReaderJsonParsingException(e);
        }
    }

    public Document getBodyAsXmlDoc() {
        return XML.fromString(new String(bytes, StandardCharsets.UTF_8));
    }

    public <T> T getBodyAsObject(Class<T> classOfT) {
        try {
            return JsonBinder.tryUnmarshal(bytes, classOfT);
        } catch (Exception e) {
            throw new RuntimeException("Unable Construct Object of " + classOfT + " from " + wsResponse.getUri() + " \n" + e.getMessage());
        }
    }


    private class WsResponseReaderJsonParsingException extends RuntimeException {
        public WsResponseReaderJsonParsingException(RuntimeException e) {
            super(e);
        }
    }

    private class UnableToReadResponseBodyException extends RuntimeException {
        public UnableToReadResponseBodyException(String msg, Exception e) {
            super(msg, e);
        }
    }
}
