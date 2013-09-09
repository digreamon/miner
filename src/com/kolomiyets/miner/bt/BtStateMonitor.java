package com.kolomiyets.miner.bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.kolomiyets.miner.Miner;
import com.kolomiyets.miner.bt.notification.BtNotification;
import com.kolomiyets.miner.bt.notification.BtNotificationDiscoveryDevice;
import com.kolomiyets.miner.bt.notification.BtNotificationDiscoveryState;
import com.kolomiyets.miner.bt.notification.BtNotificationPower;

public class BtStateMonitor extends BroadcastReceiver {

	private String TAG = BtStateMonitor.class.getSimpleName();
	
	private final FragmentActivity activity;
	
	public BtStateMonitor(FragmentActivity activity) {
		this.activity = activity;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
			int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
			String stateStr;
			switch (state) {
			case BluetoothAdapter.STATE_ON:
				stateStr = "<ON>";
				break;
			case BluetoothAdapter.STATE_OFF:
				stateStr = "<OFF>";
				break;
			case BluetoothAdapter.STATE_TURNING_ON:
				stateStr = "<TURNING_ON>";
				break;
			case BluetoothAdapter.STATE_TURNING_OFF:
				stateStr = "<TURNING_OFF>";
				break;
			default:
				stateStr = "<unknown>";
				break;
			}
			publish(context, new BtNotificationPower(state));
			if(Miner.D) Log.d(TAG, "power state: "+stateStr);
		} else if(action.equals(BluetoothDevice.ACTION_FOUND)){
			BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			BluetoothClass clas = intent.getParcelableExtra(BluetoothDevice.EXTRA_CLASS);
			String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
			Short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
			
			if(Miner.D) Log.d(TAG, "new device: "+device.getName()+"["+device.getAddress()+"]");
			
			publish(context, new BtNotificationDiscoveryDevice(device, clas, name, rssi));
		} else if(action.equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)) {
			int state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, BluetoothAdapter.STATE_DISCONNECTED);
			String stateStr;
			switch (state) {
			case BluetoothAdapter.STATE_CONNECTED:
				stateStr = "<CONNECTED>";
				break;
			case BluetoothAdapter.STATE_DISCONNECTED:
				stateStr = "<DISCONNECTED>";
				break;
			case BluetoothAdapter.STATE_CONNECTING:
				stateStr = "<CONNECTING>";
				break;
			case BluetoothAdapter.STATE_DISCONNECTING:
				stateStr = "<DISCONNECTING>";
				break;
			default:
				stateStr = "<unknown>";
				break;
			}
			if(Miner.D) Log.d(TAG, "connection state: "+stateStr);
		} else if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
			if(Miner.D) Log.d(TAG, "DISCOVERY_STARTED");
			publish(context, new BtNotificationDiscoveryState(true));
		} else if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
			if(Miner.D) Log.d(TAG, "DISCOVERY_FINISHED");
			publish(context, new BtNotificationDiscoveryState(false));
		}
	}

	public void startMonitoring() {
		IntentFilter filter = new IntentFilter();
		
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		
		activity.registerReceiver(this, filter);
	}
	
	public void stopMonitoring() {
		activity.unregisterReceiver(this);
	}
	
	private void publish(Context context, BtNotification notification){
		((Miner)context.getApplicationContext())
		.getNotificationManager().postNotification(notification);
	}
}
