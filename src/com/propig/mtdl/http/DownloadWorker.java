package com.propig.mtdl.http;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: simian
 * Date: 10/28/13
 * Time: 9:08 AM
 * To change this template use File | Settings | File Templates.
 */
public class DownloadWorker extends Thread {
    private String fileName = null;
    private long lowByte = 0;
    private long highByte = 0;
    private URL url = null;
    private long contentLength = 0;
    private byte[] buffer = null;
    private long fileSizeDl = 0;
    public boolean bExit = false;

    public DownloadWorker(String fileName, long lowByte, long highByte, URL url){
        this.fileName = fileName;
        this.lowByte = lowByte;
        this.highByte = highByte;
        this.url = url;
        buffer = new byte[1024 * 1024 * 2]; // Open 2MB buffer
    }


    public long getFileSizeDl(){
        return fileSizeDl;
    }

    private void doDownload(){

    }

    @Override
    public void run(){
        try{
            if(lowByte > highByte) return;
            // Update range information
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            String rangeInfo = String.format("bytes=%d-%d", lowByte, highByte);
            conn.setRequestProperty("Range", rangeInfo);
            conn.connect();
            if( (contentLength = conn.getContentLengthLong()) == -1){
                // To do , wait and retry
                return;
            }

            RandomAccessFile file = new RandomAccessFile(fileName, "rw");
            file.seek(lowByte);
            BufferedInputStream input = new BufferedInputStream(conn.getInputStream());

            while(!bExit){
                int datalen = input.read(buffer);
                if(datalen == -1){
                    // We have done here.
                    break;
                }
                fileSizeDl += datalen;
                file.write(buffer, 0, datalen);
            }
            Thread.sleep(500);
            if(file != null){
                file.close();
            }
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }
        finally {
            // Close the file description
        }
    }
}
