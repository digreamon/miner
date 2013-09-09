package com.kolomiyets.miner.bt.protocol;

public class CmdGameTeam extends CmdBase {
	
	public final static int VACANT = 0;
	public final static int OCUPIED = 1;
	public final static int SELECTED = 2;
	
	public int miner = VACANT;
	public int sapper = VACANT;
	
	public CmdGameTeam() {
		super(ECmdName.GAME_TEAM);
	}
	
	public CmdGameTeam(int miner, int sapper) {
		super(ECmdName.GAME_TEAM);
		this.miner = miner;
		this.sapper = sapper;
	}
}
