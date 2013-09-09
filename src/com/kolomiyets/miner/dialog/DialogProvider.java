package com.kolomiyets.miner.dialog;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.kolomiyets.miner.R;

public class DialogProvider extends DialogFragment {
	
    public static void showDialog(FragmentActivity activity, EDialogType type, 
    		String title, String message, IDialogResult callBack) {
    	DialogProvider dialog = new DialogProvider();
    	
    	dialog.type = type;
    	dialog.title = title;
    	dialog.message = message;
    	dialog.callBack = callBack;
    	
    	FragmentManager fm = activity.getSupportFragmentManager();
    	if(fm.findFragmentByTag(DialogProvider.class.getName()+dialog.message)==null){
    		FragmentTransaction ft = fm.beginTransaction();
        	ft.add(dialog, DialogProvider.class.getName()+dialog.message);
        	ft.commit();
    	}
    }
    
    private EDialogType type;
    private String title;
    private String message;
    private IDialogResult callBack;
    private View rootView;
    
    public DialogProvider() {
    	super();
    	this.type = EDialogType.INFO;
	}
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setStyle(DialogFragment.STYLE_NO_TITLE, 
    			R.style.DialogBaseTheme);
    	setCancelable(false);
    }
    
    @SuppressWarnings("unused")
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) {
    	rootView = inflater.inflate(R.layout.dialog_sceleton, null);
    	TextView titleView = (TextView)rootView.findViewById(R.id.dlg_title);
    	TextView messageView = (TextView)rootView.findViewById(R.id.dlg_message);
    	FrameLayout controlPanel = (FrameLayout)rootView.findViewById(R.id.dlg_control_panel);
    	Button okButton = null;
    	Button cancelButton = null;
    	
    	titleView.setText(title);
    	messageView.setText(message);
    	
    	switch (type) {
		case INFO:
			View infoCtlPanel = inflater.inflate(
					R.layout.dialog_control_panel_info, null);
			controlPanel.addView(infoCtlPanel);
			okButton = (Button)controlPanel.findViewById(R.id.dlg_control_ok);
			break;
		case CONFIRM:
			
			break;
		default:
			break;
		}
    	
    	if(okButton!=null){
    		okButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(callBack!=null){
						callBack.onResult(DialogProvider.this, IDialogResult.OK);
					} else {
						dismiss();
					}
				}
			});
    	}
    	
    	if(cancelButton!=null){
    		cancelButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(callBack!=null){
						callBack.onResult(DialogProvider.this, IDialogResult.CANCEL);
					} else {
						dismiss();
					}
				}
			});
    	}
    	
    	return rootView;
    }
}
