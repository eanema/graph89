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

package com.graph89.controls;

import android.app.AlertDialog;
import android.content.Context;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eanema.graph89.BuildConfig;
import com.eanema.graph89.R;
import com.graph89.emulationcore.EmulatorActivity;

public class AboutScreen
{
	private Context mContext;
	private TextView tv = null;

	public AboutScreen(Context context) {
		mContext = context;
	}

	public void Show() {
		final View view = LayoutInflater.from(mContext).inflate(R.layout.aboutscreen, (ViewGroup) ((EmulatorActivity) mContext).findViewById(R.id.aboutscreen_layout));
		tv = (TextView) view.findViewById(R.id.aboutscreen_text);
		final AlertDialog addEditdialog = new AlertDialog.Builder(mContext).setView(view).setTitle("About").setPositiveButton(android.R.string.ok, null).create();

		// @formatter:off
		String text = "Graph 89 - Version: " + BuildConfig.VERSION_NAME + "\n\n" +
				"Copyright \u00A9 2012-2013, Dritan Hashorva\n" +
				"Copyright \u00A9 2021, Micha LaQua\n" +
				"Copyright \u00A9 2024-2025, E. Anema\n\n\n" +

				"TiEmu - Version: 3.0.3\n\n" +
				"Copyright \u00A9 2000-2001, Thomas Corvazier, Romain Lievin\n" + 
				"Copyright \u00A9 2001-2003, Romain Lievin\n"+
				"Copyright \u00A9 2003, Julien Blache\n"+
				"Copyright \u00A9 2004, Romain Lievin\n"+
				"Copyright \u00A9 2005-2007, Romain Lievin, Kevin Kofler\n" +
				"Copyright \u00A9 2007 Peter Fernandes\n" +
				"http://lpg.ticalc.org/prj_tiemu/\n\n\n" +

				"Tilp - Version: 1.16\n\n" + 
				"Copyright \u00A9 2001-2003, Romain Lievin\n"+
				"Copyright \u00A9 2005-2007, Romain Lievin, Kevin Kofler\n" +
				"http://lpg.ticalc.org/prj_tilem/index.html\n\n\n" +
				
				"TilEm - Version: 2.00\n\n" +
				"Copyright \u00A9 2001 Solignac Julien\n"+
				"Copyright \u00A9 2009-2012 Benjamin Moody\n"+
				"Copyright \u00A9 2011 Duponchelle Thibault\n"+
				"Copyright \u00A9 2010 Hugues Luc BRUANT\n"+
				"http://lpg.ticalc.org/prj_tilem/index.html\n\n\n" +
				
				"Loading 8Xu, Flash files and bootloaders:\n" + 
				"Wabbitemu\n" +
				"Copyright \u00A9 2010 BuckeyeDude\n"+
				"Copyright \u00A9 2006-2007 Benjamin Moody\n"+
				"http://wabbit.codeplex.com/\n\n\n" +

				"Android UI controls:\n\n" +
				"File Picker - Anders Kalor - http://www.kaloer.com/android-file-picker-activity\n\n" +
				"android-color-picker - http://code.google.com/p/android-color-picker/";
		// @formatter:on

		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
		tv.setText(text);
		Linkify.addLinks(tv, Linkify.WEB_URLS);
		addEditdialog.show();
	}
}
