package com.propig.mtdl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;

public class Handler {
	protected long startTime = 0L;
    protected DownloadCfg cfg = null;
    protected boolean isContinue = false;
	
	protected static DownloadCfg isCfgFileAvailable(URL url){
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


}
