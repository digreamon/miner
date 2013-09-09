package com.kolomiyets.miner.bt.notification;

public class BtNotificationState extends BtNotification {
	public final EConnectionSate code;
	public BtNotificationState(EConnectionSate code) {
		this.code = code;
	}
}
