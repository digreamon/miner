package com.kolomiyets.miner.bt;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.kolomiyets.miner.Miner;
import com.kolomiyets.miner.bt.notification.BtNotificationCmd;
import com.kolomiyets.miner.bt.notification.BtNotificationHandshake;
import com.kolomiyets.miner.bt.notification.BtNotificationState;
import com.kolomiyets.miner.bt.notification.EConnectionSate;
import com.kolomiyets.miner.bt.notification.NotificationManager;
import com.kolomiyets.miner.bt.protocol.CmdBase;
import com.kolomiyets.miner.bt.protocol.CmdFactory;
import com.kolomiyets.miner.bt.protocol.CmdHandshake;

public class ConnectionProcessor {
	private BluetoothSocket socket;
	private NotificationManager notificationManager;
	private InputStreamReader input;
	private OutputStreamWriter output;
	private Thread reader;
	private HandlerThread writer;
	private Handler writerHandler;
	
	public ConnectionProcessor(BluetoothSocket socket, NotificationManager notificationManager) {
		this.socket = socket;
		this.notificationManager = notificationManager;
		
        try {
        	this.input  = new InputStreamReader(this.socket.getInputStream());
        	this.output = new OutputStreamWriter(this.socket.getOutputStream());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void writeCmd(final CmdBase cmd) {
		writerHandler.post(new Runnable() {
			
			@Override
			public void run() {
				if(Thread.currentThread().isInterrupted()){
					((HandlerThread)Thread.currentThread()).getLooper().quit();
					return;
				}
				try {
//					if(Miner.D) Log.d(Thread.currentThread().getName(), "write cmd: " + CmdFactory.toString(cmd));
					output.write(CmdFactory.toString(cmd));
					
					if(Thread.currentThread().isInterrupted()){
						((HandlerThread)Thread.currentThread()).getLooper().quit();
						return;
					}
					
					output.flush();
//					if(Miner.D) Log.d(Thread.currentThread().getName(), "write cmd - OK");
				} catch (IOException e) {
					e.printStackTrace();
					notificationManager.postNotification(new BtNotificationState(EConnectionSate.TERMINATED));
				}
			}
		});
	}
	
	public synchronized void start() {
		ConnectionBase.killThread(reader);
		reader = new Thread(){
			@Override
			public void run() {
				setName("BtReader");
				if(Miner.D) Log.d(getName(), "starting reader...");
				StringBuilder str = new StringBuilder(1024);
				char[] buf = new char[1024];
				int read = -1;
				int index = -1;
				while (!Thread.currentThread().isInterrupted()) {
					try {
						if(Miner.D) Log.d(getName(), "reading...");
						while((read = input.read(buf))>=0){
							str.append(buf, 0, read);
//							if(Miner.D) Log.d(getName(), "buffer state: " + str);
							
							while((index = str.indexOf(CmdFactory.CMD_TERMINATION)) >= 0){
								String sCmd = str.substring(0, index);
								str.delete(0, index+CmdFactory.CMD_TERMINATION.length());
								CmdBase cmd = CmdFactory.getCmd(sCmd);
								if(cmd instanceof CmdHandshake){
									notificationManager.postNotification(
											new BtNotificationHandshake((CmdHandshake) cmd));
								} else {
									notificationManager.postNotification(new BtNotificationCmd(cmd));
								}
								
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
						notificationManager.postNotification(new BtNotificationState(EConnectionSate.TERMINATED));
						return;
					}
				}
			}
		};
		reader.start();
		
		ConnectionBase.killThread(writer);
		writer = new HandlerThread("BtWriter");
		if(Miner.D) Log.d(writer.getName(), "starting writer...");
		writer.start();
		writerHandler = new Handler(writer.getLooper());
	}
	
	public synchronized void stop() throws IOException {
		ConnectionBase.killThread(reader);
		ConnectionBase.killThread(writer);
		if(input!=null) {
			final InputStreamReader _input = input;
			input = null;
			_input.close();
		}
		if(output!=null) {
			final OutputStreamWriter _output = output;
			output = null;
			_output.close();
		}
    	if(socket!=null) {
    		final BluetoothSocket _socket = socket;
    		socket = null;
    		_socket.close();
    	}
	} 
}
