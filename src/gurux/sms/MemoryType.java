//
// --------------------------------------------------------------------------
//  Gurux Ltd
// 
//
//
// Filename:        $HeadURL$
//
// Version:         $Revision$,
//                  $Date$
//                  $Author$
//
// Copyright (c) Gurux Ltd
//
//---------------------------------------------------------------------------
//
//  DESCRIPTION
//
// This file is a part of Gurux Device Framework.
//
// Gurux Device Framework is Open Source software; you can redistribute it
// and/or modify it under the terms of the GNU General Public License 
// as published by the Free Software Foundation; version 2 of the License.
// Gurux Device Framework is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of 
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
// See the GNU General Public License for more details.
//
// This code is licensed under the GNU General Public License v2. 
// Full text may be retrieved at http://www.gnu.org/licenses/gpl-2.0.txt
//---------------------------------------------------------------------------

package gurux.sms;

/** 
 SMS Message memory type.
*/
public enum MemoryType
{
	/** 
	 SMS Message memory type is unknown.
	*/
	Unknown(-1),
	/** 
	 SIM's Memory is used.
	*/
	Sim(0),
	/** 
	 Phone's memory is used.
	*/
	Phone(1);

	private int intValue;
	private static java.util.HashMap<Integer, MemoryType> mappings;
	private static java.util.HashMap<Integer, MemoryType> getMappings()
	{
		if (mappings == null)
		{
			synchronized (MemoryType.class)
			{
				if (mappings == null)
				{
					mappings = new java.util.HashMap<Integer, MemoryType>();
				}
			}
		}
		return mappings;
	}

	private MemoryType(int value)
	{
		intValue = value;
		getMappings().put(value, this);
	}

	public int getValue()
	{
		return intValue;
	}

	public static MemoryType forValue(int value)
	{
		return getMappings().get(value);
	}
}