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

import android.os.Handler;
import android.os.Looper;

import com.graph89.common.HighlightInfo;
import com.graph89.common.KeyPress;

public class ButtonState
{
	public static int				ActivePointerID	= -1;
	private static List<KeyPress>	sPressedButtons	= new ArrayList<KeyPress>();
	private static Handler			sHandler		= new Handler(Looper.getMainLooper());

	public static void Reset()
	{
		sPressedButtons.clear();
		ActivePointerID = -1;
	}

	public static void ButtonPress(KeyPress key)
	{
		if (IsKeyCodeInvalid(key.KeyCode)) return;
		boolean found = false;
		for (int i = 0; i < sPressedButtons.size(); ++i)
		{
			if (sPressedButtons.get(i).KeyCode == key.KeyCode || sPressedButtons.get(i).TouchID == key.TouchID)
			{
				found = true;
				break;
			}
		}

		if (!found)
		{
			sPressedButtons.add(key);
			EmulatorActivity.SendKeyToCalc(key.KeyCode, 1, true);
		}

		RefreshButtonHighlightView();
	}

	public static void ButtonUnpress(int touchID)
	{
		for (int i = 0; i < sPressedButtons.size(); ++i)
		{
			KeyPress button = sPressedButtons.get(i);

			if (button.TouchID == touchID)
			{
				EmulatorActivity.SendKeyToCalc(button.KeyCode, 0, false);
				RefreshButtonHighlightView();
				sPressedButtons.remove(i);
				return;
			}
		}
	}

	/**
	 * Highlight button visually without sending key to calculator engine.
	 * Used during gesture detection window to provide immediate feedback
	 * while deferring actual key dispatch decision.
	 *
	 * @param key The key to highlight (contains keyCode and touchID)
	 */
	public static void ButtonPressVisualOnly(KeyPress key)
	{
		if (IsKeyCodeInvalid(key.KeyCode)) return;

		synchronized (sPressedButtons)
		{
			// Check if already pressed
			for (KeyPress k : sPressedButtons)
			{
				if (k.TouchID == key.TouchID) return;
			}

			// Add to pressed list for visual highlight
			sPressedButtons.add(key);

			// Redraw to show highlight (must be inside synchronized block)
			RefreshButtonHighlightView();
		}
	}

	/**
	 * Remove button visual highlight without sending release to calculator.
	 * Used when swipe is detected to clear original button highlight before
	 * dispatching 2nd key combination.
	 *
	 * @param touchID The touch identifier to remove
	 */
	public static void ButtonUnpressVisualOnly(int touchID)
	{
		synchronized (sPressedButtons)
		{
			for (int i = 0; i < sPressedButtons.size(); ++i)
			{
				if (sPressedButtons.get(i).TouchID == touchID)
				{
					sPressedButtons.remove(i);
					RefreshButtonHighlightView();
					return;
				}
			}
		}
	}

	/**
	 * Briefly highlight 2nd key to provide visual confirmation when
	 * swipe gesture triggers a 2nd key combination. The highlight
	 * lasts 100ms and then auto-removes.
	 */
	public static void ButtonPress2ndVisualFeedback()
	{
		if (EmulatorActivity.CurrentSkin == null || EmulatorActivity.CurrentSkin.CalculatorInfo == null) return;

		int secondKey = EmulatorActivity.CurrentSkin.CalculatorInfo.SecondKey;
		if (secondKey == -1) return;

		// Get 2nd key coordinates from highlight info
		List<HighlightInfo> highlights = EmulatorActivity.CurrentSkin.ButtonHighlights.FindHighlightInfoByKeyCode(secondKey);
		if (highlights == null || highlights.isEmpty()) return;

		HighlightInfo info = highlights.get(0);

		// Create temporary visual press with special touchID and coordinates
		final int TEMP_TOUCH_ID = -999;
		KeyPress tempPress = new KeyPress();
		tempPress.KeyCode = secondKey;
		tempPress.TouchID = TEMP_TOUCH_ID;
		tempPress.X = info.CenterX;
		tempPress.Y = info.CenterY;
		ButtonPressVisualOnly(tempPress);

		// Trigger full feedback (haptic + acoustic) to match direct 2nd key press
		EmulatorActivity.TriggerFeedback();

		// Auto-remove after 100ms (use shared Handler instance)
		sHandler.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				ButtonUnpressVisualOnly(TEMP_TOUCH_ID);
			}
		}, 100);
	}

	public static void UnpressAll()
	{
		for (int i = 0; i < sPressedButtons.size(); ++i)
		{
			KeyPress button = sPressedButtons.get(i);
			EmulatorActivity.SendKeyToCalc(button.KeyCode, 0, false);
		}
		sPressedButtons.clear();
		RefreshButtonHighlightView();
	}

	public static KeyPress[] GetPressedKeys()
	{
		synchronized (sPressedButtons)
		{
			return (KeyPress[]) sPressedButtons.toArray(new KeyPress[sPressedButtons.size()]);
		}
	}

	private static void RefreshButtonHighlightView()
	{
		if (EmulatorActivity.UIStateManagerObj != null && EmulatorActivity.UIStateManagerObj.ButtonHighlightViewInstance != null)
		{
			EmulatorActivity.UIStateManagerObj.ButtonHighlightViewInstance.invalidate();
		}
	}

	public static boolean IsKeyCodeInvalid(int keyCode)
	{
		return keyCode < 0 || keyCode >= 255;
	}
}
