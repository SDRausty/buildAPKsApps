/**
 * This file is part of Speech Trainer.
 * Copyright (C) 2011 Jan Wrobel <wrr@mixedbit.org>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package mixedbit.speechtrainer.view;

import mixedbit.speechtrainer.R;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

/**
 * Displays a select file stored in assets directory (e.g. about.html,
 * help.html). Intent that starts this activity needs to have two extra string
 * parameters: name of a file to display (without a directory name, all files
 * are assumed to be in android_asset dir) and a suffix of a window title.
 */
public class FileViewerActivity extends Activity {

    /**
     * An ID of an extra parameter of the intent. The parameter holds a name of
     * a file to be displayed. Needs to be set with Intent.putExtra() method by
     * a creator of the FileViewerActivity.
     */
    public static final String FILE_TO_DISPLAY = "mixedbit.speechtrainer.FileToDisplay";
    /**
     * An ID of an extra parameter of the intent. The parameter holds a suffix
     * to be put in a title of this activity window. Needs to be set with
     * Intent.putExtra() method by a creator of the FileViewerActivity.
     */
    public static final String WINDOW_TITLE_SUFFIX = "mixedbit.speechtrainer.WindowTiTleSuffix";
    private static final String ASSETS_DIR = "file:///android_asset/";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_viewer);
        final WebView fileView = (WebView) findViewById(R.id.fileWebView);
        final String fileName = getIntent().getStringExtra(FILE_TO_DISPLAY);
        final String windowTitleSuffix = getIntent().getStringExtra(WINDOW_TITLE_SUFFIX);
        setWindowTitle(windowTitleSuffix);
        fileView.loadUrl(createFileURL(fileName));
    }

    /**
     * Sets the window title to the application name followed by a given suffix.
     */
    private void setWindowTitle(String windowTitleSuffix) {
        final StringBuilder titleStringBuilder = new StringBuilder(getString(R.string.app_name));
        titleStringBuilder.append(" - ");
        titleStringBuilder.append(windowTitleSuffix);
        setTitle(titleStringBuilder.toString());
    }

    /**
     * @return An URL that points to a file with a given name in the assets
     *         directory.
     */
    private String createFileURL(String fileName) {
        final StringBuilder urlStringBuilder = new StringBuilder(ASSETS_DIR);
        urlStringBuilder.append(fileName);
        return urlStringBuilder.toString();
    }

}
