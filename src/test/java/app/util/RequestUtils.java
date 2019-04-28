package app.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

public class RequestUtils {
    public static HttpURLConnection prepareRequest(String url, String method) throws IOException {
        URL requestUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
        connection.setRequestMethod(method);

        return connection;
    }

    public static HttpURLConnection prepareRequest(String url, String method, Map<String, String> params) throws IOException {
        HttpURLConnection connection = prepareRequest(url, method);

        connection.setDoOutput(true);
        DataOutputStream paramsStream = new DataOutputStream(connection.getOutputStream());
        paramsStream.writeBytes(getParamsString(params));
        paramsStream.flush();
        paramsStream.close();

        return connection;
    }

    public static String readResponse(HttpURLConnection connection) throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = input.readLine()) != null) {
            content.append(inputLine);
        }
        input.close();

        return content.toString();
    }

    public static String getParamsString(Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            result.append("&");
        }

        String resultString = result.toString();
        return resultString.length() > 0
                ? resultString.substring(0, resultString.length() - 1)
                : resultString;
    }
}
