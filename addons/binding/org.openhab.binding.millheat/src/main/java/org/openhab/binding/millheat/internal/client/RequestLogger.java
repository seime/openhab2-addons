package org.openhab.binding.millheat.internal.client;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * Logs HttpClient request/response traffic.
 *
 * @author Gili Tzabari
 * @author Arne Seime - adapted for Millheat binding
 */
public final class RequestLogger {
    private final AtomicLong nextId = new AtomicLong();
    private final Logger log = LoggerFactory.getLogger(RequestLogger.class);

    @NonNull
    private JsonParser parser;

    @NonNull
    private Gson gson;

    public RequestLogger() {
        parser = new JsonParser();
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public Request listenTo(Request request) {
        dump(request);
        return request;
    }

    private void dump(Request request) {
        long id = nextId.getAndIncrement();
        StringBuilder group = new StringBuilder();
        request.onRequestBegin(theRequest -> group.append(
                "Request " + id + "\n" + id + " > " + theRequest.getMethod() + " " + theRequest.getURI() + "\n"));
        request.onRequestHeaders(theRequest -> {
            for (HttpField header : theRequest.getHeaders()) {
                group.append(id + " > " + header + "\n");
            }
        });

        StringBuilder contentBuffer = new StringBuilder();
        request.onRequestContent((theRequest, content) -> contentBuffer
                .append(reformatJson(ByteBuffers.toString(content, getCharset(theRequest.getHeaders())))));
        request.onRequestSuccess(theRequest -> {
            if (contentBuffer.length() > 0) {
                group.append("\n" + contentBuffer.toString());
            }
            log.debug(group.toString());
            contentBuffer.delete(0, contentBuffer.length());
            group.delete(0, group.length());
        });

        request.onResponseBegin(theResponse -> {
            group.append(
                    "Response " + id + "\n" + id + " < " + theResponse.getVersion() + " " + theResponse.getStatus());
            if (theResponse.getReason() != null) {
                group.append(" " + theResponse.getReason());
            }
            group.append("\n");
        });
        request.onResponseHeaders(theResponse -> {
            for (HttpField header : theResponse.getHeaders()) {
                group.append(id + " < " + header + "\n");
            }
        });
        request.onResponseContent((theResponse, content) -> contentBuffer
                .append(reformatJson(ByteBuffers.toString(content, getCharset(theResponse.getHeaders())))));
        request.onResponseSuccess(theResponse -> {
            if (contentBuffer.length() > 0) {
                group.append("\n" + contentBuffer.toString());
            }
            log.debug(group.toString());
        });
    }

    private String reformatJson(String jsonString) {
        try {
            JsonElement json = parser.parse(jsonString);
            return gson.toJson(json);
        } catch (JsonSyntaxException e) {
            log.info("Could not reformat malformed JSON due to '{}'", e.getMessage());
        }

        return jsonString;
    }

    private Charset getCharset(HttpFields headers) {
        String contentType = headers.get(HttpHeader.CONTENT_TYPE);
        if (contentType == null) {
            return StandardCharsets.UTF_8;
        }
        String[] tokens = contentType.toLowerCase(Locale.US).split("charset=");
        if (tokens.length != 2) {
            return StandardCharsets.UTF_8;
        }
        // Remove semicolons or quotes
        String encoding = tokens[1].replaceAll("[;\"]", "");
        return Charset.forName(encoding);
    }
}
