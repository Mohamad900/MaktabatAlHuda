package com.maktabat.al.huda.fragment;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.maktabat.al.huda.R;
import com.maktabat.al.huda.activity.AboutActivity;
import com.maktabat.al.huda.activity.LoginActivity;
import com.maktabat.al.huda.activity.QuestionsActivity;
import com.maktabat.al.huda.activity.QuestionsCategoriesActivity;
import com.maktabat.al.huda.adapter.QuestionsCategoriesAdapter;
import com.maktabat.al.huda.customfonts.MyTextView_Ubuntu_Regular;
import com.maktabat.al.huda.model.Category;
import com.maktabat.al.huda.model.Question;
import com.maktabat.al.huda.model.Settings;
import com.maktabat.al.huda.network.Apis.QuestionInterface;
import com.maktabat.al.huda.network.Apis.SettingsInterface;
import com.maktabat.al.huda.network.RetrofitInstance;
import com.maktabat.al.huda.util.Utils;

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
 * Created by User on 7/6/2019.
 */

public class ProfileFragment extends Fragment {

    /*    @BindView(R.id.facebook)
        LinearLayout facebook;
        @BindView(R.id.instagram)
        LinearLayout instagram;*/
    @BindView(R.id.questions)
    LinearLayout questions;
    @BindView(R.id.rate)
    LinearLayout rate;
    @BindView(R.id.share)
    LinearLayout share;
    Unbinder unbinder;
    @BindView(R.id.phoneNumber)
    MyTextView_Ubuntu_Regular phoneNumber;
    @BindView(R.id.email)
    MyTextView_Ubuntu_Regular email;
    @BindView(R.id.borderView)
    View borderView;
    @BindView(R.id.sendQuestion)
    LinearLayout sendQuestion;
    @BindView(R.id.about)
    LinearLayout about;
    @BindView(R.id.signout)
    LinearLayout signout;
    private Context context;
    AlertDialog dialogBuilder;
    ProgressDialog progressDialog;
    ArrayList<Category> categoriesArrayList;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View convertView = inflater.inflate(R.layout.fragment_profile, container, false);
        unbinder = ButterKnife.bind(this, convertView);
        init();
        getSettings();
        return convertView;
    }

    private void getSettings() {
        SettingsInterface settingsInterface = RetrofitInstance.getRetrofitInstance().create(SettingsInterface.class);
        settingsInterface.getSettings().enqueue(new Callback<Settings>() {
            @Override
            public void onResponse(@NonNull Call<Settings> call, @NonNull Response<Settings> response) {

                try {
                    if (response.body() != null) {
                        if (borderView != null) borderView.setVisibility(View.VISIBLE);
                        phoneNumber.setText(response.body().getPhoneNumber());
                        email.setText(response.body().getEmail());
                    } else {
                        borderView.setVisibility(View.GONE);
                        phoneNumber.setText("");
                        email.setText("");
                    }
                } catch (Exception e) {
                    if (borderView != null) borderView.setVisibility(View.GONE);
                    phoneNumber.setText("");
                    email.setText("");
                }

            }

            @Override
            public void onFailure(@NonNull Call<Settings> call, @NonNull Throwable t) {

            }
        });
    }

    private void init() {

        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Baixar....");

        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.setLogin(context,false);
                Intent intent = new Intent(context, LoginActivity.class);
                intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK | intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish();
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_SUBJECT, "BIBLIOTECA ISLÂMICA");
                    String sAux = "http://play.google.com/store/apps/details?id=" + context.getPackageName();
                    i.putExtra(Intent.EXTRA_TEXT, sAux);
                    startActivity(Intent.createChooser(i, "escolha um"));
                } catch (Exception e) {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_SUBJECT, "BIBLIOTECA ISLAMICA");
                    String sAux = "market://details?id=" + context.getPackageName();
                    i.putExtra(Intent.EXTRA_TEXT, sAux);
                    startActivity(Intent.createChooser(i, "escolha um"));
                }
            }
        });

        rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName())));
                }
            }
        });

        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               /* new SweetAlertDialog(context, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                        .setTitleText("About us!")
                        .setContentText("The Islamic Library provides you with the best books, permanently and orderly, and provides an easy user interface")
                        .setCustomImage(R.drawable.ic_about)
                        .show();*/

                Intent intent = new Intent(context, AboutActivity.class);
                startActivity(intent);
            }
        });

        questions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, QuestionsCategoriesActivity.class);
                startActivity(intent);
            }
        });

        sendQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GetAllCategories();
                //showAlertDialog();
            }
        });

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

                        ArrayList<String> categoriesNames = new ArrayList<String>();

                        for(int i=0;i<response.body().size();i++){
                            categoriesNames.add(categoriesArrayList.get(i).name);
                        }
                        showAlertDialog(categoriesNames);

                    } else {
                        categoriesArrayList = null;
                        showAlertDialog(new ArrayList<String>());
                        new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                                .setTitleText("Nenhuma categoria").show();
                    }
                } else {
                    categoriesArrayList = null;
                    showAlertDialog(new ArrayList<String>());
                    new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Falha ao carregar categorias").show();
                }

            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                progressDialog.dismiss();
                categoriesArrayList = null;
                showAlertDialog(new ArrayList<String>());
                new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Falha ao carregar categorias").show();
            }
        });
    }

    private void showAlertDialog(final ArrayList<String> categoriesName) {

        final int[] selectedCategoryPosition = new int[1];
        dialogBuilder = new AlertDialog.Builder(context).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.send_question_alert_dialog, null);

        final EditText editText = (EditText) dialogView.findViewById(R.id.user_question);
        Button sendButton = dialogView.findViewById(R.id.sendButton);
        final Spinner spinnerCategories = dialogView.findViewById(R.id.spinnerCategories);

        ArrayAdapter arrayAdapter = new ArrayAdapter(context, android.R.layout.simple_spinner_item, categoriesName);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategories.setAdapter(arrayAdapter);

        spinnerCategories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

                selectedCategoryPosition[0] = position;

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub

            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editText.getText().toString() == null || editText.getText().toString().trim().equals("") || categoriesArrayList == null) {
                    new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Por favor, adicione sua pergunta").show();
                } else {
                    sendQuestion(editText.getText().toString(),selectedCategoryPosition[0]);
                }
            }
        });
        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
    }

    private void sendQuestion(String question, int categoryId) {
        progressDialog.show();
        QuestionInterface questionInterface = RetrofitInstance.getRetrofitInstance().create(QuestionInterface.class);
        Question questionObj = new Question(question,null,categoriesArrayList.get(categoryId).getId());
        questionInterface.sendQuestion(questionObj).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(@NonNull Call<Boolean> call, @NonNull Response<Boolean> response) {
                progressDialog.dismiss();
                if (dialogBuilder != null) dialogBuilder.dismiss();
                try {
                    if (response.body() != null) {

                        if (response.body()) {
                            new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
                                    .setTitleText("Pergunta foi enviada").show();
                        } else {
                            new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("O envio de perguntas falhou").show();
                        }

                    } else {
                        new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("O envio de perguntas falhou").show();
                    }
                } catch (Exception e) {
                    new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("O envio de perguntas falhou").show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Boolean> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                if (dialogBuilder != null) dialogBuilder.dismiss();
                new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("O envio de perguntas falhou").show();
            }
        });
    }

    //method to get the right URL to use in the intent
   /* public String getFacebookPageURL(Context context) {
        PackageManager packageManager = context.getPackageManager();
        try {
            int versionCode = packageManager.getPackageInfo("com.facebook.katana", 0).versionCode;
            if (versionCode >= 3002850) { //newer versions of fb app
                return "fb://facewebmodal/f?href=" + Constants.FACEBOOK_URL;
            } else { //older versions of fb app
                return "fb://page/" + Constants.FACEBOOK_PAGE_ID;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return Constants.FACEBOOK_URL; //normal web url
        }
    }*/

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
