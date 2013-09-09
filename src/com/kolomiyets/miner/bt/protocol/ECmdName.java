package com.kolomiyets.miner.bt.protocol;

public enum ECmdName {
	
	PING("ping"),
	RESPONSE("response"),
	GAME_TEAM("game_team"),
	GAME_STATE("game_state"),
	FIELD_STATE("field_state"),
	
	UNKNOWN("unknown");
	
	private String value = null;
    
    private ECmdName(String value) {
	this.value = value;
    }
    
    /**
     * @return Appropriate String Value of this Type.
     */
    @Override
    public String toString() {
        return value;
    }
    
    /**
     * @param value - Appropriate String Value of an existing Type
     * @return Enumeration Item for the specified String Value. Null if there are none corresponding. 
     */
    public static ECmdName fromString(String value){
    	for(ECmdName r: ECmdName.values()){
    		if(r.toString().equals(value)){
    			return r;
    		}
    	}
    	return null;
    }
}
