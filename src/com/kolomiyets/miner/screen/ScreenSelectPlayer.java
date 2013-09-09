package com.kolomiyets.miner.screen;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.kolomiyets.miner.R;
import com.kolomiyets.miner.bt.ConnectionBase;
import com.kolomiyets.miner.bt.notification.BtListener;
import com.kolomiyets.miner.bt.notification.BtNotificationCmd;
import com.kolomiyets.miner.bt.protocol.CmdGameState;
import com.kolomiyets.miner.bt.protocol.CmdGameTeam;
import com.kolomiyets.miner.bt.protocol.EGameState;
import com.kolomiyets.miner.screen.ScreenManager.ScreenType;

public class ScreenSelectPlayer extends ScreenBase {
	
	ToggleButton beMiner;
	ToggleButton beSapper;
	Button go;
	TeamStatePoster teamStatePoster;
	CmdGameTeam team;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity.setCurrentGameState(EGameState.PROMT);
		activity.startGameStatePoster();
		cmdReceiver.register(activity);
		team = new CmdGameTeam();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		root = inflater.inflate(R.layout.screen_select_player, null);
		beMiner = (ToggleButton)root.findViewById(R.id.btn_be_miner);
		beSapper = (ToggleButton)root.findViewById(R.id.btn_be_sapper);
		go = (Button)root.findViewById(R.id.btn_go_game);
		
		beMiner.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				team.miner = CmdGameTeam.SELECTED;
				if(beSapper.isEnabled()) {
					team.sapper = CmdGameTeam.VACANT;
					beSapper.setChecked(false);
				}
				updateGo();
			}
		});
		beSapper.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				team.sapper = CmdGameTeam.SELECTED;
				if(beMiner.isEnabled()) { 
					team.miner = CmdGameTeam.VACANT;
					beMiner.setChecked(false);
				}
				updateGo();
			}
		});
		go.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				goToPlayground();
			}
		});
		
		return root;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		teamStatePoster = new TeamStatePoster();
		teamStatePoster.start();
	}
	
	public void onStop() {
		ConnectionBase.killThread(teamStatePoster);
		super.onStop();
	};
	
	public void onDestroy() {
		cmdReceiver.unregister(activity);
		super.onDestroy();
	};
	
	private void goToPlayground(){
		if(beMiner.isChecked()&&beMiner.isEnabled()) {
			activity.getScreenManager().goTo(ScreenType.MINER);
		} else {
			activity.getScreenManager().goTo(ScreenType.SAPPER);
		}
	}
	
	OnClickListener teamSelectorListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			boolean tmpState = ((ToggleButton)v).isChecked();
			
			if(beMiner.isEnabled()) beMiner.setChecked(false);
			if(beSapper.isEnabled()) beSapper.setChecked(false);
			
			((ToggleButton)v).setChecked(tmpState);
			
			updateGo();
		}
	};
	
	BtListener<BtNotificationCmd> cmdReceiver = new BtListener<BtNotificationCmd>(BtNotificationCmd.class) {
		
		@Override
		public void processNotification(BtNotificationCmd notification) {
			if(notification.cmd instanceof CmdGameTeam) {
				
				if(((CmdGameTeam)notification.cmd).miner != team.miner){
					team.miner = ((CmdGameTeam)notification.cmd).miner;
					configTeamSelector(beMiner, team.miner);
				}
				if(((CmdGameTeam)notification.cmd).sapper != team.sapper){
					team.sapper = ((CmdGameTeam)notification.cmd).sapper;
					configTeamSelector(beSapper, ((CmdGameTeam)notification.cmd).sapper);
				}
				updateGo();
			} else if(notification.cmd instanceof CmdGameState) {
				if(((CmdGameState)notification.cmd).state == EGameState.SET){
					Toast.makeText(activity, getString(R.string.msg_op_start_game), Toast.LENGTH_LONG).show();
					goToPlayground();
				}
			}
		}
	};
	
	private void updateGo() {
		if(beMiner.isChecked() && beSapper.isChecked()){
			go.setVisibility(Button.VISIBLE);
		} else {
			go.setVisibility(Button.INVISIBLE);
		}
	}
	
	private void configTeamSelector(ToggleButton btn, int state){
		switch (state) {
		case CmdGameTeam.SELECTED:
			btn.setChecked(true);
			btn.setEnabled(true);
		case CmdGameTeam.OCUPIED:
			btn.setChecked(true);
			btn.setEnabled(false);
			break;
		case CmdGameTeam.VACANT:
			if(!btn.isEnabled()){
				btn.setChecked(false);
				btn.setEnabled(true);
			}
			break;
		default:
			break;
		}
	}
	
	private int getStateFromSelector(ToggleButton btn){
		if(btn.isEnabled() && btn.isChecked()){
			return CmdGameTeam.OCUPIED;
		} else {
			return CmdGameTeam.VACANT;
		}
	}
	
	private class TeamStatePoster extends Thread {
		
		@Override
		public void run() {
			while(!this.isInterrupted()){
				activity.getConnectionManager().sendCmd(new CmdGameTeam(
						getStateFromSelector(beMiner), getStateFromSelector(beSapper)));
				
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
					break;
				}
			}
		}
		
	}
}
