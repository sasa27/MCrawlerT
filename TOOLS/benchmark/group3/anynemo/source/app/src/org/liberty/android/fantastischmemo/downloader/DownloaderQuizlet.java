/*
Copyright (C) 2010 Haowen Ning

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
package org.liberty.android.fantastischmemo.downloader;

import java.util.LinkedList;

import org.liberty.android.fantastischmemo.*;

import java.util.ArrayList;
import java.util.List;

import java.io.IOException;
import java.net.URLEncoder;

import org.liberty.android.fantastischmemo.dao.CardDao;

import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;
import org.liberty.android.fantastischmemo.domain.LearningData;
import org.liberty.android.fantastischmemo.utils.AMGUIUtility;
import org.liberty.android.fantastischmemo.utils.AMUtil;
import org.liberty.android.fantastischmemo.utils.RecentListUtil;


import android.os.Bundle;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;
import android.util.Log;
import android.view.View;
import android.os.Handler;
import android.text.Html;
import android.text.method.LinkMovementMethod;

import org.json.JSONArray;
import org.json.JSONObject;

/*
 * Download from FlashcardExchange using its web api
 */
public class DownloaderQuizlet extends DownloaderBase implements ListView.OnScrollListener{
    public static final String INTENT_ACTION_SEARCH_TAG = "am.quizlet.intent.search_tag";
    public static final String INTENT_ACTION_SEARCH_USER = "am.quizlet.intent.search_user";

    private static final String TAG = "org.liberty.android.fantastischmemo.downloader.DownloaderQuizlet";
    private static final int PAGE_SIZE = 50;
    private static final String QUIZLET_API_KEY = "7bmBY5S2VgPbNpd8";
    private static final String QUIZLET_API_TAG = "https://api.quizlet.com/2.0/search/sets?client_id=" + QUIZLET_API_KEY+ "&per_page=" + PAGE_SIZE + "&q=";
    private static final String QUIZLET_API_USER = "https://api.quizlet.com/2.0/users/%s/sets?client_id=" + QUIZLET_API_KEY+ "&per_page=" + PAGE_SIZE;
    private static final String QUIZLET_API_GET = "https://api.quizlet.com/2.0/sets/%d?client_id=" + QUIZLET_API_KEY;
    private DownloadListAdapter dlAdapter;

    private ListView listView;
    private Handler mHandler;
    private ProgressDialog mProgressDialog;
    private String action;
    private String searchCriterion = null;
    private int currentPage = 1;
    /* This will change after first retriving list */
    private int totalPages = 1;

    @Override
    protected void initialRetrieve(){
        mHandler = new Handler();
        dlAdapter = new DownloadListAdapter(this, R.layout.filebrowser_item);
        listView = (ListView)findViewById(R.id.file_list);
        listView.setAdapter(dlAdapter);
        listView.setOnScrollListener(this);

        Intent intent = getIntent();
        action = intent.getAction();
        if(action.equals(INTENT_ACTION_SEARCH_TAG)){
        } 
        else if(action.equals(INTENT_ACTION_SEARCH_USER)){
        } 
        else{
            Log.e(TAG, "Invalid intent to invoke this activity.");
            finish();
        }
        Bundle extras = intent.getExtras();
        if(extras  == null){
            Log.e(TAG, "Extras is null.");
            finish();
        }
        else{
            searchCriterion = extras.getString("search_criterion");
        }
        AMGUIUtility.doProgressTask(this, R.string.loading_please_wait, R.string.loading_connect_net, new AMGUIUtility.ProgressTask(){
            private List<DownloadItem> dil;
            public void doHeavyTask() throws Exception{
                dil = retrieveList();
            }
            public void doUITask(){
                dlAdapter.addList(dil);
            }
        });
    }

    @Override
    protected void openCategory(DownloadItem di){
        /* No category for Quizlet*/
    }

    @Override
    protected DownloadItem getDownloadItem(int position){
        return dlAdapter.getItem(position);
    }
    
    @Override
    protected void goBack(){
        finish();
    }

    @Override
    protected void fetchDatabase(final DownloadItem di){
        View alertView = View.inflate(this, R.layout.link_alert, null);
        TextView textView = (TextView)alertView.findViewById(R.id.link_alert_message);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(Html.fromHtml(getString(R.string.downloader_download_alert_message) + di.getDescription()));

        new AlertDialog.Builder(this)
            .setView(alertView)
            .setTitle(getString(R.string.downloader_download_alert) + di.getTitle())
            .setPositiveButton(getString(R.string.yes_text), new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface arg0, int arg1){
                    mProgressDialog = ProgressDialog.show(DownloaderQuizlet.this, getString(R.string.loading_please_wait), getString(R.string.loading_downloading));
                    new Thread(){
                        public void run(){
                            try{
                                downloadDatabase(di);
                                mHandler.post(new Runnable(){
                                    public void run(){
                                        mProgressDialog.dismiss();
                                        String dbpath = AMEnv.DEFAULT_ROOT_PATH;
                                        new AlertDialog.Builder(DownloaderQuizlet.this)
                                            .setTitle(R.string.downloader_download_success)
                                            .setMessage(getString(R.string.downloader_download_success_message) + dbpath + di.getTitle() + ".db")
                                            .setPositiveButton(R.string.ok_text, null)
                                            .create()
                                            .show();
                                    }
                                });

                            }
                            catch(final Exception e){
                                Log.e(TAG, "Error downloading", e);
                                mHandler.post(new Runnable(){
                                    public void run(){
                                        mProgressDialog.dismiss();
                                        new AlertDialog.Builder(DownloaderQuizlet.this)
                                            .setTitle(R.string.downloader_download_fail)
                                            .setMessage(getString(R.string.downloader_download_fail_message) + " " + e.toString())
                                            .setPositiveButton(R.string.ok_text, null)
                                            .create()
                                            .show();
                                    }
                                });
                            }
                        }
                    }.start();
                }
            })
            .setNegativeButton(getString(R.string.no_text), null)
            .show();
    }

    private List<DownloadItem> retrieveList() throws Exception{
        List<DownloadItem> diList = new ArrayList<DownloadItem>();
        String url = "";
        if(action.equals(INTENT_ACTION_SEARCH_TAG)){
            url = QUIZLET_API_TAG + URLEncoder.encode(searchCriterion);
        } else if (action.equals(INTENT_ACTION_SEARCH_USER)){
            url = String.format(QUIZLET_API_USER, URLEncoder.encode(searchCriterion));
        } else {
            throw new IOException("Incorrect criterion used for this call");
        }
        Log.i(TAG, "Url: " + url);
        url += "&page=" + currentPage;

        String jsonString = DownloaderUtils.downloadJSONString(url);
        Log.v(TAG, "JSON String: " + jsonString);

        // The array that stores the result
        JSONArray jsonArray = null;
        if (action.equals(INTENT_ACTION_SEARCH_TAG)) {
            JSONObject jsonObject = new JSONObject(jsonString);
            totalPages = jsonObject.getInt("total_pages");
            jsonArray = jsonObject.getJSONArray("sets");
        } else if(action.equals(INTENT_ACTION_SEARCH_USER)){
            jsonArray = new JSONArray(jsonString);
        }
        for(int i = 0; i < jsonArray.length(); i++){
            JSONObject jsonItem = jsonArray.getJSONObject(i);
            int cardId;
            cardId = jsonItem.getInt("id");


            String address = String.format(QUIZLET_API_GET, cardId);
            DownloadItem di = new DownloadItem(DownloadItem.ItemType.Database,
                    jsonItem.getString("title"),
                    "From Quizlet.com", 
                    address);
            diList.add(di);
        }
        return diList;
    }

    private void downloadDatabase(DownloadItem di) throws Exception{
        String address = di.getAddress();
        String dbJsonString = DownloaderUtils.downloadJSONString(address);
        Log.v(TAG, "Download url: " + address);
        JSONObject rootObject = new JSONObject(dbJsonString);
        JSONArray flashcardsArray = rootObject.getJSONArray("terms");
        List<Card> cardList = new LinkedList<Card>();
        for(int i = 0; i < flashcardsArray.length(); i++){
            JSONObject jsonItem = flashcardsArray.getJSONObject(i);
            String question = jsonItem.getString("term");
            String answer = jsonItem.getString("definition");
            Card card = new Card();
            card.setQuestion(question);
            card.setAnswer(answer);
            card.setCategory(new Category());
            card.setLearningData(new LearningData());
            cardList.add(card);
        }
        
        /* Make a valid dbname from the title */
        String dbname = DownloaderUtils.validateDBName(di.getTitle()) + ".db";
        String dbpath = AMEnv.DEFAULT_ROOT_PATH;
        String fullpath = dbpath + dbname;
        AMUtil.deleteFileWithBackup(fullpath);
        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(DownloaderQuizlet.this, fullpath);
        try {
            CardDao cardDao = helper.getCardDao();
            cardDao.createCards(cardList);
            long count = helper.getCardDao().getTotalCount(null);
            if (count <= 0L) {
                throw new RuntimeException("Downloaded empty db.");
            }
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
        }
        RecentListUtil rlu = new RecentListUtil(this);
        rlu.addToRecentList(fullpath);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount){
        if(totalItemCount <= 0 ){
            return;
        }
        if(totalItemCount >= PAGE_SIZE
                && (firstVisibleItem + visibleItemCount == totalItemCount)
                && (currentPage < totalPages)){
                try{
                    currentPage += 1;
                    List<DownloadItem> nextPageItems = retrieveList();
                    dlAdapter.addList(nextPageItems);
                }
                catch(Exception e){
                    Log.e(TAG, "Error to scroll", e);
                }
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }
}
