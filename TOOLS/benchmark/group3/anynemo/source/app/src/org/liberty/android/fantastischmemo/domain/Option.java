package org.liberty.android.fantastischmemo.domain;

import org.liberty.android.fantastischmemo.AMPrefKeys;

import android.content.Context;
import android.content.SharedPreferences;

import android.preference.PreferenceManager;

public class Option {

    private SharedPreferences settings;
    private SharedPreferences.Editor editor;

    public Option (Context context) {
    	settings = PreferenceManager.getDefaultSharedPreferences(context);
        editor = settings.edit();
    }

    public boolean getEnableArabicEngine() {
        return settings.getBoolean(AMPrefKeys.ENABLE_THIRD_PARTY_ARABIC_KEY, true);
    }

    public void setEnableArabicEngine(boolean enable) {
        editor.putBoolean(AMPrefKeys.ENABLE_THIRD_PARTY_ARABIC_KEY, enable);
        editor.commit();
    }

    public ButtonStyle getButtonStyle() {
        return ButtonStyle.parse(settings.getString(AMPrefKeys.BUTTON_STYLE_KEY, "ANYMEMO"));
    }

    public void setButtonStyle(ButtonStyle style) {
        editor.putString(AMPrefKeys.BUTTON_STYLE_KEY, style.toString());
        editor.commit();
    }

    public boolean getVolumeKeyShortcut() {
        return settings.getBoolean(AMPrefKeys.ENABLE_VOLUME_KEY_KEY, false);
    }

    public void setVolumeKeyShortcut(boolean enable) {
        editor.putBoolean(AMPrefKeys.ENABLE_VOLUME_KEY_KEY, enable);
        editor.commit();
    }

    public boolean getCopyClipboard() {
        return settings.getBoolean(AMPrefKeys.COPY_CLIPBOARD_KEY, true);
    }

    public void setCopyClipboard(boolean enable) {
        editor.putBoolean(AMPrefKeys.COPY_CLIPBOARD_KEY, enable);
        editor.commit();
    }

	public DictApp getDictApp() {
        return DictApp.parse(settings.getString(AMPrefKeys.DICT_APP_KEY, "FORA"));
	}

	public ShuffleType getShuffleType() {
        if (settings.getBoolean(AMPrefKeys.SHUFFLING_CARDS_KEY, false)) {
            return ShuffleType.LOCAL;
        } else {
            return ShuffleType.NONE;
        }
	}

	public SpeakingType getSpeakingType() {
        return SpeakingType.parse(settings.getString(AMPrefKeys.SPEECH_CONTROL_KEY, "TAP"));
	}

    public int getSavedId(String prefix, String key, int defaultValue) {
        return settings.getInt(prefix + key, defaultValue);
    }

    public void setSavedId(String prefix, String key, int value) {
        editor.putInt(prefix + key, value);
        editor.commit();
    }
    
    public int getRecentCount() {
    	return settings.getInt(AMPrefKeys.RECENT_COUNT_KEY, 7);
    }
    
    public int getQueueSize() {
        String size = settings.getString(AMPrefKeys.LEARN_QUEUE_SIZE_KEY, "10");
        int tmpSize = Integer.parseInt(size);
        if(tmpSize > 0){
            return tmpSize;
        } else {
            return 10;
        }
    }

    public boolean getEnableAnimation() {
        return settings.getBoolean(AMPrefKeys.ENABLE_ANIMATION_KEY, true);
    }
    
    public static enum ButtonStyle {
        ANYMEMO,
        MNEMOSYNE,
        ANKI;

        public static ButtonStyle parse(String a){
            if(a.equals("MNEMOSYNE")){
                return MNEMOSYNE;
            }
            else if(a.equals("ANKI")){
                return ANKI;
            }
            else{
                return ANYMEMO;
            }
        }
    }

    public static enum DictApp {
        COLORDICT,
        FORA;
        public static DictApp parse(String a){
            if(a.equals("COLORDICT")){
                return COLORDICT;
            } else{
                return FORA;
            }
        }
    }

    public static enum ShuffleType {
        NONE,
        LOCAL
    }

    public static enum SpeakingType {
        MANUAL,
        TAP,
        AUTO,
        AUTOTAP;

        public static SpeakingType parse(String a){
            if(a.equals("MANUAL")){
                return MANUAL;
            } else if(a.equals("AUTO")){
                return AUTO;
            } else if(a.equals("AUTOTAP")){
                return AUTOTAP;
            } else{
                return TAP;
            }
        }
        
    }

}
