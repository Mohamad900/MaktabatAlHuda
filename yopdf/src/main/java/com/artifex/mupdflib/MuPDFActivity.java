package com.artifex.mupdflib;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.NetworkOnMainThreadException;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import com.artifex.mupdflib.CallbackApplication.MuPDFCallbackClass;

import com.yo.mupdf.R;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executor;

//import android.text.method.PasswordTransformationMethod;
//import android.widget.SeekBar;

class ThreadPerTaskExecutor implements Executor {
    public void execute(Runnable r) {
        new Thread(r).start();
    }
}

public class MuPDFActivity extends Activity implements FilePicker.FilePickerSupport {
    /* The core rendering instance */
    //enum TopBarMode {Main, Search, Annot, Delete, More, Accept};
    enum TopBarMode {
        Main, Search, Annot, Delete
    }

    enum AcceptMode {
        Highlight,
        Underline,
        StrikeOut,
        Ink,
        CopyText
    }


    //private final int OUTLINE_REQUEST = 0;
    private final int PRINT_REQUEST = 1;
    private final int FILEPICK_REQUEST = 2;
    private final int PAGE_CHOICE_REQUEST = 3;
    private MuPDFCore core;
    private String mFileName;
    private String mDocName;
    private int mOrientation;
    private MuPDFReaderView mDocView;
    private View mButtonsView;
    private ImageView imgTool;
    private boolean mButtonsVisible;
    // private EditText mPasswordView;
    // private TextView mFilenameView;
    ///private SeekBar mPageSlider;
    ///private int mPageSliderRes;
    private LinearLayout highlightButton, underlineButton, strikeButton, toolLayout;
    private TextView mPageNumberView;
    private TextView mInfoView;
    private ImageButton mSearchButton, closeReader;
    private LinearLayout drawButton, copyButton, deleteButton;
    private RelativeLayout layoutCancelOrFinish, layoutDelete;
    private TextView cancelEdit, finishEdit;
    private AcceptMode acceptMode;
    //private ImageButton mReflowButton;
    //private ImageButton mOutlineButton;
    //private ImageButton	mMoreButton;
    //private TextView     mAnnotTypeText;
    //private ImageButton mAnnotButton;
    private ViewAnimator mTopBarSwitcher;
    //private ImageButton mLinkButton;
    private TopBarMode mTopBarMode = TopBarMode.Main;
    private ImageButton mSearchBack;
    private ImageButton mSearchFwd;
    private EditText mSearchText;
    private SearchTask mSearchTask;
    private AlertDialog.Builder mAlertBuilder;
    //private boolean mLinkHighlight = false;
    private final Handler mHandler = new Handler();
    private FrameLayout mPreviewBarHolder;
    private TwoWayView mPreview;
    private ToolbarPreviewAdapter pdfPreviewPagerAdapter;
    private boolean mAlertsActive = false;
    //private boolean mReflow = false;
    private AsyncTask<Void, Void, MuPDFAlert> mAlertTask;
    private AlertDialog mAlertDialog;
    private FilePicker mFilePicker;
    private TextView txtEdit, txtDelete, cancelDelete;
    private TextSelectionData selectionCoordinates;

    private ShareActionProvider mShareActionProvider;


    public void createAlertWaiter() {
        mAlertsActive = true;
        // All mupdf library calls are performed on asynchronous tasks to avoid stalling
        // the UI. Some calls can lead to javascript-invoked requests to display an
        // alert dialog and collect a reply from the user. The task has to be blocked
        // until the user's reply is received. This method creates an asynchronous task,
        // the purpose of which is to wait of these requests and produce the dialog
        // in response, while leaving the core blocked. When the dialog receives the
        // user's response, it is sent to the core via replyToAlert, unblocking it.
        // Another alert-waiting task is then created to pick up the next alert.
        if (mAlertTask != null) {
            mAlertTask.cancel(true);
            mAlertTask = null;
        }
        if (mAlertDialog != null) {
            mAlertDialog.cancel();
            mAlertDialog = null;
        }
        mAlertTask = new AsyncTask<Void, Void, MuPDFAlert>() {

            @Override
            protected MuPDFAlert doInBackground(Void... arg0) {
                if (!mAlertsActive)
                    return null;

                return core.waitForAlert();
            }

            @Override
            protected void onPostExecute(final MuPDFAlert result) {
                // core.waitForAlert may return null when shutting down
                if (result == null)
                    return;
                final MuPDFAlert.ButtonPressed pressed[] = new MuPDFAlert.ButtonPressed[3];
                for (int i = 0; i < 3; i++)
                    pressed[i] = MuPDFAlert.ButtonPressed.None;
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mAlertDialog = null;
                        if (mAlertsActive) {
                            int index = 0;
                            switch (which) {
                                case AlertDialog.BUTTON1:
                                    index = 0;
                                    break;
                                case AlertDialog.BUTTON2:
                                    index = 1;
                                    break;
                                case AlertDialog.BUTTON3:
                                    index = 2;
                                    break;
                            }
                            result.buttonPressed = pressed[index];
                            // Send the user's response to the core, so that it can
                            // continue processing.
                            core.replyToAlert(result);
                            // Create another alert-waiter to pick up the next alert.
                            createAlertWaiter();
                        }
                    }
                };
                mAlertDialog = mAlertBuilder.create();
                mAlertDialog.setTitle(result.title);
                mAlertDialog.setMessage(result.message);
                switch (result.iconType) {
                    case Error:
                        break;
                    case Warning:
                        break;
                    case Question:
                        break;
                    case Status:
                        break;
                }
                switch (result.buttonGroupType) {
                    case OkCancel:
                        mAlertDialog.setButton(AlertDialog.BUTTON2, getString(R.string.cancel), listener);
                        pressed[1] = MuPDFAlert.ButtonPressed.Cancel;
                    case Ok:
                        mAlertDialog.setButton(AlertDialog.BUTTON1, getString(R.string.okay), listener);
                        pressed[0] = MuPDFAlert.ButtonPressed.Ok;
                        break;
                    case YesNoCancel:
                        mAlertDialog.setButton(AlertDialog.BUTTON3, getString(R.string.cancel), listener);
                        pressed[2] = MuPDFAlert.ButtonPressed.Cancel;
                    case YesNo:
                        mAlertDialog.setButton(AlertDialog.BUTTON1, getString(R.string.yes), listener);
                        pressed[0] = MuPDFAlert.ButtonPressed.Yes;
                        mAlertDialog.setButton(AlertDialog.BUTTON2, getString(R.string.no), listener);
                        pressed[1] = MuPDFAlert.ButtonPressed.No;
                        break;
                }
                mAlertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        mAlertDialog = null;
                        if (mAlertsActive) {
                            result.buttonPressed = MuPDFAlert.ButtonPressed.None;
                            core.replyToAlert(result);
                            createAlertWaiter();
                        }
                    }
                });

                mAlertDialog.show();
            }
        };

        mAlertTask.executeOnExecutor(new ThreadPerTaskExecutor());
    }

    public void destroyAlertWaiter() {
        mAlertsActive = false;
        if (mAlertDialog != null) {
            mAlertDialog.cancel();
            mAlertDialog = null;
        }
        if (mAlertTask != null) {
            mAlertTask.cancel(true);
            mAlertTask = null;
        }
    }

    private MuPDFCore openFile(String path) {
        int lastSlashPos = path.lastIndexOf('/');
        mFileName = lastSlashPos == -1 ? path
                : path.substring(lastSlashPos + 1);
        System.out.println("Trying to open " + path);
        try {
            core = new MuPDFCore(this, path);
            // New file: drop the old outline data
            //OutlineActivityData.set(null);
            PDFPreviewGridActivityData.set(null);
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
        return core;
    }

    //private MuPDFCore openBuffer(byte buffer[])
    private MuPDFCore openBuffer(byte buffer[], String magic) {
        System.out.println("Trying to open byte buffer");
        try {
            //core = new MuPDFCore(this, buffer);
            mFileName="test";
            core = new MuPDFCore(this, buffer, magic);

            // New file: drop the old outline data
            //OutlineActivityData.set(null);
            PDFPreviewGridActivityData.set(null);
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
        return core;
    }


    @SuppressWarnings("unused")
    private Context getContext() {
        return this;
    }

    private void setCurrentlyViewedPreview() {
        int i = mDocView.getDisplayedViewIndex();
        if (core.getDisplayPages() == 2) {
            i = (i * 2) - 1;
        }
        pdfPreviewPagerAdapter.setCurrentlyViewing(i);
        centerPreviewAtPosition(i);
    }

    public void centerPreviewAtPosition(int position) {
        if (mPreview.getChildCount() > 0) {
            View child = mPreview.getChildAt(0);
            // assume all children the same width
            int childmeasuredwidth = child.getMeasuredWidth();

            if (childmeasuredwidth > 0) {
                if (core.getDisplayPages() == 2) {
                    mPreview.setSelectionFromOffset(position,
                            (mPreview.getWidth() / 2) - (childmeasuredwidth));
                } else {
                    mPreview.setSelectionFromOffset(position,
                            (mPreview.getWidth() / 2)
                                    - (childmeasuredwidth / 2));
                }
            } else {
                Log.e("centerOnPosition", "childmeasuredwidth = 0");
            }
        } else {
            Log.e("centerOnPosition", "childcount = 0");
        }
    }
//    public class CopyActionModeCallback implements ActionMode.Callback {
//        private Intent mShareIntent;
//
//        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
//            return false;
//        }
//
//        public boolean onCreateActionMode(ActionMode actionMode, final Menu menu) {
//            MuPDFActivity.this.getMenuInflater().inflate(R.menu.copy, menu);
//            MuPDFActivity.this.mDocView.setTextSelectedListener(new MuPDFReaderView.OnTextSelectedListener() {
//                public void onTextSelected(float f, float f2, float f3, float f4) {
//                    MuPDFActivity.this.mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menu.findItem(1.8077562E38f));
//                    CopyActionModeCallback.this.mShareIntent = new Intent();
//                    CopyActionModeCallback.this.mShareIntent.setAction("android.intent.action.SEND");
//                    CopyActionModeCallback.this.mShareIntent.setType(MuPDFActivity.this.getString(1.874257E38f));
//                    CopyActionModeCallback.this.mShareIntent.putExtra("android.intent.extra.TEXT", ((MuPDFView) MuPDFActivity.this.mDocView.getDisplayedView()).getSelection());
//                    MuPDFActivity.this.mShareActionProvider.setShareIntent(CopyActionModeCallback.this.mShareIntent);
//                }
//            });
//            MuPDFActivity.this.mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menu.findItem(R.id.action_share));
//            if (MuPDFActivity.this.mShareActionProvider != null) {
//                MuPDFActivity.this.mShareActionProvider.setShareIntent(this.mShareIntent);
//            }
//            return true;
//        }
//
//        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
//            actionMode = menuItem.getItemId();
//            menuItem = null;
//            if (actionMode != R.id.accept) {
//                return false;
//            }
//            int i;
//            MuPDFView muPDFView = (MuPDFView) MuPDFActivity.this.mDocView.getDisplayedView();
//            if (muPDFView != null) {
//                menuItem = muPDFView.copySelection();
//            }
//            actionMode = MuPDFActivity.this;
//            if (menuItem != null) {
//                menuItem = MuPDFActivity.this;
//                i = R.string.copied_to_clipboard;
//            } else {
//                menuItem = MuPDFActivity.this;
//                i = R.string.no_text_selected;
//            }
//            actionMode.showInfo(menuItem.getString(i));
//            if (MuPDFActivity.this.copyActionMode != null) {
//                MuPDFActivity.this.copyActionMode.finish();
//            }
//            return true;
//        }
//
//        public void onDestroyActionMode(ActionMode actionMode) {
//            MuPDFActivity.this.mDocView.setMode(MuPDFReaderView.Mode.Viewing);
//        }
//    }


    /**
     * Called when the activity is first created.
     */
    @SuppressLint({"StringFormatMatches", "NewApi"})
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.actionbar_background_dark));
            getWindow().setNavigationBarColor(getResources().getColor(R.color.actionbar_background_dark));
        }

        LibraryUtils.reloadLocale(getApplicationContext());

        mAlertBuilder = new AlertDialog.Builder(this);

        if (core == null) {
            core = (MuPDFCore) getLastNonConfigurationInstance();

            if (savedInstanceState != null && savedInstanceState.containsKey("FileName")) {
                mFileName = savedInstanceState.getString("FileName");
            }
        }
        if (core == null) {
            Intent intent = getIntent();
            byte buffer[] = null;

            if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                Uri uri = intent.getData();
                System.out.println("URI to open is: " + uri);

                if (uri==null) {

                   /* SharedPreferences prefs = getSharedPreferences("Book", Context.MODE_PRIVATE);
                    String stringArray = prefs.getString("data", null);

                    if (stringArray != null) {
                        String[] split = stringArray.substring(1, stringArray.length()-1).split(", ");
                        buffer = new byte[split.length];
                        for (int i = 0; i < split.length; i++) {
                            buffer[i] = Byte.parseByte(split[i]);
                        }
                    }
                    //core = openBuffer(buffer);
                    if(buffer != null) {
                        core = openBuffer(buffer, intent.getType());
                    }*/

                } else {
                    //core = openFile(Uri.decode(uri.getEncodedPath()));
                    String path = Uri.decode(uri.getEncodedPath());
                    if (path == null) {
                        path = uri.toString();
                    }
                    core = openFile(path);
                }
                SearchTaskResult.set(null);
//                if (core.countPages() == 0) // comment đi vì countpage=0 thì nó là null, mà null thì ko thể countpage
//                    core = null;
            }
            if (core != null && core.needsPassword()) {
                // requestPassword(savedInstanceState);
                // required password getteris from data
                String password = intent.getStringExtra("password");
                core.authenticatePassword(password);
                // return;
            }
            if (core != null && core.countPages() == 0) {
                core = null;
            }

        }
        if (core == null) {
            AlertDialog alert = mAlertBuilder.create();
            alert.setTitle(R.string.cannot_open_document);
            alert.setButton(AlertDialog.BUTTON_POSITIVE,
                    getString(R.string.dismiss),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
            alert.show();
            return;
        }

        mOrientation = getResources().getConfiguration().orientation;

        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            core.setDisplayPages(2);
        } else {
            core.setDisplayPages(1);
        }

        createUI(savedInstanceState);
    }

	/*
     * public void requestPassword(final Bundle savedInstanceState) {
	 * mPasswordView = new EditText(this);
	 * mPasswordView.setInputType(EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
	 * mPasswordView.setTransformationMethod(new
	 * PasswordTransformationMethod());
	 *
	 * AlertDialog alert = mAlertBuilder.create();
	 * alert.setTitle(R.string.enter_password); alert.setView(mPasswordView);
	 * alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), new
	 * DialogInterface.OnClickListener() { public void onClick(DialogInterface
	 * dialog, int which) { if
	 * (core.authenticatePassword(mPasswordView.getText().toString())) {
	 * createUI(savedInstanceState); } else {
	 * requestPassword(savedInstanceState); } } });
	 * alert.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel),
	 * new DialogInterface.OnClickListener() {
	 *
	 * public void onClick(DialogInterface dialog, int which) { finish(); } });
	 * alert.show(); }
	 */

    public void createUI(Bundle savedInstanceState) {
        if (core == null)
            return;

        // Now create the UI.
        // First create the document view
        mDocView = new MuPDFReaderView(this) {
            @Override
            protected void onMoveToChild(int i) {
                updatePageNumView(i);
                //if (core == null)
                //	return;
                //mPageNumberView.setText(String.format("%d / %d", i + 1,
                //		core.countPages()));
                ///mPageSlider.setMax((core.countPages() - 1) * mPageSliderRes);
                ///mPageSlider.setProgress(i * mPageSliderRes);
                super.onMoveToChild(i);
                setCurrentlyViewedPreview();


            }

            @Override
            protected void onTapMainDocArea() {
                if (!mButtonsVisible) {
                    showButtons();
                } else {
                    if (mTopBarMode == TopBarMode.Main)
                        hideButtons();
                }
            }

            @Override
            protected void onDocMotion() {
                hideButtons();
            }

            @Override
            protected void onHit(Hit item) {

                switch (mTopBarMode) {
                    case Main:
                        if (item == Hit.Annotation) {
                            showEditLayouts(true);
                            layoutCancelOrFinish.setVisibility(GONE);
                            layoutDelete.setVisibility(VISIBLE);
//                            showButtons();
                            mTopBarMode = TopBarMode.Delete;
                            mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
                            return;
                        }
                        return;
                    case Delete:
                        mTopBarMode = TopBarMode.Main;
                        mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
                        break;
                        // fall through
                    default:
                        break;
                }
                // Not in annotation editing mode, but the pageview will
                // still select and highlight hit annotations, so
                // deselect just in case.
                MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
                if (pageView != null)
                    pageView.deselectAnnotation();
                //break;

            }
        };
        mDocView.setTextSelectedListener(new MuPDFReaderView.OnTextSelectedListener() {
            public void onTextSelected(float f, float f2, float f3, float f4) {
                MuPDFActivity.this.selectionCoordinates = new TextSelectionData(f, f2, f3, f4);
            }
        });

        mDocView.setAdapter(new MuPDFPageAdapter(this, this, core));

        Intent intent = getIntent();
        boolean idleenabled = intent.getBooleanExtra("idleenabled", false);
        boolean highlight = intent.getBooleanExtra("linkhighlight", false);
        boolean horizontalscrolling = intent.getBooleanExtra("horizontalscrolling", true);
        mDocView.setKeepScreenOn(!idleenabled);
        mDocView.setLinksHighlighted(highlight);
        mDocView.setScrollingDirectionHorizontal(horizontalscrolling);
        mDocName = intent.getStringExtra("docname");

        mSearchTask = new SearchTask(this, core) {
            @Override
            protected void onTextFound(SearchTaskResult result) {
                SearchTaskResult.set(result);
                // Ask the ReaderView to move to the resulting page
                mDocView.setDisplayedViewIndex(result.pageNumber);
                // Make the ReaderView act on the change to SearchTaskResult
                // via overridden onChildSetup method.
                mDocView.resetupChildren();
            }
        };

        // Make the buttons overlay, and store all its
        // controls in variables
        makeButtonsView();

        // Set up the page slider
        ///int smax = Math.max(core.countPages() - 1, 1);
        ///mPageSliderRes = ((10 + smax - 1) / smax) * 2;

        // Set the file-name text
        // /////////mFilenameView.setText(mFileName);

        // Activate the seekbar
        /*
		mPageSlider
				.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					public void onStopTrackingTouch(SeekBar seekBar) {
						mDocView.setDisplayedViewIndex((seekBar.getProgress() + mPageSliderRes / 2)
								/ mPageSliderRes);
					}

					public void onStartTrackingTouch(SeekBar seekBar) {
					}

					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						updatePageNumView((progress + mPageSliderRes / 2)
								/ mPageSliderRes);
					}
				});
		*/
        // Activate the search-preparing button
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                searchModeOn();
            }
        });

        drawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDocView.setMode(MuPDFReaderView.Mode.Drawing);
                acceptMode = AcceptMode.Ink;
                showEditLayouts(true);
                showToastGuide(R.string.draw);
                imgTool.setImageResource(R.drawable.ic_draw);
            }
        });
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditLayouts(false);
                toolLayout.setVisibility(View.GONE);
                showToastGuide(R.string.select_annotation);
            }
        });
        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDocView.setMode(MuPDFReaderView.Mode.Selecting);
                acceptMode = AcceptMode.CopyText;
                showEditLayouts(true);
                showToastGuide(R.string.select_text);
                imgTool.setImageResource(R.drawable.ic_copy);
            }
        });
        highlightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDocView.setMode(MuPDFReaderView.Mode.Selecting);
                acceptMode = AcceptMode.Highlight;
                showEditLayouts(true);
                showToastGuide(R.string.select_text);
                imgTool.setImageResource(R.drawable.ic_mark);
            }
        });

        underlineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDocView.setMode(MuPDFReaderView.Mode.Selecting);
                acceptMode = AcceptMode.Underline;
                showEditLayouts(true);
                showToastGuide(R.string.select_text);
                imgTool.setImageResource(R.drawable.ic_underline);
            }
        });
        strikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDocView.setMode(MuPDFReaderView.Mode.Selecting);
                acceptMode = AcceptMode.StrikeOut;
                showEditLayouts(true);
                showToastGuide(R.string.select_text);
                imgTool.setImageResource(R.drawable.ic_strike_out);
            }
        });
        // Activate the reflow button
		/*
		mReflowButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				toggleReflow();
			}
		});
		*/
		/*
		if (core.fileFormat().startsWith("PDF")) {
			mAnnotButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					mTopBarMode = TopBarMode.Annot;
					mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
				}
			});
		} else {
			mAnnotButton.setVisibility(View.GONE);
		}
		*/
        // Search invoking buttons are disabled while there is no text specified
        mSearchBack.setEnabled(false);
        mSearchFwd.setEnabled(false);
        mSearchBack.setColorFilter(Color.argb(255, 128, 128, 128));
        mSearchFwd.setColorFilter(Color.argb(255, 128, 128, 128));

        // React to interaction with the text widget
        mSearchText.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                boolean haveText = s.toString().length() > 0;
                setButtonEnabled(mSearchBack, haveText);
                setButtonEnabled(mSearchFwd, haveText);

                // Remove any previous search results
                if (SearchTaskResult.get() != null
                        && !mSearchText.getText().toString().equals(SearchTaskResult.get().txt)) {
                    SearchTaskResult.set(null);
                    mDocView.resetupChildren();
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }
        });

        // React to Done button on keyboard
        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT)
                    search(1);
                return false;
            }
        });

        mSearchText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && keyCode == KeyEvent.KEYCODE_ENTER)
                    search(1);
                return false;
            }
        });

        // Activate search invoking buttons
        mSearchBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                search(-1);
            }
        });
        mSearchFwd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                search(1);
            }
        });
		/*
		mLinkButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				setLinkHighlight(!mLinkHighlight);
			}
		});
		 */
		/*
		if (core.hasOutline()) {
			mOutlineButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					OutlineItem outline[] = core.getOutline();
					if (outline != null) {
						OutlineActivityData.get().items = outline;
						Intent intent = new Intent(MuPDFActivity.this,OutlineActivity.class);
						startActivityForResult(intent, OUTLINE_REQUEST);
					}
				}
			});
		} else {
			mOutlineButton.setVisibility(View.GONE);
		}
		*/
        // Reenstate last state if it was recorded
        ///SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        ///mDocView.setDisplayedViewIndex(prefs.getInt("page" + mFileName, 0));
        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        int orientation = prefs.getInt("orientation", mOrientation);
        int pageNum = prefs.getInt("page" + mFileName, 0);
        /*if (orientation == mOrientation)
            mDocView.setDisplayedViewIndex(pageNum);
        else {
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
               /mDocView.setDisplayedViewIndex((pageNum + 1) / 2);
            } else {
                mDocView.setDisplayedViewIndex((pageNum == 0) ? 0 : pageNum * 2 - 1);
            }
        }*/

        //if (savedInstanceState == null
        //		|| !savedInstanceState.getBoolean("ButtonsHidden", false))
        //	showButtons();

        if (savedInstanceState != null && savedInstanceState.getBoolean("SearchMode", false))
            searchModeOn();

        //if (savedInstanceState != null && savedInstanceState.getBoolean("ReflowMode", false))
        //reflowModeSet(true);

        // Give preview thumbnails time to appear before showing bottom bar
        if (savedInstanceState == null
                || !savedInstanceState.getBoolean("ButtonsHidden", false)) {
            mPreview.postDelayed(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showButtons();
                        }
                    });
                }
            }, 250);
        }

        // Stick the document view and the buttons overlay into a parent view
        RelativeLayout layout = new RelativeLayout(this);
        layout.addView(mDocView);
        layout.addView(mButtonsView);
        //layout.setBackgroundResource(R.drawable.tiled_background);
        //layout.setBackgroundResource(R.color.canvas);
        setContentView(layout);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            //case OUTLINE_REQUEST:
            //	if (resultCode >= 0)
            //		mDocView.setDisplayedViewIndex(resultCode);
            //	break;
            case PRINT_REQUEST:
                if (resultCode == RESULT_CANCELED)
                    showInfo(getString(R.string.print_failed));
                break;
            case FILEPICK_REQUEST:
                if (mFilePicker != null && resultCode == RESULT_OK)
                    mFilePicker.onPick(data.getData());
                break;
            case PAGE_CHOICE_REQUEST:
                if (resultCode >= 0) {
                    int page = resultCode;
                    if (core.getDisplayPages() == 2) {
                        page = (page + 1) / 2;
                    }
                    mDocView.setDisplayedViewIndex(page);
                    setCurrentlyViewedPreview();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public Object onRetainNonConfigurationInstance() {
        MuPDFCore mycore = core;
        core = null;
        return mycore;
    }

    /*
        private void reflowModeSet(boolean reflow) {
            mReflow = reflow;
            mDocView.setAdapter(mReflow ? new MuPDFReflowAdapter(this, core) : new MuPDFPageAdapter(this, this, core));
            mReflowButton.setColorFilter(mReflow ? Color.argb(0xFF, 172, 114, 37) : Color.argb(0xFF, 255, 255, 255));
            setButtonEnabled(mAnnotButton, !reflow);
            setButtonEnabled(mSearchButton, !reflow);
            if (reflow)
                setLinkHighlight(false);
            setButtonEnabled(mLinkButton, !reflow);
            setButtonEnabled(mMoreButton, !reflow);
            mDocView.refresh(mReflow);
        }
    */
/*
	private void toggleReflow() {
		reflowModeSet(!mReflow);
		showInfo(mReflow ? getString(R.string.entering_reflow_mode)
				: getString(R.string.leaving_reflow_mode));
	}
*/
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mFileName != null && mDocView != null) {
            outState.putString("FileName", mFileName);

            // Store current page in the prefs against the file name,
            // so that we can pick it up each time the file is loaded
            // Other info is needed only for screen-orientation change,
            // so it can go in the bundle
            SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putInt("page" + mFileName, mDocView.getDisplayedViewIndex());
            edit.putInt("orientation", mOrientation);
            edit.commit();
        }

        if (!mButtonsVisible)
            outState.putBoolean("ButtonsHidden", true);

        if (mTopBarMode == TopBarMode.Search)
            outState.putBoolean("SearchMode", true);

        //if (mReflow)
        //	outState.putBoolean("ReflowMode", true);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mSearchTask != null)
            mSearchTask.stop();

        if (mFileName != null && mDocView != null) {
            SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putInt("page" + mFileName, mDocView.getDisplayedViewIndex());
            edit.putInt("orientation", mOrientation);
            edit.commit();
        }
    }

    public void onDestroy() {
        if (mDocView != null) {
            mDocView.applyToChildren(new ReaderView.ViewMapper() {
                void applyToView(View view) {
                    ((MuPDFView) view).releaseBitmaps();
                }
            });
        }
        if (core != null)
            core.onDestroy();
        if (mAlertTask != null) {
            mAlertTask.cancel(true);
            mAlertTask = null;
        }
        core = null;
        super.onDestroy();
    }

    private void setButtonEnabled(ImageButton button, boolean enabled) {
        button.setEnabled(enabled);
        button.setColorFilter(enabled ? Color.argb(0, 255, 255, 255) : Color
                .argb(255, 128, 128, 128));
    }

    /*
    private void setLinkHighlight(boolean highlight) {
        mLinkHighlight = highlight;
        // LINK_COLOR tint
        mLinkButton.setColorFilter(highlight ? Color.argb(0xFF, 172, 114, 37)
                : Color.argb(0xFF, 255, 255, 255));
        // Inform pages of the change.
        mDocView.setLinksEnabled(highlight);
    }
    */
    private void showButtons() {
        if (core == null)
            return;
        if (!mButtonsVisible) {
            mButtonsVisible = true;
            // Update page number text and slider
            int index = mDocView.getDisplayedViewIndex();
            updatePageNumView(index);
            ///mPageSlider.setMax((core.countPages() - 1) * mPageSliderRes);
            ///mPageSlider.setProgress(index * mPageSliderRes);
            if (mTopBarMode == TopBarMode.Search) {
                mSearchText.requestFocus();
                showKeyboard();
            }

            Animation anim = new TranslateAnimation(0, 0,
                    -mTopBarSwitcher.getHeight(), 0);
            anim.setDuration(200);
            anim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    mTopBarSwitcher.setVisibility(View.VISIBLE);
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                }
            });
            mTopBarSwitcher.startAnimation(anim);

            // Update listView position
            setCurrentlyViewedPreview();
            ///anim = new TranslateAnimation(0, 0, mPageSlider.getHeight(), 0);
            anim = new TranslateAnimation(0, 0, mPreviewBarHolder.getHeight(), 0);
            anim.setDuration(200);
            anim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    ///mPageSlider.setVisibility(View.VISIBLE);
                    mPreviewBarHolder.setVisibility(View.VISIBLE);
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    mPageNumberView.setVisibility(View.VISIBLE);
                }
            });
            ///mPageSlider.startAnimation(anim);
            mPreviewBarHolder.startAnimation(anim);
        }
    }

    private void hideButtons() {
        if (mButtonsVisible) {
            toolLayout.setVisibility(View.GONE);

            mButtonsVisible = false;
            hideKeyboard();

            Animation anim = new TranslateAnimation(0, 0, 0,
                    -mTopBarSwitcher.getHeight());
            anim.setDuration(200);
            anim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    mTopBarSwitcher.setVisibility(View.INVISIBLE);
                }
            });
            mTopBarSwitcher.startAnimation(anim);

            ///anim = new TranslateAnimation(0, 0, 0, mPageSlider.getHeight());
            anim = new TranslateAnimation(0, 0, 0, mPreviewBarHolder.getHeight());
            anim.setDuration(200);
            anim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    mPageNumberView.setVisibility(View.INVISIBLE);
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    ///mPageSlider.setVisibility(View.INVISIBLE);
                    mPreviewBarHolder.setVisibility(View.INVISIBLE);
                }
            });
            ///mPageSlider.startAnimation(anim);
            mPreviewBarHolder.startAnimation(anim);
        }
    }

    private void searchModeOn() {
        if (mTopBarMode != TopBarMode.Search) {
            mTopBarMode = TopBarMode.Search;
            // Focus on EditTextWidget
            mSearchText.requestFocus();
            showKeyboard();
            mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
        }
    }

    private void searchModeOff() {
        if (mTopBarMode == TopBarMode.Search) {
            mTopBarMode = TopBarMode.Main;
            hideKeyboard();
            mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
            SearchTaskResult.set(null);
            // Make the ReaderView act on the change to mSearchTaskResult
            // via overridden onChildSetup method.
            mDocView.resetupChildren();
        }
    }

    @SuppressLint("DefaultLocale")
    private void updatePageNumView(int index) {
        if (core == null)
            return;
        String pageStr = "";
        if (core.getDisplayPages() == 2 && index != 0 && index != core.countPages() - 1) {
            pageStr = String.format("%1$d-%2$d", (index * 2), (index * 2) + 1);
            mPageNumberView.setText(String.format(getString(R.string.two_pages_of_count), (index * 2), (index * 2) + 1, core.countSinglePages()));
        } else if (core.getDisplayPages() == 2 && index == 0) {
            pageStr = String.format("%1$d", index + 1);
            mPageNumberView.setText(String.format(getString(R.string.one_page_of_count), index + 1, core.countSinglePages()));
        } else if (core.getDisplayPages() == 2 && index == core.countPages() - 1) {
            pageStr = String.format("%1$d", (index * 2));
            mPageNumberView.setText(String.format(getString(R.string.one_page_of_count), (index * 2), core.countSinglePages()));
        } else {
            pageStr = String.format("%1$d", index + 1);
            mPageNumberView.setText(String.format(getString(R.string.one_page_of_count), index + 1, core.countPages()));
        }

        MuPDFCallbackClass.sendGaiView(String.format("documentView (%1$s), page (%2$s)", mDocName, pageStr));
    }

    private void printDoc() {
        if (!core.fileFormat().startsWith("PDF")) {
            showInfo(getString(R.string.format_currently_not_supported));
            return;
        }

        Intent myIntent = getIntent();
        Uri docUri = myIntent != null ? myIntent.getData() : null;

        if (docUri == null) {
            showInfo(getString(R.string.print_failed));
        }

        if (docUri.getScheme() == null)
            docUri = Uri.parse("file://" + docUri.toString());

        Intent printIntent = new Intent(this, PrintDialogActivity.class);
        printIntent.setDataAndType(docUri, "aplication/pdf");
        printIntent.putExtra("title", mFileName);
        startActivityForResult(printIntent, PRINT_REQUEST);
    }

    private void showInfo(String message) {
        mInfoView.setText(message);

        int currentApiVersion = Build.VERSION.SDK_INT;
        if (currentApiVersion >= Build.VERSION_CODES.HONEYCOMB) {
            @SuppressWarnings("unused")
            SafeAnimatorInflater safe = new SafeAnimatorInflater(this, R.anim.info, mInfoView);
        } else {
            mInfoView.setVisibility(View.VISIBLE);
            mHandler.postDelayed(new Runnable() {
                public void run() {
                    mInfoView.setVisibility(View.INVISIBLE);
                }
            }, 500);
        }
    }

    private void makeButtonsView() {
        mButtonsView = getLayoutInflater().inflate(R.layout.buttons, null);

        // ///////mFilenameView =
        // (TextView)mButtonsView.findViewById(R.id.docNameText);
        ///mPageSlider = (SeekBar) mButtonsView.findViewById(R.id.pageSlider);
        mPreviewBarHolder = mButtonsView.findViewById(R.id.PreviewBarHolder);
        mPreview = new TwoWayView(this);
        mPreview.setOrientation(TwoWayView.Orientation.HORIZONTAL);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-1, -1);
        mPreview.setLayoutParams(lp);
        pdfPreviewPagerAdapter = new ToolbarPreviewAdapter(this, core);
        mPreview.setAdapter(pdfPreviewPagerAdapter);
        mPreview.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> pArg0, View pArg1,
                                    int position, long id) {
                hideButtons();
                mDocView.setDisplayedViewIndex((int) id);
            }
        });
        mPreviewBarHolder.addView(mPreview);

        mPageNumberView = mButtonsView.findViewById(R.id.pageNumber);
        mInfoView = mButtonsView.findViewById(R.id.info);
        mSearchButton = mButtonsView.findViewById(R.id.searchButton);
        closeReader = mButtonsView.findViewById(R.id.closeReader);

        drawButton = mButtonsView.findViewById(R.id.drawButton);
        deleteButton = mButtonsView.findViewById(R.id.deleteButton);
        highlightButton = mButtonsView.findViewById(R.id.highlightButton);
        copyButton = mButtonsView.findViewById(R.id.copyButton);
        strikeButton = mButtonsView.findViewById(R.id.strikeButton);
        underlineButton = mButtonsView.findViewById(R.id.underlineButton);
        //mReflowButton = (ImageButton) mButtonsView.findViewById(R.id.reflowButton);
        //mOutlineButton = (ImageButton) mButtonsView.findViewById(R.id.outlineButton);
        //mAnnotButton = (ImageButton)mButtonsView.findViewById(R.id.editAnnotButton);
        //mAnnotTypeText = (TextView)mButtonsView.findViewById(R.id.annotType);
        mTopBarSwitcher = mButtonsView.findViewById(R.id.switcher);
        mSearchBack = mButtonsView.findViewById(R.id.searchBack);
        mSearchFwd = mButtonsView.findViewById(R.id.searchForward);
        mSearchText = mButtonsView.findViewById(R.id.searchText);
        imgTool = mButtonsView.findViewById(R.id.imgTool);
        //mLinkButton = (ImageButton) mButtonsView.findViewById(R.id.linkButton);
        //mMoreButton = (ImageButton) mButtonsView.findViewById(R.id.moreButton);
        cancelEdit = mButtonsView.findViewById(R.id.cancelEdit);
        cancelEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelEdit();
            }
        });
        finishEdit = mButtonsView.findViewById(R.id.finishEdit);
        finishEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishEdit();
            }
        });
        layoutDelete = mButtonsView.findViewById(R.id.layoutDelete);
        cancelDelete = mButtonsView.findViewById(R.id.cancelDelete);
        cancelDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MuPDFView muPDFView = (MuPDFView) mDocView.getDisplayedView();
                if (muPDFView != null) {
                    muPDFView.deselectAnnotation();
                }
                layoutDelete.setVisibility(View.GONE);
                mTopBarMode = TopBarMode.Main;
                mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());

            }
        });
        txtDelete = mButtonsView.findViewById(R.id.txtDelete);
        txtDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MuPDFView muPDFView = (MuPDFView) mDocView.getDisplayedView();
                if (muPDFView != null) {
                    muPDFView.deleteSelectedAnnotation();
                }
                layoutDelete.setVisibility(View.GONE);
                mTopBarMode = TopBarMode.Main;
                mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
                mDocView.refresh(false);

            }
        });
        mTopBarSwitcher.setVisibility(View.INVISIBLE);
        mPageNumberView.setVisibility(View.INVISIBLE);
        mInfoView.setVisibility(View.INVISIBLE);
        ///mPageSlider.setVisibility(View.INVISIBLE);
        mPreviewBarHolder.setVisibility(View.INVISIBLE);
        layoutCancelOrFinish = mButtonsView.findViewById(R.id.layoutCancelOrFinish);
        toolLayout = mButtonsView.findViewById(R.id.toolLayout);
        txtEdit = mButtonsView.findViewById(R.id.txtEdit);
        txtEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toolLayout.setVisibility(View.VISIBLE);
            }
        });
        Intent intent = getIntent();
        if (!intent.getBooleanExtra("edit", true)) {
            txtEdit.setVisibility(View.GONE);
        }
        closeReader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    private void showEditLayouts(boolean isShow) {
        if (isShow) {
            hideButtons();
            layoutCancelOrFinish.setVisibility(View.VISIBLE);
        } else {
            showButtons();
            layoutCancelOrFinish.setVisibility(View.GONE);
        }
    }

    public void OnMoreButtonClick(View v) {


        if (core != null) {
            int i = mDocView.getDisplayedViewIndex();
            if (core.getDisplayPages() == 2) {
                i = (i * 2) - 1;
            }
            PDFPreviewGridActivityData.get().core = core;
            PDFPreviewGridActivityData.get().position = i;
            //PDFPreviewGridActivity prevAct = new PDFPreviewGridActivity();
            //Intent intent = prevAct.getIntent();
            Intent intent = new Intent(MuPDFActivity.this, PDFPreviewGridActivity.class);
            startActivityForResult(intent, PAGE_CHOICE_REQUEST);

            MuPDFCallbackClass.sendGaiView(String.format("documentThumbView (%1$s)", mDocName));

        }

        /////////////mTopBarMode = TopBarMode.More;
        /////////////mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
    }

    public void OnCancelMoreButtonClick(View v) {
        mTopBarMode = TopBarMode.Main;
        mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
    }

    public void OnPrintButtonClick(View v) {
        printDoc();
    }

    public void OnCloseReaderButtonClick(View v) {
        finish();
    }
/*
	public void OnCancelAcceptButtonClick(View v) {
		MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
		if (pageView != null) {
			pageView.deselectText();
			pageView.cancelDraw();
		}
		mDocView.setMode(MuPDFReaderView.Mode.Viewing);
		
		//switch (mAcceptMode) {
		//case CopyText:
		//	mTopBarMode = TopBarMode.More;
		//	break;
		//default:
			//mTopBarMode = TopBarMode.Annot;
		//	break;
		//}
		
		mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
	}
	*/

    public void OnCancelSearchButtonClick(View v) {
        searchModeOff();
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null)
            imm.showSoftInput(mSearchText, 0);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null)
            imm.hideSoftInputFromWindow(mSearchText.getWindowToken(), 0);
    }

    private void search(int direction) {
        hideKeyboard();
        int displayPage = mDocView.getDisplayedViewIndex();
        SearchTaskResult r = SearchTaskResult.get();
        int searchPage = r != null ? r.pageNumber : -1;
        mSearchTask.go(mSearchText.getText().toString(), direction, displayPage, searchPage);
    }

    @Override
    public boolean onSearchRequested() {
        if (mButtonsVisible && mTopBarMode == TopBarMode.Search) {
            hideButtons();
        } else {
            showButtons();
            searchModeOn();
        }
        return super.onSearchRequested();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mButtonsVisible && mTopBarMode != TopBarMode.Search) {
            hideButtons();
        } else {
            showButtons();
            searchModeOff();
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        if (core != null) {
            core.startAlerts();
            createAlertWaiter();
        }

        super.onStart();
    }

    @Override
    protected void onStop() {
        if (core != null) {
            destroyAlertWaiter();
            core.stopAlerts();
        }

        super.onStop();
    }

    @Override
    public void onBackPressed() {
        //if (core.hasChanges()) {
//		if (core != null && core.hasChanges()) {
        onBackAction();
    }

    public void onBackAction() {
        if (core != null && core.hasChanges()) {
            final MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (which == AlertDialog.BUTTON_POSITIVE) {
                        Runnable r = new Runnable() {
                            public void run() {
                                finish();
                            }
                        };
                        core.save2(getContext(),r);

                    } else {
                        finish();
                    }
                }
            };
            AlertDialog alert = mAlertBuilder.create();
            alert.setMessage(getString(R.string.document_has_changes_save_them_));
            alert.setButton(AlertDialog.BUTTON_POSITIVE,
                    getString(R.string.yes), listener);
            alert.setButton(AlertDialog.BUTTON_NEGATIVE,
                    getString(R.string.no), listener);
            alert.show();
        } else {
            super.onBackPressed();
        }
    }

    public void goBack(){
        finish();
    }

    public void finishEdit() {
        MuPDFView muPDFView = (MuPDFView) this.mDocView.getDisplayedView();
        switch (acceptMode) {
            case CopyText:
                if (muPDFView != null) {
                    muPDFView.copySelection();
                    Toast.makeText(getContext(), "Copied", Toast.LENGTH_LONG).show();
                }
                break;
            case Highlight:
                if (muPDFView != null) {
                    muPDFView.markupSelection(Annotation.Type.HIGHLIGHT);
                }

                break;
            case Underline:
                if (muPDFView != null) {
                    muPDFView.markupSelection(Annotation.Type.UNDERLINE);
                }
                break;
            case StrikeOut:
                if (muPDFView != null) {
                    muPDFView.markupSelection(Annotation.Type.STRIKEOUT);
                }
                break;
            case Ink:
                if (muPDFView != null) {
                    muPDFView.saveDraw();
                }
                break;
            default:
                break;

        }

        this.mDocView.setMode(MuPDFReaderView.Mode.Viewing);
        this.mDocView.refresh(false); // có lệnh này (hoặc ng dùng phải zoom) mới xem được kết quả
        showEditLayouts(false);

        toolLayout.setVisibility(View.GONE);

    }

    public void showToastGuide(int s) {
        Toast.makeText(MuPDFActivity.this, s, Toast.LENGTH_LONG).show();
    }

    public void cancelEdit() {
        showEditLayouts(false);

        MuPDFView muPDFView = (MuPDFView) this.mDocView.getDisplayedView();
        if (muPDFView != null) {
            muPDFView.deselectText();
            muPDFView.cancelDraw();
        }
        this.mDocView.setMode(MuPDFReaderView.Mode.Viewing);

    }

    @Override
    public void performPickFor(FilePicker picker) {
        mFilePicker = picker;
        //Intent intent = new Intent(this, ChoosePDFActivity.class);
        //startActivityForResult(intent, FILEPICK_REQUEST);
    }
}
