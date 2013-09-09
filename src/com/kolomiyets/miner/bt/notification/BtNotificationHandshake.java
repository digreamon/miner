package com.kolomiyets.miner.bt.notification;

import com.kolomiyets.miner.bt.protocol.CmdHandshake;

public class BtNotificationHandshake extends BtNotification {
	public final CmdHandshake cmd;
	public BtNotificationHandshake(CmdHandshake cmd) {
		this.cmd = cmd;
	}
}
