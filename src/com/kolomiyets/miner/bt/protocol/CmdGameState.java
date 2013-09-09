package com.kolomiyets.miner.bt.protocol;

public class CmdGameState extends CmdBase {

	public final EGameState state;
	
	public CmdGameState(EGameState state) {
		super(ECmdName.GAME_STATE);
		this.state = state;
	}

}
