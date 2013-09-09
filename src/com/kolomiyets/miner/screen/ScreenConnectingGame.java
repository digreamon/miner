package com.kolomiyets.miner.screen;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.kolomiyets.miner.Miner;
import com.kolomiyets.miner.MinerActivity;
import com.kolomiyets.miner.R;
import com.kolomiyets.miner.bt.EConnectMethod;
import com.kolomiyets.miner.bt.IRequestBtResult;
import com.kolomiyets.miner.bt.notification.BtListener;
import com.kolomiyets.miner.bt.notification.BtNotificationHandshake;
import com.kolomiyets.miner.bt.notification.BtNotificationPower;
import com.kolomiyets.miner.bt.protocol.CmdResponse;
import com.kolomiyets.miner.bt.protocol.ECmdName;
import com.kolomiyets.miner.dialog.DialogProvider;
import com.kolomiyets.miner.dialog.EDialogType;
import com.kolomiyets.miner.dialog.IDialogResult;
import com.kolomiyets.miner.screen.ScreenManager.ScreenType;

public class ScreenConnectingGame extends ScreenBase implements IRequestBtResult {
	
	String TAG = ScreenConnectingGame.class.getSimpleName();
	
	boolean isNeedTerminate = false;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		if(Miner.D) Log.d(TAG, "connection method: " + getConnectMethod());
		
		isNeedTerminate = false;
		root = inflater.inflate(R.layout.screen_connect_game, null);
		
		String msgConnecting;
		String btnTxtCancel;
		
		if(getConnectMethod().equals(EConnectMethod.MASTER)){
			msgConnecting = getString(R.string.msg_connecting_master);
			btnTxtCancel= getString(R.string.btn_cancel_con_master);
		} else {
			msgConnecting = getString(R.string.msg_connecting_slave);
			btnTxtCancel= getString(R.string.btn_cancel_con_slave);
		}
		
		((TextView)root.findViewById(R.id.txt_connecting)).setText(msgConnecting);
		Button cancel = (Button)root.findViewById(R.id.btn_cancel_connecting);
		cancel.setText(btnTxtCancel);
		cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				activity.getConnectionManager().terminateConnection();
				activity.getScreenManager().goTo(ScreenType.START);
			}
		});
		
		return root;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		invokeConnection();
	}
	
	@Override
	public void onStop() {
		cmdReceiver.unregister(activity);
		super.onStop();
	}
	
	@Override
	public void onRequestEnableResult(int resultCode) {
		if(resultCode != Activity.RESULT_OK){
			isNeedTerminate = true;
			powerListener.unregister(activity);
			cmdReceiver.unregister(activity);
		}
	}
	
	@Override
	public void onRequestVisibilityResult(int resultCode) {
		if(resultCode != Activity.RESULT_CANCELED){
			invokeConnection();
		} else {
			isNeedTerminate = true;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if(isNeedTerminate){
			isNeedTerminate = false;
			activity.getScreenManager().goTo(ScreenType.START);
		}
	}
	
	private void invokeConnection(){
		if(Miner.D) Log.d(TAG, "invoking BlueTooth connection...");
		if(!activity.getConnectionManager().isBlueToothEnabled()){
			powerListener.register(activity);
			activity.getConnectionManager()
			.requestBlueToothEnable(MinerActivity.REQUEST_ENABLE_BT);
		} else if(!activity.getConnectionManager().isBlueToothDiscovarable()
				&& getConnectMethod().equals(EConnectMethod.MASTER)){
			activity.getConnectionManager()
			.requestBlueToothDiscoverable(MinerActivity.REQUEST_VISIBLE_BT);
		} else {
			cmdReceiver.register(activity);
			activity.getConnectionManager()
			.initiateConnection(getConnectMethod(), getGameName(), getDevice());
		}
	}
	
	private EConnectMethod getConnectMethod(){
		String mStr = arguments.getString(
				ScreenManager.ARG_CONNECT_METHOD);
		if(mStr!=null){
			return EConnectMethod.valueOf(mStr);
		}
		return EConnectMethod.MASTER;
	}
	
	private String getGameName(){
		String mStr = arguments.getString(
				ScreenManager.ARG_GAME_NAME);
		if(mStr!=null&&mStr.length()>0){
			return mStr;
		}
		return getString(R.string.app_name);
	}
	
	private BluetoothDevice getDevice(){
		return (BluetoothDevice)arguments.get(
				ScreenManager.ARG_DEVICE);
	}
	
	BtListener<BtNotificationPower> powerListener = new BtListener<BtNotificationPower>(BtNotificationPower.class) {
		
		@Override
		public void processNotification(BtNotificationPower notification) {
			if(notification.state == BluetoothAdapter.STATE_ON){
				powerListener.unregister(activity);
				invokeConnection();
			}
		}
	};
	
	BtListener<BtNotificationHandshake> cmdReceiver = new BtListener<BtNotificationHandshake>(BtNotificationHandshake.class) {
		
		@Override
		public void processNotification(BtNotificationHandshake notification) {
			unregister(activity);
			
			if(getConnectMethod().equals(EConnectMethod.MASTER) 
					&& notification.cmd.name.equals(ECmdName.PING)
				|| getConnectMethod().equals(EConnectMethod.SLAVE)
						&& notification.cmd.name.equals(ECmdName.RESPONSE)
						&& ((CmdResponse)notification.cmd).code == CmdResponse.CODE_OK) {
				
				if(getConnectMethod().equals(EConnectMethod.MASTER)){
					activity.getConnectionManager().sendCmd(new CmdResponse(CmdResponse.CODE_OK));
				}
				activity.getScreenManager().goTo(ScreenType.SELECT);
				
			} else {
				DialogProvider.showDialog(activity, EDialogType.INFO, 
						getString(R.string.dlg_ttl_err), 
						getString(R.string.dlg_msg_err_connect_oponent), 
						new IDialogResult() {
					
					@Override
					public void onResult(DialogFragment dlg, int result) {
						dlg.dismiss();
						activity.getScreenManager().goTo(ScreenType.START);
					}
				});
			}
		}
	};
}
