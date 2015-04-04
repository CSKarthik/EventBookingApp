package com.exmle.login;

import java.util.ArrayList;
import java.util.List;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

public class BinderData extends BaseAdapter {

	LayoutInflater inflater;
	List<String> customList;
	List<Integer> colorList;
	ViewHolder holder;
	Activity mact;
	int noOfColors;
	int color;
	public BinderData(Activity act, List<String> list) {
		
		this.customList = list;
		mact =  act;
		inflater = (LayoutInflater) act
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		colorList = new ArrayList<Integer>();
		
		//Insert Colors
		colorList.add(R.drawable.peter_river);
		colorList.add(R.drawable.alizarin);
		
		noOfColors = 2;
		
//		colorList.add(R.drawable.amethyst);
//		colorList.add(R.drawable.emerald);
//		colorList.add(R.drawable.orange);
//		colorList.add(R.drawable.pumpkin);
//		colorList.add(R.drawable.silver);
//		colorList.add(R.drawable.turquoise);
//		colorList.add(R.drawable.sunflower);
		
		
	}
	
	
	public int getCount() {
		// TODO Auto-generated method stub
//		return idlist.size();
		return customList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View vi=convertView;
	    if(convertView==null){
	      vi = inflater.inflate(R.layout.list_row, null);
	      holder = new ViewHolder();
	      
	      holder.tvName = (TextView)vi.findViewById(R.id.tid);
	      holder.imgBaground = (ImageButton)vi.findViewById(R.id.iid);
	     vi.setTag(holder); 
	    }
	    else{
	    	
	    	holder = (ViewHolder)vi.getTag();
	    }
	    color = position % noOfColors;
	    holder.imgBaground.setBackgroundResource(colorList.get(color));
	    holder.tvName.setText(customList.get(position));
	    
	    holder.imgBaground.setId(100+position);
	    holder.tvName.setId(200+position);
	   
	    return vi;
	}
	
	static class ViewHolder{
		
		TextView tvName;
		ImageButton imgBaground;
	}

}
