package com.maktabat.al.huda.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.maktabat.al.huda.R;
import com.maktabat.al.huda.adapter.BooksAdapter;
import com.maktabat.al.huda.adapter.Home_NewBooksAdapter;
import com.maktabat.al.huda.model.Book;
import com.maktabat.al.huda.network.Apis.BookInterface;
import com.maktabat.al.huda.network.RetrofitInstance;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BooksActivity extends AppCompatActivity {

    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.appBarLayout)
    AppBarLayout appBarLayout;
    @BindView(R.id.recommended_books_recycleview)
    RecyclerView booksRecycleview;
    @BindView(R.id.no_books_found_title)
    TextView noBooksFoundTitle;
    private ArrayList<Book> booksArrayList;
    private BooksAdapter booksAdapter;
    Context context;

    ProgressDialog progressDialog;
    SweetAlertDialog errorSweetDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_books);
        ButterKnife.bind(this);
        init();
        GetAllBooks();
    }

    private void init() {
        //progress dialog
        context=this;
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Baixar....");
        //sweet alert
        errorSweetDialog = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText(getResources().getString(R.string.loading_failed))
                .setContentText(getResources().getString(R.string.something_wrong));
    }


    private void GetAllBooks() {
        progressDialog.show();
        BookInterface bookInterface = RetrofitInstance.getRetrofitInstance().create(BookInterface.class);
        bookInterface.getAllBooks().enqueue(new Callback<List<Book>>() {
            @Override
            public void onResponse(@NonNull Call<List<Book>> call, @NonNull Response<List<Book>> response) {
                progressDialog.dismiss();
                if (response.body() != null) {
                    booksArrayList = new ArrayList<Book>();
                    try {

                        booksArrayList.addAll(response.body());
                        if (booksArrayList != null) {
                            if (booksArrayList.size() > 0) {

                                booksRecycleview.setVisibility(View.VISIBLE);
                                noBooksFoundTitle.setVisibility(View.GONE);

                                booksAdapter = new BooksAdapter(context, booksArrayList);
                                booksRecycleview.setLayoutManager(new GridLayoutManager(context, 3));
                                booksRecycleview.setItemAnimator(new DefaultItemAnimator());
                                booksRecycleview.setAdapter(booksAdapter);

                            } else {
                                noBooksFoundTitle.setText(getResources().getString(R.string.no_books_found));
                                booksRecycleview.setVisibility(View.GONE);
                                noBooksFoundTitle.setVisibility(View.VISIBLE);
                            }
                        } else {
                            noBooksFoundTitle.setText(getResources().getString(R.string.no_books_found));
                            noBooksFoundTitle.setVisibility(View.VISIBLE);
                            booksRecycleview.setVisibility(View.GONE);
                        }

                    } catch (Exception e) {
                        noBooksFoundTitle.setText(getResources().getString(R.string.books_loading_failed));
                        noBooksFoundTitle.setVisibility(View.VISIBLE);
                        booksRecycleview.setVisibility(View.GONE);
                    }
                } else {
                    noBooksFoundTitle.setText(getResources().getString(R.string.no_books_found));
                    noBooksFoundTitle.setVisibility(View.VISIBLE);
                    booksRecycleview.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Book>> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                noBooksFoundTitle.setText(getResources().getString(R.string.no_internet_connection));
                noBooksFoundTitle.setVisibility(View.VISIBLE);
                booksRecycleview.setVisibility(View.GONE);
                new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText(getResources().getString(R.string.loading_failed))
                        .setContentText(getResources().getString(R.string.no_internet_connection)).show();
            }
        });
    }
}
