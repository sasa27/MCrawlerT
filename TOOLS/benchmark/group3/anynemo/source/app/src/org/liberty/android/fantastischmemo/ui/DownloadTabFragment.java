/*
Copyright (C) 2012 Haowen Ning

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

*/
package org.liberty.android.fantastischmemo.ui;

import org.liberty.android.fantastischmemo.R;

import org.liberty.android.fantastischmemo.downloader.*;

import org.liberty.android.fantastischmemo.downloader.google.SpreadsheetListScreen;

import android.app.Activity;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.view.*;
import android.content.*;

/* 
 * This class is invoked when the user share the card from other
 * apps like ColorDict 
 */
public class DownloadTabFragment extends Fragment implements View.OnClickListener{
    private static final String TAG = "org.liberty.android.fantastischmemo.ui.DownloadTabFragment";
    private View amButton;
    private View feButton;
    private View ssButton;
    private View quizletButton;
    private View dropboxButton;
    private View googleButton;
    private Activity mActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.downloader_tab, container, false);
        amButton = v.findViewById(R.id.source_anymemo);
        feButton = v.findViewById(R.id.source_fe);
        ssButton = v.findViewById(R.id.source_ss);
        quizletButton = v.findViewById(R.id.source_quizlet);
        dropboxButton = v.findViewById(R.id.source_dropbox);
        googleButton = v.findViewById(R.id.source_google);
        amButton.setOnClickListener(this);
        feButton.setOnClickListener(this);
        ssButton.setOnClickListener(this);
        quizletButton.setOnClickListener(this);
        dropboxButton.setOnClickListener(this);
        googleButton.setOnClickListener(this);
        return v;
    }
    @Override
    public void onClick(View v){
        if (v == amButton){
            startActivity(new Intent(mActivity, DownloaderAnyMemo.class));
        }
        if (v == feButton){
            startActivity(new Intent(mActivity, FELauncher.class));
        }
        if (v == ssButton){
            startActivity(new Intent(mActivity, DownloaderSS.class));
        }
        if (v == quizletButton){
            startActivity(new Intent(mActivity, QuizletLauncher.class));
        }
        if (v == dropboxButton){
            startActivity(new Intent(mActivity, DropboxLauncher.class));
        }
        if (v == googleButton) {
            startActivity(new Intent(mActivity, SpreadsheetListScreen.class));
        }
    }

}
