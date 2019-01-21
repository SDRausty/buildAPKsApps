package com.sikni8.parkingsuspension;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ArrayAdapterWithIcon extends ArrayAdapter<String> {

private List<Integer> images;
TextView textView;

	public ArrayAdapterWithIcon(Context context, List<String> items, List<Integer> images) {
		super(context, android.R.layout.select_dialog_item, items);
		this.images = images;
	}

	public ArrayAdapterWithIcon(Context context, String[] items, Integer[] images) {
		super(context, android.R.layout.select_dialog_item, items);
		this.images = Arrays.asList(images);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		textView = (TextView) view.findViewById(android.R.id.text1);
		textView.setTypeface(null, Typeface.BOLD);
		textView.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/handwriting.ttf"));
		textView.setCompoundDrawablesWithIntrinsicBounds(images.get(position), 0, 0, 0);
		textView.setCompoundDrawablePadding(
            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getContext().getResources().getDisplayMetrics()));
    return view;
	}

}