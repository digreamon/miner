package com.kolomiyets.miner.bt.notification;

import com.kolomiyets.miner.bt.protocol.CmdBase;

public class BtNotificationCmd extends BtNotification {
	public final CmdBase cmd;
	public BtNotificationCmd(CmdBase cmd) {
		this.cmd = cmd;
	}
}
