package com.smartbear.aws.amazon;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.support.StringUtils;
import com.smartbear.aws.ApplicationException;
import com.smartbear.aws.Strings;

import javax.json.JsonObject;
import javax.json.stream.JsonParsingException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;

public final class HttpRequestExecutor {
    private final SignatureBuilder signatureBuilder;

    public HttpRequestExecutor(String accessKey, String secretKey, String region) {
        this.signatureBuilder = new ApiManagmentSignatureBuilder(accessKey, secretKey, region);
    }

    public JsonObject perform(String method, String path, String query) throws ApplicationException {
        return perform(method, path, query, "");
    }

    public JsonObject perform(String method, String path, String query, String body) throws ApplicationException {
        URLConnection connection = getConnection(method, path, query, body);

        try {
            connection.connect();
        } catch (IOException e) {
            throw new ApplicationException(String.format(Strings.Error.UNAVAILABLE_HOST, connection.getURL()), e);
        }

        if (StringUtils.hasContent(body)) {
            try (OutputStream output = connection.getOutputStream()) {
                output.write(body.getBytes("UTF-8"));
            } catch (IOException ex) {
                throw new ApplicationException(Strings.Error.UNABLE_SET_REQEST_BODY, ex);
            }
        }

        Reader reader;
        try {
            reader = new InputStreamReader(connection.getInputStream());
        } catch (FileNotFoundException e) {
            throw new ApplicationException(String.format(Strings.Error.UNAVAILABLE_DATA, connection.getURL()), e);
        } catch (IOException e) {
            throw new ApplicationException("", e);
        }

        try (javax.json.JsonReader jsonReader = javax.json.Json.createReader(reader)) {
            return jsonReader.readObject();
        } catch (JsonParsingException e) {
            throw new ApplicationException(String.format(Strings.Error.INVALID_JSON_RESPONSE, connection.getURL()), e);
        }
    }

    private URLConnection getConnection(String method, String path, String query, String body) throws ApplicationException {
        String authHeader = signatureBuilder.buildAuthHeader(method, path, query, body);
        String urlString = "https://" + signatureBuilder.getHost() + path + (StringUtils.isNullOrEmpty(query) ? "" : "?" + query);

        URLConnection connection;
        try {
            URL url = new URL(urlString);
            connection = url.openConnection();
        } catch (MalformedURLException e) {
            throw new ApplicationException(String.format(Strings.Error.MALFORMED_URL, urlString), e);
        } catch (IOException e) {
            throw new ApplicationException(String.format(Strings.Error.UNABLE_CREATE_CONNECTION, urlString), e);
        }

        connection.setDoInput(true);
        if (StringUtils.hasContent(body)) {
            connection.setDoOutput(true);
        }
        connection.setRequestProperty("Content-Type", "application/x-amz-json-1.0");
        connection.setRequestProperty("Accept", SignatureBuilder.ACCEPT_TYPE);
        connection.setRequestProperty("X-Amz-Date", signatureBuilder.getAmzDate());
        connection.setRequestProperty("Authorization", authHeader);
        HttpURLConnection httpConnection = (HttpURLConnection)connection;
        if (httpConnection != null) {
            try {
                httpConnection.setRequestMethod(method);
            } catch (ProtocolException ex) {
                SoapUI.logError(ex);
            }
        }

        return connection;
    }
}
