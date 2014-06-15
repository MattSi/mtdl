package com.propig.mtdl;

import com.propig.mtdl.ftp.FtpHandler;
import com.propig.mtdl.http.HttpHandler;

import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


public class Main {
    private URL url;
    private static void usage(){
        System.out.println("Need parameter.");
    }

    public  URL getURL(){
        return url;
    }

    public static void main(String[] args) throws MalformedURLException{
        if(args.length < 1){
            usage();
            String testUrl = "http://dl.bintray.com/oneclick/rubyinstaller/rubyinstaller-2.0.0-p481.exe?direct";
            URL url = new URL(testUrl);
            System.out.print(url.getPort());
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
            
            DownloadCfg cfg = Handler.isCfgFileAvailable(jget.getURL());
            if(cfg == null)
                handler = new HttpHandler(jget.getURL());
            else
                handler = new HttpHandler(cfg);
            handler.download();
            //handler.testAnalyseURL();
        }
        else if(jget.getProtocol().compareToIgnoreCase("ftp") == 0){
        	//FtpHandler handler = null;
        	FtpHandler handler = null;
        	DownloadCfg cfg = Handler.isCfgFileAvailable(jget.getURL());
        	if(cfg == null)
        		handler = new FtpHandler(jget.getURL());
        	else
        		handler = new FtpHandler(cfg);
        	
        	handler.Download();
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
