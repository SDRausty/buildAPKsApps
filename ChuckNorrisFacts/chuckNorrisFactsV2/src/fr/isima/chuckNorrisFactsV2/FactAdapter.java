package fr.isima.chuckNorrisFactsV2;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import fr.isima.chuckNorrisFactsV2.R;
import fr.isima.chuckNorrisFactsV2.entities.Fact;

public class FactAdapter extends BaseAdapter implements Filterable {

	private static LayoutInflater inflater = null;
	private List<Fact> mData;
	private List<Fact> mSubData;

	public FactAdapter(Context context, List<Fact> data) {
		this.mData = data;
		mSubData = data;
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	}

	public int getCount() {
		return mSubData.size();
	}

	public Fact getItem(int position) {
		return mSubData.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		ViewHolder holder;
		if (convertView == null) {
			view = inflater.inflate(R.layout.item_fact, null);
			holder = new ViewHolder();
			holder.tv_id = (TextView) view.findViewById(R.id.textView_item_id);
			;
			holder.tv_fact = (TextView) view
					.findViewById(R.id.textView_item_fact);
			view.setTag(holder);
		} else
			holder = (ViewHolder) view.getTag();

		String str_id = mSubData.get(position).getId() + "";
		if (mSubData.get(position).getId() == -1)
			str_id = "Unknown Id";
		holder.tv_id.setText(str_id);
		holder.tv_fact.setText(mSubData.get(position).getFact());

		return view;
	}

	public static class ViewHolder {
		public TextView tv_id;
		public TextView tv_fact;
	}

	public Filter getFilter() {
		return new Filter() {

			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint,
					FilterResults results) {

				mSubData = (List<Fact>) results.values;
				notifyDataSetChanged();
			}

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {

				FilterResults results = new FilterResults();
				List<Fact> values = new ArrayList<Fact>();

				if (constraint == null)
					constraint = "";

				if (constraint == "") {
					results.values = mData;
					results.count = mData.size();
				} else {
					for (Fact f : mData) {
						if (f.getFact().toLowerCase()
								.contains(constraint.toString().toLowerCase())) {
							Log.i(this.getClass().toString(),
									"added " + f.getId());
							values.add(f);
						}
					}

					results.values = values;
					results.count = values.size();
				}

				return results;

			}
		};
	}

}
