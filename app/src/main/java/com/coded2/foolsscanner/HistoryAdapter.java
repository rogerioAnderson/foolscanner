package com.coded2.foolsscanner;

import android.content.Context;
import android.database.DataSetObserver;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Created by rogerio on 8/29/16.
 */
public class HistoryAdapter extends ArrayAdapter<History> {
    public HistoryAdapter(Context context, int resource, List<History> objects) {

        super(context, resource, objects);

    }

    @Override
    public void remove(History object) {
        super.remove(object);
        object.delete(getContext());
    }
}