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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.graph89.common.SkinBase;
import com.graph89.common.Util;

public class EmulatorScreen
{
	public static Object				ScreenChangeLock		= new Object();
	public static EngineScreenParams	EngineScreenParams		= new EngineScreenParams();

	public Rect							DestinationRectangle	= new Rect();

	public ScaledBitmap					ScreenBitmap			= null;

	private SkinBase					mParentSkinBase			= null;
	private Boolean						mIntegerZoom			= false;

	public int							Zoom					= 1;
	public boolean						IsFullScreen			= false;

	private volatile int[]				ScreenData;
	private volatile byte[]				Flags;												//b[0] screen_off, b[1] is_busy

	private int							mZoomedScreenWidth		= 0;
	private int							mZoomedScreenHeight		= 0;

	public int							mRawScreenWidth			= 0;
	public int							mRawScreenHeight		= 0;
	private Paint						mDrawingPaint			= null;

	private volatile boolean			isBusy					= false;
	private volatile boolean			isScreenOff				= false;
	public int							CRC						= 0;
	private int							cntr					= 0;

	public static class EngineScreenParams
	{
		int	RawWidth	= 0;
		int	RawHeight	= 0;
		int	Zoom		= 0;

		public void Reset()
		{
			RawWidth = 0;
			RawHeight = 0;
			Zoom = 0;
		}
	}

	public EmulatorScreen(SkinBase parent, Rect destinationRectangle, boolean keepNativeAspectRatio, boolean isFullScreen) throws IOException
	{
		mParentSkinBase = parent;
		DestinationRectangle = destinationRectangle;
		IsFullScreen = isFullScreen;

		mRawScreenWidth = mParentSkinBase.CalculatorInfo.ScreenWidth;
		mRawScreenHeight = mParentSkinBase.CalculatorInfo.ScreenHeight;

		if (keepNativeAspectRatio)
		{
			float ar = ((float) mParentSkinBase.CalculatorInfo.ScreenWidth) / mParentSkinBase.CalculatorInfo.ScreenHeight;

			int destinationWidth = DestinationRectangle.right - DestinationRectangle.left;
			int destinationHeight = DestinationRectangle.bottom - DestinationRectangle.top;

			int newHeight = (int) (destinationWidth / ar);

			if (newHeight > destinationHeight)
			{
				int newWidth = (int) (destinationHeight * ar);
				DestinationRectangle.left = (DestinationRectangle.left + DestinationRectangle.right) / 2 - newWidth / 2;
				DestinationRectangle.right = DestinationRectangle.left + newWidth;
			}
			else
			{
				DestinationRectangle.top = (DestinationRectangle.bottom + DestinationRectangle.top) / 2 - newHeight / 2;
				DestinationRectangle.bottom = DestinationRectangle.top + newHeight;
			}
		}

		int destinationWidth = DestinationRectangle.right - DestinationRectangle.left;
		int destinationHeight = DestinationRectangle.bottom - DestinationRectangle.top;

		mIntegerZoom = destinationWidth % mRawScreenWidth == 0;

		float zoomf = ((float) destinationWidth) / mRawScreenWidth;

		Zoom = destinationWidth / mRawScreenWidth;

		int diff = (int) Math.max(Math.abs(destinationWidth - Math.floor(zoomf) * mRawScreenWidth), Math.abs(destinationHeight - Math.floor(zoomf) * mRawScreenHeight));

		// try at best to use an integer zoom. tolerance 20px
		if (mIntegerZoom == false && diff < 20)
		{
			mIntegerZoom = true;

			Zoom = (int) Math.floor(zoomf);

			int width = mRawScreenWidth * Zoom;
			int height = mRawScreenHeight * Zoom;

			int centerx = (DestinationRectangle.left + DestinationRectangle.right) / 2;
			int centery = (DestinationRectangle.top + DestinationRectangle.bottom) / 2;

			Rect newRect = new Rect();
			newRect.top = centery - height / 2;
			newRect.bottom = newRect.top + height;

			newRect.left = centerx - width / 2;
			newRect.right = newRect.left + width;

			DestinationRectangle = newRect;
		}

		if (!mIntegerZoom)
		{
			mDrawingPaint = Util.FilteredPaint;
			Zoom = (int) Math.ceil(zoomf);
		}

		mZoomedScreenWidth = mRawScreenWidth * Zoom;
		mZoomedScreenHeight = mRawScreenHeight * Zoom;

		ScreenBitmap = new ScaledBitmap();
		ScreenBitmap.BitmapObj = Bitmap.createBitmap(mZoomedScreenWidth, mZoomedScreenHeight, Bitmap.Config.ARGB_8888);
		ScreenBitmap.BitmapRectangle = new Rect(0, 0, mZoomedScreenWidth, mZoomedScreenHeight);

		ScreenData = new int[mZoomedScreenWidth * mZoomedScreenHeight];

		Flags = new byte[6];
	}

	public EmulatorScreen(SkinBase parent)
	{
		mParentSkinBase = parent;
		IsFullScreen = true;
		DestinationRectangle = new Rect(0, 0, EmulatorActivity.AndroidDeviceScreenDimension.Width, EmulatorActivity.AndroidDeviceScreenDimension.Height);

		mRawScreenWidth = mParentSkinBase.CalculatorInfo.ScreenWidth;
		mRawScreenHeight = mParentSkinBase.CalculatorInfo.ScreenHeight;

		int destinationWidth = DestinationRectangle.right - DestinationRectangle.left;
		int destinationHeight = DestinationRectangle.bottom - DestinationRectangle.top;

		int horizontalZoom = destinationWidth / mRawScreenWidth;
		int verticalZoom = destinationHeight / mRawScreenHeight;

		Zoom = Math.min(horizontalZoom, verticalZoom);

		mIntegerZoom = true;

		mZoomedScreenWidth = mRawScreenWidth * Zoom;
		mZoomedScreenHeight = mRawScreenHeight * Zoom;

		ScreenBitmap = new ScaledBitmap();
		ScreenBitmap.BitmapObj = Bitmap.createBitmap(mZoomedScreenWidth, mZoomedScreenHeight, Bitmap.Config.ARGB_8888);
		ScreenBitmap.BitmapRectangle = new Rect(0, 0, mZoomedScreenWidth, mZoomedScreenHeight);

		ScreenData = new int[mZoomedScreenWidth * mZoomedScreenHeight];

		Flags = new byte[6];
	}

	public void refresh()
	{
		synchronized (EmulatorScreen.ScreenChangeLock)
		{
			++cntr;

			if (EngineScreenParams.RawHeight != mRawScreenHeight || EngineScreenParams.RawWidth != mRawScreenWidth || EngineScreenParams.Zoom != Zoom)
			{
				EmulatorActivity.TiEmuSetScreenParams(this);
			}
			
			int newCRC = EmulatorActivity.nativeReadEmulatedScreen(Flags);
			isScreenOff = Flags[0] != 0;
			isBusy = Flags[1] != 0;

			if (CRC != newCRC || cntr % 40 == 0)
			{
				CRC = newCRC;
				EmulatorActivity.nativeGetEmulatedScreen(ScreenData);
				EmulatorActivity.UIStateManagerObj.EmulatorViewIntstance.postInvalidate();
			}
		}
	}

	public boolean isBusy()
	{
		return isBusy && !isScreenOff();
	}

	public boolean isScreenOff()
	{
		return isScreenOff;
	}

	public Bitmap getScreenShot()
	{
		synchronized (EmulatorScreen.ScreenChangeLock)
		{
			EngineScreenParams.Zoom = 3;

			EmulatorActivity.nativeUpdateScreenZoom(EngineScreenParams.Zoom);

			byte[] flags = new byte[6];

			EmulatorActivity.nativeReadEmulatedScreen(flags);

			int[] data = new int[EngineScreenParams.RawWidth * EngineScreenParams.RawHeight * EngineScreenParams.Zoom * EngineScreenParams.Zoom];

			EmulatorActivity.nativeGetEmulatedScreen(data);

			Bitmap screenshotBitmap = Bitmap.createBitmap(EngineScreenParams.RawWidth * EngineScreenParams.Zoom, EngineScreenParams.RawHeight * EngineScreenParams.Zoom, Bitmap.Config.ARGB_8888);

			screenshotBitmap.setPixels(data, 0, screenshotBitmap.getWidth(), 0, 0, screenshotBitmap.getWidth(), screenshotBitmap.getHeight());

			return screenshotBitmap;
		}
	}

	public void drawScreen(Canvas canvas)
	{
		synchronized (EmulatorScreen.ScreenChangeLock)
		{
			ScreenBitmap.BitmapObj.setPixels(ScreenData, 0, ScreenBitmap.BitmapObj.getWidth(), 0, 0, ScreenBitmap.BitmapObj.getWidth(), ScreenBitmap.BitmapObj.getHeight());

			if (mIntegerZoom)
			{
				canvas.drawBitmap(ScreenBitmap.BitmapObj, DestinationRectangle.left, DestinationRectangle.top, null);
			}
			else
			{
				canvas.drawBitmap(ScreenBitmap.BitmapObj, ScreenBitmap.BitmapRectangle, DestinationRectangle, mDrawingPaint);
			}
		}
	}

	public void ReleaseBitmaps()
	{
		if (ScreenBitmap != null && ScreenBitmap.BitmapObj != null)
		{
			ScreenBitmap.BitmapObj.recycle();
			ScreenBitmap.BitmapObj = null;
		}
	}
}

class ScaledBitmap
{
	public Bitmap	BitmapObj				= null;
	public Rect		BitmapRectangle			= null;
	public Rect		DestinationRectangle	= null;
}
