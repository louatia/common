package com.github.louatia.common.http;

import play.Logger;
import play.libs.F;
import play.libs.ws.WS;
import play.libs.ws.WSRequestHolder;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Created by aminelouati on 6/12/15.
 */

public class HProxy {


    public static enum HttpMethod {
        DELETE, GET, HEAD, OPTIONS, POST, PUT, TRACE
    }

    public static Call proxy(String endpoint, Http.Context ctx) {
        return new Call(endpoint, ctx);
    }

    //once the proxy request is build there is no way of excluding the params.. adding this utility to solve this problem
    public static final class Call {
        public Call setHeader(String s, String s1) {
            wsRequest.setHeader(s, s1);
            return this;
        }
        public Call setParam(String s, String s1) {
            wsRequest.setQueryParameter(s, s1);
            return this;
        }

        private Call(String endpoint, Http.Context ctx) {
            this.ctx = ctx;
            this.endpoint = endpoint;
            this.path = ctx.request().path();
            this.wsRequest = WS.url(endpoint + path);
        }

        private String endpoint;
        private String path;
        private Http.Context ctx;
        private List<String> excludeHeaders = new ArrayList<>();
        private List<String> excludeParams = new ArrayList<>();

        /**
         * exclude the header h from being forwarded to the endpoint
         *
         * @param h
         */
        public Call excludeHeader(String... h) {
            excludeHeaders.addAll(Arrays.asList(h));
            return this;
        }


        /**
         * exclude the params p from being forwarded to the endpoint
         *
         * @param p
         */
        public Call excludeParam(String... p) {
            excludeParams.addAll(Arrays.asList(p));
            return this;
        }

        private WSRequestHolder wsRequest;

        private WSRequestHolder request() {

            //set Query String
            ctx.request().queryString().entrySet().stream()
                    .sorted((o1, o2) -> o1.getKey().compareTo(o2.getKey()))
                    .forEach(e -> {
                        if (!excludeParams.contains(e.getKey())) {
                            wsRequest.setQueryParameter(e.getKey(), Arrays.asList(e.getValue()).stream().collect(Collectors.joining(",")));
                        }
                    });
            //set Headers
            final Http.Request httpRequest = ctx.request();
            httpRequest.headers().forEach((header, values) -> Arrays.asList(values).forEach(v -> {
                        if (!excludeHeaders.contains(header)) {
                            wsRequest.setHeader(header, v);
                        }
                    }
            ));
            wsRequest.setMethod(httpRequest.method());

            switch (HttpMethod.valueOf(httpRequest.method())) {
                case POST:
                case PUT:
                    wsRequest.setBody(RequestReader.read(httpRequest).getBodyAsString());
                    break;
            }
            logger.trace("calling service: " + wsRequest.getUrl());
            return wsRequest;
        }

        public F.Promise<WsResponseReader> execute() {
            return request().execute().map(ws -> WsResponseReader.read(ws));
        }

        public F.Promise<Result> fetch(final Http.Response httpResponse) {
            return execute().map(response -> {
                        response.getAllHeaders()
                                .forEach((header, values) -> {
                                    //skip Http.HeaderNames.TRANSFER_ENCODING
                                    if (!Http.HeaderNames.TRANSFER_ENCODING.equalsIgnoreCase(header)) {
                                        values.forEach(v -> httpResponse.setHeader(header, v));
                                    }
                                });
                        return (Result) Results.status(response.getStatus(), response.getBodyAsBytes()).as(response.getHeader(Http.HeaderNames.CONTENT_TYPE));
                    }
            ).recover(throwable -> {
                logger.error("Error calling service: " + this.wsRequest.getUrl(), throwable);
                if (throwable instanceof TimeoutException) {
                    return Results.status(Http.Status.GATEWAY_TIMEOUT, throwable.getMessage());
                }
                return Results.internalServerError(throwable.getMessage());
            });
        }
    }


    public static final String HTTP_POST = "POST";
    public static final String HTTP_GET = "GET";
    public static final String HTTP_PUT = "PUT";
    public static final String HTTP_DELETE = "DELETE";
    public static final String CHUNKED_TRANSFER_ENCODING = "chunked";

    private static Logger.ALogger logger = Logger.of(HProxy.class);

    /**
     * method to create a clean query strings , netty implementation in play had issues with req.queryString()
     *
     * @param req
     * @return
     */
    public static String buildProxyQueryString(Http.Request req) {
        String qs = req.queryString().entrySet().stream().sorted((o1, o2) -> o1.getKey().compareTo(o2.getKey()))
                .map(e -> e.getKey() + "=" + Arrays.asList(e.getValue()).stream().collect(Collectors.joining(",")))
                .collect(Collectors.joining("&"));
        return (qs.length() > 0 ? "?" + qs : "");
    }

    /**
     * Create a body ( as string ) from a list of parameters (httpRequest.body().asFormUrlEncoded())
     *
     * @param parameters
     * @return
     */
    public static String bodyFromFormUrlEncoded(Map<String, String[]> parameters) {
        Set<String> keys = parameters.keySet();
        StringJoiner join = new StringJoiner("&");
        for (String key : keys) {
            String[] values = parameters.get(key);
            for (String v : values) {
                String encoded = v;
                try {
                    encoded = URLEncoder.encode(v, UTF_8.toString());
                } catch (UnsupportedEncodingException e) {
                    logger.trace("Unable to encode param to UTF-8", e);
                }
                join.add(key + "=" + encoded);
            }
        }
        return join.toString();
    }

    @Deprecated
    /**
     * use CometProxy.proxy(domain, ctx()) .excludeParam(...) . excludeHeader(...)   .setHeader(...)   .setParam(...)      .fetch(response());
     */
    public static WSRequestHolder buildProxyHttpRequest(String endpoint, String path, Http.Context ctx, String... excludeParams) {
        //set url
        List<String> exclude = Arrays.asList(excludeParams);
        WSRequestHolder wsRequest = WS.url(endpoint + path);
        //set Query String
        ctx.request().queryString().entrySet().stream()
                .sorted((o1, o2) -> o1.getKey().compareTo(o2.getKey()))
                .forEach(e -> {
                    if (!exclude.contains(e.getKey())) {
                        wsRequest.setQueryParameter(e.getKey(), Arrays.asList(e.getValue()).stream().collect(Collectors.joining(",")));
                    }
                });
        final Http.Request httpRequest = ctx.request();
        httpRequest.headers().forEach((header, values) -> Arrays.asList(values).forEach(v -> wsRequest.setHeader(header, v)));
        if (HTTP_POST.equalsIgnoreCase(httpRequest.method())) {
            wsRequest.setBody(RequestReader.read(httpRequest).getBodyAsString());
            wsRequest.setMethod(HTTP_POST);
        } else {
            wsRequest.setMethod(HTTP_GET);
        }
        return wsRequest;
    }


    @Deprecated
    //due to a bug in WS.url - see     CHAPI-105
    public static WSRequestHolder buildProxyHttpRequest(String url, Http.Context ctx) {
        WSRequestHolder wsRequest = WS.url(url);
        final Http.Request httpRequest = ctx.request();
        httpRequest.headers().forEach((header, values) -> Arrays.asList(values).forEach(v -> wsRequest.setHeader(header, v)));
        if (HTTP_POST.equalsIgnoreCase(httpRequest.method())) {
            wsRequest.setBody(RequestReader.read(httpRequest).getBodyAsString());
            wsRequest.setMethod(HTTP_POST);
        } else {
            wsRequest.setMethod(HTTP_GET);
        }
        return wsRequest;
    }

    @Deprecated
    /**
     * use CometProxy.proxy(domain, ctx()) .excludeParam(...) . excludeHeader(...)   .setHeader(...)   .setParam(...)      .fetch(response());
     */
    public static F.Promise<Result> fetch(WSRequestHolder wsRequest, final Http.Response httpResponse) {
        return wsRequest.execute().map(wsResponse -> {
                    wsResponse.getAllHeaders()
                            .forEach((header, values) -> {
                                //skip Http.HeaderNames.TRANSFER_ENCODING
                                if (!Http.HeaderNames.TRANSFER_ENCODING.equalsIgnoreCase(header)) {
                                    values.forEach(v -> httpResponse.setHeader(header, v));
                                }
                            });
                    WsResponseReader response = WsResponseReader.read(wsResponse);
                    return (Result) Results.status(response.getStatus(), response.getBodyAsBytes()).as(wsResponse.getHeader(Http.HeaderNames.CONTENT_TYPE));
                }
        ).recover(throwable -> {
            logger.error("Error calling service: " + wsRequest.getUrl(), throwable);
            if (throwable instanceof TimeoutException) {
                return Results.status(Http.Status.GATEWAY_TIMEOUT, throwable.getMessage());
            }
            return Results.internalServerError(throwable.getMessage());
        });
    }


}