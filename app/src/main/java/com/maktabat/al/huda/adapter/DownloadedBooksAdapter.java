package com.maktabat.al.huda.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.artifex.mupdfdemo.MuPDFActivity;
import com.maktabat.al.huda.R;
import com.maktabat.al.huda.Room.AppDatabase;
import com.maktabat.al.huda.activity.PDFViewerActivity;
import com.maktabat.al.huda.model.Book;
import com.maktabat.al.huda.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.zip.Inflater;

import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * Created by Admin on 16-Sep-18.
 */

public class DownloadedBooksAdapter extends BaseAdapter {
    private ArrayList<File> files;
    Inflater infalter;
    Context context;


    public DownloadedBooksAdapter(Context context, ArrayList<File> files) {;
        this.files = files;
        this.context=context;
    }

    @Override
    public int getCount() {
        return files.size();
    }

    @Override
    public Object getItem(int position) {
        return files.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context).
                    inflate(R.layout.downloaded_book_item_row, parent, false);
        }

        TextView txtFileName = convertView.findViewById(R.id.txtFileName);
        TextView txtDate = convertView.findViewById(R.id.txtDate);
        final TextView textViewOptions = convertView.findViewById(R.id.textViewOptions);
        ImageView imgIconFile = convertView.findViewById(R.id.imgIconFile);

        final File currentItem = (File) getItem(position);
        String Id = currentItem.getName().substring(0, currentItem.getName().lastIndexOf("."));
        final Book b = AppDatabase.getAppDatabase(context).bookDao().GetBook(Id);
        if(b!=null)
        txtFileName.setText(b.title);

        File imagePath=new File(context.getFilesDir(),Id+".png");
        if(imagePath !=null && imagePath.exists()){
            imgIconFile.setImageDrawable(Drawable.createFromPath(imagePath.toString()));
        }else {
            imgIconFile.setImageResource(R.drawable.default_image_not_available);
        }

        //listener
        textViewOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(textViewOptions,b,position);
            }
        });

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openReaderPicker(currentItem.getAbsolutePath());
                //goToEdit(currentItem.getAbsolutePath(),true);
            }
        });

        txtDate.setText(Utils.buildTextFileSize(currentItem.length()) + "    " +  Utils.longToDateString(currentItem.lastModified()));

        return convertView;
    }

    private void showPopupMenu(TextView textViewOptions, final Book book, final int position) {
        //creating a popup menu
        PopupMenu popup = new PopupMenu(context, textViewOptions);
        //inflating menu from xml resource
        popup.inflate(R.menu.downloaded_book_options);
        //adding click listener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.deleteOption:
                        int numberOfDeletedRows = AppDatabase.getAppDatabase(context).bookDao().DeleteBook(new Book(book.id, book.title));
                        File file = new File(context.getFilesDir(), book.id + ".pdf");
                        boolean deleted = file.delete();
                        if (numberOfDeletedRows > 0 && deleted) {
                            files.remove(position);
                            notifyDataSetChanged();
                            new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
                                    .setTitleText("Livro removido com sucesso")
                                    .show();
                        } else {
                            new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("O livro não pode ser excluído...")
                                    .setContentText("Algo deu errado")
                                    .show();
                        }
                        break;
                }
                return false;
            }
        });
        //displaying the popup
        popup.show();
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
                goToEdit(filePath,true);
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

    private void goToEdit(String filePath, boolean edit) {

        try {
            Uri uri = Uri.parse(filePath);

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            sharedPrefs.edit().putString("prefKeyLanguage", "en").apply();
            //Uri uri = Uri.parse("file:///android_asset/" + TEST_FILE_NAME);
            Intent intent = new Intent(context, MuPDFActivity.class);
            intent.setAction(Intent.ACTION_VIEW);
            if (!edit)
                intent.putExtra("edit", false);
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

            context.startActivity(intent);
        }catch (Exception e){
            Log.d("pdf error",e.getMessage());
        }
    }
}
