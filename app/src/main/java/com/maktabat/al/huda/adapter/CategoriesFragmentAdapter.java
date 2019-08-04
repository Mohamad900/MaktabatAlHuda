package com.maktabat.al.huda.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.maktabat.al.huda.R;
import com.maktabat.al.huda.model.Category;

import java.util.ArrayList;
import java.util.List;


public class CategoriesFragmentAdapter extends BaseAdapter {

    Context context;
    ArrayList<Category> categories;
    LayoutInflater inflter;
    public CategoriesFragmentAdapter(Context applicationContext, ArrayList<Category> categories) {
        this.context = applicationContext;
        this.categories = categories;
        inflter = (LayoutInflater.from(applicationContext));
    }


    @Override
    public int getCount() {
        return categories.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflter.inflate(R.layout.item_category, null); // inflate the layout
        TextView txt_cat_name = (TextView) convertView.findViewById(R.id.txt_cat_name); // get the reference of ImageView
        TextView txt_cat_no = (TextView) convertView.findViewById(R.id.txt_cat_no); // get the reference of ImageView
        txt_cat_name.setText(categories.get(position).getName()); // set name
        txt_cat_no.setText("("+categories.get(position).getNumberOfBooks()+")"); // set number
        return convertView;
    }
}
