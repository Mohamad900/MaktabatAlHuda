package com.maktabat.al.huda.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.maktabat.al.huda.R;
import com.maktabat.al.huda.adapter.QuestionsExpandableAdapter;
import com.maktabat.al.huda.customviews.CustomSwipeToRefresh;
import com.maktabat.al.huda.model.Category;
import com.maktabat.al.huda.model.Question;
import com.maktabat.al.huda.model.QuestionCategory;
import com.maktabat.al.huda.network.Apis.QuestionInterface;
import com.maktabat.al.huda.network.RetrofitInstance;
import com.maktabat.al.huda.util.Utils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuestionsActivity extends AppCompatActivity {

    @BindView(R.id.questionsRecyclerView)
    RecyclerView expanderRecyclerView;
    @BindView(R.id.error)
    TextView error;
    Context context;
    ProgressDialog progressDialog;
    @BindView(R.id.swiperefresh_items)
    CustomSwipeToRefresh swiperefreshItems;
    String categoryId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);
        ButterKnife.bind(this);
        context = this;

        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Baixar....");

        try {
            if (getIntent() != null) {
                categoryId = getIntent().getStringExtra("categoryId");
            }
        }catch (Exception e){
            categoryId = null;
        }

        initiateExpander();

        swiperefreshItems.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initiateExpander();
            }
        });

    }

    private void initiateExpander() {
        progressDialog.show();
        QuestionInterface questionInterface = RetrofitInstance.getRetrofitInstance().create(QuestionInterface.class);
        questionInterface.GetAllFilteredQuestionsByCategory(new QuestionCategory(categoryId)).enqueue(new Callback<List<Question>>() {
            @Override
            public void onResponse(@NonNull Call<List<Question>> call, @NonNull Response<List<Question>> response) {
                progressDialog.dismiss();
                if(swiperefreshItems!=null)swiperefreshItems.setRefreshing(false);
                try {
                    if (response.body() != null) {

                        if (response.body().size() > 0) {
                            error.setVisibility(View.GONE);
                            expanderRecyclerView.setVisibility(View.VISIBLE);
                            expanderRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                            expanderRecyclerView.setAdapter(new QuestionsExpandableAdapter(getApplicationContext(), response.body()));
                        } else {
                            error.setVisibility(View.VISIBLE);
                            error.setText("Não há perguntas");
                            expanderRecyclerView.setVisibility(View.GONE);
                        }

                    } else {
                            error.setVisibility(View.VISIBLE);
                            error.setText("Falha ao carregar perguntas");
                            expanderRecyclerView.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                        error.setVisibility(View.VISIBLE);
                        error.setText("Falha ao carregar perguntas");
                        expanderRecyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Question>> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                if(swiperefreshItems!=null)swiperefreshItems.setRefreshing(false);
                error.setVisibility(View.VISIBLE);
                error.setText("Falha ao carregar perguntas");
                expanderRecyclerView.setVisibility(View.GONE);
            }
        });
    }
}
