package com.maktabat.al.huda.activity;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.maktabat.al.huda.R;
import com.maktabat.al.huda.adapter.BooksAdapter;

import org.parceler.Parcels;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BooksByCategoryActivity extends AppCompatActivity {

    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.appBarLayout)
    AppBarLayout appBarLayout;
    @BindView(R.id.books_recycleview)
    RecyclerView booksRecycleview;
    @BindView(R.id.no_books_found_title)
    TextView noBooksFoundTitle;
    private ArrayList booksArrayList;
    private BooksAdapter booksAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_books_by_category);

        try {
            ButterKnife.bind(this);
            Bundle args = getIntent().getExtras();
            booksArrayList = Parcels.unwrap(args.getParcelable("booksList"));

            if(booksArrayList!=null) {
                if (booksArrayList.size() > 0) {
                    booksRecycleview.setVisibility(View.VISIBLE);
                    noBooksFoundTitle.setVisibility(View.GONE);
                    booksAdapter = new BooksAdapter(this, booksArrayList);
                    booksRecycleview.setLayoutManager(new GridLayoutManager(this, 3));
                    booksRecycleview.setItemAnimator(new DefaultItemAnimator());
                    booksRecycleview.setAdapter(booksAdapter);
                } else {
                    booksRecycleview.setVisibility(View.GONE);
                    noBooksFoundTitle.setText(getResources().getString(R.string.no_books_found));
                    noBooksFoundTitle.setVisibility(View.VISIBLE);
                }
            }else{
                booksRecycleview.setVisibility(View.GONE);
                noBooksFoundTitle.setText(getResources().getString(R.string.no_books_found));
                noBooksFoundTitle.setVisibility(View.VISIBLE);
            }

        }catch(Exception e){
            booksRecycleview.setVisibility(View.GONE);
            noBooksFoundTitle.setText(getResources().getString(R.string.books_loading_failed));
            noBooksFoundTitle.setVisibility(View.VISIBLE);
        }


    }
}
