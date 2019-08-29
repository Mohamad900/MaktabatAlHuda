package com.maktabat.al.huda.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.maktabat.al.huda.R;
import com.maktabat.al.huda.activity.BooksByCategoryActivity;
import com.maktabat.al.huda.adapter.CategoriesFragmentAdapter;
import com.maktabat.al.huda.adapter.RecommendedCategories_Adapter;
import com.maktabat.al.huda.model.Book;
import com.maktabat.al.huda.model.Category;
import com.maktabat.al.huda.network.Apis.BookInterface;
import com.maktabat.al.huda.network.Apis.CategoryInterface;
import com.maktabat.al.huda.network.RetrofitInstance;
import com.maktabat.al.huda.util.Utils;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cn.pedant.SweetAlert.SweetAlertDialog;
import co.ceryle.fitgridview.FitGridView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by User on 6/3/2019.
 */

public class CategoryFragment extends Fragment {

    @BindView(R.id.recommended_recycleview)
    RecyclerView recommendedRecycleview;
    @BindView(R.id.gridView)
    FitGridView gridView;
    Unbinder unbinder;
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.connection)
    TextView connection;
    @BindView(R.id.scroll)
    ScrollView scroll;
    @BindView(R.id.recommended_cat_not_found_title)
    TextView recommendedCatNotFoundTitle;
    //@BindView(R.id.swiperefresh_items)
    //SwipeRefreshLayout swiperefreshItems;
    private ArrayList<Category> recommendedCategoriesArrayList;
    private ArrayList<Category> categoriesArrayList;
    private ArrayList<Book> booksArrayList;

    CategoriesFragmentAdapter gridAdapter;
    private Context context;
    ProgressDialog progressDialog;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View convertView = inflater.inflate(R.layout.fragment_category, container, false);
        unbinder = ButterKnife.bind(this, convertView);
        init();
        return convertView;
    }

    private void init() {
        //progress dialog
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Baixar....");
        categoriesArrayList = new ArrayList<>();
        /*swiperefreshItems.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                GetAllCategories();
            }
        });*/

        if (Utils.checkIfNewForCategories(context)) {
            GetAllCategories();
            Utils.changeNewForCategories(context);
        } else {

            if (Utils.hasCacheDataForCategories(context)) {
                scroll.setVisibility(View.VISIBLE);
                connection.setVisibility(View.GONE);
                recommendedCatNotFoundTitle.setVisibility(View.GONE);

                categoriesArrayList = new ArrayList<>();
                categoriesArrayList = Utils.getCacheDataForCategories(context);
                gridAdapter = new CategoriesFragmentAdapter(context, categoriesArrayList);
                gridView.setAdapter(gridAdapter);
                recommendedCategoriesArrayList = new ArrayList<>();
                for (int i = 0; i < categoriesArrayList.size(); i++) {
                    Category category = categoriesArrayList.get(i);
                    if (category.isRecommended) {
                        recommendedCategoriesArrayList.add(category);
                    }
                }
                if(recommendedCategoriesArrayList.size()>0) {
                    RecommendedCategories_Adapter adapter_category = new RecommendedCategories_Adapter(getActivity(), recommendedCategoriesArrayList);
                    LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(getActivity(), OrientationHelper.HORIZONTAL, false);
                    recommendedRecycleview.setLayoutManager(linearLayoutManager1);
                    recommendedRecycleview.setItemAnimator(new DefaultItemAnimator());
                    recommendedRecycleview.setAdapter(adapter_category);
                }else{
                    recommendedCatNotFoundTitle.setVisibility(View.VISIBLE);
                }

                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        GetBooksByCategory(i);
                    }
                });
            } else {
                scroll.setVisibility(View.GONE);
                connection.setText("Não foram encontradas categorias");
                connection.setVisibility(View.VISIBLE);
            }
        }
    }

    private void GetAllCategories() {
        progressDialog.show();
        CategoryInterface categoryInterface = RetrofitInstance.getRetrofitInstance().create(CategoryInterface.class);
        categoryInterface.getAllCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(@NonNull Call<List<Category>> call, @NonNull Response<List<Category>> response) {
                progressDialog.dismiss();
                //if(swiperefreshItems!=null)swiperefreshItems.setRefreshing(false);
                if (response.body() != null) {

                    if (response.body().size() > 0) {
                        scroll.setVisibility(View.VISIBLE);
                        connection.setVisibility(View.GONE);
                        recommendedCatNotFoundTitle.setVisibility(View.GONE);

                        categoriesArrayList = new ArrayList<>();
                        recommendedCategoriesArrayList = new ArrayList<>();

                        try {
                            categoriesArrayList.addAll(response.body());
                            gridAdapter = new CategoriesFragmentAdapter(getActivity(), categoriesArrayList);
                            gridView.setAdapter(gridAdapter);

                            for (int i = 0; i < response.body().size(); i++) {
                                Category category = response.body().get(i);
                                if (category.isRecommended) {
                                    recommendedCategoriesArrayList.add(category);
                                }
                            }
                            if(recommendedCategoriesArrayList.size()>0) {

                                RecommendedCategories_Adapter adapter_category = new RecommendedCategories_Adapter(getActivity(), recommendedCategoriesArrayList);
                                LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(getActivity(), OrientationHelper.HORIZONTAL, false);
                                recommendedRecycleview.setLayoutManager(linearLayoutManager1);
                                recommendedRecycleview.setItemAnimator(new DefaultItemAnimator());
                                recommendedRecycleview.setAdapter(adapter_category);

                            }else{
                                recommendedCatNotFoundTitle.setVisibility(View.VISIBLE);
                            }

                            Utils.saveCacheDataForCatgeories(context, categoriesArrayList);

                            //add listener
                            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                    GetBooksByCategory(i);
                                }
                            });

                        } catch (Exception e) {
                            if (Utils.hasCacheDataForCategories(context)) {
                                scroll.setVisibility(View.VISIBLE);
                                connection.setVisibility(View.GONE);
                                gridAdapter = new CategoriesFragmentAdapter(context, Utils.getCacheDataForCategories(context));
                                gridView.setAdapter(gridAdapter);
                                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                        GetBooksByCategory(i);
                                    }
                                });
                                recommendedCategoriesArrayList = new ArrayList<>();
                                for (int i = 0; i < Utils.getCacheDataForCategories(context).size(); i++) {
                                    Category category = Utils.getCacheDataForCategories(context).get(i);
                                    if (category.isRecommended) {
                                        recommendedCategoriesArrayList.add(category);
                                    }
                                }
                                if(recommendedCategoriesArrayList.size()>0) {
                                    RecommendedCategories_Adapter adapter_category = new RecommendedCategories_Adapter(getActivity(), recommendedCategoriesArrayList);
                                    LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(getActivity(), OrientationHelper.HORIZONTAL, false);
                                    recommendedRecycleview.setLayoutManager(linearLayoutManager1);
                                    recommendedRecycleview.setItemAnimator(new DefaultItemAnimator());
                                    recommendedRecycleview.setAdapter(adapter_category);
                                }else{
                                    recommendedCatNotFoundTitle.setVisibility(View.VISIBLE);
                                }

                            } else {
                                scroll.setVisibility(View.GONE);
                                connection.setText("Não foram encontradas categorias");
                                connection.setVisibility(View.VISIBLE);
                            }
                        }
                    } else {
                        Utils.clearCacheDataForCategories(context);
                        scroll.setVisibility(View.GONE);
                        connection.setText("Não foram encontradas categorias");
                        connection.setVisibility(View.VISIBLE);
                    }
                } else {

                    if (Utils.hasCacheDataForCategories(context)) {
                        scroll.setVisibility(View.VISIBLE);
                        connection.setVisibility(View.GONE);
                        gridAdapter = new CategoriesFragmentAdapter(context, Utils.getCacheDataForCategories(context));
                        gridView.setAdapter(gridAdapter);
                        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                GetBooksByCategory(i);
                            }
                        });
                        recommendedCategoriesArrayList = new ArrayList<>();
                        for (int i = 0; i < Utils.getCacheDataForCategories(context).size(); i++) {
                            Category category = Utils.getCacheDataForCategories(context).get(i);
                            if (category.isRecommended) {
                                recommendedCategoriesArrayList.add(category);
                            }
                        }
                        if(recommendedCategoriesArrayList.size()>0) {
                            RecommendedCategories_Adapter adapter_category = new RecommendedCategories_Adapter(getActivity(), recommendedCategoriesArrayList);
                            LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(getActivity(), OrientationHelper.HORIZONTAL, false);
                            recommendedRecycleview.setLayoutManager(linearLayoutManager1);
                            recommendedRecycleview.setItemAnimator(new DefaultItemAnimator());
                            recommendedRecycleview.setAdapter(adapter_category);
                        }else{
                            recommendedCatNotFoundTitle.setVisibility(View.VISIBLE);
                        }

                    } else {
                        scroll.setVisibility(View.GONE);
                        connection.setText("o carregamento de categorias falhou");
                        connection.setVisibility(View.VISIBLE);
                    }

                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Category>> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                //if(swiperefreshItems!=null)swiperefreshItems.setRefreshing(false);

                if (Utils.hasCacheDataForCategories(context)) {
                    scroll.setVisibility(View.VISIBLE);
                    connection.setVisibility(View.GONE);
                    gridAdapter = new CategoriesFragmentAdapter(context, Utils.getCacheDataForCategories(context));
                    gridView.setAdapter(gridAdapter);
                    gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            GetBooksByCategory(i);
                        }
                    });
                    recommendedCategoriesArrayList = new ArrayList<>();
                    for (int i = 0; i < Utils.getCacheDataForCategories(context).size(); i++) {
                        Category category = Utils.getCacheDataForCategories(context).get(i);
                        if (category.isRecommended) {
                            recommendedCategoriesArrayList.add(category);
                        }
                    }
                    if(recommendedCategoriesArrayList.size()>0) {
                        RecommendedCategories_Adapter adapter_category = new RecommendedCategories_Adapter(getActivity(), recommendedCategoriesArrayList);
                        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(getActivity(), OrientationHelper.HORIZONTAL, false);
                        recommendedRecycleview.setLayoutManager(linearLayoutManager1);
                        recommendedRecycleview.setItemAnimator(new DefaultItemAnimator());
                        recommendedRecycleview.setAdapter(adapter_category);
                    }else{
                        recommendedCatNotFoundTitle.setVisibility(View.VISIBLE);
                    }

                } else {
                    scroll.setVisibility(View.GONE);
                    connection.setText(getResources().getString(R.string.no_internet_connection));
                    connection.setVisibility(View.VISIBLE);
                }

            }
        });
    }

    private void GetBooksByCategory(int position) {
        progressDialog.show();
        BookInterface bookInterface = RetrofitInstance.getRetrofitInstance().create(BookInterface.class);
        bookInterface.getBooksByCategory(categoriesArrayList.size() == 0 || categoriesArrayList == null ? null : categoriesArrayList.get(position)).enqueue(new Callback<List<Book>>() {
            @Override
            public void onResponse(@NonNull Call<List<Book>> call, @NonNull Response<List<Book>> response) {
                progressDialog.dismiss();
                if (response.body() != null) {
                    booksArrayList = new ArrayList<>();
                    try {
                        booksArrayList.addAll(response.body());
                        Intent intent = new Intent(context, BooksByCategoryActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("booksList", Parcels.wrap(booksArrayList));
                        intent.putExtras(bundle);
                        startActivity(intent);

                    } catch (Exception e) {
                        new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText(getResources().getString(R.string.loading_failed))
                                .setContentText("Algo deu errado").show();
                    }
                } else {
                    new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText(getResources().getString(R.string.loading_failed))
                            .setContentText(getResources().getString(R.string.no_internet_connection)).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Book>> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText(getResources().getString(R.string.loading_failed))
                        .setContentText(getResources().getString(R.string.no_internet_connection)).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
