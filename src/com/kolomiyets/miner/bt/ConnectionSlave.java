package com.kolomiyets.miner.bt;

import java.io.IOException;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.kolomiyets.miner.Miner;
import com.kolomiyets.miner.bt.notification.NotificationManager;

public class ConnectionSlave extends ConnectionBase {

	String TAG = ConnectionMaster.class.getSimpleName();
	
	private BluetoothSocket socket;
    private BluetoothDevice device;
	
	ConnectionSlave(BluetoothDevice device, NotificationManager notificationManager) {
		super(notificationManager);
		this.device = device;
	}

	@Override
	BluetoothSocket doConnection() {
		BluetoothSocket tmp = null;
		BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
		bt.cancelDiscovery();
		try {
            tmp = device.createRfcommSocketToServiceRecord(MINER_UUID);
        } catch (IOException e) { }
        socket = tmp;
        
        int count = 5;
        while(count --> 0 && socket!=null && !socket.isConnected()){
        	try {
            	if(Miner.D) Log.d(TAG, "listening for device to connect...");
            	socket.connect();
            	return socket;
            } catch (IOException e) {
            	e.printStackTrace();
            }
            
            try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				break;
			}
        }
        
        return null;
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
			if(socket!=null){
				final BluetoothSocket _socket = socket;
				socket = null;
	    		_socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
