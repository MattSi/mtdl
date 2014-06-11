package com.propig.mtdl.http;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class HttpHandler {
    private long startTime = 0L;
    private DownloadCfg cfg = null;
    private boolean isContinue = false;


    class ExitHandler extends Thread{
        DownloadWorker [] threads = null;
        public ExitHandler(DownloadWorker[] threads){
            this.threads = threads;
        }
        @Override
        public void run(){
            //System.out.println("Catch crtl c\n");
            try{
                if(threads == null) return;
                for(int i=0; i< threads.length; i++){
                    threads[i].bExit = true;
                }
                Thread.sleep(500);
            }
            catch (InterruptedException e){}

        }
    }


    class DownloadSpeed extends TimerTask{
        private boolean isOnlyThread = true;
        private DownloadWorker thread = null;
        private DownloadWorker[] threads = null;

        public DownloadSpeed(DownloadWorker thread){
            this.thread = thread;
        }
        public DownloadSpeed(DownloadWorker[] threads){
            isOnlyThread = false;
            this.threads = threads;
        }

        private void checksingleSpeed(){
            if(thread == null) return;
            long now = new Date().getTime();
            double speed = thread.getFileSizeDl() * 1.0 / (now - startTime) * 1000 / 1024;    //KBps
            double percent = thread.getFileSizeDl() * 1.0 / cfg.contentLength* 100;
            System.out.printf(
                    "Thread Number: 1,     Speed: %5.3fKBps,     Progress: %%%5.3f\n",
                    speed, percent);
        }

        private void checkMultiSpeed(){
            if(threads == null) return;
            long sumOfSize = 0;
            int numOfThread = 0;
            long speedSize = 0;

            for(int i=0; i<cfg.part; i++){
                sumOfSize += (cfg.partArray[i*2 + 1] - cfg.partArray[i*2] + 1);
            }
            sumOfSize = cfg.contentLength - sumOfSize;

            for(int i=0; i<threads.length; i++){
                if(threads[i] == null) continue;
                long size = threads[i].getFileSizeDl();
                sumOfSize += size;
                speedSize += size;
                cfg.partBytesGet[i] = size;
                numOfThread++;
                //System.out.printf("Thread %3d: begin byte %10d, end byte %10d, progress byte %10d\n",
                  //      i, cfg.partArray[i*2], cfg.partArray[i*2 + 1], cfg.partBytesGet[i]);
            }

            long now = new Date().getTime();
            double speed = (speedSize*1.0) / (now - startTime) *1000 / 1024;
            double percent = (sumOfSize *1.0) / cfg.contentLength * 100;
            System.out.printf("Thread Number: %d,     Current size: %5d, Speed : %5.3f,      Progress: %%%5.3f\n",
                    numOfThread, sumOfSize, speed, percent);

            try{
                ObjectOutputStream oos=new ObjectOutputStream(new FileOutputStream(cfg.fileName+".cfg"));
                oos.writeObject(cfg);
                oos.close();
            }

            catch (FileNotFoundException e){}
            catch (IOException e){}

        }

        @Override
        public void run(){
            if(isOnlyThread){
                checksingleSpeed();
            }
            else{
                checkMultiSpeed();
            }
        }
    }

    public HttpHandler(URL url){
        this.createCfg(url);
        this.isContinue = false;
    }

    public HttpHandler(DownloadCfg cfg){
        this.cfg = cfg;
        this.isContinue = true;
    }

    public void download(){
        testAnalyseURL();
    }
    public void updateConfig(){
        //
    }

    private void createDownloadFile(){
        // 1. Create a binary file, size = contentLength
        RandomAccessFile file = null;
        try{
            File f = new File(cfg.fileName);
            if(isContinue){
            }else{
                if(f.exists()){
                    f.delete();
                }
                f.createNewFile();

                file = new RandomAccessFile(cfg.fileName, "rw");
                file.setLength(cfg.contentLength);
            }
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
        DownloadWorker [] threads = null;
        startTime = new Date().getTime();
        Timer timer = new Timer(true);

        try{
            if(cfg.part == 1){
                DownloadWorker t = new DownloadWorker(
                        cfg.fileName, 0, cfg.contentLength, cfg.url);
                timer.scheduleAtFixedRate(new DownloadSpeed(t), 200, 600 );
                t.setDaemon(true);
                t.start();
                t.join();
            }
            else{

                threads = new DownloadWorker[cfg.part];
                for(int i=0; i<cfg.part; i++){
                    DownloadWorker t = new DownloadWorker(
                            cfg.fileName,
                            cfg.partArray[i*2] + cfg.partBytesGet[i],
                            cfg.partArray[i*2 + 1],
                            cfg.url);
                    cfg.partArray[i*2] += cfg.partBytesGet[i];
                    cfg.partBytesGet[i] = 0;
                    t.setDaemon(true);
                    t.start();
                    threads[i] = t;
                }

                timer.scheduleAtFixedRate(new DownloadSpeed(threads), 1000, 800);

                Runtime.getRuntime().addShutdownHook(new ExitHandler(threads));

                for(int i=0; i<threads.length; i++){
                    threads[i].join();
                }
                Thread.sleep(1000);
                timer.cancel();

            }
        }
        catch (InterruptedException e){
            // Deal with Interrupt
        }
        finally {
            timer.cancel();
        }
    }

    public void testAnalyseURL(){
        System.out.printf("The download file name is %s\n", cfg.fileName);

        if(cfg.partArray == null) {
            System.out.printf("The file length is %d\n", cfg.contentLength);
        }
        else{

            System.out.printf("The file length is %d, divided into %d parts.\n",
                    cfg.contentLength, cfg.part);
            for(int i = 0; i<cfg.part ; i++){
                System.out.printf("%10d:%10d\n", cfg.partArray[i*2],
                        cfg.partArray[i*2 + 1]);
            }
        }
        createDownloadFile();
        downloadFile();
    }


    public static DownloadCfg isCfgFileAvailable(URL url){
        String[] tmp = url.getPath().split("/");
        String fileName = tmp[tmp.length - 1];
        File fileConfig = new File(fileName+".cfg");
        File f = new File(fileName);
        ObjectInputStream ois = null;
        DownloadCfg cfg = null;
        try{
            if(f.exists()){
                if(fileConfig.exists()){
                    ois = new ObjectInputStream(new FileInputStream(fileConfig));
                    cfg = (DownloadCfg) ois.readObject();
                    ois.close();
                    if(f.length() == cfg.contentLength){
                        return cfg;
                    }
                    else
                        return null;
                }
                else
                    return null;
            }
            else
                return null;
        }
        catch (ClassNotFoundException e){
            try{ois.close();}catch (IOException ex){}
            fileConfig.delete();
            return null;
        }
        catch (IOException e){
            try{ois.close();}catch (IOException ex){}
            fileConfig.delete();
            return null;
        }
    }

    private void createCfg(URL url){
        if(url == null)
            return;
        try{
            URLConnection conn = url.openConnection();
            long contentLength = -1;
            cfg = new DownloadCfg();
            cfg.url = url;
            String[] tmp = url.getPath().split("/");
            contentLength = conn.getContentLengthLong();
            cfg.contentLength = contentLength;
            cfg.fileName = tmp[tmp.length - 1];
            cfg.protocol = url.getProtocol();

            //decide how many parts will be divided.
            if(contentLength < 1024 *1024){
                // do not set parts
                cfg.part = 1;
            }else if(contentLength < 1024 * 1024 * 10){
                // set 3 parts
                cfg.part = 3;
            }else{
                // set 5 parts
                cfg.part = 5;
            }
            if(cfg.part == 1){
                return;
            }
            else{
                long[] partArray = new long[cfg.part * 2];
                long piece = contentLength / cfg.part;
                long begin = 0;
                long chunk = 0;
                for(int i = 0; i < cfg.part; i++ ){
                    partArray[i*2] = begin;
                    if(i == cfg.part -1){
                        partArray[i*2 + 1] = contentLength -1;
                    }
                    else{
                        chunk = piece + begin - 1;
                        partArray[i*2 + 1] = chunk;
                        begin = chunk + 1;
                    }
                }
                cfg.partArray = partArray;
                cfg.partBytesGet = new long[cfg.part];
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}

