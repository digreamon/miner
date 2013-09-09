package com.kolomiyets.miner.screen;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.kolomiyets.miner.MinerActivity;

public class ScreenBase extends Fragment {
	View root;
	MinerActivity activity;
	Bundle arguments;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = (MinerActivity)getActivity();
		arguments = getArguments();
	}
}
