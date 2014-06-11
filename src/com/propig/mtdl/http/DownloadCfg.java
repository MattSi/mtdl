package com.propig.mtdl.http;

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
    public String protocol = null;
    public String fileName = null;
    public URL url = null;
    public long contentLength = -1;
    public int part = -1;
    public long[] partArray = null;
    public long[] partBytesGet = null;

    public DownloadCfg(){}

}
