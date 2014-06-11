package com.propig.mtdl;

import com.propig.mtdl.http.DownloadCfg;
import com.propig.mtdl.http.HttpHandler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

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
            jget.generateURL(args[0]);
        }
        catch (MalformedURLException e){
            System.out.println(e.getMessage());
            System.exit(-1);
        }

        if(jget.getProtocol().compareToIgnoreCase("http") == 0){

            //If config file doesnt exist, Call http handler
            //Else Call
            HttpHandler handler = null;
            DownloadCfg cfg = HttpHandler.isCfgFileAvailable(jget.getURL());
            if(cfg == null)
                handler = new HttpHandler(jget.getURL());
            else
                handler = new HttpHandler(cfg);
            handler.download();
            //handler.testAnalyseURL();
        }
        else if(jget.getProtocol().compareToIgnoreCase("ftp") == 0){
            try{
                URL url = jget.getURL();
                System.out.printf("%s\n", url.getFile());
                URLConnection conn = url.openConnection();
                conn.connect();
                System.out.printf("%d\n", conn.getContentLength());
            }
            catch (IOException e){}
        }

    }
    public String getProtocol(){
        return url.getProtocol();
    }

    public void generateURL(String str) throws MalformedURLException{
        //To do, if there is no protocol, add the default one, http
        url = new URL(str);
    }
}
