package com.kolomiyets.miner.screen;

import java.util.ArrayList;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kolomiyets.miner.Miner;
import com.kolomiyets.miner.MinerActivity;
import com.kolomiyets.miner.R;
import com.kolomiyets.miner.bt.EConnectMethod;
import com.kolomiyets.miner.bt.IRequestBtResult;
import com.kolomiyets.miner.bt.notification.BtListener;
import com.kolomiyets.miner.bt.notification.BtNotificationDiscovery;
import com.kolomiyets.miner.bt.notification.BtNotificationDiscoveryDevice;
import com.kolomiyets.miner.bt.notification.BtNotificationDiscoveryState;
import com.kolomiyets.miner.bt.notification.BtNotificationPower;
import com.kolomiyets.miner.screen.ScreenManager.ScreenType;

public class ScreenDevices extends ScreenBase implements IRequestBtResult {
	
	String TAG = ScreenDevices.class.getSimpleName();
	
	ArrayList<BluetoothDevice> devices;
	ProgressBar pgs;
	Button rescan;
	ListView list;
	DevicesAdapter adapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		devices = new ArrayList<BluetoothDevice>();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		root = inflater.inflate(R.layout.screen_devices, null);
		pgs = (ProgressBar)root.findViewById(R.id.pgs_devices_search);
		rescan = (Button)root.findViewById(R.id.btn_devices_rescan);
		list = (ListView)root.findViewById(R.id.list_devices_servers);
		
		rescan.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				devices.clear();
				adapter.notifyDataSetChanged();
				list.invalidate();
				BluetoothAdapter.getDefaultAdapter().startDiscovery();
			}
		});
		
		adapter = new DevicesAdapter(devices);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Bundle args = new Bundle();
				args.putString(
						ScreenManager.ARG_CONNECT_METHOD, 
						EConnectMethod.SLAVE.toString());
				args.putParcelable(
						ScreenManager.ARG_DEVICE, 
						devices.get(position));
				
				activity.getScreenManager().goTo(
						ScreenType.CONNECTING, args);
			}
		});
		
		return root;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		invokeDiscovery();
	}
	
	@Override
	public void onStop() {
		((Miner)getActivity().getApplicationContext())
		.getNotificationManager().unregisterListener(listener);
		
		super.onStop();
	}
	
	@Override
	public void onRequestEnableResult(int resultCode) {
		if(resultCode == Activity.RESULT_OK){
			invokeDiscovery();
		} else {
			activity.getScreenManager().goTo(ScreenType.START);
		}
	}
	
	@Override
	public void onRequestVisibilityResult(int resultCode) {}
	
	private void invokeDiscovery(){
		if(Miner.D) Log.d(TAG, "invoking BlueTooth connection...");
		if(!activity.getConnectionManager().isBlueToothEnabled()){
			powerListener.register(activity);
			activity.getConnectionManager()
			.requestBlueToothEnable(MinerActivity.REQUEST_ENABLE_BT);
		} else {
			((Miner)getActivity().getApplicationContext())
			.getNotificationManager().registerListener(listener);
			
			BluetoothAdapter.getDefaultAdapter().startDiscovery();
		}
	}
	
	BtListener<BtNotificationPower> powerListener = 
		new BtListener<BtNotificationPower>(BtNotificationPower.class) {
		
		@Override
		public void processNotification(BtNotificationPower notification) {
			if(notification.state == BluetoothAdapter.STATE_ON){
				unregister(activity);
				invokeDiscovery();
			}
		}
	};
	
	BtListener<BtNotificationDiscovery> listener = 
		new BtListener<BtNotificationDiscovery>(BtNotificationDiscovery.class) {
		
		@Override
		public void processNotification(BtNotificationDiscovery notification) {
			if(notification instanceof BtNotificationDiscoveryDevice){
				for(BluetoothDevice d: devices){
					if(((BtNotificationDiscoveryDevice)notification).
							device.getAddress().equals(d.getAddress())) return;
				}
				
				devices.add(((BtNotificationDiscoveryDevice)notification).device);
				adapter.notifyDataSetChanged();
				list.invalidate();
			} else if(notification instanceof BtNotificationDiscoveryState){
				toggleControlsVisibility(((BtNotificationDiscoveryState)notification).state);
			}
		}
	};
	
	private void toggleControlsVisibility(boolean isDescovering){
		if(isDescovering){
			pgs.setVisibility(View.VISIBLE);
			rescan.setVisibility(View.INVISIBLE);
		} else {
			pgs.setVisibility(View.INVISIBLE);
			rescan.setVisibility(View.VISIBLE);
		}
	}
	
	private class DevicesAdapter extends ArrayAdapter<BluetoothDevice> {
		public DevicesAdapter(ArrayList<BluetoothDevice> devices) {
			super(getActivity(), R.layout.devices_list_item, R.id.lbl_title, devices);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView==null){
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.devices_list_item, null);
			}
			
			BluetoothDevice device = getItem(position);
			TextView lblTitle = (TextView)convertView.findViewById(R.id.lbl_title);
			TextView lblDetails = (TextView)convertView.findViewById(R.id.lbl_detail);
			
			lblTitle.setText(device.getName());
			lblDetails.setText(device.getAddress());
			
			return convertView;
		}
	}
}
