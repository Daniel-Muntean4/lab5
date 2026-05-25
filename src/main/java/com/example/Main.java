package com.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.print.DocFlavor;
import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;


public class Main {
    public static void main(String[] args) throws IOException {
        int counter = 0;
        System.out.println("Hello world!");
        int port = 80;
        if(args.length<2){
            System.out.println("-h for help, -s for search, -u for url");

        }
        if(args[0].equals("-h")){
            System.out.println("-h for help, -s for search, -u for url");
        }
        if(args[0].equals("-u")){
            fetchAndPrint(args[1]);
        }
        if(args[0].equals("-s")){
            StringBuilder searchedTerm = new StringBuilder();
            for(int i= 1; i<args.length;i++){
                searchedTerm.append(args[i]+" ");
            }
            search(searchedTerm.toString());

        }



    }

    static void fetchAndPrint(String urlString) throws IOException {
        int counter = 0;
        System.out.println("Hello world!");
        int port = 80;
        URI uri = URI.create(urlString);
        while(true){
            String scheme = uri.getScheme();
            String host = uri.getHost();
            String path = uri.getRawPath();
            String query = uri.getRawQuery();
            Socket socket;

            if (scheme.equals("https")) {
                SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                socket = sslSocketFactory.createSocket(host, 443);
            }
            else {
                socket = new Socket(host, 80);
            }
            String requestTarget=path;
            if(!(query==null)){
                requestTarget=path+query;

            }
            System.out.println(requestTarget);
            String httpRequest = "GET " + requestTarget + " HTTP/1.1\r\n" +
                    "Host: " + host + "\r\n" +
                    "User-agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Safari/605.1.15\r\n" +
                    "Accept: text/html\r\n" +
                    "Accept-encoding: identity\r\n" +
                    "Connection: close\r\n" +
                    "\r\n";

            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(httpRequest.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();

            InputStream inputStream = socket.getInputStream();

            int n;
            byte[] chunk = new byte[4096];

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            while((n=inputStream.read(chunk))!=-1){
                buffer.write(chunk,0,n);
            }

            String plainText = buffer.toString(StandardCharsets.UTF_8);
            int separator = plainText.indexOf("\r\n\r\n");
            String bodyText = plainText.substring(separator);
            Document document = Jsoup.parse(bodyText);

            String parsedText = Jsoup.parse(bodyText).text();
            String[] parsedArray = plainText.split(" ");
            String httpCodeResponse = parsedArray[1];
            System.out.println(parsedText);
            System.out.println(uri.toString());
            System.out.println(document.location());
            if(httpCodeResponse.contains("30")){
                uri = uri.resolve(document.location());
                counter++;
                if (counter>=5){
                    break;
                }
                else {
                    continue;
                }
            }

            break;

        }
    }

    static void search(String searchedItem) throws IOException {
        String encoded = URLEncoder.encode(searchedItem, StandardCharsets.US_ASCII);
        String url = "https://html.duckduckgo.com/html/?q="+encoded;
        URI uri = URI.create(url);
        String scheme = uri.getScheme();
        String host = uri.getHost();
        String path = uri.getRawPath();
        String query = uri.getRawQuery();
        Socket socket;

        if (scheme.contains("https")) {
            SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            socket = sslSocketFactory.createSocket(host, 443);
        }
        else {
            socket = new Socket(host, 80);
        }
        String httpRequest = "GET " + "/html/?q=" + encoded+ " HTTP/1.1\r\n" +
                "Host: " + host + "\r\n" +
                "User-agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Safari/605.1.15\r\n"
                +"Accept: text/html\r\n" +
                "Accept-encoding: identity\r\n" +
                "Connection: close\r\n" +
                "\r\n";

        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(httpRequest.getBytes(StandardCharsets.UTF_8));
        outputStream.flush();

        InputStream inputStream = socket.getInputStream();

        int n;
        byte[] chunk = new byte[4096];

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        while((n=inputStream.read(chunk))!=-1){
            buffer.write(chunk,0,n);
        }

        String plainText = buffer.toString(StandardCharsets.UTF_8);

        String parsedText = Jsoup.parse(plainText).text();
        String[] parsedArray = plainText.split(" ");
        String httpCodeResponse = parsedArray[1];
        System.out.println(encoded);
        Document document = Jsoup.parse(plainText);
        System.out.println(plainText+" "+httpCodeResponse);
        System.out.println();
        Elements results = document.select("a.result__a");
        for (Element link : results.subList(0, Math.min(10, results.size()))){
            String title = link.text();
            String href = link.attr("href");
            System.out.println(title + " - " + href);
        }



    }
}


