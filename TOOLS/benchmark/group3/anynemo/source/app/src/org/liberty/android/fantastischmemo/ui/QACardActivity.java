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

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.amr.arabic.ArabicUtilities;
import org.apache.mycommons.lang3.StringUtils;
import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.AMEnv;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.AnyMemoService;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.CategoryDao;
import org.liberty.android.fantastischmemo.dao.LearningDataDao;
import org.liberty.android.fantastischmemo.dao.SettingDao;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Option;
import org.liberty.android.fantastischmemo.domain.Setting;
import org.liberty.android.fantastischmemo.tts.AnyMemoTTS;
import org.liberty.android.fantastischmemo.tts.AnyMemoTTSImpl;
import org.liberty.android.fantastischmemo.utils.AMGUIUtility;
import org.liberty.android.fantastischmemo.utils.AMUtil;
import org.liberty.android.fantastischmemo.utils.AnyMemoExecutor;
import org.xml.sax.XMLReader;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.Html.TagHandler;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

abstract public class QACardActivity extends AMActivity {

    public static String EXTRA_DBPATH = "dbpath";

    private String dbPath;
    
    private String dbName;

    private AnyMemoDBOpenHelper dbOpenHelper;

    /* DAOs */
    private SettingDao settingDao;

    private CardDao cardDao;

    private LearningDataDao learningDataDao;

    private CategoryDao categoryDao;

    private Card currentCard;

    private int screenWidth;

    private int screenHeight;

    private int animationInResId = 0;
    private int animationOutResId = 0;
    
    private Option option;

    private Setting setting;

    private InitTask initTask = null;

    private WaitDbTask waitDbTask = null;

    private boolean isAnswerShown = true;

    private TextView smallTitleBar;
    
    private AnyMemoTTS questionTTS = null;
    private AnyMemoTTS answerTTS = null;


    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            dbPath = extras.getString(EXTRA_DBPATH);
        }

        dbOpenHelper = AnyMemoDBOpenHelperManager.getHelper(this, dbPath);

        dbPath = extras.getString(EXTRA_DBPATH);
        setContentView(R.layout.qa_card_layout);

        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();


        // Set teh default animation
        animationInResId = R.anim.slide_left_in;
        animationOutResId = R.anim.slide_left_out;

        initTask = new InitTask();
        initTask.execute();
    }

    protected void setCurrentCard(Card card) {
        currentCard = card;
    }

    protected Card getCurrentCard() {
        return currentCard;
    }

    protected String getDbPath() {
        return dbPath;
    }

    protected String getDbName() {
        return dbName;
    }

    // Important class that display the card using fragment
    // the showAnswer parameter is handled differently on single
    // sided card and double sided card.
    protected void displayCard(boolean showAnswer) {

        // First prepare the text to display
        
        String questionTypeface = setting.getQuestionFont();
        String answerTypeface = setting.getAnswerFont();

        boolean enableThirdPartyArabic = option.getEnableArabicEngine();

        Setting.Align questionAlign = setting.getQuestionTextAlign();
        Setting.Align answerAlign = setting.getAnswerTextAlign();


        EnumSet<Setting.CardField> htmlDisplay = setting.getDisplayInHTMLEnum();


        String itemQuestion = currentCard.getQuestion();
        String itemAnswer = currentCard.getAnswer();
        String itemCategory = currentCard.getCategory().getName();
        String itemNote = currentCard.getNote();

        if(enableThirdPartyArabic){
            itemQuestion = ArabicUtilities.reshape(itemQuestion);
            itemAnswer = ArabicUtilities.reshape(itemAnswer);
            itemCategory = ArabicUtilities.reshape(itemCategory);
            itemNote = ArabicUtilities.reshape(itemNote);
        }

        // For question field (field1)
        SpannableStringBuilder sq = new SpannableStringBuilder();

        // For answer field  (field2)
        SpannableStringBuilder sa = new SpannableStringBuilder();
        /* Show the field that is enabled in settings */
        EnumSet<Setting.CardField> field1 = setting.getQuestionFieldEnum();
        EnumSet<Setting.CardField> field2 = setting.getAnswerFieldEnum();

        /* Iterate all fields */
        for (Setting.CardField cf : Setting.CardField.values()) {
            String str = "";
            if (cf == Setting.CardField.QUESTION) {
                str = itemQuestion;
            } else if (cf == Setting.CardField.ANSWER) {
                str = itemAnswer;
            } else if (cf == Setting.CardField.NOTE) {
                str = itemNote;
            } else {
                throw new AssertionError("This is a bug! New CardField enum has been added but the display field haven't been nupdated");
            }
            SpannableStringBuilder buffer = new SpannableStringBuilder();

            /* Automatic check HTML */
            if(AMUtil.isHTML(str) && (htmlDisplay.contains(cf))) {
                if(setting.getHtmlLineBreakConversion() == true) {
                    String s = str.replace("\n", "<br />");
                    buffer.append(Html.fromHtml(s, imageGetter, tagHandler));
                } else {
                    buffer.append(Html.fromHtml(str, imageGetter, tagHandler));
                }
            } else {
                if(buffer.length() != 0){
                    buffer.append("\n\n");
                }
                buffer.append(str);
            }
            if (field1.contains(cf)) {
                if(sq.length() != 0) {
                    sq.append(Html.fromHtml("<br /><br />", imageGetter, tagHandler));
                }
                sq.append(buffer);
            }
            if (field2.contains(cf)) {
                if(sa.length() != 0) {
                    sa.append(Html.fromHtml("<br /><br />", imageGetter, tagHandler));
                }
                sa.append(buffer);
            }

        }

        int questionAlignValue;
        int answerAlignValue;
        /* Here is tricky to set up the alignment of the text */
		if (questionAlign == Setting.Align.CENTER) {
            questionAlignValue = Gravity.CENTER;
		} else if (questionAlign == Setting.Align.RIGHT){
            questionAlignValue = Gravity.RIGHT;
		} else {
            questionAlignValue = Gravity.LEFT;
		}
        
		if (answerAlign == Setting.Align.CENTER) {
            answerAlignValue = Gravity.CENTER;
		} else if (answerAlign == Setting.Align.RIGHT){
            answerAlignValue = Gravity.RIGHT;
		} else {
            answerAlignValue = Gravity.LEFT;
		}


        String questionTypefaceValue = null;
        String answerTypefaceValue = null;
        /* Set the typeface of question and answer */
        if (StringUtils.isNotEmpty(questionTypeface)) {
            questionTypefaceValue = questionTypeface;

        }
        if (StringUtils.isNotEmpty(answerTypeface)) {
            answerTypefaceValue = answerTypeface;
        }

        // Handle the QA ratio
        LinearLayout questionLayout = (LinearLayout) findViewById(R.id.question);
        LinearLayout answerLayout = (LinearLayout) findViewById(R.id.answer);
        float qRatio = setting.getQaRatio();
        if (qRatio > 99.0f) {
            answerLayout.setVisibility(View.GONE);
            questionLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1.0f));
            answerLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1.0f));
        } else if (qRatio < 1.0f) {
            questionLayout.setVisibility(View.GONE);
            questionLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1.0f));
            answerLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1.0f));
        } else {
            float aRatio = 100.0f - qRatio;
            qRatio /= 50.0;
            aRatio /= 50.0;
            questionLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, qRatio));
            answerLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, aRatio));
        }

        // Finally we generate the fragments
        CardFragment questionFragment = new CardFragment.Builder(sq)
            .setTextAlignment(questionAlignValue)
            .setTypefaceFromFile(questionTypefaceValue)
            .setTextOnClickListener(onQuestionTextClickListener)
            .setCardOnClickListener(onQuestionViewClickListener)
            .setTextFontSize(setting.getQuestionFontSize())
            .setTypefaceFromFile(setting.getQuestionFont())
            .build();

        CardFragment answerFragment = null;

        if (setting.getCardStyle() == Setting.CardStyle.DOUBLE_SIDED || showAnswer) {
            answerFragment = new CardFragment.Builder(sa)
                .setTextAlignment(answerAlignValue)
                .setTypefaceFromFile(answerTypefaceValue)
                .setTextOnClickListener(onAnswerTextClickListener)
                .setCardOnClickListener(onAnswerViewClickListener)
                .setTextFontSize(setting.getAnswerFontSize())
                .setTypefaceFromFile(setting.getAnswerFont())
                .build();
        } else {
            answerFragment = new CardFragment.Builder(getString(R.string.memo_show_answer))
                .setTextAlignment(answerAlignValue)
                .setTypefaceFromFile(answerTypefaceValue)
                .setTextOnClickListener(onAnswerTextClickListener)
                .setCardOnClickListener(onAnswerViewClickListener)
                .setTextFontSize(setting.getAnswerFontSize())
                .setTypefaceFromFile(setting.getAnswerFont())
                .build();
        }


        // Double sided card has no animation and no horizontal line
        if (setting.getCardStyle() == Setting.CardStyle.DOUBLE_SIDED) {
            if (showAnswer) {
                findViewById(R.id.question).setVisibility(View.GONE);
                findViewById(R.id.answer).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.question).setVisibility(View.VISIBLE);
                findViewById(R.id.answer).setVisibility(View.GONE);
            }
            findViewById(R.id.horizontal_line).setVisibility(View.GONE);
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        if (setting.getCardStyle() != Setting.CardStyle.DOUBLE_SIDED && option.getEnableAnimation()) {
            if (isAnswerShown == false && showAnswer == true) {
                // No animation here.
            } else {
                ft.setCustomAnimations(animationInResId, animationOutResId);
            }
        }
        ft.replace(R.id.question, questionFragment);
        ft.commit();

        ft = getSupportFragmentManager().beginTransaction();

        if (setting.getCardStyle() != Setting.CardStyle.DOUBLE_SIDED && option.getEnableAnimation()) {
            if (isAnswerShown == false && showAnswer == true) {
                ft.setCustomAnimations(0, R.anim.slide_down);
            } else {
                ft.setCustomAnimations(animationInResId, animationOutResId);
            }
        }

        ft.replace(R.id.answer, answerFragment);
        ft.commit();

        isAnswerShown = showAnswer;

        // Set up the small title bar
        // It is defualt "GONE" so it won't take any space
        // if there is no text
        smallTitleBar = (TextView) findViewById(R.id.small_title_bar);

        // Copy the question to clickboard.
        if (option.getCopyClipboard()) {
            ClipboardManager cm = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
            cm.setText(currentCard.getQuestion());
        }
 
        onPostDisplayCard();
    }

    protected boolean isAnswerShown() {
        return isAnswerShown;
    }

    protected AnyMemoDBOpenHelper getDbOpenHelper() {
        return dbOpenHelper;
    }

    protected Setting getSetting() {
        return setting;
    }

    protected Option getOption() {
        return option;
    }

    // Call when initializing thing
    abstract protected void onInit() throws Exception;

    // Called when the initalizing finished.
    protected void onPostInit() {
        // Do nothing

    }

    // Set the card animation, 0 = no animation
    protected void setAnimation(int animationInResId, int animationOutResId) {
        this.animationInResId = animationInResId;
        this.animationOutResId = animationOutResId;
    }

    private class InitTask extends AsyncTask<Void, Void, Exception> {

        private ProgressDialog progressDialog;

		@Override
        public void onPreExecute() {
            progressDialog = new ProgressDialog(QACardActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle(getString(R.string.loading_please_wait));
            progressDialog.setMessage(getString(R.string.loading_database));
            progressDialog.setCancelable(false);
            progressDialog.show();

            option = new Option(QACardActivity.this);
            dbName = AMUtil.getFilenameFromPath(dbPath);
        }

        @Override
        public Exception doInBackground(Void... params) {
            try {
                cardDao = dbOpenHelper.getCardDao();
                learningDataDao = dbOpenHelper.getLearningDataDao();
                settingDao = dbOpenHelper.getSettingDao();
                categoryDao = dbOpenHelper.getCategoryDao();
                setting = settingDao.queryForId(1);

                // Init of common functions here
                initTTS();

                // Call customized init funciton defined in
                // the subclass
                onInit();

                // No Exception.
                return null;
            } catch (Exception e) {
                Log.e(TAG, "Excepting doing in bacground", e);
                return e;
            }
        }
        
        @Override
        public void onCancelled(){
            return;
        }

        @Override
        public void onPostExecute(Exception e){
            progressDialog.dismiss();
            if (e != null) {
                AMGUIUtility.displayError(QACardActivity.this, getString(R.string.exception_text),
                        getString(R.string.exception_message), e);
                return;
            }

            // Call customized method when init completed
            onPostInit();
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Only if the initTask has been finished and no waitDbTask is waiting.
        if ((initTask != null && AsyncTask.Status.FINISHED.equals(initTask.getStatus()))
                && (waitDbTask == null || !AsyncTask.Status.RUNNING.equals(waitDbTask.getStatus()))) {
            waitDbTask = new WaitDbTask();
            waitDbTask.execute((Void)null);
        } else {
            Log.i(TAG, "There is another task running. Do not run tasks");
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        AnyMemoDBOpenHelperManager.releaseHelper(dbOpenHelper);
        shutdownQAndATTS();

        /* Update the widget because StudyActivity can be accessed though widget*/
        Intent myIntent = new Intent(this, AnyMemoService.class);
        myIntent.putExtra("request_code", AnyMemoService.CANCEL_NOTIFICATION | AnyMemoService.UPDATE_WIDGET);
        startService(myIntent);
    }


    // Set the small title to display additional informaiton
    public void setSmallTitle(CharSequence text) {
        if (StringUtils.isNotEmpty(text)) {
            smallTitleBar.setText(text);
            smallTitleBar.setVisibility(View.VISIBLE);
        } else {
            smallTitleBar.setVisibility(View.GONE);
        }

    }

    /*
     * Use AsyncTask to make sure there is no running task for a db 
     */
    private class WaitDbTask extends AsyncTask<Void, Void, Void>{
        private ProgressDialog progressDialog;

        @Override
        public void onPreExecute(){
            super.onPreExecute();
            progressDialog = new ProgressDialog(QACardActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle(getString(R.string.loading_please_wait));
            progressDialog.setMessage(getString(R.string.loading_save));
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        public Void doInBackground(Void... nothing){
            AnyMemoExecutor.waitAllTasks();
            return null;
        }

        @Override
        public void onCancelled(){
            return;
        }

        @Override
        public void onPostExecute(Void result){
            super.onPostExecute(result);
            progressDialog.dismiss();
        }
    }

    /* Called when the card is displayed. */
    protected void onPostDisplayCard() {
        // Nothing
    }


    private ImageGetter imageGetter = new ImageGetter() {
        @Override
        public Drawable getDrawable(String source){
            Log.v(TAG, "Source: " + source);
            String dbName = AMUtil.getFilenameFromPath(dbPath);
            try{
                String[] paths = {
                    /* Relative path */
                    "" + dbName + "/" + source,
                    /* Try the image in /sdcard/anymemo/images/dbname/myimg.png */
                    AMEnv.DEFAULT_IMAGE_PATH + dbName + "/" + source,
                    /* Try the image in /sdcard/anymemo/images/myimg.png */
                    AMEnv.DEFAULT_IMAGE_PATH + source};
                Bitmap orngBitmap = null;
                for(String path : paths) {
                    Log.v(TAG, "Try path: " + path);
                    if(new File(path).exists()) {
                        orngBitmap = BitmapFactory.decodeFile(path);
                        break;
                    }
                }
                /* Try the image from internet */
                if(orngBitmap == null) {
                    InputStream is = (InputStream)new URL(source).getContent();
                    orngBitmap = BitmapFactory.decodeStream(is);
                }

                int width = orngBitmap.getWidth();
                int height = orngBitmap.getHeight();
                int scaledWidth = width;
                int scaledHeight = height;
                float scaleFactor = ((float)screenWidth) / width;
                Matrix matrix = new Matrix();
                if(scaleFactor < 1.0f){
                    matrix.postScale(scaleFactor, scaleFactor);
                    scaledWidth = (int)(width * scaleFactor);
                    scaledHeight = (int)(height * scaleFactor);
                }
                Bitmap resizedBitmap = Bitmap.createBitmap(orngBitmap, 0, 0, width, height, matrix, true);
                BitmapDrawable d = new BitmapDrawable(resizedBitmap);
                //d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
                d.setBounds(0, 0, scaledWidth, scaledHeight);
                return d; 
            }
            catch(Exception e){
                Log.e(TAG, "getDrawable() Image handling error", e);
            }

            /* Fallback, display default image */
            Drawable d = getResources().getDrawable(R.drawable.picture);
            d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            return d;
        }
    };

    
    protected void initTTS(){
        String defaultLocation = AMEnv.DEFAULT_AUDIO_PATH;
        String dbName = getDbName();
        
        if (setting.isQuestionAudioEnabled()) {
            String qa = setting.getQuestionAudio();
            List<String> questionAudioSearchPath = new ArrayList<String>();
            questionAudioSearchPath.add(setting.getQuestionAudioLocation());
            questionAudioSearchPath.add(setting.getQuestionAudioLocation() + "/" + dbName);
            questionAudioSearchPath.add(defaultLocation + "/" + dbName);
            questionAudioSearchPath.add(setting.getQuestionAudioLocation());
            questionTTS = new AnyMemoTTSImpl(this, qa, questionAudioSearchPath);
        } 
        
        if (setting.isAnswerAudioEnabled()) {
            String aa = setting.getAnswerAudio();
            List<String> answerAudioSearchPath = new ArrayList<String>();
            answerAudioSearchPath.add(setting.getAnswerAudioLocation());
            answerAudioSearchPath.add(setting.getAnswerAudioLocation() + "/" + dbName);
            answerAudioSearchPath.add(defaultLocation + "/" + dbName);
            answerAudioSearchPath.add(defaultLocation);
            answerTTS = new AnyMemoTTSImpl(this, aa, answerAudioSearchPath);
        }
    }
    
    protected boolean speakQuestion(){
        if(questionTTS != null){
            questionTTS.sayText(getCurrentCard().getQuestion());
            return true;
        }
        return false;
    }
    
    protected boolean speakAnswer(){
        if(answerTTS != null){
            answerTTS.sayText(getCurrentCard().getAnswer());
            return true;
        }
        return false;
    }
    
    protected void stopQAndATTS(){
        stopAnswerTTS();
        stopQuestionTTS();
    }
    
    protected void stopQuestionTTS(){
        if(questionTTS != null){
            questionTTS.stop();
        }
    }
    
    protected void stopAnswerTTS(){
        if(answerTTS != null){
            answerTTS.stop();
        }
    }
    
    private void shutdownQAndATTS(){
        if(questionTTS != null){
            questionTTS.shutdown();
        }
        
        if(answerTTS != null){
            answerTTS.shutdown();
        }
    }
    
    private TagHandler tagHandler = new TagHandler() {
        @Override
        public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader){
            return;
        }
    };

    protected void onClickQuestionText() {
        // Nothing
    }

    protected void onClickAnswerText() {
        // Nothing
    }

    protected void onClickQuestionView() {
        // Nothing
    }

    protected void onClickAnswerView() {
        // Nothing
    }

    private View.OnClickListener onQuestionTextClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            onClickQuestionText();
            
        }
    };

    private View.OnClickListener onAnswerTextClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            onClickAnswerText();
            
        }
    };

    private View.OnClickListener onQuestionViewClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
            onClickQuestionView();
		}
    };
    private View.OnClickListener onAnswerViewClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
            onClickAnswerView();
		}
    };
}
