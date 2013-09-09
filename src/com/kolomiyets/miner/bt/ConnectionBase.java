package com.kolomiyets.miner.bt;

import java.util.UUID;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.kolomiyets.miner.Miner;
import com.kolomiyets.miner.bt.notification.BtNotificationState;
import com.kolomiyets.miner.bt.notification.EConnectionSate;
import com.kolomiyets.miner.bt.notification.NotificationManager;
import com.kolomiyets.miner.bt.protocol.CmdBase;

public abstract class ConnectionBase {
	
	String TAG = ConnectionBase.class.getSimpleName();
	
//	public final static String MINER_BT_SERVER_NAME = "Miner Android Game";
	public final static UUID MINER_UUID = UUID.fromString("1C04DBB7-D264-4FCA-82E6-26103FB1FA48");
//	public final static UUID MINER_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	NotificationManager notificationManager;
	private Thread connectThread;
	private Thread disconnectThread;
	private ConnectionProcessor connectionProcessor;
	
	ConnectionBase(NotificationManager notificationManager) {
		this.notificationManager = notificationManager;
	}
	
	public synchronized void connect() {
		killThread(connectThread);
		connectThread = new Thread(){
			public void run() {
				setName("BlueTooth connect thread");
				
				doDisconnection(connectionProcessor);
				
				BluetoothSocket socket = doConnection();
				if(socket!=null){
					if(Miner.D) Log.d(TAG, "connect - " + "OK");
					connectionProcessor = new ConnectionProcessor(socket, notificationManager);
					connectionProcessor.start();
					notificationManager.postNotification(new BtNotificationState(EConnectionSate.CONNECTED));
				} else {
					if(Miner.D) Log.d(TAG, "connect - " + "ERR");
					notificationManager.postNotification(new BtNotificationState(EConnectionSate.FAILED));
					disconnect();
				}
			};
		}; 
		connectThread.start();
	}
	
	public synchronized void disconnect(){
		killThread(disconnectThread);
		disconnectThread = new Thread(){
			public void run() {
				setName("BlueTooth disconnect thread");
				doDisconnection(connectionProcessor);
				killThread(connectThread);
				if(Miner.D) Log.d(TAG, "disconnect - OK");
			};
		};
		disconnectThread.start();
	}
	
	public synchronized void sendCmd(CmdBase cmd){
		connectionProcessor.writeCmd(cmd);
	}
	
	public static void killThread(Thread thread){
		try {
			if(thread!=null){
				final Thread _thread = thread;
				_thread.interrupt();
				_thread.join(5000);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			thread = null;
		}
	}
	
	abstract BluetoothSocket doConnection();
	abstract void doDisconnection(ConnectionProcessor connectionProcessor);
}
