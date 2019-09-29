package com.maktabat.al.huda.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.maktabat.al.huda.R;
import com.maktabat.al.huda.adapter.QuestionsCategoriesAdapter;
import com.maktabat.al.huda.model.Category;
import com.maktabat.al.huda.network.Apis.QuestionInterface;
import com.maktabat.al.huda.network.RetrofitInstance;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.ceryle.fitgridview.FitGridView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuestionsCategoriesActivity extends AppCompatActivity {

    @BindView(R.id.connection)
    TextView connection;
    @BindView(R.id.gridView)
    FitGridView gridView;
    QuestionsCategoriesAdapter gridAdapter;
    @BindView(R.id.scroll)
    ScrollView scroll;
    private ArrayList<Category> categoriesArrayList;
    Context context;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions_categories);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        context = this;
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Baixar....");
        GetAllCategories();
    }

    private void GetAllCategories() {
        progressDialog.show();
        QuestionInterface categoryInterface = RetrofitInstance.getRetrofitInstance().create(QuestionInterface.class);
        categoryInterface.getQuestionsCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                progressDialog.dismiss();
                if (response.body() != null) {
                    if (response.body().size() > 0) {
                        categoriesArrayList = new ArrayList<>();
                        categoriesArrayList.addAll(response.body());
                        gridAdapter = new QuestionsCategoriesAdapter(context, categoriesArrayList);
                        gridView.setAdapter(gridAdapter);
                        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                Intent intent = new Intent(QuestionsCategoriesActivity.this, QuestionsActivity.class);
                                intent.putExtra("categoryId", categoriesArrayList.get(i).getId());
                                startActivity(intent);
                            }
                        });
                    } else {
                        scroll.setVisibility(View.GONE);
                        connection.setText("Não foram encontradas categorias");
                        connection.setVisibility(View.VISIBLE);
                    }
                } else {
                    scroll.setVisibility(View.GONE);
                    connection.setText("o carregamento de categorias falhou");
                    connection.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                progressDialog.dismiss();
                scroll.setVisibility(View.GONE);
                connection.setText(getResources().getString(R.string.no_internet_connection));
                connection.setVisibility(View.VISIBLE);
            }
        });

    }
}
