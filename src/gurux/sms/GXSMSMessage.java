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
// More information of Gurux products: http://www.gurux.org
//
// This code is licensed under the GNU General Public License v2. 
// Full text may be retrieved at http://www.gnu.org/licenses/gpl-2.0.txt
//---------------------------------------------------------------------------

package gurux.sms;

public class GXSMSMessage
{
    private java.util.Date m_Time = new java.util.Date(0);
    private String m_Data;
    private String m_PhoneNumber;
    private String m_ServiceCenterNumber;    
    private MessageCodeType m_CodeType = MessageCodeType.values()[0];
    private int m_Index;    
    private MemoryType m_Memory = MemoryType.Unknown;
    private MessageStatus m_Status = MessageStatus.Unsent;
    
    /** 
     SMS Data to send.
    */    
    public final String getData()
    {
        return m_Data;
    }
    public final void setData(String value)
    {
        m_Data = value;
    }

    /** 
     Phone number where SMS is send or received.
    */    
    public final String getPhoneNumber()
    {
        return m_PhoneNumber;
    }
    public final void setPhoneNumber(String value)
    {
        m_PhoneNumber = value;
    }

    /** 
     When SMS is send.

     This property is set only with read messages.
    */
    public final java.util.Date getTime()
    {
        return m_Time;
    }
    public final void setTime(java.util.Date value)
    {
        m_Time = value;
    }

    /** 
     Used Service Center number. 

     This property is set only with read messages.
    */
    public final String getServiceCenterNumber()
    {
        return m_ServiceCenterNumber;
    }
    public final void setServiceCenterNumber(String value)
    {
        m_ServiceCenterNumber = value;
    }

    /** 
     In which code type is used to code SMS data. 7 bit is default.
    */
    public final MessageCodeType getCodeType()
    {
        return m_CodeType;
    }
    public final void setCodeType(MessageCodeType value)
    {
        m_CodeType = value;
    }

    /** 
     SMS Msg status.

     This property is set only with read messages.

    */
    public final MessageStatus getStatus()
    {
        return m_Status;
    }
    public final void setStatus(MessageStatus value)
    {
        m_Status = value;
    }

    /** 
     SMS Message index in the memory.

     This property is set only with read messages.
    */    
    public final int getIndex()
    {
        return m_Index;
    }
    public final void setIndex(int value)
    {
        m_Index = value;
    }

    /** 
     SMS Message index in the memory.
    */
    public final MemoryType getMemory()
    {
        return m_Memory;
    }
    public final void setMemory(MemoryType value)
    {
        m_Memory = value;
    }
}