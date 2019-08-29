package com.maktabat.al.huda.fragment;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.maktabat.al.huda.R;
import com.maktabat.al.huda.activity.BooksActivity;
import com.maktabat.al.huda.adapter.Home_NewBooksAdapter;
import com.maktabat.al.huda.adapter.Home_PopularBooksAdapter;
import com.maktabat.al.huda.adapter.Home_RecommendedBooksAdapter;
import com.maktabat.al.huda.configs.Constants;
import com.maktabat.al.huda.customfonts.MyTextView_Ubuntu_Bold;
import com.maktabat.al.huda.model.Book;
import com.maktabat.al.huda.model.Slider;
import com.maktabat.al.huda.network.Apis.BookInterface;
import com.maktabat.al.huda.network.Apis.SliderInterface;
import com.maktabat.al.huda.network.RetrofitInstance;
import com.maktabat.al.huda.util.Utils;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by User on 5/23/2019.
 */

public class HomeFragment extends Fragment implements BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener {


    @BindView(R.id.slider)
    SliderLayout mDemoSlider;
    Unbinder unbinder;
    @BindView(R.id.newbooks_recycleview)
    RecyclerView newbooksRecycleview;
    @BindView(R.id.recommended_recycleview)
    RecyclerView recommendedRecycleview;
    @BindView(R.id.popular_recycleview)
    RecyclerView popularRecycleview;
    @BindView(R.id.viewmore_recommendedbooks)
    MyTextView_Ubuntu_Bold viewmoreRecommendedbooks;
    @BindView(R.id.viewmore_popularbooks)
    MyTextView_Ubuntu_Bold viewmorePopularbooks;
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.appBarLayout)
    AppBarLayout appBarLayout;
    @BindView(R.id.connection)
    TextView connection;
    @BindView(R.id.scroll)
    NestedScrollView scroll;

    @BindView(R.id.recom_books_error)
    TextView recomBooksError;
    @BindView(R.id.popular_books_error)
    TextView popularBooksError;
    @BindView(R.id.viewmore_newbooks)
    MyTextView_Ubuntu_Bold viewmoreNewbooks;
    @BindView(R.id.new_books_error)
    TextView newBooksError;
    @BindView(R.id.swiperefresh_items)
    SwipeRefreshLayout swiperefreshItems;
    private Context context;

    Home_NewBooksAdapter newBooksAdapter;
    Home_RecommendedBooksAdapter recommendedBooksAdapter;
    Home_PopularBooksAdapter popularBooksAdapter;

    private ArrayList<Book> recommendeBooksArrayList;
    private ArrayList<Book> newBooksArrayList;
    private ArrayList<Book> popularBooksArrayList;
    ProgressDialog progressDialog;
    SweetAlertDialog errorSweetDialog;
    RotateAnimation rotate;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;

    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View convertView = inflater.inflate(R.layout.fragment_home, container, false);
        unbinder = ButterKnife.bind(this, convertView);
        init();
        //new CheckInternetConnection().execute();
        return convertView;
    }

    private void init() {
        //progress dialog
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Baixar....");
        //sweet alert
        errorSweetDialog = new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Carregamento falhou...")
                .setContentText("Unable to connect to the server!");

        if(Utils.checkIfNewForBooks(context)){
            setUpNewbooksRecycleview();
            GetRecommendedAndPopularBooks();
            setUpSlider();
            Utils.changeNewForBooks(context);
        }else{

            newbooksRecycleview.setVisibility(View.VISIBLE);
            viewmoreNewbooks.setVisibility(View.VISIBLE);
            newBooksError.setVisibility(View.GONE);
            recommendedRecycleview.setVisibility(View.VISIBLE);
            viewmoreRecommendedbooks.setVisibility(View.VISIBLE);
            popularRecycleview.setVisibility(View.VISIBLE);
            viewmorePopularbooks.setVisibility(View.VISIBLE);
            recomBooksError.setVisibility(View.GONE);
            popularBooksError.setVisibility(View.GONE);


            viewmoreNewbooks.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, BooksActivity.class);
                    startActivity(intent);
                }
            });

            if(Utils.hasCacheDataForNewBooks(context)){
                newBooksAdapter = new Home_NewBooksAdapter(getActivity(), Utils.getCacheDataForNewBooks(context));
                newbooksRecycleview.setLayoutManager(new LinearLayoutManager(getActivity(), OrientationHelper.HORIZONTAL, false));
                newbooksRecycleview.setItemAnimator(new DefaultItemAnimator());
                newbooksRecycleview.setAdapter(newBooksAdapter);
                ViewCompat.setNestedScrollingEnabled(newbooksRecycleview, false);
            }else{
                newbooksRecycleview.setVisibility(View.GONE);
                newBooksError.setText(getResources().getString(R.string.books_loading_failed));
                newBooksError.setVisibility(View.VISIBLE);
            }

            viewmoreRecommendedbooks.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, BooksActivity.class);
                    startActivity(intent);
                }
            });
            if(Utils.hasCacheDataForRecommendedBooks(context)){
                recommendedBooksAdapter = new Home_RecommendedBooksAdapter(getActivity(),Utils.getCacheDataForRecommendedBooks(context));
                recommendedRecycleview.setLayoutManager(new LinearLayoutManager(getActivity(), OrientationHelper.HORIZONTAL, false));
                recommendedRecycleview.setItemAnimator(new DefaultItemAnimator());
                recommendedRecycleview.setAdapter(recommendedBooksAdapter);
                ViewCompat.setNestedScrollingEnabled(recommendedRecycleview, false);
            }else{
                recommendedRecycleview.setVisibility(View.GONE);
                recomBooksError.setText(getResources().getString(R.string.books_loading_failed));
                recomBooksError.setVisibility(View.VISIBLE);
            }

            viewmorePopularbooks.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, BooksActivity.class);
                    startActivity(intent);
                }
            });
            if(Utils.hasCacheDataForPopularBooks(context)){
                popularBooksAdapter = new Home_PopularBooksAdapter(getActivity(), Utils.getCacheDataForPopularBooks(context));
                popularRecycleview.setLayoutManager(new LinearLayoutManager(getActivity(), OrientationHelper.HORIZONTAL, false));
                popularRecycleview.setItemAnimator(new DefaultItemAnimator());
                popularRecycleview.setAdapter(popularBooksAdapter);
                ViewCompat.setNestedScrollingEnabled(popularRecycleview, false);
            }else{
                popularRecycleview.setVisibility(View.GONE);
                popularBooksError.setText(getResources().getString(R.string.books_loading_failed));
                popularBooksError.setVisibility(View.VISIBLE);
            }

            if(Utils.hasCacheDataForSliders(context)){
                ArrayList<Slider> sliders= Utils.getCacheDataForSliders(context);
                HashMap<Integer, String> content2 = new HashMap<Integer, String>();

                for (int i = 0; i < sliders.size(); i++) {
                    Slider slider = sliders.get(i);
                    content2.put(i, Constants.SLIDER_IMAGES_BASE_URL + slider.getImage());
                }

                for (Integer index : content2.keySet()) {
                    TextSliderView textSliderView = new TextSliderView(context);
                    // initialize a SliderLayout
                    textSliderView
                            .description(sliders.get(index).getTitle())
                            .image(content2.get(index))
                            .setScaleType(BaseSliderView.ScaleType.Fit)
                            .setOnSliderClickListener(HomeFragment.this);


                    //add your extra information
                    textSliderView.bundle(new Bundle());
                    textSliderView.getBundle()
                            .putString("extra", index.toString());

                    mDemoSlider.addSlider(textSliderView);
                }
                mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Accordion);
                mDemoSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
                mDemoSlider.setCustomAnimation(new DescriptionAnimation());
                mDemoSlider.setDuration(3000);
                mDemoSlider.addOnPageChangeListener(HomeFragment.this);

            }else{
               createErrorImagesSlider();
            }
        }

        swiperefreshItems.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setUpNewbooksRecycleview();
                GetRecommendedAndPopularBooks();
            }
        });
    }

    private void setUpNewbooksRecycleview() {
        progressDialog.show();
        BookInterface bookInterface = RetrofitInstance.getRetrofitInstance().create(BookInterface.class);
        bookInterface.getNewBooks().enqueue(new Callback<List<Book>>() {
            @Override
            public void onResponse(@NonNull Call<List<Book>> call, @NonNull Response<List<Book>> response) {
                progressDialog.dismiss();
                if(swiperefreshItems!=null)swiperefreshItems.setRefreshing(false);
                if (response.body() != null) {

                    if(response.body().size()>0) {
                        newbooksRecycleview.setVisibility(View.VISIBLE);
                        viewmoreNewbooks.setVisibility(View.VISIBLE);
                        newBooksError.setVisibility(View.GONE);
                        newBooksArrayList = new ArrayList<Book>();

                        try {
                            newBooksArrayList.addAll(response.body());
                            newBooksAdapter = new Home_NewBooksAdapter(getActivity(), newBooksArrayList);
                            newbooksRecycleview.setLayoutManager(new LinearLayoutManager(getActivity(), OrientationHelper.HORIZONTAL, false));
                            newbooksRecycleview.setItemAnimator(new DefaultItemAnimator());
                            newbooksRecycleview.setAdapter(newBooksAdapter);
                            Utils.saveCacheDataForNewBooks(context,newBooksArrayList);
                            ViewCompat.setNestedScrollingEnabled(newbooksRecycleview, false);
                        } catch (Exception e) {
                            if(Utils.hasCacheDataForNewBooks(context)){
                                newBooksAdapter = new Home_NewBooksAdapter(getActivity(), Utils.getCacheDataForNewBooks(context));
                                newbooksRecycleview.setLayoutManager(new LinearLayoutManager(getActivity(), OrientationHelper.HORIZONTAL, false));
                                newbooksRecycleview.setItemAnimator(new DefaultItemAnimator());
                                newbooksRecycleview.setAdapter(newBooksAdapter);
                                ViewCompat.setNestedScrollingEnabled(newbooksRecycleview, false);
                            }else{
                                newbooksRecycleview.setVisibility(View.GONE);
                                newBooksError.setText(getResources().getString(R.string.books_loading_failed));
                                newBooksError.setVisibility(View.VISIBLE);
                            }
                        }
                    }else{
                                Utils.clearCacheDataForNewBooks(context);
                                newbooksRecycleview.setVisibility(View.GONE);
                                newBooksError.setText(getResources().getString(R.string.no_books_found));
                                newBooksError.setVisibility(View.VISIBLE);
                    }
                } else {
                    if(Utils.hasCacheDataForNewBooks(context)){
                        newBooksAdapter = new Home_NewBooksAdapter(getActivity(), Utils.getCacheDataForNewBooks(context));
                        newbooksRecycleview.setLayoutManager(new LinearLayoutManager(getActivity(), OrientationHelper.HORIZONTAL, false));
                        newbooksRecycleview.setItemAnimator(new DefaultItemAnimator());
                        newbooksRecycleview.setAdapter(newBooksAdapter);
                        ViewCompat.setNestedScrollingEnabled(newbooksRecycleview, false);
                    }else{
                        newbooksRecycleview.setVisibility(View.GONE);
                        newBooksError.setText(getResources().getString(R.string.books_loading_failed));
                        newBooksError.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Book>> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                if(swiperefreshItems!=null)swiperefreshItems.setRefreshing(false);
                if(Utils.hasCacheDataForNewBooks(context)){
                    newBooksAdapter = new Home_NewBooksAdapter(getActivity(), Utils.getCacheDataForNewBooks(context));
                    newbooksRecycleview.setLayoutManager(new LinearLayoutManager(getActivity(), OrientationHelper.HORIZONTAL, false));
                    newbooksRecycleview.setItemAnimator(new DefaultItemAnimator());
                    newbooksRecycleview.setAdapter(newBooksAdapter);
                    ViewCompat.setNestedScrollingEnabled(newbooksRecycleview, false);
                }else{
                    newbooksRecycleview.setVisibility(View.GONE);
                    newBooksError.setText(getResources().getString(R.string.books_loading_failed));
                    newBooksError.setVisibility(View.VISIBLE);
                }
            }
        });

        viewmoreNewbooks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, BooksActivity.class);
                startActivity(intent);
            }
        });
    }

    private void GetRecommendedAndPopularBooks() {
        progressDialog.show();
        BookInterface bookInterface = RetrofitInstance.getRetrofitInstance().create(BookInterface.class);
        bookInterface.getRecommendedAndPopularBooks().enqueue(new Callback<List<Book>>() {
            @Override
            public void onResponse(@NonNull Call<List<Book>> call, @NonNull Response<List<Book>> response) {
                progressDialog.dismiss();
                if(swiperefreshItems!=null)swiperefreshItems.setRefreshing(false);
                if (response.body() != null) {

                    recommendedRecycleview.setVisibility(View.VISIBLE);
                    viewmoreRecommendedbooks.setVisibility(View.VISIBLE);
                    popularRecycleview.setVisibility(View.VISIBLE);
                    viewmorePopularbooks.setVisibility(View.VISIBLE);
                    recomBooksError.setVisibility(View.GONE);
                    popularBooksError.setVisibility(View.GONE);

                    recommendeBooksArrayList = new ArrayList<>();
                    popularBooksArrayList = new ArrayList<>();

                    try {
                        for (int i = 0; i < response.body().size(); i++) {
                            Book book = response.body().get(i);
                            if (book.isRecommended) {
                                recommendeBooksArrayList.add(book);
                            }
                            if (book.isPopular) {
                                popularBooksArrayList.add(book);
                            }
                        }
                        if(recommendeBooksArrayList.size()>0) {
                            recommendedBooksAdapter = new Home_RecommendedBooksAdapter(getActivity(), recommendeBooksArrayList);
                            recommendedRecycleview.setLayoutManager(new LinearLayoutManager(getActivity(), OrientationHelper.HORIZONTAL, false));
                            recommendedRecycleview.setItemAnimator(new DefaultItemAnimator());
                            recommendedRecycleview.setAdapter(recommendedBooksAdapter);
                            Utils.saveCacheDataForRecommendedBooks(context,recommendeBooksArrayList);
                            ViewCompat.setNestedScrollingEnabled(recommendedRecycleview, false);
                        }else{
                            Utils.clearCacheDataForRecommendedBooks(context);
                            recommendedRecycleview.setVisibility(View.GONE);
                            recomBooksError.setText(getResources().getString(R.string.no_books_found));
                            recomBooksError.setVisibility(View.VISIBLE);
                        }

                        if(popularBooksArrayList.size()>0) {
                            popularBooksAdapter = new Home_PopularBooksAdapter(getActivity(), popularBooksArrayList);
                            popularRecycleview.setLayoutManager(new LinearLayoutManager(getActivity(), OrientationHelper.HORIZONTAL, false));
                            popularRecycleview.setItemAnimator(new DefaultItemAnimator());
                            popularRecycleview.setAdapter(popularBooksAdapter);
                            Utils.saveCacheDataForPopularBooks(context,popularBooksArrayList);
                            ViewCompat.setNestedScrollingEnabled(popularRecycleview, false);
                        }else{
                            Utils.clearCacheDataForPopularBooks(context);
                            popularRecycleview.setVisibility(View.GONE);
                            popularBooksError.setText(getResources().getString(R.string.no_books_found));
                            popularBooksError.setVisibility(View.VISIBLE);
                        }

                    } catch (Exception e) {
                        if(Utils.hasCacheDataForRecommendedBooks(context)){
                            recommendedBooksAdapter = new Home_RecommendedBooksAdapter(getActivity(),Utils.getCacheDataForRecommendedBooks(context));
                            recommendedRecycleview.setLayoutManager(new LinearLayoutManager(getActivity(), OrientationHelper.HORIZONTAL, false));
                            recommendedRecycleview.setItemAnimator(new DefaultItemAnimator());
                            recommendedRecycleview.setAdapter(recommendedBooksAdapter);
                            ViewCompat.setNestedScrollingEnabled(recommendedRecycleview, false);
                        }else{
                            recommendedRecycleview.setVisibility(View.GONE);
                            recomBooksError.setText(getResources().getString(R.string.books_loading_failed));
                            recomBooksError.setVisibility(View.VISIBLE);
                        }

                        if(Utils.hasCacheDataForPopularBooks(context)){
                            popularBooksAdapter = new Home_PopularBooksAdapter(getActivity(), Utils.getCacheDataForPopularBooks(context));
                            popularRecycleview.setLayoutManager(new LinearLayoutManager(getActivity(), OrientationHelper.HORIZONTAL, false));
                            popularRecycleview.setItemAnimator(new DefaultItemAnimator());
                            popularRecycleview.setAdapter(popularBooksAdapter);
                            ViewCompat.setNestedScrollingEnabled(popularRecycleview, false);
                        }else{
                            popularRecycleview.setVisibility(View.GONE);
                            popularBooksError.setText(getResources().getString(R.string.books_loading_failed));
                            popularBooksError.setVisibility(View.VISIBLE);
                        }
                    }

                } else {
                    if(Utils.hasCacheDataForRecommendedBooks(context)){
                        recommendedBooksAdapter = new Home_RecommendedBooksAdapter(getActivity(),Utils.getCacheDataForRecommendedBooks(context));
                        recommendedRecycleview.setLayoutManager(new LinearLayoutManager(getActivity(), OrientationHelper.HORIZONTAL, false));
                        recommendedRecycleview.setItemAnimator(new DefaultItemAnimator());
                        recommendedRecycleview.setAdapter(recommendedBooksAdapter);
                        ViewCompat.setNestedScrollingEnabled(recommendedRecycleview, false);
                    }else{
                        recommendedRecycleview.setVisibility(View.GONE);
                        recomBooksError.setText(getResources().getString(R.string.books_loading_failed));
                        recomBooksError.setVisibility(View.VISIBLE);
                    }

                    if(Utils.hasCacheDataForPopularBooks(context)){
                        popularBooksAdapter = new Home_PopularBooksAdapter(getActivity(), Utils.getCacheDataForPopularBooks(context));
                        popularRecycleview.setLayoutManager(new LinearLayoutManager(getActivity(), OrientationHelper.HORIZONTAL, false));
                        popularRecycleview.setItemAnimator(new DefaultItemAnimator());
                        popularRecycleview.setAdapter(popularBooksAdapter);
                        ViewCompat.setNestedScrollingEnabled(popularRecycleview, false);
                    }else{
                        popularRecycleview.setVisibility(View.GONE);
                        popularBooksError.setText(getResources().getString(R.string.books_loading_failed));
                        popularBooksError.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Book>> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                if(swiperefreshItems!=null)swiperefreshItems.setRefreshing(false);
                new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                        //.setTitleText(getResources().getString(R.string.loading_failed))
                        .setContentText(getResources().getString(R.string.no_internet_connection)).show();

                if(Utils.hasCacheDataForRecommendedBooks(context)){
                    recommendedBooksAdapter = new Home_RecommendedBooksAdapter(getActivity(),Utils.getCacheDataForRecommendedBooks(context));
                    recommendedRecycleview.setLayoutManager(new LinearLayoutManager(getActivity(), OrientationHelper.HORIZONTAL, false));
                    recommendedRecycleview.setItemAnimator(new DefaultItemAnimator());
                    recommendedRecycleview.setAdapter(recommendedBooksAdapter);
                    ViewCompat.setNestedScrollingEnabled(recommendedRecycleview, false);
                }else{
                    recommendedRecycleview.setVisibility(View.GONE);
                    recomBooksError.setText(getResources().getString(R.string.books_loading_failed));
                    recomBooksError.setVisibility(View.VISIBLE);
                }

                if(Utils.hasCacheDataForPopularBooks(context)){
                    popularBooksAdapter = new Home_PopularBooksAdapter(getActivity(), Utils.getCacheDataForPopularBooks(context));
                    popularRecycleview.setLayoutManager(new LinearLayoutManager(getActivity(), OrientationHelper.HORIZONTAL, false));
                    popularRecycleview.setItemAnimator(new DefaultItemAnimator());
                    popularRecycleview.setAdapter(popularBooksAdapter);
                    ViewCompat.setNestedScrollingEnabled(popularRecycleview, false);
                }else{
                    popularRecycleview.setVisibility(View.GONE);
                    popularBooksError.setText(getResources().getString(R.string.books_loading_failed));
                    popularBooksError.setVisibility(View.VISIBLE);
                }
            }
        });

        viewmoreRecommendedbooks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, BooksActivity.class);
                startActivity(intent);
            }
        });

        viewmorePopularbooks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, BooksActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setUpSlider() {
        //progressDialog.show();
        SliderInterface sliderInterface = RetrofitInstance.getRetrofitInstance().create(SliderInterface.class);
        sliderInterface.getSliders().enqueue(new Callback<List<Slider>>() {
            @Override
            public void onResponse(@NonNull Call<List<Slider>> call, @NonNull Response<List<Slider>> response) {
                //progressDialog.dismiss();
                if(swiperefreshItems!=null)swiperefreshItems.setRefreshing(false);
                mDemoSlider.removeAllSliders();
                if (response.body() != null) {

                    if(response.body().size()>0) {
                        HashMap<Integer, String> content = new HashMap<Integer, String>();
                        Utils.saveCacheDataForSliders(context, response.body());

                        try {
                            for (int i = 0; i < response.body().size(); i++) {
                                Slider slider = response.body().get(i);
                                content.put(i, Constants.SLIDER_IMAGES_BASE_URL + slider.getImage());
                            }

                            for (Integer index : content.keySet()) {
                                TextSliderView textSliderView = new TextSliderView(context);
                                // initialize a SliderLayout
                                textSliderView
                                        .description(response.body().get(index).getTitle())
                                        .image(content.get(index))
                                        .setScaleType(BaseSliderView.ScaleType.Fit)
                                        .setOnSliderClickListener(HomeFragment.this);


                                //add your extra information
                                textSliderView.bundle(new Bundle());
                                textSliderView.getBundle()
                                        .putString("extra", index.toString());

                                mDemoSlider.addSlider(textSliderView);
                            }
                            mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Accordion);
                            mDemoSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
                            mDemoSlider.setCustomAnimation(new DescriptionAnimation());
                            mDemoSlider.setDuration(3000);
                            mDemoSlider.addOnPageChangeListener(HomeFragment.this);
                        } catch (Exception e) {
                            if(Utils.hasCacheDataForSliders(context)){

                                ArrayList<Slider> sliders= Utils.getCacheDataForSliders(context);
                                HashMap<Integer, String> content2 = new HashMap<Integer, String>();

                                for (int i = 0; i < sliders.size(); i++) {
                                    Slider slider = sliders.get(i);
                                    content2.put(i, Constants.SLIDER_IMAGES_BASE_URL + slider.getImage());
                                }

                                for (Integer index : content2.keySet()) {
                                    TextSliderView textSliderView = new TextSliderView(context);
                                    // initialize a SliderLayout
                                    textSliderView
                                            .description(sliders.get(index).getTitle())
                                            .image(content2.get(index))
                                            .setScaleType(BaseSliderView.ScaleType.Fit)
                                            .setOnSliderClickListener(HomeFragment.this);


                                    //add your extra information
                                    textSliderView.bundle(new Bundle());
                                    textSliderView.getBundle()
                                            .putString("extra", index.toString());

                                    mDemoSlider.addSlider(textSliderView);
                                }
                                mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Accordion);
                                mDemoSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
                                mDemoSlider.setCustomAnimation(new DescriptionAnimation());
                                mDemoSlider.setDuration(3000);
                                mDemoSlider.addOnPageChangeListener(HomeFragment.this);

                            }else {
                                createErrorImagesSlider();
                            }
                        }
                    }else{
                        Utils.clearCacheDataForSliders(context);
                        createErrorImagesSlider();
                    }
                } else {

                    if(Utils.hasCacheDataForSliders(context)){

                        ArrayList<Slider> sliders= Utils.getCacheDataForSliders(context);
                        HashMap<Integer, String> content = new HashMap<Integer, String>();

                        for (int i = 0; i < sliders.size(); i++) {
                            Slider slider = sliders.get(i);
                            content.put(i, Constants.SLIDER_IMAGES_BASE_URL + slider.getImage());
                        }

                        for (Integer index : content.keySet()) {
                            TextSliderView textSliderView = new TextSliderView(context);
                            // initialize a SliderLayout
                            textSliderView
                                    .description(sliders.get(index).getTitle())
                                    .image(content.get(index))
                                    .setScaleType(BaseSliderView.ScaleType.Fit)
                                    .setOnSliderClickListener(HomeFragment.this);


                            //add your extra information
                            textSliderView.bundle(new Bundle());
                            textSliderView.getBundle()
                                    .putString("extra", index.toString());

                            mDemoSlider.addSlider(textSliderView);
                        }
                        mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Accordion);
                        mDemoSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
                        mDemoSlider.setCustomAnimation(new DescriptionAnimation());
                        mDemoSlider.setDuration(3000);
                        mDemoSlider.addOnPageChangeListener(HomeFragment.this);

                    }else {
                        createErrorImagesSlider();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Slider>> call, @NonNull Throwable t) {
                //progressDialog.dismiss();
                if(swiperefreshItems!=null)swiperefreshItems.setRefreshing(false);
                if(Utils.hasCacheDataForSliders(context)){

                    ArrayList<Slider> sliders= Utils.getCacheDataForSliders(context);
                    HashMap<Integer, String> content = new HashMap<Integer, String>();

                    for (int i = 0; i < sliders.size(); i++) {
                        Slider slider = sliders.get(i);
                        content.put(i, Constants.SLIDER_IMAGES_BASE_URL + slider.getImage());
                    }

                    for (Integer index : content.keySet()) {
                        TextSliderView textSliderView = new TextSliderView(context);
                        // initialize a SliderLayout
                        textSliderView
                                .description(sliders.get(index).getTitle())
                                .image(content.get(index))
                                .setScaleType(BaseSliderView.ScaleType.Fit)
                                .setOnSliderClickListener(HomeFragment.this);


                        //add your extra information
                        textSliderView.bundle(new Bundle());
                        textSliderView.getBundle()
                                .putString("extra", index.toString());

                        mDemoSlider.addSlider(textSliderView);
                    }
                    mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Accordion);
                    mDemoSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
                    mDemoSlider.setCustomAnimation(new DescriptionAnimation());
                    mDemoSlider.setDuration(3000);
                    mDemoSlider.addOnPageChangeListener(HomeFragment.this);

                }else {
                    createErrorImagesSlider();
                }
            }
        });
    }

    private void createErrorImagesSlider() {
        HashMap<String, Integer> content = new HashMap<String, Integer>();
        mDemoSlider.removeAllSliders();
        for (int i = 0; i < 4; i++) {
            content.put("+i+", R.drawable.image_not_avai);
        }

        for (String index : content.keySet()) {
            TextSliderView textSliderView = new TextSliderView(context);
            // initialize a SliderLayout
            textSliderView
                    .description("Não disponível")
                    .image(content.get(index))
                    .setScaleType(BaseSliderView.ScaleType.Fit)
                    .setOnSliderClickListener(HomeFragment.this);


            //add your extra information
            textSliderView.bundle(new Bundle());
            textSliderView.getBundle()
                    .putString("extra", index);

            mDemoSlider.addSlider(textSliderView);
        }
        mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Accordion);
        mDemoSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mDemoSlider.setCustomAnimation(new DescriptionAnimation());
        mDemoSlider.setDuration(3000);
        mDemoSlider.addOnPageChangeListener(HomeFragment.this);
    }

   /* private class CheckInternetConnection extends AsyncTask<String, String, Boolean> {

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
            if(swiperefreshItems!=null)swiperefreshItems.setRefreshing(false);
            if (isInternetAvailable) {
                scroll.setVisibility(View.VISIBLE);
                connection.setVisibility(View.GONE);
               // refresh.setVisibility(View.GONE);
                setUpSlider();
                setUpNewbooksRecycleview();
                GetRecommendedAndPopularBooks();
            } else {
                scroll.setVisibility(View.GONE);
                connection.setVisibility(View.VISIBLE);
                //refresh.setVisibility(View.VISIBLE);
            }
        }
    }*/


    @Override
    public void onStart() {
        mDemoSlider.startAutoCycle();
        super.onStart();
    }

    @Override
    public void onResume() {
        mDemoSlider.startAutoCycle();
        super.onResume();
    }

    @Override
    public void onStop() {
        // To prevent a memory leak on rotation, make sure to call stopAutoCycle() on the slider before activity or fragment is destroyed
        mDemoSlider.stopAutoCycle();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        mDemoSlider.stopAutoCycle();
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onSliderClick(BaseSliderView slider) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
