package com.kolomiyets.miner.bt.protocol;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CmdFactory {
	
	public static final String CMD_TERMINATION = "&#cmd;";
	public static final String KEY_NAME = "name";
	public static final String KEY_CODE = "code";
	public static final String KEY_MINER = "miner";
	public static final String KEY_SAPPER = "sapper";
	public static final String KEY_STATE = "state";
	public static final String KEY_FIELD_MATRIX = "field_matrix";
	
	public static CmdBase getCmd(JSONObject jCmd){
		CmdBase cmd = null;
		try {
			ECmdName name = ECmdName.fromString(jCmd.getString(KEY_NAME));
			switch (name) {
			case GAME_STATE:
				cmd = new CmdGameState(
						EGameState.fromInteger(jCmd.getInt(KEY_STATE)));
				break;
			case GAME_TEAM:
				cmd = new CmdGameTeam(
						jCmd.getInt(KEY_MINER),
						jCmd.getInt(KEY_SAPPER));
				break;
			case FIELD_STATE:
				JSONArray jArr = jCmd.getJSONArray(KEY_FIELD_MATRIX);
				ArrayList<Integer> arr = new ArrayList<Integer>(jArr.length());
				for(int i=0; i<jArr.length(); i++){
					arr.add(jArr.getInt(i));
				}
				cmd = new CmdFieldState(arr);
				break;
			case PING:
				cmd = new CmdPing();
				break;
			case RESPONSE:
				cmd = new CmdResponse(
						jCmd.getInt(KEY_CODE));
				break;
			default:
				cmd = new CmdBase(ECmdName.UNKNOWN){};
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
			cmd = new CmdBase(ECmdName.UNKNOWN){};
		}
		return cmd;
	}
	
	public static CmdBase getCmd(String sCmd){
		try {
			JSONObject jCmd = new JSONObject(sCmd);
			return getCmd(jCmd);
		} catch (JSONException e) {
			e.printStackTrace();
		}		
		return getCmd((JSONObject)null);
	}
	
	public static JSONObject toJson(CmdBase cmd) {
		JSONObject json = new JSONObject();
		try {
			json.put(KEY_NAME, cmd.name.toString());
			switch (cmd.name) {
			case GAME_STATE:
				json.put(KEY_STATE, ((CmdGameState)cmd).state.toInteger());
				break;
			case PING:
				break;
			case RESPONSE:
				json.put(KEY_CODE, ((CmdResponse)cmd).code);
				break;
			case GAME_TEAM:
				json.put(KEY_MINER, ((CmdGameTeam)cmd).miner);
				json.put(KEY_SAPPER, ((CmdGameTeam)cmd).sapper);
				break;
			case FIELD_STATE:
				JSONArray jArr = new JSONArray(
						((CmdFieldState)cmd).stateMatrix);
				json.put(KEY_FIELD_MATRIX, jArr);
			default:
				break;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}
	
	public static String toString(CmdBase cmd){
		return toJson(cmd)+CMD_TERMINATION;
	}
}
