package com.kolomiyets.miner.screen;

import java.util.ArrayList;

import android.os.Bundle;
import android.text.InputFilter.LengthFilter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.kolomiyets.miner.R;
import com.kolomiyets.miner.bt.notification.BtListener;
import com.kolomiyets.miner.bt.protocol.CmdFieldState;
import com.kolomiyets.miner.bt.protocol.EGameState;
import com.kolomiyets.miner.view.GridCell;

public class PlayGroundMiner extends PlayGroundBase implements OnItemSelectedListener {
	
	String TAG = PlayGroundMiner.class.getSimpleName();
	
	Button finish;
	Spinner dimenSelect;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		root = inflater.inflate(R.layout.playground_miner, null);
		grid = (TableLayout)root.findViewById(R.id.grid_miner_field);
		
		dimenSelect = (Spinner)root.findViewById(R.id.select_field_size);
		finish = (Button)root.findViewById(R.id.btn_finish_set_mines);
		
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(activity,
		        R.array.dimen_lbls, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		dimenSelect.setAdapter(adapter);
		dimenSelect.setSelection(1);
		dimenSelect.setOnItemSelectedListener(this);
		dimenSelect.setEnabled(false);
		
		finish.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				passFieldToSapper();
			}
		});
		
		return root;
	}
	
	public void onStart() {
		super.onStart();
		buildFieldBySelection(dimenSelect.getSelectedItemPosition());
	};
	
	private int getDimension(int labelIndex){
		return getResources().getIntArray(R.array.dimen_vals)[labelIndex];
	}

	private void buildFieldBySelection(int selectIndex){
		cells = generateCells(getDimension(selectIndex));
		generateGrid(grid, cells, getDimension(selectIndex));
	}
	
	@Override
	public void onItemSelected(AdapterView<?> data, View view, int position, long id) {
		buildFieldBySelection(position);
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {}

	@Override
	public void onClick(View v) {
		GridCell cell = (GridCell)v;
		if(cell.getIsMined()){
			minesCount--;
			cell.clear();
		} else {
			cell.setMine();
			if(++minesCount == cells.size()/2){
				passFieldToSapper();
				return;
			}
		}
		finish.setEnabled(minesCount>0);
	}

	@Override
	public boolean onLongClick(View v) { return true; }
	
	private void passFieldToSapper() {
		activity.setCurrentGameState(EGameState.SOLVE);
		activity.getConnectionManager().sendCmd(
				new CmdFieldState(generateStateMatrix()));
		finish.setEnabled(false);
		dimenSelect.setEnabled(false);
		setGridEnabled(false);
		Toast.makeText(activity, R.string.msg_watch_sapper_play, Toast.LENGTH_LONG).show();
	}

	@Override
	ArrayList<Integer> generateStateMatrix(){
		ArrayList<Integer> matrix = new ArrayList<Integer>(cells.size());
		for(GridCell cell: cells){
			if(cell.getIsMined()){
				matrix.add(CmdFieldState.CELL_STATE_MINE);
			} else {
				matrix.add(CmdFieldState.CELL_STATE_EMPTY);
			}
		}
		return matrix;
	}
	
	@Override
	public void processIncommingFieldState(ArrayList<Integer> stateMatrix) {
		if(activity.getCurrentGameState().equals(EGameState.FINISH)) return;
		int processedCount = 0;
		for(int i=0; i<stateMatrix.size(); i++){
			int state = stateMatrix.get(i);
			if(state == CmdFieldState.CELL_STATE_MARK){
				cells.get(i).highlight(R.color.blue);
			} else if(state == CmdFieldState.CELL_STATE_PROCESSED){
				if(cells.get(i).getIsMined()){
					cells.get(i).blowUp();
					minerWins();
					return;
				} else {
					cells.get(i).highlight(R.color.green);
					if(++processedCount+minesCount == cells.size()){
						sapperWins();
						return;
					}
				}
			} else if(state == CmdFieldState.CELL_STATE_MINE){
				cells.get(i).highlight(R.color.grey);
			}
		}
	}

	@Override
	public void minerWins() {
		finalizeConnection();
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
		activity.getConnectionManager().terminateConnection();
		cmdReceiver.unregister(activity);
		setGridEnabled(false);
	}
}
