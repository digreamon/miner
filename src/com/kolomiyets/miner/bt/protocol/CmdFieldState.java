package com.kolomiyets.miner.bt.protocol;

import java.util.ArrayList;

public class CmdFieldState extends CmdBase {
	
	public static final int CELL_STATE_EMPTY = 0;
	public static final int CELL_STATE_MINE = -1;
	public static final int CELL_STATE_MARK = -2;
	public static final int CELL_STATE_PROCESSED = 1;
	
	public final ArrayList<Integer> stateMatrix;
	public CmdFieldState(ArrayList<Integer> stateMatrix) {
		super(ECmdName.FIELD_STATE);
		this.stateMatrix = stateMatrix;
	}
}
