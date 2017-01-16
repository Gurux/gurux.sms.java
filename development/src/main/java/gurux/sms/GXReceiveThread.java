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

import java.lang.reflect.Array;

import gurux.common.GXSynchronousMediaBase;

/**
 * Receive thread listens serial port and sends received data to the listeners.
 * 
 * @author Gurux Ltd.
 *
 */
class GXReceiveThread extends Thread {

    /**
     * Serial port handle.
     */
    private long comPort;
    /**
     * Parent component.
     */
    private GXSMS parentMedia;
    /**
     * Bytes received.
     */
    private long bytesReceived = 0;

    /**
     * Constructor.
     * 
     * @param parent
     *            Parent component.
     * @param hComPort
     *            Handle for the serial port.
     */
    GXReceiveThread(final GXSMS parent, final long hComPort) {
        super("GXTerminal " + new Long(hComPort).toString());
        comPort = hComPort;
        parentMedia = parent;
    }

    /**
     * Get amount of received bytes.
     * 
     * @return Amount of received bytes.
     */
    public final long getBytesReceived() {
        return bytesReceived;
    }

    /**
     * Reset amount of received bytes.
     */
    public final void resetBytesReceived() {
        bytesReceived = 0;
    }

    /**
     * Handle received data.
     * 
     * @param buffer
     *            Received data from the serial port.
     */
    private void handleReceivedData(final byte[] buffer) {
        int len = buffer.length;
        if (len == 0) {
            return;
        }
        bytesReceived += len;
        int totalCount = 0;
        synchronized (parentMedia.getSyncBase().getSync()) {
            parentMedia.getSyncBase().appendData(buffer, bufferPosition, len);
            // Search end of packet if given.
            if (parentMedia.getEop() != null) {
                if (parentMedia.getEop() instanceof Array) {
                    for (Object eop : (Object[]) parentMedia.getEop()) {
                        totalCount = GXSynchronousMediaBase.indexOf(buffer,
                                GXSynchronousMediaBase.getAsByteArray(eop),
                                0, len);
                        if (totalCount != -1) {
                            break;
                        }
                    }
                } else {
                    totalCount = GXSynchronousMediaBase.indexOf(buffer,
                            GXSynchronousMediaBase
                                    .getAsByteArray(parentMedia.getEop()),
                            0, len);
                }
            }
            if (totalCount != -1) {
                parentMedia.getSyncBase().setReceived();
            }
        }
    }

    @Override
    public final void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                byte[] buff = gurux.io.NativeCode.read(this.comPort,
                        parentMedia.getReadTimeout(), parentMedia.getClosing());
                // If connection is closed.
                if (buff.length == 0
                        && Thread.currentThread().isInterrupted()) {
                    parentMedia.setClosing(0);
                    break;
                }
                handleReceivedData(buff);
            } catch (Exception ex) {
                if (!Thread.currentThread().isInterrupted()) {
                    parentMedia
                            .notifyError(new RuntimeException(ex.getMessage()));
                } else {
                    break;
                }
            }
        }
    }
}