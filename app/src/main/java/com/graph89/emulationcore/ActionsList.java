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

package com.graph89.emulationcore;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.graph89.common.CalculatorTypes;
import com.graph89.common.Directories;
import com.graph89.common.EmulatorThread;
import com.graph89.common.TI84Specific;
import com.graph89.common.TI89Specific;
import com.graph89.common.TI92PSpecific;
import com.graph89.common.TI92Specific;
import com.graph89.common.V200Specific;
import com.graph89.controls.AboutScreen;
import com.graph89.controls.FilePickerActivity;
import com.graph89.controls.ListItem;
import com.graph89.controls.ListViewAdapter;
import com.graph89.controls.ScreenshotTaker;

public class ActionsList extends ListView
{
	public static List<ListItem>	ActionEntries			= null;

	public static final int			SHOW_KEYBOARD			= 0;
	public static final int			SEND_ON_KEY_PRESS		= 1;
	public static final int			INSTALL_APPS			= 2;
	public static final int			TAKE_SCREENSHOT			= 3;
	public static final int			SYNCHRONIZE_CLOCK		= 4;
	public static final int			LOAD_STATE				= 5;
	public static final int			SAVE_STATE				= 6;
	public static final int			RESET					= 7;
	public static final int			BACKUP_MANAGER			= 8;
	public static final int			ROM_MANAGER				= 9;
	public static final int 		INSTANCE_CONFIGURATION	= 10;
	public static final int			GLOBAL_CONFIGURATION	= 11;
	public static final int			ABOUT					= 12;

	private Context					mContext				= null;
	private ListViewAdapter			mAdapter				= null;

	static
	{
		ActionEntries = new ArrayList<ListItem>();
		ActionEntries.add(new ListItem(SHOW_KEYBOARD, "Show Keyboard"));
		ActionEntries.add(new ListItem(SEND_ON_KEY_PRESS, "Send Break (ON Key Press)"));
		ActionEntries.add(new ListItem(INSTALL_APPS, "Install Application / Send Files"));
		ActionEntries.add(new ListItem(TAKE_SCREENSHOT, "Take Screenshot"));
		ActionEntries.add(new ListItem(SYNCHRONIZE_CLOCK, "Synchronize Clock"));
		ActionEntries.add(new ListItem(LOAD_STATE, "Load State"));
		ActionEntries.add(new ListItem(SAVE_STATE, "Save State"));
		ActionEntries.add(new ListItem(RESET, "Reset"));
		ActionEntries.add(new ListItem(BACKUP_MANAGER, "Backup Manager"));
		ActionEntries.add(new ListItem(ROM_MANAGER, "ROM Manager"));
		ActionEntries.add(new ListItem(INSTANCE_CONFIGURATION, "ROM Configuration"));
		ActionEntries.add(new ListItem(GLOBAL_CONFIGURATION, "Settings"));
		ActionEntries.add(new ListItem(ABOUT, "About"));
	}

	public ActionsList(Context context)
	{
		super(context);
		Init(context);
	}

	public ActionsList(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		Init(context);
	}

	private void Init(Context context)
	{
		mContext = context;

		mAdapter = new ListViewAdapter(context, android.R.layout.simple_list_item_1, android.R.id.text1, ActionEntries);

		this.setAdapter(mAdapter);

		this.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				EmulatorActivity activity = (EmulatorActivity) mContext;

				switch (position)
				{
					case SHOW_KEYBOARD:
						activity.ShowKeyboard();
						activity.HideActions();
						break;
					case SEND_ON_KEY_PRESS:
						activity.SendOnKeyPress();
						activity.HideActions();
						break;
					case INSTALL_APPS:
						ChooseUploadFiles();
						break;
					case TAKE_SCREENSHOT:
						ScreenshotTaker screenshot = new ScreenshotTaker(activity);
						activity.HideActions();
						screenshot.ShowDialog();
						break;
					case SYNCHRONIZE_CLOCK:
						EmulatorActivity.SyncClock = true;
						activity.HideActions();
						break;
					case LOAD_STATE:
						if (EmulatorActivity.IsEmulating)
						{
							EmulatorThread.LoadState = true;
							activity.HideActions();
						}
						break;
					case SAVE_STATE:
						if (EmulatorActivity.IsEmulating)
						{
							EmulatorThread.SaveState = true;
							activity.HideActions();
						}
						break;
					case RESET:
						if (EmulatorActivity.IsEmulating)
						{
							String s68k = "This will clear the entire RAM. Unarchived data will be erased. It is equivalent of removing the batteries from your calculator.\nContinue?";
							String sz80 = "This will clear the whole memory, RAM and Archive. All the data and applications will be erased. \nContinue?"; 
							
							String msg = CalculatorTypes.isTilem(EmulatorActivity.ActiveInstance.CalculatorType) ? sz80 : s68k;
							final AlertDialog alert = new AlertDialog.Builder(mContext).setTitle("Warning").setMessage(msg).setNegativeButton(android.R.string.no, null).setPositiveButton(android.R.string.ok, new Dialog.OnClickListener() {
								@Override
								public void onClick(DialogInterface d, int which)
								{
									EmulatorThread.ResetCalc = true;
								}
							}).create();

							alert.show();
							activity.HideActions();
						}
						break;
					case ROM_MANAGER:
					{
						Intent intent = new Intent(activity, RomManagerActivity.class);
						intent.putExtra("Orientation", Integer.toString(EmulatorActivity.Orientation));
						activity.startActivity(intent);
					}
						break;
					case BACKUP_MANAGER:
					{
						Intent intent = new Intent(activity, BackupManager.class);
						intent.putExtra("Orientation", Integer.toString(EmulatorActivity.Orientation));
						activity.startActivity(intent);
					}
						break;
					case INSTANCE_CONFIGURATION:
					{
						Intent intent = new Intent(activity, InstanceConfigurationPage.class);
						activity.startActivity(intent);
					}
						break;
					case GLOBAL_CONFIGURATION:
					{
						Intent intent = new Intent(activity, GlobalConfigurationPage.class);
						activity.startActivity(intent);
					}
					break;
					case ABOUT:
						AboutScreen a = new AboutScreen(mContext);
						a.Show();
						break;

				}
			}
		});
	}

	public void AdjustVisibility()
	{
		if (EmulatorActivity.IsEmulating)
		{
			EmulatorActivity.UIStateManagerObj.ActionsListIntstance.setBackgroundColor(0xDA000000);

			ResetVisibility(true);
		}
		else
		{
			EmulatorActivity.UIStateManagerObj.ActionsListIntstance.setBackgroundColor(0xFF000000);

			ResetVisibility(false);
			ActionEntries.get(BACKUP_MANAGER).IsActive = true;
			ActionEntries.get(ROM_MANAGER).IsActive = true;
			ActionEntries.get(ABOUT).IsActive = true;
		}

		mAdapter.notifyDataSetChanged();
	}

	private void ResetVisibility(boolean visibilityOn)
	{
		for (int i = 0; i < ActionEntries.size(); ++i)
		{
			ActionEntries.get(i).IsActive = visibilityOn;
		}
	}

	private void ChooseUploadFiles()
	{
		// create new file selection intent
		Intent myIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
		myIntent.addCategory(Intent.CATEGORY_OPENABLE);
		myIntent.setType("*/*");
		myIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
		myIntent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Environment.getExternalStorageDirectory().getAbsolutePath());
		// start the intent
		((EmulatorActivity) mContext).startActivityForResult(myIntent, EmulatorActivity.INSTALL_APP);
	}

	private void AddAppExtensions(ArrayList<String> extensions)
	{
		switch (EmulatorActivity.ActiveInstance.CalculatorType)
		{
			case CalculatorTypes.TI89:
			case CalculatorTypes.TI89T:
				TI89Specific.AddAppExtensions(extensions);
				break;
			case CalculatorTypes.V200:
				V200Specific.AddAppExtensions(extensions);
				TI92PSpecific.AddAppExtensions(extensions);
				TI92Specific.AddAppExtensions(extensions);
				break;
			case CalculatorTypes.TI92PLUS:
				TI92PSpecific.AddAppExtensions(extensions);
				TI92Specific.AddAppExtensions(extensions);
				break;
			case CalculatorTypes.TI92:
				TI92Specific.AddAppExtensions(extensions);
				break;
			case CalculatorTypes.TI83:
			case CalculatorTypes.TI83PLUS:
			case CalculatorTypes.TI83PLUS_SE:
			case CalculatorTypes.TI84PLUS:
			case CalculatorTypes.TI84PLUS_SE:
				TI84Specific.AddAppExtensions(extensions);
				break;
		}
	}
}
