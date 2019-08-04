package com.maktabat.al.huda.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.maktabat.al.huda.R;
import com.maktabat.al.huda.model.Question;

import java.util.ArrayList;

/**
 * Created by Aroliant on 1/3/2018.
 */

public class ResponseInnerRecyclerViewAdapter extends RecyclerView.Adapter<ResponseInnerRecyclerViewAdapter.ViewHolder> {
     Question question;

    public ResponseInnerRecyclerViewAdapter(Question questionsList) {

        this.question = questionsList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.itemTextView);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_answer, parent, false);
        return new ResponseInnerRecyclerViewAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.name.setText(question.getResponse());

    }

    @Override
    public int getItemCount() {
        return 1;
    }


}