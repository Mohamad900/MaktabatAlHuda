package com.maktabat.al.huda.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.maktabat.al.huda.R;
import com.maktabat.al.huda.adapter.BooksAdapter;
import com.maktabat.al.huda.customfonts.Edittext_Ubuntu_Regular;
import com.maktabat.al.huda.model.Book;
import com.maktabat.al.huda.network.Apis.BookInterface;
import com.maktabat.al.huda.network.RetrofitInstance;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by User on 6/16/2019.
 */

public class SearchFragment extends Fragment {


    Unbinder unbinder;
    @BindView(R.id.rcv_data)
    RecyclerView rcvData;
    @BindView(R.id.connection)
    TextView connection;
    @BindView(R.id.imv_search)
    ImageView imvSearch;
    @BindView(R.id.shape)
    FrameLayout shape;
    @BindView(R.id.search_box)
    Edittext_Ubuntu_Regular searchBox;
    private Context context;
    private String type = "0";
    ProgressDialog progressDialog;
    ArrayList<Book> booksList;
    BooksAdapter booksAdapter;
    SweetAlertDialog errorSweetDialog;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View convertView = inflater.inflate(R.layout.fragment_search, container, false);
        unbinder = ButterKnife.bind(this, convertView);
        init();
        new CheckInternetConnection().execute();
        return convertView;
    }

    private void init() {
        //progress dialog
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Baixar....");
        errorSweetDialog = new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Pesquisa falhou...")
                .setContentText("Há um falha!");

        imvSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (searchBox.getText().toString().trim().isEmpty()) {
                    booksList = new ArrayList<Book>();
                    booksAdapter = new BooksAdapter(getActivity(), booksList);
                    rcvData.setLayoutManager(new GridLayoutManager(context, 3));
                    rcvData.setItemAnimator(new DefaultItemAnimator());
                    rcvData.setAdapter(booksAdapter);
                    new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Aviso...")
                            .setContentText("O campo de pesquisa é obrigatório").show();
                } else {
                    searchBook(searchBox.getText().toString());
                }
            }
        });
    }

    private void searchBook(String word) {
        progressDialog.show();
        BookInterface bookInterface = RetrofitInstance.getRetrofitInstance().create(BookInterface.class);
        bookInterface.searchBooks(word).enqueue(new Callback<List<Book>>() {
            @Override
            public void onResponse(@NonNull Call<List<Book>> call, @NonNull Response<List<Book>> response) {
                progressDialog.dismiss();
                if (response.body() != null) {
                    booksList = new ArrayList<Book>();
                    try {
                        booksList.addAll(response.body());
                        if (booksList.size() > 0) {
                            booksAdapter = new BooksAdapter(getActivity(), booksList);
                            rcvData.setLayoutManager(new GridLayoutManager(context, 3));
                            rcvData.setItemAnimator(new DefaultItemAnimator());
                            rcvData.setAdapter(booksAdapter);
                        } else {
                            booksList = new ArrayList<Book>();
                            booksAdapter = new BooksAdapter(getActivity(), booksList);
                            rcvData.setLayoutManager(new GridLayoutManager(context, 3));
                            rcvData.setItemAnimator(new DefaultItemAnimator());
                            rcvData.setAdapter(booksAdapter);
                            new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                                    .setTitleText("Pesquisa concluída...")
                                    .setContentText(getResources().getString(R.string.no_books_found)).show();
                        }
                    } catch (Exception e) {
                        new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Pesquisa falhou...")
                                .setContentText(getResources().getString(R.string.something_wrong)).show();
                    }
                } else {
                    new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Pesquisa falhou...")
                            .setContentText(getResources().getString(R.string.something_wrong)).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Book>> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Pesquisa falhou...")
                        .setContentText(getResources().getString(R.string.something_wrong)).show();
            }
        });
    }

    private class CheckInternetConnection extends AsyncTask<String, String, Boolean> {

        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("Verifique a conexão....");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... f_url) {
            try {
                return InetAddress.getByName("www.google.com").isReachable(5000);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean isInternetAvailable) {
            // dismiss the dialog after the file was downloaded
            this.progressDialog.dismiss();
            if (isInternetAvailable) {
                //shape.setVisibility(View.VISIBLE);
                rcvData.setVisibility(View.VISIBLE);
                connection.setVisibility(View.GONE);
            } else {
                //shape.setVisibility(View.GONE);
                rcvData.setVisibility(View.GONE);
                connection.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
