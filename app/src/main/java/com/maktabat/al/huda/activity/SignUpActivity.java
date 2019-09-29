package com.maktabat.al.huda.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.maktabat.al.huda.MainActivity;
import com.maktabat.al.huda.R;
import com.maktabat.al.huda.customfonts.MyTextView_Ubuntu_Bold;
import com.maktabat.al.huda.model.User;
import com.maktabat.al.huda.network.Apis.UserInterface;
import com.maktabat.al.huda.network.RetrofitInstance;
import com.maktabat.al.huda.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {

    Spinner spinner;
    List<String> items = new ArrayList<String>();
    ArrayAdapter<String> adapter;
    /*@BindView(R.id.country)
    Spinner countrySpinner;*/
    @BindView(R.id.loginBtn)
    MyTextView_Ubuntu_Bold loginBtn;
    @BindView(R.id.name)
    TextInputLayout name;
    @BindView(R.id.phoneNumber)
    TextInputLayout phoneNumber;
    @BindView(R.id.email)
    TextInputLayout email;
    @BindView(R.id.password)
    TextInputLayout password;
    @BindView(R.id.city)
    TextInputLayout city;
    @BindView(R.id.age)
    TextInputLayout age;
    @BindView(R.id.country)
    TextInputLayout country;
    //String country=null;
    Context context;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);
        context=this;
        //countrySpinner = findViewById(R.id.country);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Registro de usuário....");

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(name.getEditText().getText().toString().trim().isEmpty() || phoneNumber.getEditText().getText().toString().trim().isEmpty() || email.getEditText().getText().toString().trim().isEmpty() || password.getEditText().getText().toString().trim().isEmpty() || age.getEditText().getText().toString().trim().isEmpty() || city.getEditText().getText().toString().trim().isEmpty() || country.getEditText().getText().toString().trim().isEmpty()){
                    new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                            .setContentText(context.getResources().getString(R.string.emptyFields)).show();
                }else{
                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.getEditText().getText().toString().trim()).matches()) {
                        new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                                .setContentText("Endereço de email inválido").show();
                    }else {
                        register();
                    }
                }
            }
        });

       /* Locale[] locales = Locale.getAvailableLocales();
        ArrayList<String> countries = new ArrayList<String>();
        for (Locale locale : locales) {
            String country = locale.getDisplayCountry();
            if (country.trim().length() > 0 && !countries.contains(country)) {
                countries.add(country);
            }
        }
        Collections.sort(countries);
        countries.add(0, "Select Country");


        adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, countries);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        countrySpinner.setAdapter(adapter);

        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (position == 0) {

                }
                if (position != -1 && position!=0) {
                    country = countrySpinner.getItemAtPosition(position).toString();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });*/

    }

    private void register() {
        progressDialog.show();
        UserInterface userInterface = RetrofitInstance.getRetrofitInstance().create(UserInterface.class);
        userInterface.createUser(new User(name.getEditText().getText().toString().trim(),email.getEditText().getText().toString().trim(),phoneNumber.getEditText().getText().toString().trim(),age.getEditText().getText().toString().trim(),city.getEditText().getText().toString().trim(),country.getEditText().getText().toString().trim(),password.getEditText().getText().toString().trim())).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                progressDialog.dismiss();

                if (response.body() != null) {

                    if(response.body().equals("Succeeded")){
                        Utils.setLogin(context,true);
                        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                        intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK | intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }else if(response.body().equals("Exist")){
                        new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("registro falhou")
                                .setContentText("O usuário existe").show();
                    }else{
                        new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                                .setContentText("registro falhou").show();
                    }

                } else {
                    new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                            .setContentText("registro falhou").show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("registro falhou")
                        .setContentText(getResources().getString(R.string.no_internet_connection)).show();
            }
        });
    }
}
