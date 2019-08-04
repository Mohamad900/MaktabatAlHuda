package com.maktabat.al.huda.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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


public class Home_NewBooksAdapter extends RecyclerView.Adapter<Home_NewBooksAdapter.MyViewHolder> {

    private Context context;
    private List<Book> newBooksList;


    public Home_NewBooksAdapter(Context context, List<Book> newBooksList) {
        this.newBooksList = newBooksList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_newbook, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        final Book book = newBooksList.get(position);
        holder.book_title.setText(book.getTitle());
        holder.book_author.setText(book.getAuthor());
        Picasso.with(context).load(Constants.IMAGES_BASE_URL+book.getImage()).placeholder(R.drawable.default_image_not_available).into(holder.book_image);
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
        return newBooksList.size();
    }

     //ViewHolder class
     class MyViewHolder extends RecyclerView.ViewHolder {

        TextView book_title;
        TextView book_author;
        ImageView book_image;

         MyViewHolder(View view) {
            super(view);
            book_title = (TextView) view.findViewById(R.id.book_name);
            book_author = (TextView) view.findViewById(R.id.book_author);
            book_image = (ImageView) view.findViewById(R.id.book_image);
        }
    }
}



