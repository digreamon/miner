package com.kolomiyets.miner.bt.protocol;

public enum EGameState {
	
	IDLE(0),
	PROMT(1),
	SET(2),
	SOLVE(3),
	TERMINATE(4),
	FINISH(5);
	
	private int value = 0;
    
    private EGameState(int value) {
    	this.value = value;
    }
    
    @Override
    public String toString() {
    	return String.valueOf(value);
    }
    
    public int toInteger(){
    	return value;
    }
    
    public static EGameState fromInteger(int value){
    	for(EGameState r: EGameState.values()){
    		if(r.value == value){
    			return r;
    		}
    	}
    	return null;
    }
    
    /**
     * @param value - Appropriate String Value of an existing Type
     * @return Enumeration Item for the specified String Value. Null if there are none corresponding. 
     */
    public static EGameState fromString(String value){
    	return fromInteger(Integer.valueOf(value));
    }

}
