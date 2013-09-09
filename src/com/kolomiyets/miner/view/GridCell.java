package com.kolomiyets.miner.view;

import java.util.zip.Inflater;

import com.kolomiyets.miner.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GridCell extends LinearLayout {

	public static final String MINE = "X";
	public static final String MARK = "M";
	
	boolean isMystery = true;
	String val;
	TextView valLbl;
	
	public GridCell(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);
		init(context);
	}
	
	public GridCell(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	public GridCell(Context context) {
		super(context, null);
		init(context);
	}
	
	private void init(Context context){
		LayoutInflater.from(getContext()).inflate(R.layout.grid_cell_layout, this, true);
		valLbl = (TextView)findViewById(R.id.lbl_cell_value);
		val = "";
	}

	public void setMine(boolean isMasked){
		setCellValue(MINE, R.color.black, isMasked);
	}
	
	public void setMine(){
		setMine(false);
	}
	
	public void setMark() {
		if(getIsMarked()) return;
		if(!isMystery) throw new IllegalStateException();
		valLbl.setText(MARK);
		isMystery = false;
	}
	
	public void removeMark(){
		if(!getIsMarked()) throw new IllegalStateException();
		setMystery();	
	}
	
	public void setMystery(){
		isMystery = true;
		valLbl.setText("");
		valLbl.setBackground(getResources().getDrawable(
				R.drawable.btn_square_overlay_normal));
	}
	
	public void setIndication(int number, boolean isMasked){
		int colorResId;
		switch (number) {
		case 1:
			colorResId = R.color.blue;
			break;
		case 2:
			colorResId = R.color.green;
			break;
		case 3:
		case 4:
			colorResId = R.color.orange;
			break;
		case 5:
		case 6:
			colorResId = R.color.redish;
			break;
		case 7:
		case 8:
			colorResId = R.color.red;
			break;
		default:
			colorResId = R.color.black;
			break;
		}
		setCellValue(String.valueOf(number), colorResId, isMasked);
	}
	
	public void setIndication(int number) {
		setIndication(number, false);
	}
	
	public void clear(){
		setCellValue("", R.color.black, false);
	}
	
	public void blowUp(){
		setCellValue(MINE, R.color.black, false);
		highlight(R.color.red);
	}
	
	@SuppressLint("ResourceAsColor")
	private void setCellValue(String value, int colorResId, boolean isMasked){
		val = value;
		if(isMasked){
			setMystery();
		} else {
			isMystery = false;
			valLbl.setBackgroundColor(getResources().getColor(R.color.grey));
			valLbl.setText(val);
		}
		valLbl.setTextColor(getResources().getColor(colorResId));
	}
	
	public boolean getIsMined(){
		return val.equals(MINE);
	}
	
	public boolean getIsMarked(){
		return valLbl.getText().toString().equals(MARK);
	}
	
	public boolean getIsMystery(){
		return isMystery;
	}
	
	public boolean getIsClear(){
		return val.equals("") && valLbl.getText().toString().equals("");
	}
	
	public void highlight(int colorResId){
		valLbl.setBackgroundColor(getResources().getColor(colorResId));
	}
	
	public String getCellValue() {
		return val;
	}
	
	@Override
	public void setOnClickListener(final OnClickListener l) {
		valLbl.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				l.onClick(GridCell.this);
			}
		});
	}
	
	@Override
	public void setOnLongClickListener(final OnLongClickListener l) {
		valLbl.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				l.onLongClick(GridCell.this);
				return true;
			}
		});
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		valLbl.setEnabled(enabled);
		super.setEnabled(enabled);
	}
}
