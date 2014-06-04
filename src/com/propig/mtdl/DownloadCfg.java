package com.propig.mtdl;

import java.io.Serializable;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: simian
 * Date: 10/28/13
 * Time: 2:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class DownloadCfg implements Serializable{
    private String protocol = null;
    private String fileName = null;
    private URL url = null;
    private int part = 0;
    private long[] partArray = null;
    private long fileLength = 0;

    class PartItem{
        public PartItem(long low, long high){
            this.lowByte = low;
            this.highByte = high;
        }
        public long lowByte;
        public long highByte;
    }
    public DownloadCfg(String protocol, String fileName, URL url, int part, long[] partArray){
        this.part = part;
        this.partArray = partArray;
        this.protocol = protocol;
        this.fileName = fileName;
        this.url = url;
        this.fileLength = fileLength;
    }
    public String getProtocol(){
        return protocol;
    }
    public String getFileName(){
        return fileName;
    }
    public URL getUrl(){
        return url;
    }
    public int getPart(){
        return part;
    }
    public long[] getPartArray(){
        return partArray;
    }
    public PartItem getPartItem(int part){
        if(partArray == null) return null;
        if(part <= 0 || part > partArray.length)
            throw new IndexOutOfBoundsException("Index out of bound.");
        PartItem item = new PartItem(partArray[(part-1)*2], partArray[(part-1)*2 + 1]);
        return item;
    }
    public long getFileLength(){
        return fileLength;
    }
}
