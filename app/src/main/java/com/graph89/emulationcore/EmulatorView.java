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

import java.io.IOException;
import java.util.List;

import com.graph89.common.ConfigurationHelper;
import com.graph89.common.HighlightInfo;
import com.graph89.common.KeyPress;
import com.graph89.common.Util;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class EmulatorView extends View implements OnTouchListener
{
	/**
	 * Tracks touch state for gesture detection.
	 * Each active touch has its own tracker to support multi-touch.
	 */
	private static class TouchTracker
	{
		int pointerId;           // MotionEvent pointer ID
		float startX, startY;    // Initial touch position
		int startKeyCode;        // Key code at touch start
		float swipeThreshold;    // Vertical distance needed for swipe detection
		boolean isUpSwipeDetected; // True if upward swipe detected
		boolean isDownSwipeDetected; // True if downward swipe detected
		boolean longPressMode;   // True if timeout fired (long-press activated)
		Runnable longPressRunnable; // Timeout callback reference for cancellation

		TouchTracker(int pointerId, float x, float y, int keyCode, float threshold)
		{
			this.pointerId = pointerId;
			this.startX = x;
			this.startY = y;
			this.startKeyCode = keyCode;
			this.swipeThreshold = threshold;
			this.isUpSwipeDetected = false;
			this.isDownSwipeDetected = false;
			this.longPressMode = false;
			this.longPressRunnable = null;
		}
	}

	private EmulatorActivity	mContext	= null;

	// Gesture detection state
	private SparseArray<TouchTracker> mActiveTouches = new SparseArray<>();
	private Handler mLongPressHandler = new Handler(Looper.getMainLooper());

	// Constants
	private static final float DEFAULT_BUTTON_HEIGHT = 50.0f;  // Default button height
	private static final float SWIPE_THRESHOLD_RATIO = 0.5f;   // Threshold ratio (50%)
	private static final long LONG_PRESS_TIMEOUT = 300;  // 300ms timeout

	public EmulatorView(Context context)
	{
		super(context);
		mContext = (EmulatorActivity) context;
	}

	public EmulatorView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		mContext = (EmulatorActivity) context;
	}

	public EmulatorView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		mContext = (EmulatorActivity) context;
	}

	@Override
	public void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);

		if (!EmulatorActivity.IsEmulating) return;

		if (EmulatorActivity.CurrentSkin.CanvasDimensions.Height != canvas.getHeight() || EmulatorActivity.CurrentSkin.CanvasDimensions.Width != canvas.getWidth())
		{
			try
			{
				EmulatorActivity.CurrentSkin.Init(canvas.getWidth(), canvas.getHeight());
			}
			catch (IOException e)
			{
				Util.ShowAlert(mContext, "EmulatorView - onDraw", e);
			}
		}

		if (EmulatorActivity.CurrentSkin.Screen.IsFullScreen)
		{
			canvas.drawColor(EmulatorActivity.CurrentSkin.LCDSpaceBackgroundColor);
			EmulatorActivity.CurrentSkin.Screen.drawScreen(canvas);
		}
		else
		{
			if (EmulatorActivity.CurrentSkin.SkinBitmap != null)
			{
				canvas.drawBitmap(EmulatorActivity.CurrentSkin.SkinBitmap, 0, 0, null);
			}

			if (EmulatorActivity.CurrentSkin.Screen != null)
			{
				EmulatorActivity.CurrentSkin.Screen.drawScreen(canvas);
			}
		}
	}

	/**
	 * Calculate button height for swipe threshold calculation.
	 * Uses calculator skin's button layout information.
	 * @param keyCode The key code to get height for
	 * @return Button height in pixels
	 */
	private float calculateButtonHeight(int keyCode)
	{
		// Try to get button height from highlight info
		if (EmulatorActivity.CurrentSkin.ButtonHighlights != null) {
			List<HighlightInfo> highlights =
				EmulatorActivity.CurrentSkin.ButtonHighlights.FindHighlightInfoByKeyCode(keyCode);

			if (highlights != null && !highlights.isEmpty()) {
				HighlightInfo info = highlights.get(0);
				if (info.ButtonType != null) {
					return info.ButtonType.Height;
				}
			}
		}

		// Fallback: use default value
		return DEFAULT_BUTTON_HEIGHT;
	}

	/**
	 * Detect if current touch movement constitutes an upward swipe.
	 *
	 * @param tracker The touch tracker with start position
	 * @param currentX Current X coordinate
	 * @param currentY Current Y coordinate
	 * @return true if upward swipe detected (vertical distance > threshold AND vertical > horizontal)
	 */
	private boolean isSwipeUpGesture(TouchTracker tracker, float currentX, float currentY)
	{
		float deltaY = tracker.startY - currentY;  // Positive = upward swipe
		float deltaX = Math.abs(currentX - tracker.startX);

		// Condition 1: Upward movement exceeds threshold
		if (deltaY < tracker.swipeThreshold) {
			return false;
		}

		// Condition 2: Movement is more vertical than horizontal
		return deltaY > deltaX;
	}

	/**
	 * Detect if current touch movement constitutes an downward swipe.
	 *
	 * @param tracker The touch tracker with start position
	 * @param currentX Current X coordinate
	 * @param currentY Current Y coordinate
	 * @return true if downward swipe detected (vertical distance > threshold AND vertical > horizontal)
	 */
	private boolean isSwipeDownGesture(TouchTracker tracker, float currentX, float currentY)
	{
		float deltaY = tracker.startY - currentY;  // Positive = upward swipe, negative = downward swipe
		float deltaX = Math.abs(currentX - tracker.startX);

		// Condition 1: Upward movement exceeds threshold
		if (-deltaY < tracker.swipeThreshold) {
			return false;
		}

		// Condition 2: Movement is more vertical than horizontal
		return -deltaY > deltaX;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		if (!EmulatorActivity.IsEmulating) return false;

		final int eventaction = event.getActionMasked();
		final int actionIndex = event.getActionIndex();
		final int pointerID = event.getPointerId(actionIndex);

		int x = (int) event.getX(actionIndex);
		int y = (int) event.getY(actionIndex);

		switch (eventaction)
		{

			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_DOWN:
				// Preserve existing landscape screen swap logic
				if (EmulatorActivity.CurrentSkin instanceof LandscapeSkin)
				{
					if (EmulatorActivity.CurrentSkin.IsKeypressInScreen(x, y) || EmulatorActivity.CurrentSkin.IsFull)
					{
						EmulatorActivity.CurrentSkin.SwapScreen();
						return true;
					}
				}

				KeyPress key = EmulatorActivity.CurrentSkin.GetKeypress(x, y);
				if (key != null)
				{
					// Check if swipe gesture is enabled
					boolean swipeEnabled = ConfigurationHelper.getBoolean(
						getContext(),
						ConfigurationHelper.CONF_KEY_SWIPE_GESTURE_ENABLED,
						ConfigurationHelper.CONF_DEFAULT_SWIPE_GESTURE_ENABLED
					);

					if (!swipeEnabled)
					{
						// Direct dispatch when gesture disabled
						key.TouchID = pointerID;
						ButtonState.ButtonPress(key);
						this.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
						break;
					}

					// Calculate button height and swipe threshold
					float buttonHeight = calculateButtonHeight(key.KeyCode);
					float swipeThreshold = buttonHeight * SWIPE_THRESHOLD_RATIO;

					// Create touch tracker
					TouchTracker tracker = new TouchTracker(
						pointerID, x, y, key.KeyCode, swipeThreshold
					);
					mActiveTouches.put(pointerID, tracker);

					// Use ButtonPressVisualOnly() - visual highlight only
					key.TouchID = pointerID;
					ButtonState.ButtonPressVisualOnly(key);

					// Trigger full feedback (haptic + acoustic)
					EmulatorActivity.TriggerFeedback();

					// Create and start long-press timeout timer
					final TouchTracker finalTracker = tracker;
					tracker.longPressRunnable = new Runnable()
					{
						@Override
						public void run()
						{
							// Timeout fired: enter long-press mode, send key press
							finalTracker.longPressMode = true;
							EmulatorActivity.SendKeyToCalc(finalTracker.startKeyCode, 1, false);
						}
					};
					mLongPressHandler.postDelayed(tracker.longPressRunnable, LONG_PRESS_TIMEOUT);
				}
				break;

			case MotionEvent.ACTION_MOVE:
				// Process all active touch points
				for (int i = 0; i < event.getPointerCount(); i++) {
					int pid = event.getPointerId(i);
					TouchTracker tracker = mActiveTouches.get(pid);

					// Skip untracked or already-detected touches
					if (tracker == null || tracker.isUpSwipeDetected || tracker.isDownSwipeDetected) {
						continue;
					}

					float currentX = event.getX(i);
					float currentY = event.getY(i);

					// Detect up swipe gesture, mark only (don't send yet)
					if (isSwipeUpGesture(tracker, currentX, currentY)) {
						tracker.isUpSwipeDetected = true;

						// Swipe detected, cancel long-press timer
 						if (tracker.longPressRunnable != null) {
							mLongPressHandler.removeCallbacks(tracker.longPressRunnable);
							tracker.longPressRunnable = null;
						}
					}

					// Detect down swipe gesture, mark only (don't send yet)
					if (isSwipeDownGesture(tracker, currentX, currentY)) {
						tracker.isDownSwipeDetected = true;

						// Swipe detected, cancel long-press timer
						if (tracker.longPressRunnable != null) {
							mLongPressHandler.removeCallbacks(tracker.longPressRunnable);
							tracker.longPressRunnable = null;
						}
					}
				}
				break;

			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
			{
				TouchTracker tracker = mActiveTouches.get(pointerID);

				if (tracker != null) {
					int keyCode = tracker.startKeyCode;

					// Cancel timer first (if still running)
					if (tracker.longPressRunnable != null) {
						mLongPressHandler.removeCallbacks(tracker.longPressRunnable);
						tracker.longPressRunnable = null;
					}

					// Remove visual highlight
					ButtonState.ButtonUnpressVisualOnly(pointerID);

					// Handle different modes
					if (tracker.longPressMode) {
						// Long-press mode: only send release (press already sent in timeout)
						EmulatorActivity.SendKeyToCalc(keyCode, 0, false);
					} else {
						// Quick tap or swipe mode

						// Detect if this is ON key (EMU button)
						boolean isOnKey = (EmulatorActivity.CurrentSkin != null &&
										   EmulatorActivity.CurrentSkin.CalculatorInfo != null &&
										   keyCode == EmulatorActivity.CurrentSkin.CalculatorInfo.OnKey);

						if (isOnKey) {
							// ON key special handling
							EmulatorActivity.SendKeyToCalc(keyCode, 0, false);
						} else if (tracker.isUpSwipeDetected) {
							// Swipe gesture: send 2nd + key combination
							// *** NEW: Trigger 2nd key visual feedback ***
							ButtonState.ButtonPress2ndVisualFeedback();

							// Dynamically get current calculator's 2nd key code
							int secondKey = (EmulatorActivity.CurrentSkin != null &&
											 EmulatorActivity.CurrentSkin.CalculatorInfo != null)
											? EmulatorActivity.CurrentSkin.CalculatorInfo.SecondKey
											: 7;  // Default value (TI-89)
							int[] comboKeys = {secondKey, keyCode};
							EmulatorActivity.SendKeysToCalc(comboKeys);
						} else if (tracker.isDownSwipeDetected) {
							// Swipe gesture: send alpha + key combination
							// *** NEW: Trigger alpha key visual feedback ***
							ButtonState.ButtonPressAlphaVisualFeedback();

							// Dynamically get current calculator's alpha key code
							int alphaKey = (EmulatorActivity.CurrentSkin != null &&
											 EmulatorActivity.CurrentSkin.CalculatorInfo != null)
											? EmulatorActivity.CurrentSkin.CalculatorInfo.AlphaKey
											: 79;  // Default value (TI-89)
							int[] comboKeys = {alphaKey, keyCode};
							EmulatorActivity.SendKeysToCalc(comboKeys);
						} else {
							// Normal tap: send normal key (press+release)
							int[] normalKey = {keyCode};
							EmulatorActivity.SendKeysToCalc(normalKey);
						}
					}

					// Cleanup tracker
					mActiveTouches.remove(pointerID);
				} else {
					// No tracker: was a bypassed touch (gesture disabled)
					// Release handled by traditional ButtonState.ButtonUnpress()
					ButtonState.ButtonUnpress(pointerID);
				}
				break;
			}

			case MotionEvent.ACTION_CANCEL:
				// Clean up all touch trackers
				mActiveTouches.clear();
				// Don't call ButtonState.UnpressAll() - we never called ButtonState.ButtonPress()
				break;
		}

		return true;
	}
}
