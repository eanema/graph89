/*
 *   Graph89 - Emulator for Android
 *  
 *	 Copyright (C) 2012-2013  Dritan Hashorva
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.

 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.graph89.common;

import android.content.Context;
import android.content.SharedPreferences;

public class ConfigurationHelper
{
	private static final String	ConfigurationName = "TI_EMU_DH";

	public static final String CONF_KEY_CALCULATOR_INSTANCES = "CalculatorInstances";

	public static final String CONF_KEY_HIDE_STATUSBAR = "hide_statusbar";
	public static final String CONF_KEY_KEEP_SCREEN_ON = "keep_screen_on";
	public static final String CONF_KEY_AUTO_OFF = "auto_off";
	public static final String CONF_KEY_HAPTIC_FEEDBACK = "haptic_feedback";
	public static final String CONF_KEY_AUDIO_FEEDBACK = "audio_feedback";
	public static final String CONF_KEY_UNIQUE_ID = "unique_id";
	public static final String CONF_KEY_SWIPE_GESTURE_ENABLED = "swipe_gesture_enabled";

	public static final boolean CONF_DEFAULT_HIDE_STATUSBAR = false;
	public static final boolean CONF_DEFAULT_KEEP_SCREEN_ON = false;
	public static final int CONF_DEFAULT_AUTO_OFF = 5;
	public static final boolean CONF_DEFAULT_HAPTIC_FEEDBACK = true;
	public static final boolean CONF_DEFAULT_AUDIO_FEEDBACK = false;
	public static final boolean CONF_DEFAULT_SWIPE_GESTURE_ENABLED = true;

	private static SharedPreferences getSharedPrefs(Context context) {
		return context.getSharedPreferences(ConfigurationName, Context.MODE_PRIVATE);
	}

	public static void writeString(Context context, String key, String value) {
		SharedPreferences.Editor editor = getSharedPrefs(context).edit();
		editor.putString(key, value);
		editor.commit();
	}

	public static void writeInt(Context context, String key, int value) {
		SharedPreferences.Editor editor = getSharedPrefs(context).edit();
		editor.putInt(key, value);
		editor.commit();
	}

	public static void writeBoolean(Context context, String key, boolean value) {
		SharedPreferences.Editor editor = getSharedPrefs(context).edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

	public static String getString(Context context, String key, String defaultValue) {
		return getSharedPrefs(context).getString(key, defaultValue);
	}

	public static int getInt(Context context, String key, int defaultValue) {
		return getSharedPrefs(context).getInt(key, defaultValue);
	}

	public static boolean getBoolean(Context context, String key, boolean defaultValue) {
		return getSharedPrefs(context).getBoolean(key, defaultValue);
	}
}
