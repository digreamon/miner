package com.kolomiyets.miner.bt.protocol;

public class CmdResponse extends CmdHandshake {
	
	public final static int CODE_OK = 0;
	public final static int CODE_ERR = -1;
	
	public final Integer code;
	
	public CmdResponse(Integer code) {
		super(ECmdName.RESPONSE);
		this.code = code;
	}
}
