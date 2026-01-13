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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import android.util.Base64;

import com.graph89.common.CalculatorInstance;
import com.graph89.common.ConfigurationHelper;
import com.graph89.common.Directories;
import com.graph89.common.EmulatorThread;
import com.graph89.common.SkinBase;
import com.graph89.common.TiEmuErrorCodes;
import com.graph89.common.Util;

public class TIEmuThread extends EmulatorThread implements Runnable
{
	public static volatile int	EngineLoopSleep		= 30;
	public static volatile int	ScreenLoopSleep		= 50;

	public static String		ReceivedFilePath	= null;
	public static String		ReceivedFileName	= null;

	public volatile boolean		IsSleeping			= false;
	private boolean				firstCycleComplete	= false;

	public TIEmuThread(EmulatorActivity activity, CalculatorInstance calculatorInstance)
	{
		super(activity, calculatorInstance);

		synchronized (EmulatorThread.EmulatorLock)
		{
			EngineThread = new Thread(this);
			EngineThread.start();
		}
	}

	public class ScreenRunnable implements Runnable
	{
		@Override
		public void run()
		{
			boolean prevScreenOff = true;
			SkinBase skin = EmulatorActivity.CurrentSkin;
			boolean isScreenOff = false;

			while (KillFlag == false)
			{
				try
				{
					if (!IsSleeping)
					{
						skin.Screen.refresh();

						isScreenOff = skin.Screen.isScreenOff();
						prevScreenOff = isScreenOff;
					}

					Thread.sleep(TIEmuThread.ScreenLoopSleep);
				}
				catch (InterruptedException e)
				{
					return;
				}
			}
		}
	}

	@Override
	public void run()
	{
		synchronized (EmulatorThread.EmulatorLock)
		{
			try
			{
				EmulatorActivity.nativeTiEmuStep1LoadDefaultConfig();
				int err = 0;
				err = EmulatorActivity.nativeTiEmuStep2LoadImage(this.CalculatorInstance.ImageFilePath);
				if (err != 0)
				{
					EmulatorActivity.AlertControlObj.SetTitleMessage("Error", "There was an error loading the IMG file. Error code: " + TiEmuErrorCodes.GetErrorCode(err));
					Activity.HandlerShowAlert();
					return;
				}
				err = EmulatorActivity.nativeTiEmuStep3Init();
				if (err != 0)
				{
					EmulatorActivity.AlertControlObj.SetTitleMessage("Error", "There was an error during init. Error code: " + TiEmuErrorCodes.GetErrorCode(err));
					Activity.HandlerShowAlert();
					return;
				}

				EmulatorActivity.nativeTiEmuStep4Reset();

				err = LoadState();
				if (err != 0)
				{
					EmulatorActivity.AlertControlObj.SetTitleMessage("Error", "There was an error reading the State file. Make sure your internal storage is accessible.");
					Activity.HandlerShowAlert();
					return;
				}

				PatchSerialNumber();

				EmulatorActivity.nativeTiEmuTurnScreenOn();

				float speedCoefficient = EmulatorActivity.ActiveInstance.Configuration.CPUSpeed / 100.0f;

		//		EmulatorActivity.TiEmuLoadEmulationInfo(Directories.getTempDirectory(Activity), Util.BoolToInt(EmulatorActivity.ActiveInstance.Configuration.EnableGrayScale), EmulatorActivity.CurrentSkin.LCDPixelON, EmulatorActivity.CurrentSkin.LCDPixelOFF, EmulatorActivity.CurrentSkin.LCDGRID, speedCoefficient, Util.BoolToInt(EmulatorActivity.ActiveInstance.Configuration.UseLCDGrid));

				ScreenThread = new Thread(new ScreenRunnable());
				ScreenThread.start();

				EmulatorActivity.IsEmulating = true;
				EmulatorActivity.UIStateManagerObj.EmulatorViewIntstance.postInvalidate();

				boolean turbo = false;
				int runCntr = 0;

				int sleepInterval = (int) ((float) TIEmuThread.EngineLoopSleep / speedCoefficient);

				int autoOff = 0;
				if (ConfigurationHelper.getBoolean(Activity, ConfigurationHelper.CONF_KEY_KEEP_SCREEN_ON,
						ConfigurationHelper.CONF_DEFAULT_KEEP_SCREEN_ON)) {
					autoOff = ConfigurationHelper.getInt(Activity,
							ConfigurationHelper.CONF_KEY_AUTO_OFF, ConfigurationHelper.CONF_DEFAULT_AUTO_OFF);
				}

				while (true)
				{
					++runCntr;

					long diff = new Date().getTime() - EmulatorActivity.LastTouched.getTime();

					if (KillFlag == true)
					{
						if (runCntr > 20 && CalculatorInstance.Configuration.SaveStateOnExit) { //don't save if the engine hasn't run for a second or so. Android tends to kill the app on orientation change. Might corrupt the state.
							WriteState();
						}
						break;
					}

					if (LoadState)
					{
						LoadState();
						LoadState = false;
					}

					if (SaveState)
					{
						WriteState();
						SaveState = false;
					}

					if (ResetCalc)
					{
						EmulatorActivity.nativeTiEmuStep4Reset();
						ResetCalc = false;
					}

					SkinBase skin = EmulatorActivity.CurrentSkin;

					if (EmulatorActivity.UploadFilesPath != null)
					{
						EmulatorActivity.ProgressDialogObj.Message = "";
						Activity.HandlerShowProgressDialog();
						for (int i = 0; i < EmulatorActivity.UploadFilesPath.size(); ++i)
						{
							String file = EmulatorActivity.UploadFilesPath.get(i);

							EmulatorActivity.ProgressDialogObj.Message = "Sending - " + file;
							Activity.HandlerUpdateProgressDialog();
							int ret = EmulatorActivity.nativeTiEmuUploadFile(file);
							
							if (ret != 0) break;
						}

						Util.deleteFile(EmulatorActivity.UploadFilesPath);
						EmulatorActivity.UploadFilesPath = null;
						Activity.HandlerHideProgressDialog();
					}

					if (EmulatorActivity.SyncClock)
					{
						EmulatorActivity.nativeTiEmuSyncClock();
						EmulatorActivity.SyncClock = false;
					}

					if (EmulatorActivity.ActiveInstance.Configuration.EnergySave)
					{
						IsSleeping = diff > 30 * 1000 && runCntr % 50 != 0 && !skin.Screen.isBusy();
					}

					if (runCntr % 40 == 0 && autoOff > 0 && diff > autoOff * 60 * 1000) {
						Activity.HandlerTerminate();
					}

					if (!IsSleeping)
					{
						EmulatorActivity.nativeTiEmuRunEngine();
						firstCycleComplete = true;
					}

					turbo = EmulatorActivity.ActiveInstance.Configuration.OverclockWhenBusy && skin.Screen.isBusy();

					if (turbo && !IsSleeping)
					{
						// ensure busy status is up to date
						skin.Screen.refresh();
					}
					else
					{
						Thread.sleep(sleepInterval);
					}
				}
			}
			catch (InterruptedException e)
			{
			}
		}
	}

	public static void ReceiveFile(String source, String dest)
	{
		if (Activity != null)
		{
			ReceivedFilePath = source;
			ReceivedFileName = dest;
			Activity.HandlerReceiveFile();
		}
	}

	private int LoadState()
	{
		boolean isStorageAvailable = Util.IsStorageAvailable();
		boolean isROMFileVisible = new File(CalculatorInstance.ImageFilePath).isFile();
		boolean isStateFileVisible = new File(CalculatorInstance.StateFilePath).isFile();

		if (!isStorageAvailable || !isROMFileVisible) return -1;

		if (isStateFileVisible && !CalculatorInstance.WasStateFileCreated)
		{
			CalculatorInstance.WasStateFileCreated = true;
			EmulatorActivity.CalculatorInstances.Save();
		}

		if (CalculatorInstance.WasStateFileCreated)
		{
			return EmulatorActivity.nativeTiEmuLoadState(CalculatorInstance.StateFilePath);
		}

		return 0;
	}

	private void WriteState()
	{
		if (Util.IsStorageAvailable() && !Activity.isFinishing())
		{
			EmulatorActivity.nativeTiEmuSaveState(CalculatorInstance.StateFilePath);

			if (CalculatorInstance.WasStateFileCreated == false)
			{
				CalculatorInstance.WasStateFileCreated = true;
				EmulatorActivity.CalculatorInstances.Save();
			}
		}
	}
	
    private void PatchSerialNumber()
    {
            String hash = Util.getMD5(EmulatorActivity.UniqueId);

            try
            {
                    File lic = new File(Directories.getLicenceFile(Activity));

                    if (lic.isFile())
                    {
                            String email = hash.substring(0, 2) + "$" + EmulatorActivity.UniqueId.trim().toLowerCase(Locale.getDefault());

                            byte[] myk = email.getBytes("UTF8");
                            byte[] k = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 };

                            for (int i = 0; i < 8 && i < myk.length; ++i)
                            {
                                    k[i] = myk[i];
                            }

                            DESKeySpec keySpec = new DESKeySpec(k);
                            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
                            SecretKey key = keyFactory.generateSecret(keySpec);

                            Cipher cipher = Cipher.getInstance("DES");
                            cipher.init(Cipher.DECRYPT_MODE, key);

                            byte[] fileData = new byte[(int) lic.length()];
                            DataInputStream dis = new DataInputStream(new FileInputStream(lic));
                            dis.readFully(fileData);
                            dis.close();

                            byte[] en = Base64.decode(fileData, Base64.DEFAULT);

                            String v = new String(cipher.doFinal(en), "UTF8");

                            if (!v.startsWith("ID:")) return;
                            v = v.replace("ID:", "");
                            if (v.length() != 14) return;

                            EmulatorActivity.nativeTiEmuPatch(v.substring(0, 10), v.substring(10, 14));
                    }
                    else
                    {
                            String part1 = "AB" + hash.substring(0, 8);
                            String part2 = hash.substring(11, 15);
                            EmulatorActivity.nativeTiEmuPatch(part1, part2);
                    }
            }
            catch (Exception e)
            {
                    e.printStackTrace();
            }
    }

}
