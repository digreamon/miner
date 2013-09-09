package com.kolomiyets.miner;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

import com.kolomiyets.miner.bt.BtStateMonitor;
import com.kolomiyets.miner.bt.ConnectionBase;
import com.kolomiyets.miner.bt.ConnectionManager;
import com.kolomiyets.miner.bt.EConnectMethod;
import com.kolomiyets.miner.bt.notification.EConnectionSate;
import com.kolomiyets.miner.bt.protocol.CmdGameState;
import com.kolomiyets.miner.bt.protocol.EGameState;
import com.kolomiyets.miner.dialog.DialogProvider;
import com.kolomiyets.miner.dialog.EDialogType;
import com.kolomiyets.miner.dialog.IDialogResult;
import com.kolomiyets.miner.screen.PlayGroundBase;
import com.kolomiyets.miner.screen.ScreenBase;
import com.kolomiyets.miner.screen.ScreenConnectingGame;
import com.kolomiyets.miner.screen.ScreenDevices;
import com.kolomiyets.miner.screen.ScreenManager;
import com.kolomiyets.miner.screen.ScreenManager.ScreenType;
import com.kolomiyets.miner.screen.ScreenSelectPlayer;

public class MinerActivity extends FragmentActivity {

	public final static int REQUEST_ENABLE_BT = 1001;
	public final static int REQUEST_VISIBLE_BT = 1002;
	
	private ScreenManager screenManager;
	private ConnectionManager connectionManager;
	private BtStateMonitor btStateMonitor;
	private GameStatePoster gameStatePoster;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.miner_activity_layout);
		
		connectionManager = new ConnectionManager(this);
		screenManager = new ScreenManager(getSupportFragmentManager(), R.id.screen_container);
		btStateMonitor = new BtStateMonitor(this);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		btStateMonitor.startMonitoring();
	}
	
	@Override
	protected void onStop() {
		stopGameStatePoster();
		btStateMonitor.stopMonitoring();
		connectionManager.terminateConnection();
		super.onStop();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(!connectionManager.isBlueToothAvailable()){
			DialogProvider.showDialog(this, EDialogType.INFO, 
					getString(R.string.dlg_ttl_err), getString(R.string.dlg_msg_no_blue_tooth), 
					new IDialogResult() {
						
						@Override
						public void onResult(DialogFragment dlg, int result) {
							finish();
						}
					});
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		ScreenBase curScr = screenManager.getCurrentScreen();
		if(requestCode==REQUEST_ENABLE_BT
				&& curScr instanceof ScreenConnectingGame){
			
			((ScreenConnectingGame)curScr).onRequestEnableResult(resultCode);
		} else if(requestCode==REQUEST_VISIBLE_BT
				&& curScr instanceof ScreenConnectingGame) {
			
			((ScreenConnectingGame)curScr).onRequestVisibilityResult(resultCode);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	public ScreenManager getScreenManager() {
		return screenManager;
	}
	
	public synchronized ConnectionManager getConnectionManager() {
		return connectionManager;
	}
	
	public synchronized EGameState getCurrentGameState(){
		return ((Miner)getApplicationContext()).getCurrentGameState();
	}
	
	public synchronized void setCurrentGameState(EGameState newState){
		((Miner)getApplicationContext()).setCurrentGameState(newState);
	}
	
	public void startGameStatePoster(){
		gameStatePoster = new GameStatePoster();
		gameStatePoster.start();
	}
	
	public void stopGameStatePoster(){
		ConnectionBase.killThread(gameStatePoster);
	}
	
	private class GameStatePoster extends Thread {
		@Override
		public void run() {
			while(!this.isInterrupted()
					&& !getCurrentGameState().equals(EGameState.TERMINATE)
					&& !getCurrentGameState().equals(EGameState.FINISH)
					&& !getCurrentGameState().equals(EGameState.IDLE)){
				try {
					getConnectionManager().sendCmd(new CmdGameState(getCurrentGameState()));
					Thread.sleep(200);
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
			}
		}
		
	}
	
	@Override
	public void onBackPressed() {
		ScreenBase scr = screenManager.getCurrentScreen();
		if(scr instanceof PlayGroundBase
				|| scr instanceof ScreenSelectPlayer){
			connectionManager.terminateConnection();
			screenManager.goTo(ScreenType.START);
		} else if (scr instanceof ScreenConnectingGame
				|| scr instanceof ScreenDevices){
			connectionManager.terminateConnection();
			screenManager.previous();
		} else if(scr instanceof PlayGroundBase){
			connectionManager.terminateConnection();
			screenManager.goTo(ScreenType.START);
		} else {
			super.onBackPressed();
		}
	}
	
}
