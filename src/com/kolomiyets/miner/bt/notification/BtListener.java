package com.kolomiyets.miner.bt.notification;

import android.content.Context;
import android.os.Handler;

import com.kolomiyets.miner.Miner;

public abstract class BtListener<T extends BtNotification> {
	
	public final Class<T> filter;
	private Handler handler = new Handler();
	
	public BtListener(Class<T> filter) {
		this.filter = filter;
	}
	
	public void postNotification(final BtNotification notification){
		if(filter.isInstance(notification)){
			handler.post(new Runnable() {
				
				@Override
				public void run() {
					processNotification((T)notification);
				}
			});
		}
	}
	
	public void register(Context context){
		Miner app = (Miner)context.getApplicationContext();
		app.getNotificationManager().registerListener(this);
	}
	
	public void unregister(Context context){
		Miner app = (Miner)context.getApplicationContext();
		app.getNotificationManager().unregisterListener(this);
	}
	
	public abstract void processNotification(T notification);
}
