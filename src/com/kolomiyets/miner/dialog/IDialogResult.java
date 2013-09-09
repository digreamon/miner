package com.kolomiyets.miner.dialog;

import android.support.v4.app.DialogFragment;

public interface IDialogResult {
    
    public final static int OK = 0;
    public final static int CANCEL = 1;
    
    public void onResult(DialogFragment dlg, int result);
}
