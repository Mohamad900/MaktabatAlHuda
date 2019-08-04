package com.maktabat.al.huda.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.maktabat.al.huda.R;
import com.maktabat.al.huda.activity.BooksActivity;
import com.maktabat.al.huda.activity.BooksByCategoryActivity;
import com.maktabat.al.huda.model.Book;
import com.maktabat.al.huda.model.Category;
import com.maktabat.al.huda.network.Apis.BookInterface;
import com.maktabat.al.huda.network.RetrofitInstance;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class RecommendedCategories_Adapter extends RecyclerView.Adapter<RecommendedCategories_Adapter.MyViewHolder> {
    Context context;

    private List<Category> recommendedCategoryList;


    public RecommendedCategories_Adapter(Context context, List<Category> modalClassList) {
        this.recommendedCategoryList = modalClassList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_recommended, parent, false);
        return new MyViewHolder(itemView);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        Category modalClass = recommendedCategoryList.get(position);
        holder.cat_name.setText(modalClass.getName());

        //add listener
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GetBooksByCategory(position);
            }
        });
    }

    private void GetBooksByCategory(int position) {

        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Carregando....");
        progressDialog.show();

        BookInterface bookInterface = RetrofitInstance.getRetrofitInstance().create(BookInterface.class);
        bookInterface.getBooksByCategory(recommendedCategoryList.get(position)).enqueue(new Callback<List<Book>>() {
            @Override
            public void onResponse(Call<List<Book>> call, Response<List<Book>> response) {
                progressDialog.dismiss();
                if(response.body()!=null){
                    ArrayList<Book> booksArrayList = new ArrayList();
                    try {
                        booksArrayList.addAll(response.body());
                        Intent intent = new Intent(context,BooksByCategoryActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("booksList", Parcels.wrap(booksArrayList));
                        intent.putExtras(bundle);
                        context.startActivity(intent);

                    }catch (Exception e){
                        new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText(context.getResources().getString(R.string.loading_failed))
                                .setContentText("Algo deu errado").show();
                    }
                }else{
                    new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText(context.getResources().getString(R.string.loading_failed))
                            .setContentText(context.getResources().getString(R.string.no_internet_connection)).show();
                }
            }

            @Override
            public void onFailure(Call<List<Book>> call, Throwable t) {
                progressDialog.dismiss();
                new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText(context.getResources().getString(R.string.loading_failed))
                        .setContentText(context.getResources().getString(R.string.no_internet_connection)).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return recommendedCategoryList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView cat_name;

        public MyViewHolder(View view) {
            super(view);
            cat_name = (TextView) view.findViewById(R.id.cat_name);
        }
    }


}



