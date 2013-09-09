package com.kolomiyets.miner.screen;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.kolomiyets.miner.R;
import com.kolomiyets.miner.bt.EConnectMethod;
import com.kolomiyets.miner.bt.protocol.EGameState;
import com.kolomiyets.miner.screen.ScreenManager.ScreenType;

public class ScreenCreateGame extends ScreenBase {
	
	EditText inputGameName;
	Button connectGame;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		root = inflater.inflate(R.layout.screen_create_game, null);
		inputGameName = (EditText)root.findViewById(R.id.input_game_name);
		
		((Button)root.findViewById(R.id.btn_start_game))
		.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Bundle args = new Bundle();
				args.putString(
						ScreenManager.ARG_CONNECT_METHOD, 
						EConnectMethod.MASTER.toString());
				args.putString(
						ScreenManager.ARG_GAME_NAME, 
						inputGameName.getText().toString());
				
				activity.getScreenManager().goTo(
						ScreenType.CONNECTING, args);
			}
		});
		
		connectGame = ((Button)root.findViewById(R.id.btn_connect_game));
		connectGame.setFocusable(true);
		connectGame.setFocusableInTouchMode(true);
		connectGame.requestFocus();
		connectGame.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				activity.getScreenManager().goTo(ScreenType.DEVICES);
			}
		});
		
		return root;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		activity.setCurrentGameState(EGameState.IDLE);
	}
	
	@Override
	public void onStop() {
		((InputMethodManager)activity
				.getSystemService(Context.INPUT_METHOD_SERVICE))
				.hideSoftInputFromWindow(inputGameName.getWindowToken(), 0);
		super.onStop();
	}
}
