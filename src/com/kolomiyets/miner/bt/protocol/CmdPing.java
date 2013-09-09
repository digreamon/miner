package com.kolomiyets.miner.bt.protocol;

public class CmdPing extends CmdHandshake {
	public CmdPing() {
		super(ECmdName.PING);
	}
}
