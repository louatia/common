package com.github.louatia.common.play;

import com.github.louatia.common.binding.JsonBinder;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTimeZone;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.api.mvc.EssentialFilter;
import play.libs.F;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

import static play.mvc.Results.*;

/**
 * Created by aminelouati on 10/27/15.
 */
public  class ApplicationLoader extends GlobalSettings {

    private static Logger.ALogger logger = Logger.of(ApplicationLoader.class);

    public void onStart(Application app) {
        super.onStart(app);
        DateTimeZone.setDefault(DateTimeZone.UTC);
        Json.setObjectMapper(JsonBinder.mapper);
        app.configuration().getString("");
//        CometLogstashProvider.addAdditionalCustomField("app_name", Config.APP_NAME);
//        CometLogstashProvider.addAdditionalCustomField("app_version", Config.APP_VERSION);

//        logger.info("Application '" + Config.APP_NAME + ":" + Config.APP_VERSION + "" + "' has started");
    }

    public void onStop(Application app) {
        super.onStop(app);
//        logger.info("Application '" + Config.APP_NAME + ":" + Config.APP_VERSION + "" + "' shutdown...");
    }

    @Override
    public F.Promise<Result> onBadRequest(Http.RequestHeader request, String error) {
        return F.Promise.pure(badRequest(error));
    }


    @Override
    public F.Promise<Result> onHandlerNotFound(Http.RequestHeader request) {
        if (request.path().endsWith("/")) {
            // Redirect without the slash
            String strippedPath = request.path().substring(0, request.path().length() - 1);

            String queryString = request.queryString().entrySet().stream()
                    .map(e -> e.getKey() + "=" + Arrays.asList(e.getValue()).stream().collect(Collectors.joining(",")))
                    .collect(Collectors.joining("&"));

            if (StringUtils.isNotEmpty(queryString)) {
                strippedPath += "?" + queryString;
            }

            return F.Promise.pure(found(strippedPath));
        }
        return F.Promise.pure(notFound("{\"error\":\"HTTP 404\"}"));
    }

    @Override
    public F.Promise<Result> onError(Http.RequestHeader request, Throwable t) {
        final String errorId = "error_id:" + UUID.randomUUID();
        logger.error(errorId + "\nAction error." + "\nRequest: " + request.toString(), t);
        final String msg = errorId + "\nCaused by " + t.getCause();
        return F.Promise.pure(internalServerError(msg));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends EssentialFilter> Class<T>[] filters() {
        return new Class[]{
//                GzipFilter.class,
//                JsonpJava.class
        };
    }
}