package com.propig.mtdl;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: simian
 * Date: 10/28/13
 * Time: 2:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class Main {
    private URL url;
    private static void usage(){
        System.out.println("Need parameter.");
    }

    public  URL getURL(){
        return url;
    }


    public static void main(String[] args){
        if(args.length < 1){
            usage();
            System.exit(1);
        }


        Main jget = new Main();
        try{
            jget.getUrl(args[0]);
        }
        catch (MalformedURLException e){
            System.out.println(e.getMessage());
            System.exit(-1);
        }

        if(jget.getProtocol().compareToIgnoreCase("http") == 0){
            //Call http handler
            HttpHandler handler = new HttpHandler(jget.getURL());
            handler.testAnalyseURL();
        }

    }
    public String getProtocol(){
        return url.getProtocol();
    }

    public void getUrl(String str) throws MalformedURLException{
        //To do, if there is no protocol, add the default one, http
        url = new URL(str);
    }
}
