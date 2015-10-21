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

import gurux.common.ReceiveEventArgs;
import gurux.sms.enums.MessageStatus;

/**
 * This class handles received SMSs and send them to the media component.
 * 
 * @author Gurux Ltd.
 *
 */
class SMSReceiveThread extends Thread {
    /**
     * Parent component.
     */
    private GXSMS parentMedia;

    /**
     * Constructor.
     * 
     * @param parent
     *            Parent component.
     */
    public SMSReceiveThread(final GXSMS parent) {
        parentMedia = parent;
    }

    /**
     * Check are there new SMSs.
     * 
     * @throws InterruptedException
     */
    public final void receive() {
        try {
            do {
                for (GXSMSMessage it : parentMedia.read()) {
                    if (it.getStatus() == MessageStatus.NOT_READ) {
                        try {
                            parentMedia.notifyReceived(new ReceiveEventArgs(it,
                                    it.getPhoneNumber()));
                        } catch (RuntimeException ex) {
                            parentMedia.notifyError(ex);
                        }
                    }
                }
                Thread.sleep(parentMedia.getSMSCheckInterval() * 1000);
            } while (!Thread.currentThread().isInterrupted());
        } catch (RuntimeException ex) {
            parentMedia.notifyError(ex);
        } catch (InterruptedException e) {
            parentMedia.notifyError(new RuntimeException(e.getMessage()));
        }
    }
}