package com.kolomiyets.miner.bt.protocol;

public abstract class CmdBase {
	
	public final ECmdName name;
	
	public CmdBase(ECmdName cmd) {
		this.name = cmd;
	}
}
