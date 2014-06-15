package com.propig.mtdl;

import java.io.Serializable;
import java.net.URL;


public class DownloadCfg implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1825080457510727560L;
	public String protocol = null;
    public String fileName = null;
    public URL url = null;
    public long contentLength = -1;
    public int part = -1;
    public long[] partArray = null;
    public long[] partBytesGet = null;
    
    //For FTP use
    public String host = null;
    public String user = null;
    public String password = null;
    public String path = null;
    public int port = -1;

    public DownloadCfg(){}

}
