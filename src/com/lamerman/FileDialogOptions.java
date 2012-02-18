package com.lamerman;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

public class FileDialogOptions {
    // Legacy
    private static final int SELECTION_MODE_CREATE = 0;
    private static final int SELECTION_MODE_OPEN = 1;

    
    // This is used to configure the start folder when it opens and the folder of the result file.
    public String currentPath = FileDialog.ROOT;
    // Used to retrieve the absolute filename of the result file.
    public String selectedFile = null;
    
    // Set to enable the "New" file button.
    public boolean allowCreate = true;
    // Set to show current folder in activity titlebar and hide the "myPath" TextView.
    public boolean titlebarForCurrentPath;
    // Option for one-click select
    public boolean oneClickSelect;
    
    // Option for file icon.
    public int iconFile = R.drawable.document;
    // Option for folder icon.
    public int iconFolder = R.drawable.folder_horizontal;
    // Option for up/root icon.
    public int iconUp = R.drawable.shortcut_overlay;
    // Option for SDCard icon
    public int iconSDCard = R.drawable.floppy;



    // This is used to configure the initial folder when it opens.
    private static final String START_PATH = "START_PATH";
    // Deprecated: Set to SelectionMode.MODE_OPEN to disable the "New" button.
    private static final String SELECTION_MODE = "SELECTION_MODE";

    private static final String OPTION_ALLOW_CREATE = "OPTION_ALLOW_CREATE";
    // Set to hide the "myPath" TextView.
    public static final String OPTION_CURRENT_PATH_IN_TITLEBAR = "OPTION_CURRENT_PATH_IN_TITLEBAR";
    // Option for one-click select
    public static final String OPTION_ONE_CLICK_SELECT = "OPTION_ONE_CLICK_SELECT";
    // Option for file icon.
    public static final String OPTION_ICON_FILE = "OPTION_ICON_FILE";
    // Option for folder icon.
    public static final String OPTION_ICON_FOLDER = "OPTION_ICON_FOLDER";
    // Option for up icon.
    public static final String OPTION_ICON_UP = "OPTION_ICON_UP";

    
//  // Used to retrieve the absolute filename of the result file.
//  public static final String RESULT_PATH = "RESULT_PATH";
    private static final String RESULT_FILE = "RESULT_FILE";
    // Used to retrieve the full folder of the result file.
    private static final String RESULT_FOLDER = "RESULT_FOLDER";
    
    
    
    /**
     * Default constructor used by activities which need the FileDialog.
     */
    public FileDialogOptions() {
    }
    
    /**
     * Constructor (used by FileDialog) which automatically reads all the intent option values.
     * 
     * @param intent The intent passed to FileDialog.
     */
    public FileDialogOptions(Intent intent) {
        // Configure the initial folder when it opens.
        if (intent.hasExtra(START_PATH)) {
            this.currentPath = intent.getStringExtra(START_PATH);
        }
        
        // Allow creation of new files
        // Check the old intent for compatibility
        if (intent.hasExtra(SELECTION_MODE)) {
            Log.w("FileDialogOptions", "SELECTION_MODE intent value is deprecated. Use FileDialogOptions.allowCreate");
            this.allowCreate = (intent.getIntExtra(SELECTION_MODE, SELECTION_MODE_CREATE) == SELECTION_MODE_OPEN);
        }
        else {
            this.allowCreate = intent.getBooleanExtra(OPTION_ALLOW_CREATE, this.allowCreate);
        }
        
        // Hide the titlebar if needed
        this.titlebarForCurrentPath = intent.getBooleanExtra(OPTION_CURRENT_PATH_IN_TITLEBAR, this.titlebarForCurrentPath);
        
        // One click select
        this.oneClickSelect = intent.getBooleanExtra(OPTION_ONE_CLICK_SELECT, this.oneClickSelect);
        
        // Icons
        this.iconFile = intent.getIntExtra(OPTION_ICON_FILE, this.iconFile);
        this.iconFolder = intent.getIntExtra(OPTION_ICON_FOLDER, this.iconFolder);
        this.iconUp = intent.getIntExtra(OPTION_ICON_UP, this.iconUp);
    }

    /**
     * Once the options are all configured, return an intent with everything set.
     * 
     * @param activity The activity wishing to call FileDialog.
     * @return Intent An intent which is ready to be used with startActivityForResult()
     */
    public Intent createFileDialogIntent(Activity activity) {
        Intent intent = new Intent(activity.getBaseContext(), FileDialog.class);
        
        intent.putExtra(START_PATH, this.currentPath);
        intent.putExtra(OPTION_ALLOW_CREATE, this.allowCreate);
        intent.putExtra(OPTION_CURRENT_PATH_IN_TITLEBAR, this.titlebarForCurrentPath);
        intent.putExtra(OPTION_ONE_CLICK_SELECT, this.oneClickSelect);
        intent.putExtra(OPTION_ICON_FILE, this.iconFile);
        intent.putExtra(OPTION_ICON_FOLDER, this.iconFolder);
        intent.putExtra(OPTION_ICON_UP, this.iconUp);
        
        return intent;
    }
    
    
    public Intent createResultIntent() {
        Intent intent = new Intent();
        
        intent.putExtra(RESULT_FILE, this.selectedFile);
        intent.putExtra(RESULT_FOLDER, this.currentPath);
        
        return intent;
    }
    
    
    /**
     * Returns the selected filename from the intent.
     */
    public static String readResultFile(Intent intent) {
        return intent.getStringExtra(RESULT_FILE);
    }
    
    /**
     * Returns the selected folder from the intent.
     */
    public static String readResultFolder(Intent intent) {
        return intent.getStringExtra(RESULT_FOLDER);
    }
}
