package com.kolomiyets.miner.screen;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.SparseArray;

public class ScreenManager {
	
	public final static String ARG_CONNECT_METHOD = "CONNECT_METHOD";
	public final static String ARG_GAME_NAME = "GAME_NAME";
	public final static String ARG_DEVICE = "GAME_DEVICE";
	
	public enum ScreenType {
		START, DEVICES, CONNECTING, SELECT, MINER, SAPPER
	}
	
	private SparseArray<ScreenType> scenarium;
	private FragmentManager fragmentManager;
	private int defaultContainerResId;
	private int curPos;
	private ScreenBase curScr;
	
	public ScreenManager(FragmentManager fragmentManager, int defaultContainerResId) {
		this.fragmentManager = fragmentManager;
		this.defaultContainerResId = defaultContainerResId;
		initDefaultScenarium();
	}
	
	public void initDefaultScenarium(){
		curPos = 0;
		
		scenarium = new SparseArray<ScreenManager.ScreenType>(1);
		scenarium.put(0, ScreenType.START);
		scenarium.put(1, ScreenType.DEVICES);
		scenarium.put(2, ScreenType.CONNECTING);
		scenarium.put(3, ScreenType.SELECT);
		scenarium.put(4, ScreenType.MINER);
		scenarium.put(5, ScreenType.SAPPER);
		
		goTo(curPos);
//		goTo(5);
	}
	
	public void next(int containerResId, Bundle args){
		goTo(containerResId, curPos+1, args);
	}
	
	public void next(){
		next(defaultContainerResId, null);
	}
	
	public void next(Bundle args){
		next(defaultContainerResId, args);
	}
	
	public void previous(int containerResId, Bundle args){
		goTo(containerResId, curPos-1, args);
	}
	
	public void previous(){
		previous(defaultContainerResId, null);
	}
	
	public void previous(Bundle args){
		previous(defaultContainerResId, args);
	}
	
	public void goTo(int containerResId, int position, Bundle args){
		if(position<0) {
			curPos = 0;
		} else if(position>=scenarium.size()){
			curPos = scenarium.size()-1;
		} else {
			curPos = position;
		}
		
		curScr = getScreen(scenarium.get(curPos));
		curScr.setArguments(args);
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		transaction.replace(containerResId, curScr, getScreenTag(curScr.getClass()))
		.commit();
	}
	
	public void goTo(int position, Bundle args){
		goTo(defaultContainerResId, position, args);
	}
	
	public void goTo(int position){
		goTo(defaultContainerResId, position, null);
	}
	
	public void goTo(int containerResId, ScreenType type, Bundle args){
		for(int i=0; i<scenarium.size(); i++){
			if(scenarium.get(i).equals(type)) {
				goTo(containerResId, i, args);
				return;
			}
		}
	}
	
	public void goTo(ScreenType type, Bundle args){
		goTo(defaultContainerResId, type, args);
	}
	
	public void goTo(ScreenType type){
		goTo(defaultContainerResId, type, null);
	}
	
	private ScreenBase getScreen(ScreenType type){
		ScreenBase result;
		switch (type) {
		case START:
			result = new ScreenCreateGame();
			break;
		case CONNECTING:
			result = new ScreenConnectingGame();
			break;
		case DEVICES:
			result = new ScreenDevices();
			break;
		case SELECT:
			result = new ScreenSelectPlayer();
			break;
		case MINER:
			result = new PlayGroundMiner();
			break;
		case SAPPER:
			result = new PlayGroundSapper();
			break;
		default:
			result = null;
			break;
		}
		
		return result;
	}
	
	public static <T extends ScreenBase> String getScreenTag(Class<T> cls){
		return cls.getName();
	}
	
	public ScreenBase getCurrentScreen(){
		return curScr;
	}
}
