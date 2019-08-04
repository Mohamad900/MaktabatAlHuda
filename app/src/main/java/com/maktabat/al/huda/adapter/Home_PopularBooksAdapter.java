package com.maktabat.al.huda.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.maktabat.al.huda.R;
import com.maktabat.al.huda.activity.BookDetailsActivity;
import com.maktabat.al.huda.configs.Constants;
import com.maktabat.al.huda.model.Book;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.util.List;

/**
 * Created by User on 6/3/2019.
 */

public class Home_PopularBooksAdapter extends RecyclerView.Adapter<Home_PopularBooksAdapter.MyViewHolder> {
    private Context context;
    private List<Book> popularBooksList;

    public Home_PopularBooksAdapter(Context context, List<Book> popularBooksList) {
        this.popularBooksList = popularBooksList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_popularbook, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final Book book = popularBooksList.get(position);
        Picasso.with(context).load(Constants.IMAGES_BASE_URL+book.getImage()).placeholder(R.drawable.default_image_not_available).into(holder.book_image);
        holder.book_title.setText(book.getTitle());
        //listener
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,BookDetailsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("bookDetail", Parcels.wrap(book));
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return popularBooksList.size();
    }

    //ViewHolder class
    class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView book_image;
        TextView book_title;

        MyViewHolder(View view) {
            super(view);
            book_title = (TextView) view.findViewById(R.id.book_title);
            book_image = (ImageView) view.findViewById(R.id.book_image);
        }
    }
}
