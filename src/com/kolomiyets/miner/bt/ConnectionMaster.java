package com.kolomiyets.miner.bt;

import java.io.IOException;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.kolomiyets.miner.Miner;
import com.kolomiyets.miner.bt.notification.NotificationManager;

public class ConnectionMaster extends ConnectionBase {

	String TAG = ConnectionMaster.class.getSimpleName();
	
	private BluetoothServerSocket mmServerSocket;
	
	public ConnectionMaster(NotificationManager notificationManager) {
		super(notificationManager);
	}

	@Override
	BluetoothSocket doConnection() {
		
		BluetoothSocket socket = null;
		
		BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
		try {
			mmServerSocket = bt.listenUsingInsecureRfcommWithServiceRecord(bt.getName(), MINER_UUID);
        } catch (IOException e) { 
        	e.printStackTrace();
        	return null;
        }
        
        bt.cancelDiscovery();
		
		int count = 5;
        while (count --> 0 
        		&& !Thread.currentThread().isInterrupted() 
        		&& mmServerSocket!=null) {
            try {
            	if(Miner.D) Log.d(TAG, "listening for device to connect...");
                socket = mmServerSocket.accept();
                if (socket != null) {
                	break;
                }
            } catch (IOException e) {
            	e.printStackTrace();
            	if(Thread.currentThread().isInterrupted()) break;
            }
            
            if(Thread.currentThread().isInterrupted()) break;
            
            try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				break;
			}
        }
        
        return socket;
	}

	@Override
	void doDisconnection(ConnectionProcessor connectionProcessor) {
		if(Miner.D) Log.d(TAG, "terminating connection...");
		try {
			if(connectionProcessor!=null){
				final ConnectionProcessor _connectionProcessor = connectionProcessor;
				connectionProcessor = null;
				_connectionProcessor.stop();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			if(mmServerSocket!=null){
				final BluetoothServerSocket _mmServerSocket = mmServerSocket;
				mmServerSocket = null;
				_mmServerSocket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
