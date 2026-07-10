package com.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Arrays;
public class Main {


    private static final int MAX_REDIRECTS = 5;
    private static final String USER_AGENT =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) " +
                    "AppleWebKit/605.1.15 (KHTML, like Gecko) " +
                    "Version/17.5 Safari/605.1.15";

    public static void main(String[] args) throws IOException {


        if(args.length == 0 || args[0].equals("h")){
            printHelp();
            return;
        }

        switch (args[0]) {
            case "-u":
                if (args.length<2){
                    System.out.println("Missing URL.");
                    printHelp();
                }
                else {
                    fetchAndPrint(args[1]);
                }
            case "-s":
                if (args.length<2){
                    System.out.println("Missing search term.");
                    printHelp();
                }
                else {
                    search(String.join(" ",Arrays.copyOfRange(args,1,args.length)));
                }

            default: printHelp();

        }
    }
    static void printHelp(){
        System.out.println("Usage:");
        System.out.println("-h Show help");
        System.out.println("-s <search terms> Search DuckDuckGo");
        System.out.println("-u <url> Fetch and print all text from url, with a limit of "+MAX_REDIRECTS+" redirects");
    }

    static void fetchAndPrint(String urlString) throws IOException {
        HttpResponse httpResponse = fetchWithRedirects(URI.create(urlString));
        Document document = Jsoup.parse(httpResponse.body);
        String body = document.text();
        System.out.println(body);
    }

    static void search(String searchedItem) throws IOException {
        String encoded = URLEncoder.encode(searchedItem, StandardCharsets.UTF_8);
        URI uri = URI.create("https://html.duckduckgo.com/html/?q="+encoded);
        HttpResponse httpResponse = fetchWithRedirects(uri);
        Document document = Jsoup.parse(httpResponse.body, uri.toString());
        Elements results  = document.select("a.results__a");
        for(Element link : results.subList(0, Math.min(10, results.size()))){
            String title = link.text();
            String href = link.absUrl("href");
            System.out.println(title +" - "+href);
        }
    }

    static HttpResponse fetchWithRedirects(URI uri) throws IOException {
        int redirects = 0;
        while (true){
            HttpResponse response = sendHttpRequest(uri);

            if(response.statusCode>=300 && response.statusCode<400){
                String location = response.headers.get("location");
                if(location==null) return response;
                uri = uri.resolve(location);
                redirects++;
                if(redirects>=MAX_REDIRECTS){
                    throw new IOException("Too many redirects");
                }

            }
            else {
                return response;
            }

        }
    }

    static HttpResponse sendHttpRequest(URI uri) throws IOException {
        String host = uri.getHost();
        String path = uri.getRawPath();
        String scheme = uri.getScheme();
        String query = uri.getRawQuery();
        HttpResponse httpResponse;
        int port;
        if (host == null || scheme == null) {
            throw new IOException("Missing host or scheme");
        }
        if (path == null || path.isBlank()) {
            path = "/";
        }
        String requestTarget = path;
        if (query != null) {
            requestTarget += "?" + query;
        }
        Socket socket;
        if (scheme.equalsIgnoreCase("https")) {
            port = 443;
            SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            socket = sslSocketFactory.createSocket(host, port);
        } else if(scheme.equalsIgnoreCase("http")){
            port = 80;
            socket = new Socket(host, port);
        }
        else {
            throw new IOException("Unsupported scheme: "+ scheme);
        }
        try(socket;
        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream()){
        String httpRequest = "GET " + requestTarget + " HTTP/1.1\r\n" +
                "Host: " + host + "\r\n" +
                "User-Agent: " + USER_AGENT + "\r\n" +
                "Accept: text/html\r\n" +
                "Accept-Encoding: identity\r\n" +
                "Connection: close\r\n"
                + "\r\n";
        outputStream.write(httpRequest.getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bytes = new byte[2048];
        int n;
        while(( n = inputStream.read(bytes))!=-1){
            byteArrayOutputStream.write(bytes,0,n);
            }
         httpResponse = parseHttpResponse(byteArrayOutputStream.toString(StandardCharsets.UTF_8));
    }
        return httpResponse;
    }


    static HttpResponse parseHttpResponse(String plainText) throws IOException {
        String separator = "\r\n\r\n";
        if(!plainText.contains(separator)){
            throw new IOException("Invalid HTTP response");
        }
        String[] dividedResponse = plainText.split(separator);
        String header = dividedResponse[0];
        String body = dividedResponse[1];
        String[] headerLines = header.split("\r\n");
        int statusCode = Integer.parseInt(headerLines[0].split(" ")[1]);
        LinkedHashMap<String, String> headers = new LinkedHashMap<>();
        String headerName;
        String headerValue;
        for(int i = 1; i< headerLines.length; i++){
            String headerLine = headerLines[i];
            headerName = headerLine.substring(0, headerLine.indexOf(":")).trim().toLowerCase();
            headerValue = headerLine.substring(headerLine.indexOf(":")+1).trim();
            headers.put(headerName,headerValue);
        }
        return new HttpResponse(statusCode,headers,body);

    }

    static class HttpResponse {
        int statusCode;
        Map<String, String> headers;
        String body;

        HttpResponse(int statusCode, Map<String, String> headers, String body) {
            this.statusCode = statusCode;
            this.headers = headers;
            this.body = body;
        }
    }
}

