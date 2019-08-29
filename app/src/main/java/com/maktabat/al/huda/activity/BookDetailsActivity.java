package com.maktabat.al.huda.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.artifex.mupdfdemo.MuPDFActivity;
import com.maktabat.al.huda.R;
import com.maktabat.al.huda.Room.AppDatabase;
import com.maktabat.al.huda.configs.Constants;
import com.maktabat.al.huda.customfonts.MyTextView_Ubuntu_Light;
import com.maktabat.al.huda.customfonts.MyTextView_Ubuntu_Medium;
import com.maktabat.al.huda.model.Book;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class BookDetailsActivity extends AppCompatActivity {


    @BindView(R.id.book_image)
    ImageView bookImage;
    @BindView(R.id.book_title)
    MyTextView_Ubuntu_Medium bookTitle;
    @BindView(R.id.book_description_title)
    MyTextView_Ubuntu_Medium bookDescriptionTitle;
    @BindView(R.id.book_description)
    MyTextView_Ubuntu_Light bookDescription;
    @BindView(R.id.book_author)
    MyTextView_Ubuntu_Light bookAuthor;
    @BindView(R.id.book_publisher)
    MyTextView_Ubuntu_Light bookPublisher;
    /*@BindView(R.id.read)
    ImageView read;*/
    @BindView(R.id.download)
    ImageView download;
    @BindView(R.id.root)
    RelativeLayout root;
    @BindView(R.id.download_title)
    TextView downloadTitle;
    @BindView(R.id.remove)
    ImageView remove;
    private Book book;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);
        ButterKnife.bind(this);
        init();
        checkIfBookExist();
        //add listener
       /* read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ReadFileOnline().execute();
            }
        });*/
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    new DownloadFile().execute();
            }
        });
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    int numberOfDeletedRows = AppDatabase.getAppDatabase(context).bookDao().DeleteBook(new Book(book.id, book.title));
                    File file = new File(getFilesDir(), book.id + ".pdf");
                    boolean deleted = file.delete();
                    if (numberOfDeletedRows > 0 && deleted) {
                        download.setVisibility(View.VISIBLE);
                        remove.setVisibility(View.GONE);
                        downloadTitle.setText(getResources().getString(R.string.read));
                        downloadTitle.setGravity(Gravity.CENTER);
                        new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Reservar com sucesso removido")
                                .show();
                    } else {
                        download.setVisibility(View.GONE);
                        remove.setVisibility(View.VISIBLE);
                        downloadTitle.setText(getResources().getString(R.string.remove_book));
                        new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Não é possível excluir o livro....")
                                .setContentText(getResources().getString(R.string.something_wrong))
                                .show();
                    }
            }
        });
    }

    private void checkIfBookExist() {
        Book b = AppDatabase.getAppDatabase(context).bookDao().GetBook(book.id);
        if (b == null) {
            download.setVisibility(View.VISIBLE);
            remove.setVisibility(View.GONE);
            downloadTitle.setText(getResources().getString(R.string.read));
            downloadTitle.setGravity(Gravity.CENTER);
        } else {
            download.setVisibility(View.GONE);
            remove.setVisibility(View.VISIBLE);
            downloadTitle.setText(getResources().getString(R.string.remove_book));
            downloadTitle.setGravity(Gravity.CENTER);
        }
    }

    private void init() {
        Bundle args = getIntent().getExtras();
        book = Parcels.unwrap(args.getParcelable("bookDetail"));
        //load image
        Picasso.with(this).load(Constants.IMAGES_BASE_URL + book.getImage()).placeholder(R.drawable.default_image_not_available).into(bookImage);
        //set book data
        bookTitle.setText(book.getTitle());
        bookDescription.setText(book.getDescription());
        bookAuthor.setText(book.getAuthor());
        bookPublisher.setText(book.getPublisher());
        context = this;
    }

    private class DownloadFile extends AsyncTask<String, String, Boolean> {

        private ProgressDialog progressDialog;
        File file;

        /**
         * Before starting background thread
         * Show Progress Bar Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(BookDetailsActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        /**
         * Downloading file in background thread
         */
        @Override
        protected Boolean doInBackground(String... f_url) {
            int count;
            long total = 0;
            int lengthOfFile;
            byte data[] = new byte[1024];
            byte data2[] = new byte[1024];

            try {

                URL url = new URL(Constants.DOCUMENTS_BASE_URL + book.getDocumentName());
                URLConnection connection = url.openConnection();
                connection.connect();
                // getting file length
                lengthOfFile = connection.getContentLength();

                // input stream to read file - with 8k buffer
                InputStream input = new BufferedInputStream(url.openStream(), 8192);
                File dir = getApplicationContext().getFilesDir();
                file = new File(dir, book.id + ".pdf");

                // Output stream to write file
                FileOutputStream output = new FileOutputStream(file);

                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress("" + (int) ((total * 100) / lengthOfFile));
                    // writing data to file
                    output.write(data, 0, count);
                }

                AppDatabase.getAppDatabase(getApplicationContext()).bookDao().AddBook(new Book(book.id, book.title));

                // flushing output
                output.flush();
                // closing streams
                output.close();
                input.close();

                //add book to room local database
                try {
                    URL url2 = new URL(Constants.IMAGES_BASE_URL + book.getImage());
                    HttpURLConnection connection2 = (HttpURLConnection) url2.openConnection();
                    connection2.setDoInput(true);
                    connection2.connect();
                    InputStream input2 = new BufferedInputStream(url2.openStream(), 8192);
                    File dir2 = getApplicationContext().getFilesDir();
                    File file2 = new File(dir2, book.id + ".png");
                    FileOutputStream output2 = new FileOutputStream(file2);
                    while ((count = input2.read(data2)) != -1) {
                        // writing data to file
                        output2.write(data2, 0, count);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                //goToEdit(file.getAbsolutePath());


                return true;

            } catch (Exception e) {
                Log.d("error", e.getMessage());
            }
            return false;
        }

        /**
         * Updating progress bar
         */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            progressDialog.setProgress(Integer.parseInt(progress[0]));
        }


        @Override
        protected void onPostExecute(Boolean isSucceded) {
            // dismiss the dialog after the file was downloaded
            this.progressDialog.dismiss();
            if (!isSucceded) {
                download.setVisibility(View.VISIBLE);
                remove.setVisibility(View.GONE);
                downloadTitle.setText(getResources().getString(R.string.read));
                downloadTitle.setGravity(Gravity.CENTER);
                new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText(getResources().getString(R.string.loading_failed))
                        .setContentText(getResources().getString(R.string.something_wrong)).show();

            } else {
                download.setVisibility(View.GONE);
                remove.setVisibility(View.VISIBLE);
                downloadTitle.setText(getResources().getString(R.string.remove_book));
                downloadTitle.setGravity(Gravity.CENTER);
                openReaderPicker(file.getAbsolutePath());
            }
        }
    }

    private void openReaderPicker(final String filePath){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.pdf_reader_picker,null);
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();
        dialog.show();

        LinearLayout custom = dialogView.findViewById(R.id.custom_pdf_reader);
        LinearLayout defaultReader = dialogView.findViewById(R.id.default_pdf_reader);

        custom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToEdit(filePath);
                dialog.hide();
            }
        });

        defaultReader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDefaultPDFReader(filePath);
                dialog.hide();
            }
        });

    }

    private void openDefaultPDFReader(String filePath) {
        Intent intent = new Intent(context,PDFViewerActivity.class);
        intent.putExtra("FilePath",filePath);
        context.startActivity(intent);
    }

    private void goToEdit(String filePath) {
        Uri uri = Uri.parse(filePath);

        Intent intent = new Intent(this, MuPDFActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtra("edit", true);
        intent.setData(uri);
        //if document protected with password
        intent.putExtra("password", "encrypted PDF password");
        //if you need highlight link boxes
        intent.putExtra("linkhighlight", true);
        //if you don't need device sleep on reading document
        intent.putExtra("idleenabled", false);
        //set true value for horizontal page scrolling, false value for vertical page scrolling
        intent.putExtra("horizontalscrolling", true);
        //document name
        intent.putExtra("docname", "PDF document name");

        startActivity(intent);

    }
}
