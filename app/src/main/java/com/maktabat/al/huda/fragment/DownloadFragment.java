package com.maktabat.al.huda.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.maktabat.al.huda.R;
import com.maktabat.al.huda.Room.AppDatabase;
import com.maktabat.al.huda.adapter.DownloadedBooksAdapter;
import com.maktabat.al.huda.model.Book;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by User on 6/12/2019.
 */

public class DownloadFragment extends Fragment {

    @BindView(R.id.listView)
    ListView listView;
    @BindView(R.id.no_books_found_title)
    TextView noBooksFoundTitle;
    Unbinder unbinder;
    Context context;
    String pdfPattern = ".pdf";
    ArrayList<File> filteredFiles= new ArrayList<>();
    ArrayList<File> pdffiles= new ArrayList<>();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View convertView = inflater.inflate(R.layout.fragment_download, container, false);
        unbinder = ButterKnife.bind(this, convertView);

        try {

            File dir = context.getFilesDir();
            File[] files = dir.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {

                    if (files[i].isFile()) {
                        if (files[i].getName().endsWith(pdfPattern)) {
                            filteredFiles.add(files[i]);
                        }
                    }
                }
            }
            if (filteredFiles != null) {
                for(int i=0;i<filteredFiles.size();i++){
                    String Id = filteredFiles.get(i).getName().substring(0, filteredFiles.get(i).getName().lastIndexOf("."));
                    Book b = AppDatabase.getAppDatabase(context).bookDao().GetBook(Id);
                    if(b!=null){
                        pdffiles.add(filteredFiles.get(i));
                    }
                }
                if (pdffiles.size() > 0) {
                    listView.setVisibility(View.VISIBLE);
                    noBooksFoundTitle.setVisibility(View.GONE);
                    DownloadedBooksAdapter downloadedBooksAdapter = new DownloadedBooksAdapter(context, pdffiles);
                    listView.setAdapter(downloadedBooksAdapter);
                } else {
                    listView.setVisibility(View.GONE);
                    noBooksFoundTitle.setText(getResources().getString(R.string.no_books_found));
                    noBooksFoundTitle.setVisibility(View.VISIBLE);
                }
            } else {
                listView.setVisibility(View.GONE);
                noBooksFoundTitle.setText(getResources().getString(R.string.no_books_found));
                noBooksFoundTitle.setVisibility(View.VISIBLE);
            }
        }catch (Exception E){
            listView.setVisibility(View.GONE);
            noBooksFoundTitle.setText(getResources().getString(R.string.books_loading_failed));
            noBooksFoundTitle.setVisibility(View.VISIBLE);
        }
        return  convertView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
