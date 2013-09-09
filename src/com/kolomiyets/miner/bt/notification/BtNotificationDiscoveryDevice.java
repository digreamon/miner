package com.kolomiyets.miner.bt.notification;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;

public class BtNotificationDiscoveryDevice extends BtNotificationDiscovery {
	public final BluetoothDevice device;
	public final BluetoothClass clas;
	public final String name;
	public final Short rssi;
	
	public BtNotificationDiscoveryDevice(BluetoothDevice device, 
			BluetoothClass clas, String name, Short rssi) {
		
		this.device = device;
		this.clas = clas;
		this.name = name;
		this.rssi = rssi;
	}
}
