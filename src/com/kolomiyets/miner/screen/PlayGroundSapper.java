package com.kolomiyets.miner.screen;

import java.util.ArrayList;

import com.kolomiyets.miner.R;
import com.kolomiyets.miner.bt.ConnectionBase;
import com.kolomiyets.miner.bt.protocol.CmdFieldState;
import com.kolomiyets.miner.bt.protocol.CmdGameTeam;
import com.kolomiyets.miner.bt.protocol.EGameState;
import com.kolomiyets.miner.dialog.DialogProvider;
import com.kolomiyets.miner.dialog.EDialogType;
import com.kolomiyets.miner.view.GridCell;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;

public class PlayGroundSapper extends PlayGroundBase {
	String TAG = PlayGroundSapper.class.getSimpleName();
	
	ProgressIndicationPoster progressIndicationPoster;
	int dimention;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		root = inflater.inflate(R.layout.playground_sapper, null);
		grid = (TableLayout)root.findViewById(R.id.grid_miner_field);
//		ArrayList<Integer> testMatrix = getTestMatrix();
//		processIncommingFieldState(testMatrix);
		return root;
	}
	
	ArrayList<Integer> getTestMatrix(){
		int[] testInt = new int[]{
				0,	0,	0,	0,	0,	0,	0,	0,
				0,	0,	-1,	-1,	-1,	0,	-1,	0,
				0,	0,	-1,	0,	-1,	0,	0,	0,
				0,	0,	-1,	-1,	-1,	0,	0,	0,
				0,	0,	0,	-1,	0,	0,	0,	0,
				0,	-1,	0,	0,	0,	-1,	0,	0,
				0,	0,	0,	-1,	-1,	-1,	0,	0,
				0,	0,	0,	0,	0,	0,	0,	0
		};
		ArrayList<Integer> testMatrix = new ArrayList<Integer>();
		for(int i: testInt){
			testMatrix.add(i);
		}
		return testMatrix;
	}
	
	private void autoRevealCells(int startIndex) {
		if(cells.get(startIndex).getIsMarked() 
				|| !cells.get(startIndex).getIsMystery() 
				|| !activity.getCurrentGameState().equals(EGameState.SOLVE)) {
			return;
		}
		
		ArrayList<Integer> indecesToCheck = new ArrayList<Integer>(8);
		int minesCount = 0;
		
		int y = startIndex/dimention;
		int x = startIndex-y*dimention;
		
		for(int i=-1; i<2; i++){
			for(int j=-1; j<2; j++){
				int _x = x + j;
				int _y = y + i;
				
				if(_x>=0&&_x<dimention&&_y>=0&&_y<cells.size()) {
					int indexToCheck = startIndex+(i*dimention)+j;
					
					if(indexToCheck!=startIndex 
							&& indexToCheck>=0
							&& indexToCheck<cells.size()) {
						
						GridCell cellToCheck = cells.get(indexToCheck);
						if(cellToCheck.getIsMined()) { 
							minesCount++; 
						} else if(cellToCheck.getIsMystery()) { 
							indecesToCheck.add(indexToCheck);
						}
					}
				}
			}
		}
		
		if(minesCount>0) {
			cells.get(startIndex).setIndication(minesCount);
		} else {
			cells.get(startIndex).clear();
		}
		
		if(++processedCount+this.minesCount == cells.size()){
			sapperWins();
			return;
		}
		
		if(cells.get(startIndex).getIsClear()){
			for(Integer i: indecesToCheck){
				autoRevealCells(i);
			}
		}
	}
	
	@Override
	public void onClick(View v) {
		GridCell cell = (GridCell)v;
		if(cell.getIsMarked()){
			cell.removeMark();
		} else if (cell.getIsMined()) {
			cell.blowUp();
			minerWins();
		} else {
			autoRevealCells(cells.indexOf(cell));
		}
	}

	@Override
	public boolean onLongClick(View v) {
		GridCell cell = (GridCell)v;
		if(cell.getIsMystery()){
			((Vibrator)activity.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(300);
			cell.setMark();
		}
		return true;
	}

	private void revealMines(){
		for(GridCell cell: cells){
			if(cell.getIsMined()){
				cell.blowUp();
			}
		}
	}
	
	@Override
	public void processIncommingFieldState(ArrayList<Integer> stateMatrix) {
		activity.setCurrentGameState(EGameState.SOLVE);
		
		dimention = (int)Math.sqrt(stateMatrix.size());
		
		cells = generateCells(stateMatrix);
		generateGrid(grid, cells, dimention);
		
		root.findViewById(R.id.wait_layout).setVisibility(View.INVISIBLE);
		root.findViewById(R.id.field_layout).setVisibility(View.VISIBLE);
		
		progressIndicationPoster = new ProgressIndicationPoster();
		progressIndicationPoster.start();
	}
	
	@Override
	public void minerWins() {
		finalizeConnection();
		revealMines();
		anounceWinner(true);
	}
	
	@Override
	public void sapperWins() {
		finalizeConnection();
		anounceWinner(false);
	}
	
	@Override
	public void finalizeConnection() {
		activity.setCurrentGameState(EGameState.FINISH);
		activity.stopGameStatePoster();
		setGridEnabled(false);
		ConnectionBase.killThread(progressIndicationPoster);
		activity.getConnectionManager().sendCmd(
				new CmdFieldState(generateStateMatrix()));
		activity.getConnectionManager().terminateConnection();
	}
	
	@Override
	ArrayList<Integer> generateStateMatrix(){
		ArrayList<Integer> matrix = new ArrayList<Integer>(cells.size());
		for(GridCell cell: cells){
			if(cell.getIsMarked()){
				matrix.add(CmdFieldState.CELL_STATE_MARK);
			} else if(!cell.getIsMystery()){
				matrix.add(CmdFieldState.CELL_STATE_PROCESSED);
			} else {
				matrix.add(CmdFieldState.CELL_STATE_EMPTY);
			}
		}
		return matrix;
	}
	
	private class ProgressIndicationPoster extends Thread {
		
		@Override
		public void run() {
			while(!this.isInterrupted()
					&& !activity.getCurrentGameState().equals(EGameState.IDLE)
					&& !activity.getCurrentGameState().equals(EGameState.FINISH)
					&& !activity.getCurrentGameState().equals(EGameState.TERMINATE)){
				
				try {
					activity.getConnectionManager().sendCmd(
							new CmdFieldState(generateStateMatrix()));
					Thread.sleep(200);
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
			}
		}
		
	}
}
