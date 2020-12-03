package org.omicon.entry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.omicon.R;
import org.omicon.initial.logger;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

public class SuggestionAdapter extends ArrayAdapter<String> {
    private final String MY_DEBUG_TAG = "CustomerAdapter";
    private ArrayList<String> items;
    private ArrayList<String> itemsAll;
    private ArrayList<String> suggestions;
    private int viewResourceId;

    public SuggestionAdapter(Context context, int viewResourceId,
                             ArrayList<String> items) {
        super(context, viewResourceId, items);
        this.items = items;
        this.itemsAll = (ArrayList<String>) items.clone();
        this.suggestions = new ArrayList<String>();
        this.viewResourceId = viewResourceId;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(viewResourceId, parent, false);
        }
        String customer = items.get(position);
        if (customer != null) {
            TextView customerNameLabel = (TextView) v
                    .findViewById(R.id.textView1);
            if (customerNameLabel != null) {
                // Log.i(MY_DEBUG_TAG,
                // "getView Customer Name:"+customer.getName());
                customerNameLabel.setText(customer);
            }
        }
        return v;
    }

    @Override
    public Filter getFilter() {
        return nameFilter;
    }

    Filter nameFilter = new Filter() {
        public String convertResultToString(Object resultValue) {
            String str = ((String) (resultValue));
            return str;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            try {
                if (constraint != null) {
                    suggestions.clear();
                    int count = 0;
                    for (String customer : itemsAll) {
                        if (constraint.toString().length() == 1) {
                            if (customer.contains("(")) {
                                String tmp = customer.substring(customer
                                        .lastIndexOf("(") + 1);
                                if (tmp.startsWith(constraint.toString())) {
                                    ++count;
                                    suggestions.add(customer);
                                }
                            }
                        } else if (customer.toLowerCase().contains(
                                constraint.toString().toLowerCase())) {
                            ++count;
                            suggestions.add(customer);
                        }

                        if (count > 30)
                            break;
                    }

                    Collections.sort(suggestions, comp);

                    FilterResults filterResults = new FilterResults();
                    filterResults.values = suggestions;
                    filterResults.count = suggestions.size();
                    return filterResults;
                } else {
                    return new FilterResults();
                }
            } catch (Exception ex) {
                new logger(getContext()).appendLog(MY_DEBUG_TAG
                        + ex.getMessage());
                return new FilterResults();
            }
        }

        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            try {
                ArrayList<String> filteredList = (ArrayList<String>) results.values;
                ArrayList<String> tmp = new ArrayList<String>();
                if (results != null && results.count > 0) {
                    try {
                        clear();
                        for (String c : filteredList) {
                            tmp.add(c);
                        }
                    } catch (Exception ex) {
                        new logger(getContext()).appendLog(MY_DEBUG_TAG
                                + ex.getMessage());
                    }
                    Iterator<String> tmpIterator = tmp.iterator();
                    while (tmpIterator.hasNext()) {
                        add(tmpIterator.next());
                    }

                    notifyDataSetChanged();
                }
            } catch (Exception ex) {
                new logger(getContext()).appendLog(MY_DEBUG_TAG
                        + ex.getMessage());
            }
        }

    };
    Comparator<String> comp = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            String t1 = o1.substring(o1.lastIndexOf("(") + 1, o1.length() - 1);
            String t2 = o2.substring(o2.lastIndexOf("(") + 1, o2.length() - 1);
            int it1 = 0, it2 = 0;
            try {
                it1 = Integer.parseInt(t1);
                it2 = Integer.parseInt(t2);
            } catch (Exception ex) {
            }
            if (it1 > it2)
                return 1;
            else if (it1 < it2)
                return -1;
            else
                return 0;
        }
    };

}