package com.kolomiyets.miner.bt.notification;

import java.util.ArrayList;


public class NotificationManager {
	
	ArrayList<BtListener<? extends BtNotification>> listeners = new ArrayList<BtListener<? extends BtNotification>>();
	
	public synchronized void registerListener(BtListener<? extends BtNotification> listener){
		listeners.add(listener);
	}
	
	public synchronized void unregisterListener(BtListener<? extends BtNotification> listener) {
		listeners.remove(listener);
	}
	
	public synchronized void postNotification(BtNotification notification){
		for(BtListener<? extends BtNotification> listener: listeners){
			listener.postNotification(notification);
		}
	}
}
