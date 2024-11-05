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

public class SkinDefinition
{
	public static final int	SOURCE_FILESYSTEM					= 1;
	public static final int	SOURCE_ASSETS						= 2;

	public static final int	BUILD_IN_UNKNOWN					= 0;
	public static final int	BUILD_IN_89_DEFAULT					= 1;
	public static final int	BUILD_IN_89_CLASSIC					= 2;
	public static final int	BUILD_IN_89T_CLASSIC				= 3;
	public static final int	BUILD_IN_LANDSCAPE_V200_CLASSIC		= 4;
	public static final int	BUILD_IN_LANDSCAPE_TI92P_CLASSIC	= 5;
	public static final int	BUILD_IN_TI84_CLASSIC				= 6;
	public static final int	BUILD_IN_TI84_MINIMAL				= 7;

	public int				Orientation							= SkinBase.ORIENTATION_UNKNOWN;

	public String			ImagePath							= null;
	public String			MaskPath							= null;
	public String			ButtonLocationPath					= null;
	public String			InfoPath							= null;

	public int				Source								= SkinDefinition.SOURCE_ASSETS;

	public SkinDefinition(int skinType, boolean isPortrait)
	{
		switch (skinType)
		{
			case SkinDefinition.BUILD_IN_89_DEFAULT:
				if (isPortrait)
				{
					ImagePath = "portrait/ti89default/skin.png";
					MaskPath = "portrait/ti89default/buttonmask.bin";
					ButtonLocationPath = "portrait/ti89default/buttonloaction.location";
					InfoPath = "portrait/ti89default/info";
					Orientation = SkinBase.ORIENTATION_PORTRAIT;
				}
				else
				{
					ImagePath = "landscape/ti89default/skin.png";
					MaskPath = "landscape/ti89default/buttonmask.bin";
					ButtonLocationPath = "landscape/ti89default/buttonloaction.location";
					InfoPath = "landscape/ti89default/info";
					Orientation = SkinBase.ORIENTATION_LANDSCAPE;
				}
				break;
			case SkinDefinition.BUILD_IN_89_CLASSIC:
				if (isPortrait)
				{
					ImagePath = "portrait/ti89classic/skin.jpg";
					MaskPath = "portrait/ti89classic/buttonmask.bin";
					ButtonLocationPath = "portrait/ti89classic/buttonloaction.location";
					InfoPath = "portrait/ti89classic/info";
					Orientation = SkinBase.ORIENTATION_PORTRAIT;
				}
				else
				{
					ImagePath = "landscape/ti89classic/skin.jpg";
					MaskPath = "landscape/ti89classic/buttonmask.bin";
					ButtonLocationPath = "landscape/ti89classic/buttonloaction.location";
					InfoPath = "landscape/ti89classic/info";
					Orientation = SkinBase.ORIENTATION_LANDSCAPE;
				}
				break;
			case SkinDefinition.BUILD_IN_89T_CLASSIC:
				if (isPortrait)
				{
					ImagePath = "portrait/ti89tclassic/skin.jpg";
					MaskPath = "portrait/ti89tclassic/buttonmask.bin";
					ButtonLocationPath = "portrait/ti89tclassic/buttonloaction.location";
					InfoPath = "portrait/ti89tclassic/info";
					Orientation = SkinBase.ORIENTATION_PORTRAIT;
				}
				else
				{
					ImagePath = "landscape/ti89tclassic/skin.jpg";
					MaskPath = "landscape/ti89tclassic/buttonmask.bin";
					ButtonLocationPath = "landscape/ti89tclassic/buttonloaction.location";
					InfoPath = "landscape/ti89tclassic/info";
					Orientation = SkinBase.ORIENTATION_LANDSCAPE;
				}
				break;
			case SkinDefinition.BUILD_IN_LANDSCAPE_V200_CLASSIC:
				ImagePath = "landscape/v200/skin.jpg";
				MaskPath = "landscape/v200/buttonmask.bin";
				ButtonLocationPath = "landscape/v200/buttonloaction.location";
				InfoPath = "landscape/v200/info";
				Orientation = SkinBase.ORIENTATION_LANDSCAPE;
				break;
			case SkinDefinition.BUILD_IN_LANDSCAPE_TI92P_CLASSIC:
				ImagePath = "landscape/ti92plus/skin.jpg";
				MaskPath = "landscape/ti92plus/buttonmask.bin";
				ButtonLocationPath = "landscape/ti92plus/buttonloaction.location";
				InfoPath = "landscape/ti92plus/info";
				Orientation = SkinBase.ORIENTATION_LANDSCAPE;
				break;
			case SkinDefinition.BUILD_IN_TI84_CLASSIC:
				if (isPortrait)
				{
					ImagePath = "portrait/ti84classic/skin.jpg";
					MaskPath = "portrait/ti84classic/buttonmask.bin";
					ButtonLocationPath = "portrait/ti84classic/buttonloaction.location";
					InfoPath = "portrait/ti84classic/info";
					Orientation = SkinBase.ORIENTATION_PORTRAIT;
				}
				else
				{
					ImagePath = "landscape/ti84classic/skin.jpg";
					MaskPath = "landscape/ti84classic/buttonmask.bin";
					ButtonLocationPath = "landscape/ti84classic/buttonloaction.location";
					InfoPath = "landscape/ti84classic/info";
					Orientation = SkinBase.ORIENTATION_LANDSCAPE;
				}
				break;
			case SkinDefinition.BUILD_IN_TI84_MINIMAL:
				if (isPortrait)
				{
					ImagePath = "portrait/ti84minimal/skin.jpg";
					MaskPath = "portrait/ti84minimal/buttonmask.bin";
					ButtonLocationPath = "portrait/ti84minimal/buttonloaction.location";
					InfoPath = "portrait/ti84minimal/info";
					Orientation = SkinBase.ORIENTATION_PORTRAIT;
				}
				else
				{
					ImagePath = "landscape/ti84minimal/skin.jpg";
					MaskPath = "landscape/ti84minimal/buttonmask.bin";
					ButtonLocationPath = "landscape/ti84minimal/buttonloaction.location";
					InfoPath = "landscape/ti84minimal/info";
					Orientation = SkinBase.ORIENTATION_LANDSCAPE;
				}
				break;
		}
	}

	public static String SkinTypeToString(int id, int calcType)
	{
		switch (id)
		{
			case SkinDefinition.BUILD_IN_89_DEFAULT:
				return "Default";
			case SkinDefinition.BUILD_IN_89_CLASSIC:
				return "Classic 89";
			case SkinDefinition.BUILD_IN_89T_CLASSIC:
				return "Classic 89 Titanium";
			case SkinDefinition.BUILD_IN_LANDSCAPE_TI92P_CLASSIC:
				return "Classic 92 Plus";
			case SkinDefinition.BUILD_IN_LANDSCAPE_V200_CLASSIC:
				return "Classic V200";
			case SkinDefinition.BUILD_IN_TI84_CLASSIC:
				return "Classic 84";
			case SkinDefinition.BUILD_IN_TI84_MINIMAL:
				return "Minimal 84";
		}

		if (id == SkinDefinition.BUILD_IN_UNKNOWN)
		{
			if (calcType == CalculatorTypes.TI89 || calcType == CalculatorTypes.TI89T)
			{
				return "Default";
			}
			else if (calcType == CalculatorTypes.V200 || calcType == CalculatorTypes.TI92PLUS || calcType == CalculatorTypes.TI92)
			{
				return "Classic V200";
			}
			else if  (calcType == CalculatorTypes.TI83 || calcType == CalculatorTypes.TI83PLUS || calcType == CalculatorTypes.TI83PLUS_SE || calcType == CalculatorTypes.TI84PLUS || calcType == CalculatorTypes.TI84PLUS_SE)
			{
				return "Classic 84";
			}
		}

		return "";
	}

	public static int StringToSkinType(String name, int calcType)
	{
		if (name.equals("Default"))
		{
			return SkinDefinition.BUILD_IN_89_DEFAULT;
		}
		else if (name.equals("Classic 89"))
		{
			return SkinDefinition.BUILD_IN_89_CLASSIC;
		}
		else if (name.equals("Classic 89 Titanium"))
		{
			return SkinDefinition.BUILD_IN_89T_CLASSIC;
		}
		else if (name.equals("Classic V200"))
		{
			return SkinDefinition.BUILD_IN_LANDSCAPE_V200_CLASSIC;
		}
		else if (name.equals("Classic 92 Plus"))
		{
			return SkinDefinition.BUILD_IN_LANDSCAPE_TI92P_CLASSIC;
		}
		else if (name.equals("Classic 84"))
		{
			return SkinDefinition.BUILD_IN_TI84_CLASSIC;
		}
		else if (name.equals("Minimal 84"))
		{
			return SkinDefinition.BUILD_IN_TI84_MINIMAL;
		}
		if (calcType == CalculatorTypes.TI89 || calcType == CalculatorTypes.TI89T)
		{
			return SkinDefinition.BUILD_IN_89_DEFAULT;
		}
		else if (calcType == CalculatorTypes.V200 || calcType == CalculatorTypes.TI92PLUS || calcType == CalculatorTypes.TI92)
		{
			return SkinDefinition.BUILD_IN_LANDSCAPE_V200_CLASSIC;
		}
		else if  (calcType == CalculatorTypes.TI83 || calcType == CalculatorTypes.TI83PLUS || calcType == CalculatorTypes.TI83PLUS_SE || calcType == CalculatorTypes.TI84PLUS || calcType == CalculatorTypes.TI84PLUS_SE)
		{
			return SkinDefinition.BUILD_IN_TI84_CLASSIC;
		}

		return SkinDefinition.BUILD_IN_89_DEFAULT;
	}
}
