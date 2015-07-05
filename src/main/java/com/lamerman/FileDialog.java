package com.lamerman;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;

public class FileDialog extends Activity {
	private static final String ITEM_KEY = "key";
	private static final String ITEM_IMAGE = "image";

	public static final String PATH_ROOT = "/";
	public static final String PATH_SDCARD = Environment.getExternalStorageDirectory().getAbsolutePath();

	private FileDialogOptions options;

	// TODO: This needs a cleanup
	private AlertDialog dialog;
	private ListView listview;

	private List<String> path;

	private String parentPath;
	private String currentPath = PATH_ROOT;

	private File selectedFile;
	private final HashMap<String, Integer> lastPositions = new HashMap<String, Integer>();

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getWindow().setBackgroundDrawable(new ColorDrawable(0));
		}

		setResult(RESULT_CANCELED, getIntent());


		RelativeLayout layout = (RelativeLayout) getLayoutInflater().inflate(R.layout.file_dialog_main, null);

		listview = (ListView) layout.findViewById(android.R.id.list);
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> lv, View v, int pos, long id) {
				handleListItemClick(v, pos, id);
			}
		});


		// Read options
		options = new FileDialogOptions(getIntent());

		dialog = new AlertDialog.Builder(this)
			.setTitle("Select file")
			.setView(layout)
			.setCancelable(false)
			.setOnKeyListener(new DialogInterface.OnKeyListener() {
				@Override
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
					if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
						if (!currentPath.equals(PATH_ROOT)) {
							getDir(parentPath);
						}
						else {
							finish();
						}
					}

					return true;
				}
			})
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override public void onClick(DialogInterface dialog, int which) {
					if (options.selectFolderMode) {
						returnSelection(null);
						return;
					}

					finish();
				}
			})
			.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				@Override public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			})
			.create();



		// Try to restore current path after screen rotation
		if (savedInstanceState != null) {
		    getDir(savedInstanceState.getString("currentPath"));
		    // TODO: restore scroll position also
		}
		// New instance of FileDialog
		else {
		    File file = new File(options.currentPath);

		    if (file.isDirectory() && file.exists()) {
		        getDir(options.currentPath);
		    }
		    else {
		        getDir(PATH_ROOT);
		    }
		}


		dialog.setTitle(currentPath);



		// This sets the dialog to fill the screen all the time,
		// so navigating doesn't make the height twitch
//		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//	    lp.copyFrom(dialog.getWindow().getAttributes());
//	    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
//	    lp.height = WindowManager.LayoutParams.MATCH_PARENT;
		dialog.show();
//		dialog.getWindow().setAttributes(lp);

//		ViewGroup.MarginLayoutParams mlop = new ViewGroup.MarginLayoutParams(listview.getLayoutParams());
//		mlop.height = LayoutParams.MATCH_PARENT;
//	    listview.setLayoutParams(mlop);
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (dialog != null) {
			dialog.dismiss();
			dialog = null;
		}
	}


	private void getDir(String dirPath) {

		boolean useAutoSelection = dirPath.length() < currentPath.length();

		Integer position = lastPositions.get(parentPath);

		getDirImpl(dirPath);

		if (position != null && useAutoSelection) {
			listview.setSelection(position);
		}

	}

	private void getDirImpl(final String dirPath) {
		currentPath = dirPath;

		path = new ArrayList<String>();
		ArrayList<HashMap<String, Object>> mList = new ArrayList<HashMap<String, Object>>();

		File f = new File(currentPath);
		File[] files = f.listFiles();

		// Null if file is not a directory
		if (files == null) {
			currentPath = PATH_ROOT;
			f = new File(currentPath);
			files = f.listFiles();
		}

		// Sort files by alphabet and ignore casing
		Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File lhs, File rhs) {
                return lhs.getPath().compareToIgnoreCase(rhs.getPath());
            }
		});

	    dialog.setTitle(currentPath);

		/*
         * http://stackoverflow.com/questions/5090915/show-songs-from-sdcard
         * http://developer.android.com/reference/android/os/Environment.html
         * http://stackoverflow.com/questions/5453708/android-how-to-use-environment-getexternalstoragedirectory
         */
        if (currentPath.equals(PATH_ROOT)) {
            boolean mounted = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);

            if (mounted) {
                addItem(mList, PATH_SDCARD + "(SD Card)", this.options.iconSDCard);
                path.add(PATH_SDCARD);
            }
        }

		if (!currentPath.equals(PATH_ROOT)) {
			addItem(mList, "/ (Root folder)", this.options.iconUp);
			path.add(PATH_ROOT);

			addItem(mList, "../ (Parent folder)", this.options.iconUp);
			path.add(f.getParent());
			parentPath = f.getParent();
		}

		ArrayList<File> listDirs = new ArrayList<File>();
		ArrayList<File> listFiles = new ArrayList<File>();

		for (File file : files) {
			if (file.isDirectory()) {
				listDirs.add(file);
			}
			// Only add files if we're not in folder mode
			else if (!options.selectFolderMode) {
			    listFiles.add(file);
			}
		}

		for (File dir : listDirs) {
		    path.add(dir.getPath());
			addItem(mList, dir.getName(), this.options.iconFolder);
		}

		for (File file : listFiles) {
		    path.add(file.getPath());
			addItem(mList, file.getName(), this.options.iconFile);
		}

		SimpleAdapter fileList = new SimpleAdapter(this, mList,
            R.layout.file_dialog_row,
            new String[] { ITEM_KEY, ITEM_IMAGE },
            new int[] { R.id.fdrowtext, R.id.fdrowimage }
        );

		fileList.notifyDataSetChanged();

		listview.setAdapter(fileList);
	}

	private void addItem(ArrayList<HashMap<String, Object>> mList, String fileName, int imageId) {
		HashMap<String, Object> item = new HashMap<String, Object>();
		item.put(ITEM_KEY, fileName);
		item.put(ITEM_IMAGE, imageId);
		mList.add(item);
	}


	protected void handleListItemClick(View v, int position, long id) {
		File file = new File(path.get(position));

		if (!file.exists()) {
		    new AlertDialog.Builder(this)
                .setIcon(R.drawable.icon)
                .setTitle("Does not exist.")
                .setMessage(file.getName())
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
		    return;
		}

		if (file.isDirectory()) {
			if (file.canRead()) {
			    // Save the scroll position so users don't get confused when they come back
				lastPositions.put(currentPath, listview.getFirstVisiblePosition());
				getDir(path.get(position));
			}
			else {
				new AlertDialog.Builder(this)
					.setIcon(R.drawable.icon)
					.setTitle("[" + file.getName() + "] " + getText(R.string.cant_read_folder))
					.setPositiveButton("OK", null)
					.show();
			}
		}
		else {
			selectedFile = file;
			v.setSelected(true);

			if (selectedFile != null) {
			    returnSelection(selectedFile.getPath());
			}
		}
	}

//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
//			if (!currentPath.equals(PATH_ROOT)) {
//				getDir(parentPath);
//				return true;
//			}
//		}
//
//		return super.onKeyDown(keyCode, event);
//	}



	private void returnSelection(String filepath) {
	    this.options.currentPath = currentPath;
	    this.options.selectedFile = filepath;

	    setResult(RESULT_OK, options.createResultIntent());
	    finish();
	}

	// Remember the information when the screen is just about to be rotated.
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString("currentPath", this.currentPath);
		outState.putParcelable("listview", listview.onSaveInstanceState());
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		this.currentPath = savedInstanceState.getString("currentPath");
		listview.onRestoreInstanceState(savedInstanceState.getParcelable("listview"));
	}
}