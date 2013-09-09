package com.kolomiyets.miner.screen;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.kolomiyets.miner.Miner;
import com.kolomiyets.miner.R;
import com.kolomiyets.miner.bt.notification.BtListener;
import com.kolomiyets.miner.bt.notification.BtNotificationCmd;
import com.kolomiyets.miner.bt.protocol.CmdFieldState;
import com.kolomiyets.miner.bt.protocol.CmdGameState;
import com.kolomiyets.miner.bt.protocol.CmdGameTeam;
import com.kolomiyets.miner.bt.protocol.EGameState;
import com.kolomiyets.miner.dialog.DialogProvider;
import com.kolomiyets.miner.dialog.EDialogType;
import com.kolomiyets.miner.dialog.IDialogResult;
import com.kolomiyets.miner.screen.ScreenManager.ScreenType;
import com.kolomiyets.miner.view.GridCell;

public abstract class PlayGroundBase extends ScreenBase implements OnClickListener, OnLongClickListener {

	String TAG = PlayGroundBase.class.getSimpleName();
	
	TableLayout grid;	
	volatile ArrayList<GridCell> cells;
	int minesCount;
	int processedCount;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity.setCurrentGameState(EGameState.SET);
		cmdReceiver.register(activity);
	}

	@Override
	public void onStart() {
		super.onStart();
		
	};
	
	@Override
	public void onStop() {
		activity.setCurrentGameState(EGameState.TERMINATE);
		super.onStop();
	};
	
	@Override
	public void onDestroy() {
		cmdReceiver.unregister(activity);
		super.onDestroy();
	};
	
	ArrayList<GridCell> generateCells(int dimention){
		ArrayList<GridCell> cells = new ArrayList<GridCell>();
		for(int i=0; i<dimention*dimention; i++){
			GridCell cell = new GridCell(activity);
			cell.setOnClickListener(this);
			cell.setOnLongClickListener(this);
			cells.add(cell);
		}
		return cells;
	}
	
	ArrayList<GridCell> generateCells(List<Integer> mineConfig){
		ArrayList<GridCell> cells = new ArrayList<GridCell>();
		for(Integer v: mineConfig){
			GridCell cell = new GridCell(activity);
			if(v.equals(Integer.valueOf(CmdFieldState.CELL_STATE_MINE))) {
				cell.setMine(true);
				minesCount++;
			} else {
				cell.setMystery();
			}
			cell.setOnClickListener(this);
			cell.setOnLongClickListener(this);
			cells.add(cell);
		}
		return cells;
	}
	
	void generateGrid(TableLayout grid, ArrayList<GridCell> cells, int dimention){
		grid.removeAllViews();
		for(int i=0; i<dimention*dimention; i+=dimention){
			TableRow row = new TableRow(activity);
			row.setFocusable(false);
			row.setClickable(false);
			for(int j=i; j<i+dimention; j++){
				GridCell cell = cells.get(j);
				row.addView(cell, new TableRow.LayoutParams(
						TableRow.LayoutParams.WRAP_CONTENT, 
						TableRow.LayoutParams.WRAP_CONTENT));
			}
			grid.addView(row, new TableLayout.LayoutParams(
					TableLayout.LayoutParams.WRAP_CONTENT, 
					TableLayout.LayoutParams.WRAP_CONTENT));
		}
		grid.setFocusable(false);
		grid.setClickable(false);
		grid.invalidate();
	}
	
	public static float dpToPixel(float dp, Context context){
	    Resources resources = context.getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    float px = dp * (metrics.densityDpi / 160f);
	    return px;
	}
	
	void setGridEnabled(boolean isEnabled){
		for(GridCell c: cells){
			c.setEnabled(isEnabled);
		}
	}
	
	BtListener<BtNotificationCmd> cmdReceiver = new BtListener<BtNotificationCmd>(BtNotificationCmd.class) {
		
		@Override
		public void processNotification(BtNotificationCmd notification) {
			
			if(notification.cmd instanceof CmdGameState){
				if(((CmdGameState)notification.cmd).state.equals(EGameState.TERMINATE)){
					this.unregister(activity);
					DialogProvider.showDialog(activity, EDialogType.INFO, 
							getResources().getString(R.string.dlg_ttl_err), 
							getResources().getString(R.string.dlg_msg_op_abandoned), 
							new IDialogResult() {
								
								@Override
								public void onResult(DialogFragment dlg, int result) {
									activity.getConnectionManager().terminateConnection();
									activity.getScreenManager().goTo(ScreenType.START);
								}
							});
				}
			} else if(notification.cmd instanceof CmdFieldState) {
				processIncommingFieldState(((CmdFieldState)notification.cmd).stateMatrix);
			}
		}
	};
	
	void anounceWinner(boolean isMiner){
		DialogProvider.showDialog(activity, EDialogType.INFO, 
				getResources().getString(R.string.dlg_ttl_finish), 
				getResources().getString(isMiner?R.string.dlg_msg_miner_wins:R.string.dlg_msg_sapper_wins), null);
	}
	
	abstract void processIncommingFieldState(ArrayList<Integer> stateMatrix);
	abstract void minerWins();
	abstract void sapperWins();
	abstract void finalizeConnection();
	abstract ArrayList<Integer> generateStateMatrix();
}
