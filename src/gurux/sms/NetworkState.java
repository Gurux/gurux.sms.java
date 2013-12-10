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
 Status of SMS network.
*/
public enum NetworkState
{
    /** 
     Not registered, ME is not currently searching for a new operator at which to register.
    */
    NotRegistered,
    /** 
     Registered to home network.
    */
    Home,
    /** 
     Not registered, but ME is currently searching for a new operator at which to register.
    */
    Searching,
    /** 
     Registration is denied.
    */
    Denied,
    /** 
     Network status is unknown.
    */
    Unknown,
    /** 
     Registered, roaming.
    */
    Roaming;

    public int getValue()
    {
        return this.ordinal();
    }

    public static NetworkState forValue(int value)
    {
        return values()[value];
    }
}