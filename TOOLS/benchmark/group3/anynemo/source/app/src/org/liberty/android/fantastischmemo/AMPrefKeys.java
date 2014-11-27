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

package org.liberty.android.fantastischmemo;

/*
 * Class that defines the contants that is used in AnyMemo.
 */
public class AMPrefKeys {
    
    // Keys for Algorithm customization.
    public static final String getInitialGradingIntervalKey(int grade) {
        return "initial_grading_interval_" + grade;
    }

    public static final String getFailedGradingIntervalKey(int grade) {
        return "failed_grading_interval_" + grade;
    }

    public static final String getEasinessIncrementalKey(int grade) {
        return "easiness_incremental_" + grade;
    }

    public static final String ENABLE_NOISE_KEY = "enable_noise";

    public static final String MINIMAL_INTERVAL_KEY = "minimal_interval";

    public static final String INITIAL_EASINESS_KEY = "initial_easiness";

    public static final String MINIMAL_EASINESS_KEY = "minimal_easiness";

    public static final String LEARN_QUEUE_SIZE_KEY = "learning_queue_size";

    // Keys for Options

    public static final String ENABLE_THIRD_PARTY_ARABIC_KEY = "enable_third_party_arabic";

    public static final String BUTTON_STYLE_KEY = "button_style";

    public static final String ENABLE_VOLUME_KEY_KEY = "enable_volume_key";

    public static final String COPY_CLIPBOARD_KEY = "copyclipboard";

    public static final String DICT_APP_KEY = "dict_app";

    public static final String SHUFFLING_CARDS_KEY = "shuffling_cards";

    public static final String SPEECH_CONTROL_KEY = "speech_ctl";


    public static final String RECENT_COUNT_KEY = "recent_count";

    public static final String ENABLE_ANIMATION_KEY = "enable_animation";

    // Dropbox
    public static final String DROPBOX_USERNAME_KEY = "dropbox_username";
    
    public static final String DROPBOX_TOKEN_KEY = "dropbox_token";

    public static final String DROPBOX_SECRET_KEY = "dropbox_secret";

    // FlashcardExchange
    public static final String FE_SAVED_USERNAME_KEY = "saved_username";
    public static final String FE_SAVED_OAUTH_TOKEN_KEY = "saved_oauth_token";
    public static final String FE_SAVED_OAUTH_TOKEN_SECRET_KEY = "saved_oauth_token_secret";
    public static final String FE_SAVED_SEARCH_KEY = "fe_saved_search";
    public static final String FE_SAVED_USER_KEY = "fe_saved_user";
    public static final String QUIZLET_SAVED_SEARCH = "quizlet_saved_search";
    public static final String QUIZLET_SAVED_USER = "quizlet_saved_user";
    public static final String GOOGLE_AUTH_TOKEN = "google_auth_token";
    
    // AnyMemo main activity
    public static final String FIRST_TIME_KEY = "first_time";

    public static final String getRecentPathKey(int ord) {
        return "recentdbpath" + ord;
    }

    public static final String SAVED_VERSION_KEY = "saved_version";

    public static final String SAVED_FILEBROWSER_PATH_KEY = "saved_fb_path";

    // Quiz
    public static final String QUIZ_GROUP_SIZE_KEY = "quiz_group_size";

    public static final String QUIZ_GROUP_NUMBER_KEY = "quiz_group_number";

    // Card Editor
    public static final String ADD_BACK_KEY = "add_back";
    // public static final String 
    // public static final String 
    // public static final String 
    
    // List edit screen
    public static final String LIST_EDIT_SCREEN_PREFIX = "ListEditScreen";

    // AMActivity
    public static final String INTERFACE_LOCALE_KEY = "interface_locale";

    public static final String FULLSCREEN_MODE_KEY = "fullscreen_mode";

    public static final String ALLOW_ORIENTATION_KEY= "allow_orientation";

    // AnyMemoService
    public static final String NOTIFICATION_INTERVAL_KEY = "notification_interval";
    
}
