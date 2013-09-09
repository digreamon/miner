package com.kolomiyets.miner;

import android.app.Application;
import android.util.Log;

import com.kolomiyets.miner.bt.notification.NotificationManager;
import com.kolomiyets.miner.bt.protocol.EGameState;

public class Miner extends Application {

	public static boolean D = true;
	
	String TAG = Miner.class.getSimpleName();
	
	private NotificationManager notificationManager;
	public volatile EGameState currentGameState = EGameState.IDLE;
	
	@Override
	public void onCreate() {
		super.onCreate();
		this.notificationManager = new NotificationManager();
	}
	
	public NotificationManager getNotificationManager() {
		return notificationManager;
	}
	
	public synchronized EGameState getCurrentGameState() {
		return currentGameState;
	}
	
	public synchronized void setCurrentGameState(EGameState currentGameState) {
		if(Miner.D) Log.d(TAG, "set currentGameState to " + currentGameState);
		this.currentGameState = currentGameState;
	}
}
