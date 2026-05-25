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

}


