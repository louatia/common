package com.nejma.common.http;

import play.mvc.Http;

import javax.validation.constraints.NotNull;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Created by aminelouati
 * utility class to read the Body from a play.mvc.Http.Request
 */
public class RequestReader {

    public static String getRequestUrl(play.libs.ws.WSRequestHolder    request) {
        String url = null;
        try {
            URL nUrl = new URL(request.getUrl());
            url = nUrl.getProtocol() + "://" + nUrl.getHost() + nUrl.getPath();
        } catch (Exception e) {

        }
        return url + "?" + request.getQueryParameters()
                .entrySet().stream().sorted((o1, o2) -> o1.getKey().compareTo(o2.getKey()))
                .map(e -> (e.getKey() + "=" + e.getValue().stream().collect(Collectors.joining(","))))
                .collect(Collectors.joining("&"));
    }



    private Http.RequestBody body;
    byte[] bytes;

    private RequestReader(@NotNull Http.Request request) {
        body = request.body();
        if (body.asRaw() != null) {
            bytes = body.asRaw().asBytes();
        } else if (body.asJson() != null) {
            bytes = body.asJson().toString().getBytes(StandardCharsets.UTF_8);
        } else if (body.asText() != null) {
            bytes = body.asText().getBytes(StandardCharsets.UTF_8);
        } else if (body.asFormUrlEncoded() != null) {
            bytes = NProxy.bodyFromFormUrlEncoded(body.asFormUrlEncoded()).getBytes(StandardCharsets.UTF_8);
        } else if (body.asXml() != null) {
            throw new UnsupportedOperationException("RequestReader Not Implemented for " + request.getHeader(Http.HeaderNames.CONTENT_TYPE));
        } else {
            throw new UnsupportedOperationException("RequestReader Not Implemented for " + request.getHeader(Http.HeaderNames.CONTENT_TYPE));
        }
    }

    public static RequestReader read(@NotNull Http.Request request) {
        return new RequestReader(request);
    }

    public byte[] getBody() {
        return bytes;
    }

    public String getBodyAsString() {
        return new String(bytes, StandardCharsets.UTF_8);
    }


}
