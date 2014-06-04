package com.propig.mtdl;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;

/**
 * Created with IntelliJ IDEA.
 * User: simian
 * Date: 10/25/13
 * Time: 10:19 AM
 * To change this template use File | Settings | File Templates.
 */
public class HttpHandler {
    private URL url = null;
    private long contentLength = 0;
    private String contentType = null;
    private int part = 0;
    private long piece = 0;
    private long[] partArray = null;
    private String fileName = null;

    public HttpHandler(URL url){
        this.url = url;
        this.getDownloadName();
        this.analyseURL();
    }

    public HttpHandler(DownloadCfg cfg){
        this.url = cfg.getUrl();
        this.contentLength = cfg.getFileLength();
        this.contentType = cfg.getProtocol();
        this.part = cfg.getPart();
        this.partArray = cfg.getPartArray();
        this.fileName = cfg.getFileName();
    }

    public void updateConfig(){
        //
    }

    private void createDownloadFile(){
        // 1. Create a binary file, size = contentLength
        RandomAccessFile file = null;
        try{
            File f = new File(fileName);
            if(f.exists()){
                f.delete();
            }
            f.createNewFile();

            file = new RandomAccessFile(fileName, "rw");
            file.setLength(contentLength);
        }
        catch (FileNotFoundException e){
        }
        catch (IOException e){
        }
        finally {
            if(file != null){
                try{
                    file.close();
                }
                catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    public void downloadFile(){
        Thread [] threads = null;
        Timer timer = new Timer();
        try{
            if(part == 1){
                DownloadWorker t = new DownloadWorker(fileName, 0, contentLength, url);
                t.start();
                t.join();
            }
            else{
                threads = new Thread[part];
                for( int i=0; i<part; i++){
                    DownloadWorker t = new DownloadWorker(fileName, partArray[i*2 ], partArray[i*2 + 1], url);
                    t.start();
                    threads[i] = t;
                }
                for(int i=0; i<threads.length; i++){
                    threads[i].join();
                }
            }
        }
        catch (InterruptedException e){
            // Deal with Interrupt
        }
    }

    public void testAnalyseURL(){
        System.out.printf("The download file name is %s\n", fileName);

        if(partArray == null) {
            System.out.printf("The file length is %d\n", contentLength);
        }
        else{

            System.out.printf("The file length is %d, divided into %d parts.\n", contentLength, part);
            for(int i = 0; i<partArray.length ; i+=2){
                System.out.printf("%10d:%10d\n", partArray[i], partArray[i + 1]);
            }
        }
        createDownloadFile();
        downloadFile();
        /*
        for(int i=0; i<part; i++){
            DownloadWorker t = new DownloadWorker(fileName, partArray[i*2 ], partArray[i*2 + 1], url);
            t.start();
        }
        */

    }

    private void getDownloadName(){
        String[] tmp = url.getPath().split("/");
        fileName = tmp[tmp.length - 1];
    }

    private void analyseURL(){
        if(url == null)
            return ;
        try{
            URLConnection conn = url.openConnection();
            contentLength = conn.getContentLengthLong();
            contentType = conn.getContentType();

            //decide how many parts will be divided.
            if(contentLength < 1024 *1024){
                // do not set parts
                part = 1;
            }else if(contentLength < 1024 * 1024 * 10){
                // set 3 parts
                part = 3;
            }else{
                // set 5 parts
                part = 5;
            }
            if(part == 1){
                return;
            }
            else{
                partArray = new long[part * 2];
                piece = contentLength / part;
                long begin = 0;
                long chunk = 0;
                for(int i = 0; i < part; i++ ){
                    partArray[i*2] = begin;
                    if(i == part -1){
                        partArray[i*2 + 1] = contentLength -1;
                    }
                    else{
                        chunk = piece + begin - 1;
                        partArray[i*2 + 1] = chunk;
                        begin = chunk + 1;
                    }
                }
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
