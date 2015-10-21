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

import gurux.sms.enums.MemoryType;
import gurux.sms.enums.MessageCodeType;
import gurux.sms.enums.MessageStatus;

/**
 * This class implements SMS message to send.
 * 
 * @author Gurux Ltd.
 *
 */
public class GXSMSMessage {
    /**
     * Send time.
     */
    private java.util.Date time = new java.util.Date(0);
    /**
     * Message data.
     */
    private String data;
    /**
     * Sender or receiver phone number.
     */
    private String phoneNumber;
    /**
     * Service center phone number.
     */
    private String serviceCenterNumber;
    /**
     * Code type that is used to code the message.
     */
    private MessageCodeType codeType = MessageCodeType.values()[0];
    /**
     * Memory index.
     */
    private int index;
    /**
     * Used memory.
     */
    private MemoryType memory = MemoryType.UNKNOWN;
    /**
     * Message status.
     */
    private MessageStatus status = MessageStatus.NOT_SENT;

    /**
     * Get SMS data to send.
     * 
     * @return SMS Data.
     */
    public final String getData() {
        return data;
    }

    /**
     * Send SMS data to send.
     * 
     * @param value
     *            SMS data.
     */
    public final void setData(final String value) {
        data = value;
    }

    /**
     * Gets phone number where SMS is send or received.
     * 
     * @return Phone number.
     */
    public final String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Sets phone number where SMS is send or received.
     * 
     * @param value
     *            Phone number.
     */
    public final void setPhoneNumber(final String value) {
        phoneNumber = value;
    }

    /**
     * Get When SMS is send. This property is set only with read messages.
     * 
     * @return Date time when message was send.
     */
    public final java.util.Date getTime() {
        return time;
    }

    /**
     * Set When SMS is send.
     * 
     * @param value
     *            Date time value.
     */
    final void setTime(final java.util.Date value) {
        time = value;
    }

    /**
     * Get used Service Center number. This property is set only with read
     * messages.
     * 
     * @return Service Center number.
     */
    public final String getServiceCenterNumber() {
        return serviceCenterNumber;
    }

    /**
     * Set used Service Center number. This property is set only with read
     * messages.
     * 
     * @param value
     *            Service Center number.
     */
    final void setServiceCenterNumber(final String value) {
        serviceCenterNumber = value;
    }

    /**
     * Gets used code type with SMS data. 7 bit is default.
     * 
     * @return Message code type.
     */
    public final MessageCodeType getCodeType() {
        return codeType;
    }

    /**
     * Sets used code type with SMS data. 7 bit is default.
     * 
     * @param value
     *            Message code type.
     */
    public final void setCodeType(final MessageCodeType value) {
        codeType = value;
    }

    /**
     * Gets SMS message status. This property is set only with read messages.
     * 
     * @return Message status.
     */
    public final MessageStatus getStatus() {
        return status;
    }

    /**
     * Sets SMS message status. This property is set only with read messages.
     * 
     * @param value
     *            Message status.
     */
    final void setStatus(final MessageStatus value) {
        status = value;
    }

    /**
     * Gets SMS Message index in the memory.
     * 
     * This property is set only with read messages.
     * 
     * @return Message index.
     */
    public final int getIndex() {
        return index;
    }

    /**
     * SMS Message index in the memory.
     * 
     * This property is set only with read messages.
     * 
     * @param value
     *            Message index.
     */
    final void setIndex(final int value) {
        index = value;
    }

    /**
     * Gets SMS Message memory type.
     * 
     * @return Memory type.
     */
    public final MemoryType getMemory() {
        return memory;
    }

    /**
     * Sets SMS Message memory type.
     * 
     * @param value
     *            Memory type.
     */
    public final void setMemory(final MemoryType value) {
        memory = value;
    }

    @Override
    public final String toString() {
        return getPhoneNumber() + " : " + String.valueOf(getTime()) + " : "
                + getData();
    }
}