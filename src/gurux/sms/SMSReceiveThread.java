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

public class SMSReceiveThread extends Thread
{
    private GXSMS m_Parent;

    public SMSReceiveThread(GXSMS parent)
    {
        m_Parent = parent;
    }

    /** 
     Check are there new SMSs.
    */
    public final void Receive()
    {
        try
        {
            do
            {
                /*
                for (GXSMSMessage it : m_Parent.Read())
                {
                    if (it.getStatus() == MessageStatus.Unread)
                    {
                        try
                        {
                                if (m_Parent.getIsSynchronous())
                                {
                                        m_Parent.SyncMessage = it;
                                        m_Parent.m_SMSReceived.Set();
                                        break;
                                }
                                m_Parent.m_OnReceived(m_Parent, new Gurux.Common.ReceiveEventArgs(it, it.getPhoneNumber()));
                        }
                        catch (RuntimeException ex)
                        {
                                m_Parent.NotifyError(ex);
                        }
                    }
                }
                * */
            }
            while(!Thread.currentThread().isInterrupted());
            //while (!Closing.WaitOne(m_Parent.getSMSCheckInterval() * 1000));
        }
        catch (RuntimeException ex)
        {
            m_Parent.notifyError(ex);
        }
    }
}