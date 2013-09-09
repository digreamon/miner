package com.kolomiyets.miner.bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import com.kolomiyets.miner.Miner;
import com.kolomiyets.miner.MinerActivity;
import com.kolomiyets.miner.R;
import com.kolomiyets.miner.bt.notification.BtListener;
import com.kolomiyets.miner.bt.notification.BtNotificationState;
import com.kolomiyets.miner.bt.protocol.CmdBase;
import com.kolomiyets.miner.bt.protocol.CmdPing;
import com.kolomiyets.miner.bt.protocol.EGameState;
import com.kolomiyets.miner.dialog.DialogProvider;
import com.kolomiyets.miner.dialog.EDialogType;
import com.kolomiyets.miner.dialog.IDialogResult;
import com.kolomiyets.miner.screen.ScreenManager.ScreenType;

public class ConnectionManager {
	
	String TAG = ConnectionManager.class.getSimpleName();
	
	MinerActivity activity;
	BluetoothAdapter adapter;
	ConnectionBase connection;
	String prevConName;
	
	public ConnectionManager(MinerActivity activity) {
		this.activity = activity;
		this.adapter = BluetoothAdapter.getDefaultAdapter();
	}
	
	public boolean isBlueToothAvailable(){
		return adapter != null;
	}
	
	public boolean isBlueToothEnabled(){
		return adapter.isEnabled() && adapter.getState()==BluetoothAdapter.STATE_ON;
	}
	
	public boolean isBlueToothDiscovarable(){
		return adapter.getScanMode() == 
			BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE;
	}
	
	public void requestBlueToothEnable(int requestCode){
		if(Miner.D) Log.d(TAG, "requesting to enable BlueTooth; requestCode="+requestCode);
		Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		activity.startActivityForResult(enableBtIntent, requestCode);
	}
	
	public void requestBlueToothDiscoverable(int requestCode){
		if(Miner.D) Log.d(TAG, "requesting to make BlueTooth discoverable; requestCode="+requestCode);
		Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		activity.startActivityForResult(enableBtIntent, requestCode);
	}
	
	public void initiateConnection(EConnectMethod connectionType, String connectionName, BluetoothDevice device){
		if(Miner.D) Log.d(TAG, "initiating BlueTooth connection of type "+connectionType);
		stateListener.register(activity);
		switch (connectionType) {
		case MASTER:
			prevConName = adapter.getName();
			boolean result = adapter.setName(connectionName);
			if(Miner.D) Log.d(TAG, "set BlueTooth name to "+connectionName+" - "+(result?"OK":"ERR"));
			connection = new ConnectionMaster(
					((Miner)activity.getApplicationContext()).getNotificationManager());
			break;
		case SLAVE:
			
			connection = new ConnectionSlave(
					device,
					((Miner)activity.getApplicationContext()).getNotificationManager());
			break;
		default:
			break;
		}
		
		connection.connect();
	}
	
	public void terminateConnection() {
		stateListener.unregister(activity);
		if(connection!=null){
			if(connection instanceof ConnectionMaster) {
				adapter.setName(prevConName);
			}
			connection.disconnect();
			connection = null;
		}
	}
	
	public synchronized void sendCmd(CmdBase cmd) {
		if(connection!=null){
			connection.sendCmd(cmd);
		}
	}
	
	BtListener<BtNotificationState> stateListener = new BtListener<BtNotificationState>(BtNotificationState.class) {
		
		@Override
		public void processNotification(BtNotificationState notification) {
			switch (notification.code) {
			case CONNECTED:
				if(connection instanceof ConnectionSlave){
					connection.sendCmd(new CmdPing());
				}
				break;
			case FAILED:
				terminateConnection();
				DialogProvider.showDialog(activity, EDialogType.INFO, 
						activity.getString(R.string.dlg_ttl_err), 
						activity.getString(R.string.dlg_msg_err_connect_device), 
						new IDialogResult() {
					
					@Override
					public void onResult(DialogFragment dlg, int result) {
						dlg.dismiss();
						activity.getScreenManager().goTo(ScreenType.START);
					}
				});
				break;
			case TERMINATED:
				if(!activity.getCurrentGameState().equals(EGameState.FINISH)){
					terminateConnection();
					DialogProvider.showDialog(activity, EDialogType.INFO, 
							activity.getString(R.string.dlg_ttl_err), 
							activity.getString(R.string.dlg_msg_err_interrupted), 
							new IDialogResult() {
						
						@Override
						public void onResult(DialogFragment dlg, int result) {
							dlg.dismiss();
							activity.getScreenManager().goTo(ScreenType.START);
						}
					});
				}
				break;
			default:
				break;
			}
		}
	};
	
	public EConnectMethod getConnectMethod(){
		if(connection instanceof ConnectionMaster){
			return EConnectMethod.MASTER;
		} else {
			return EConnectMethod.SLAVE;
		}
	}
}
