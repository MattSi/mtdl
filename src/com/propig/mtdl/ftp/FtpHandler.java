package com.propig.mtdl.ftp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.TimerTask;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import com.propig.mtdl.DownloadCfg;
import com.propig.mtdl.Handler;

public class FtpHandler extends Handler {
	public FtpHandler (){}

	public FtpHandler (URL url){
		createCfg(url);
		this.isContinue = false;
	}
	public FtpHandler (DownloadCfg cfg){
		this.cfg = cfg;
		this.isContinue = true;
	}
	
	public class DownloadWorker implements Runnable{
		private int part = -1;
		private long total = -1; //total bytes to read
		public boolean successful = false;
		public boolean bExit = false;
		public DownloadWorker(int part){
			this.part = part;
			this.total = cfg.partArray[(part )*2 + 1] - cfg.partArray[(part)*2] + 1;
			
		}
		
		@Override
		public void run(){
			FTPClient ftp = new FTPClient();
			byte[] buff = new byte[1024 * 1024];
			long current = 0;
			try{
				ftp.connect(cfg.host, cfg.port);
				int reply = ftp.getReplyCode();
				if(!FTPReply.isPositiveCompletion(reply)){
					ftp.disconnect();
					throw new IOException("connection failed.");
				}
				
				/*
				ftp.login(cfg.user, cfg.password);
				ftp.setFileType(FTP.BINARY_FILE_TYPE);
				ftp.setRestartOffset(cfg.partArray[part]);
				*/	
				//return ftp.retrieveFileStream(cfg.path);
			}
			catch(IOException e){	
				this.successful=false;
			}
			
	__main:
			try{
				if(!ftp.login(cfg.user, cfg.password)){
					ftp.logout();
					System.err.printf("Thread %d login failed.", part);
					break __main;
				}
				ftp.setFileType(FTP.BINARY_FILE_TYPE);
				ftp.setRestartOffset(cfg.partArray[part * 2]);
				BufferedInputStream is = new BufferedInputStream(ftp.retrieveFileStream(cfg.path));
				RandomAccessFile file = new RandomAccessFile(cfg.fileName, "rw");
				file.seek(cfg.partArray[(part)*2]);
				
				while(!bExit){
					int datalen = is.read(buff);
					if(datalen == -1)
						break;
					if(datalen + current <= total){
						file.write(buff, 0, datalen);
						current += datalen;
					}else{
						file.write(buff, 0, (int)(total-current));
						System.out.println(total-current);
						bExit = true;
					}
					cfg.partBytesGet[part] = datalen;
					System.out.printf("Thread %d get %d bytes data\n", part, datalen);
				}
				Thread.sleep(500);
				if(file != null)
					file.close();
				is.close();
			}
			catch(IOException e){e.printStackTrace();}
			catch(InterruptedException e){e.printStackTrace();}
			
		}
	}
	class DownloadSpeed extends TimerTask{
		
		@Override
		public void run(){
			for(int i=0; i<cfg.part; i++){
				
			}
		}
	}
	private InputStream getStreamFromFTP(int part){
		FTPClient ftp = new FTPClient();
		try{
			ftp.connect(cfg.host, cfg.port);
			int reply = ftp.getReplyCode();
			if(!FTPReply.isPositiveCompletion(reply)){
				ftp.disconnect();
				throw new IOException("connection failed.");
			}
			
			ftp.login(cfg.user, cfg.password);
			ftp.setFileType(FTP.BINARY_FILE_TYPE);
			ftp.setRestartOffset(cfg.partArray[part - 1]);
			
			return ftp.retrieveFileStream(cfg.path);
		}
		catch(IOException e){	
			return null;
		}
	}
	public void Download() {
		if(cfg.part == 1)
			return ;
		File f = new File(cfg.fileName);
		System.out.printf("The file length is %d, divided into %d parts.\n",
                cfg.contentLength, cfg.part);
        for(int i = 0; i<cfg.part ; i++){
            System.out.printf("%10d:%10d\n", cfg.partArray[i*2],
                    cfg.partArray[i*2 + 1]);
        }
		try{
		f.createNewFile();
		RandomAccessFile file = new RandomAccessFile(cfg.fileName, "rw");
		file.setLength(cfg.contentLength);
		
		Thread [] threads = new Thread[cfg.part];
		for(int i=0; i<cfg.part; i++){
			threads[i] = new Thread(new DownloadWorker(i));
			threads[i].start();
		}
		System.out.println("All thread started");
		
			for(int i=0; i<cfg.part;i++){
				threads[i].join();
			}
		}
		catch(IOException e){}
		catch(InterruptedException e){
			
		}
	}
	private void createCfg(URL url){
		this.cfg = new DownloadCfg();
		cfg.protocol = url.getProtocol();
		cfg.url = url;
		cfg.host = url.getHost();
		if(url.getPort() == -1)
			cfg.port = 21;
		else
			cfg.port = url.getPort();
		cfg.path = url.getPath();
		String[] tmp = url.getPath().split("/");
		cfg.fileName = tmp[tmp.length - 1];
		if(url.getUserInfo() == null){
			cfg.user = "anonymous";
			cfg.password = "anonymous";
		}else{
			String[] userinfo = url.getUserInfo().split(":");
			cfg.user = userinfo[0];
			cfg.password = userinfo[1];
		}
		FTPClient ftp = new FTPClient();
		try{
			if(cfg.port > 0){
				ftp.connect(cfg.host, cfg.port);
			}else{
				ftp.connect(cfg.host);
			}
			int reply = ftp.getReplyCode();
			if(!FTPReply.isPositiveCompletion(reply)){
				ftp.disconnect();
				System.err.println("FTP server refuse connection");
			}
			
			ftp.login(cfg.user, cfg.password);
			ftp.setFileType(FTP.BINARY_FILE_TYPE);
			FTPFile[] files = ftp.listFiles(cfg.path);
			if(files == null){
				//throw new IOException("File not exist.");
				System.err.println("File not exist");
				System.exit(-1);
			}
			cfg.contentLength = files[0].getSize();
			System.out.println(cfg.contentLength);
			//decide how many parts will be divided.
            if(cfg.contentLength < 1024 *1024){
                // do not set parts
                cfg.part = 1;
            }else if(cfg.contentLength < 1024 * 1024 * 10){
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
                long piece = cfg.contentLength / cfg.part;
                long begin = 0;
                long chunk = 0;
                for(int i = 0; i < cfg.part; i++ ){
                    partArray[i*2] = begin;
                    if(i == cfg.part -1){
                        partArray[i*2 + 1] = cfg.contentLength -1;
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
            
            //Test code
//            OutputStream os = new FileOutputStream("test",false);
//            
//            SocketInputStream sis = (SocketInputStream) ftp.retrieveFileStream(cfg.path);
//            
//            System.out.println("haha");
//            os.close();
			ftp.disconnect();
		}
		catch(IOException e){
			if(ftp.isConnected()){
				try{
					ftp.disconnect();
				}
				catch(IOException f){}
			}
			System.err.println("Cound not connect to server");
			System.exit(-1);
		}
	}
}
