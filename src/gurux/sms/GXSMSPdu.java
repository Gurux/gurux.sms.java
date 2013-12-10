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

class GXSMSPdu
{
    private static String CodeInteger(int value)
    {
        char[] tmp = new char[2];
        int b = ((byte)(value >> 4));
        tmp[0] = (char)(b > 9 ? b + 0x37 : b + 0x30);
        b = ((byte)(value & 0x0F));
        tmp[1] = (char)(b > 9 ? b + 0x37 : b + 0x30);
        return new String(tmp);
    }

    private static String CodeString(String data)
    {
        StringBuilder sb = new StringBuilder(data.length());
        for (int pos = 0; pos < data.length(); pos += 2)
        {
            sb.append(data.charAt(pos + 1));
            sb.append(data.charAt(pos));
        }
        return sb.toString();
    }

    /** 
     Convert SMS char to Unicode char.	 
    */
    private static int ASCIItoSMS(int ch)
    {
        switch (ch)
        {
            case 0x40: //@
                ch = 0;
                break;
            case 0xA3:
                ch = 1;
                break;
            case 0x24: //'$'
                ch = 2;
                break;
            case 165:
                ch = 3;
                break;
            case 0xE8:
                ch = 4;
                break;
            case 0xE9:
                ch = 5;
                break;
            case 0xF9:
                ch = 6;
                break;
            case 0xEC:
                ch = 7;
                break;
            case 0xF2:
                ch = 8;
                break;
            case 0xC7:
                    ch = 9;
                    break;
            case 0xD8:
                    ch = 11;
                    break;
            case 0xF8:
                    ch = 12;
                    break;
            case 0xC5:
                    ch = 14;
                    break;
            case 0xE5:
                    ch = 15;
                    break;
            case 0x0394:
                    ch = 16;
                    break;
            case 0x5F: //'_'
                    ch = 17;
                    break;
            case 0x03A6:
                    ch = 18;
                    break;
            case 0x0393:
                    ch = 19;
                    break;
            case 0x039B:
                    ch = 20;
                    break;
            case 0x03A9:
                    ch = 21;
                    break;
            case 0x03A0:
                    ch = 22;
                    break;
            case 0x03A8:
                    ch = 23;
                    break;
            case 0x03A3:
                    ch = 24;
                    break;
            case 0x0398:
                    ch = 25;
                    break;
            case 0x039E:
                    ch = 26;
                    break;
            case 0xC6:
                    ch = 28;
                    break;
            case 0xE6:
                    ch = 29;
                    break;
            case 0xDF:
                    ch = 30;
                    break;
            case 0xC9:
                    ch = 31;
                    break;
            case 0xA4:
                    ch = 36;
                    break;
            case 0xA1:
                    ch = 64;
                    break;
            case 0xC4:
                    ch = 91;
                    break;
            case 0xD6:
                    ch = 92;
                    break;
            case 0xD1:
                    ch = 93;
                    break;
            case 0xDC:
                    ch = 94;
                    break;
            case 167: //Section sign
                    ch = 95;
                    break;
            case 0x00BF: //Inverted question mark.
                    ch = 96;
                    break;
            case 0xE4:
                    ch = 123;
                    break;
            case 0xF6:
                    ch = 124;
                    break;
            case 0xF1:
                    ch = 125;
                    break;
            case 0xFC:
                    ch = 126;
                    break;
            case 0xE0:
                    ch = 127;
                    break;
            case 12: //FORM FEED
                    ch = 0x1B0A;
                    break;
            case 94: //CIRCUMFLEX ACCENT ^
                    ch = 0x1B14;
                    break;
            case 123: //LEFT CURLY BRACKET {
                    ch = 0x1B28;
                    break;
            case 125: //RIGHT CURLY BRACKET }
                    ch = 0x1B29;
                    break;
            case 92: //REVERSE SOLIDUS (BACKSLASH)
                    ch = 0x1B2F;
                    break;
            case 91: //LEFT SQUARE BRACKET [
                    ch = 0x1B3C;
                    break;
            case 126: //TILDE
                    ch = 0x1B3D;
                    break;
            case 93: //RIGHT SQUARE BRACKET ]
                    ch = 0x1B3E;
                    break;
            case 124: //VERTICAL BAR |
                    ch = 0x1B40;
                    break;
            case 0x20AC: //EURO SIGN
                    ch = 0x1B65;
                    break;
        }
        return ch;
    }


    /** 
     Convert 8 bits data to the 7 bits data.

     @param data
     @return 
    */
    private static String Code7Bit(String data)
    {
        if (data == null || data.equals(""))
        {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        //Convert ASCII chars to SMS chars.
        int pos;
        for (pos = 0; pos < data.length(); ++pos)
        {
            int val = ASCIItoSMS(data.charAt(pos));
            //If 16 bits.
            if ((val & 0xFF00) != 0)
            {
                sb.append((char)((val >> 8) & 0xFF));
                sb.append((char)(val & 0xFF));
            }
            else //If 8 bits.
            {
                sb.append((char) val);
            }
        }
        String str = sb.toString();
        String output = "";
        for (pos = 0; pos < str.length(); ++pos)
        {
            int mask = (1 << ((pos % 8) + 1)) - 1;
            if (mask == 0xFF)
            {
                mask = 1;
                continue;
            }
            int ch1 = str.charAt(pos) >> (pos % 8);
            int ch2 = 0;
            if (pos < str.length() - 1)
            {
                ch2 = str.charAt(pos + 1) & mask;
            }
            ch2 = ch2 << (7 - (pos % 8));
            output += CodeInteger(ch1 | ch2);
        }
        return output;
    }

    private static String Code8Bit(String data)
    {
        if (data == null || data.equals(""))
        {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int it : data.getBytes())
        {
            sb.append(CodeInteger(it));
        }
        return sb.toString();
    }

    private static String CodeUnicode(String data)
    {
        if (data == null || data.equals(""))
        {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int ch : data.getBytes())
        {
            //Add high part.
            sb.append(CodeInteger((ch & 0xFF00) >> 8));
            //Add low part.
            sb.append(CodeInteger(ch & 0xFF));
        }
        return sb.toString();
    }
    
   /**
    * Get integer as Word. bSwap is used to swap lo byte and hibyte when time is get.
    */
    private static int getInteger(String data, int index)
    {
        if (data.length() < 2)
        {
            throw new IllegalArgumentException("Invalid data");
        }
        int c = data.charAt(index);
        int value = ((c > '9' ? (c > 'Z' ? (c - 'a' + 10) : (c - 'A' + 10)) : (c - '0')) << 4) & 0xF0;
        c = data.charAt(index + 1);
        value |= (c > '9' ? (c > 'Z' ? (c - 'a' + 10) : (c - 'A' + 10)) : (c - '0')) & 0xF;
        return value;
    }

    private static String getString(String data, int index, int length, boolean swap)
    {
        if (data.length() < 0)
        {
            throw new IllegalArgumentException("Invalid data");
        }
        if (swap)
        {
            char[] bytes = new char[length];
            for (int pos = 0; pos < length; pos += 2)
            {
                bytes[pos] = data.charAt(index + pos + 1);
                bytes[pos + 1] = data.charAt(index + pos);
            }
            return new String(bytes);
        }
        return data.substring(index, index + length);
    }

    public static String Code(String receiver, String message, MessageCodeType type)
    {
        if (receiver == null || receiver.equals(""))
        {
            throw new IllegalArgumentException("Receiver is invalid.");
        }
        receiver = receiver.trim();
        if (receiver.length() == 0)
        {
            throw new IllegalArgumentException("Receiver is invalid.");
        }
        //Length of SMSC information. Here the length is 0, which means that the SMSC stored in the phone should be used. 
        //Note: This octet is optional. On some phones this octet should be omitted! 
        //(Using the SMSC stored in phone is thus implicit) 
        String data = CodeInteger(0x00);
        //First octet of the SMS-SUBMIT message. 
        data += CodeInteger(0x011);
        //TP-Message-Reference. The "00" value here lets the phone set the message reference number itself.  
        data += CodeInteger(0x00);
        //Is phone number give as internal format.
        boolean bInternational = (receiver.charAt(0) == '+');
        if (bInternational)
        {
            receiver = receiver.substring(1);
        }
        //Address-Length. Length of phone number.            
        //If the length of the phone number is odd (11), a trailing F has been added.
        if ((receiver.length() % 2) != 0)
        {
            receiver += "F";
        }
        data += CodeInteger(receiver.length());
        //Type-of-Address. (91 indicates international format of the phone number).
        data += CodeInteger(bInternational ? 0x91 : 0x81);
        //The phone number in semi octets.
        data += CodeString(receiver);
        //TP-PID. Protocol identifier 
        data += CodeInteger(0x00);

        //TP-DCS. Data coding scheme. This message is coded according to the 7bit default alphabet. 
        //Having "04" instead of "00" here, would indicate that the TP-User-Data field of this 
        //message should be interpreted as 8bit rather than 7bit (used in e.g. smart messaging, OTA provisioning etc). 
        String msg;
        if (type == MessageCodeType.Bits7)
        {
            data += CodeInteger(0x00);
            msg = Code7Bit(message);
        }
        else if (type == MessageCodeType.Bits8)
        {
            data += CodeInteger(0x04);
            msg = Code8Bit(message);
        }
        else if (type == MessageCodeType.Unicode)
        {
            data += CodeInteger(0x08);
            msg = CodeUnicode(message);
        }
        else
        {
            throw new IllegalArgumentException("Unknown message code type.");
        }
        //TP-Validity-Period. "AA" means 4 days. Note: This octet is optional, see bits 4 and 3 of the first octet 
        data += CodeInteger(0xAA);
        //TP-User-Data-Length. Length of message. The TP-DCS field indicated 7-bit data, so the length here is the number of septets (10). 
        data += CodeInteger(msg.length() / 2);
        //If the TP-DCS field were set to 8-bit data or Unicode, the length would be the number of octets. 
        data += msg;
        return data;
    }
    
    //Convert SMS char to Unicode char.
    private static int SMStoASCII(int ch, boolean[] escch)
    {
        if (escch[0])
        {
            ch = 0x1B << 8 | ch;
        }
        switch (ch)
        {
            case 0:
                ch = '@';
                break;
            case 1:
                ch = 0xA3;
                break;
            case 2:
                ch = '$';
                break;
            case 3:
                ch = 165;
                break;
            case 4:
                ch = 0xE8;
                break;
            case 5:
                ch = 0xE9;
                break;
            case 6:
                ch = 0xF9;
                break;
            case 7:
                ch = 0xEC;
                break;
            case 8:
                ch = 0xF2;
                break;
            case 9:
                ch = 0xC7;
                break;
            case 11:
                ch = 0xD8;
                break;
            case 12:
                ch = 0xF8;
                break;
            case 14:
                ch = 0xC5;
                break;
            case 15:
                ch = 0xE5;
                break;
            case 16:
                ch = 0x0394;
                break;
            case 17:
                ch = '_';
                break;
            case 18:
                ch = 0x03A6;
                break;
            case 19:
                ch = 0x0393;
                break;
            case 20:
                ch = 0x039B;
                break;
            case 21:
                ch = 0x03A9;
                break;
            case 22:
                ch = 0x03A0;
                break;
            case 23:
                ch = 0x03A8;
                break;
            case 24:
                ch = 0x03A3;
                break;
            case 25:
                ch = 0x0398;
                break;
            case 26:
                ch = 0x039E;
                break;
            case 28:
                ch = 0xC6;
                break;
            case 29:
                ch = 0xE6;
                break;
            case 30:
                ch = 0xDF;
                break;
            case 31:
                ch = 0xC9;
                break;
            case 36:
                ch = 0xA4;
                break;
            case 64:
                ch = 0xA1;
                break;
            case 91:
                ch = 0xC4;
                break;
            case 92:
                ch = 0xD6;
                break;
            case 93:
                ch = 0xD1;
                break;
            case 94:
                ch = 0xDC;
                break;
            case 95: //Section sign.
                ch = 167;
                break;
            case 96: //Inverted question mark.
                ch = 0x40;
                break;
            case 123:
                ch = 0xE4;
                break;
            case 124:
                ch = 0xF6;
                break;
            case 125:
                ch = 0xF1;
                break;
            case 126:
                ch = 0xFC;
                break;
            case 127:
                ch = 0xE0;
                break;
            case 0x1B0A: //FORM FEED
                ch = 12;
                break;
            case 0x1B14: //CIRCUMFLEX ACCENT ^
                ch = 94;
                break;
            case 0x1B28: //LEFT CURLY BRACKET {
                ch = 123;
                break;
            case 0x1B29: //RIGHT CURLY BRACKET }
                ch = 125;
                break;
            case 0x1B2F: //REVERSE SOLIDUS (BACKSLASH)
                ch = 92;
                break;
            case 0x1B3C: //LEFT SQUARE BRACKET [
                ch = 91;
                break;
            case 0x1B3D: //TILDE
                ch = 126;
                break;
            case 0x1B3E: //RIGHT SQUARE BRACKET ]
                ch = 93;
                break;
            case 0x1B40: //VERTICAL BAR |
                ch = 124;
                break;
            case 0x1B65: //EURO SIGN
                ch = 0x20AC;
                break;
        }
        escch[0] = ch == 27;
        if (escch[0])
        {
            return 0;
        }
        if ((ch & 0xFF00) == 0xFF00)
        {
            ch = ch & 0xFF;
        }
        return ch;
    }

    
    /**
     * Convert 7 bits data to the 8 bits data.
    */
    private static String decode7Bit(String data)
    {
        int[] bytes = new int[data.length() / 2];
        //Convert data to the byte array.
        for (int pos = 0; pos < bytes.length; ++pos)
        {
            bytes[pos] = getInteger(data, 2 * pos);
        }
        //Get data at 7 bits at the time
        int newData = 0;
        int off = 0;
        int ch = 0;
        int mask = 1;
        long chCnt = 0;
        boolean[] escch = new boolean[1];
        StringBuilder sb = new StringBuilder();
        for (int pos = 0; pos < bytes.length; ++pos)
        {
            newData = bytes[pos] << off;
            for (long bitpos = 0; bitpos < 8; ++bitpos)
            {
                if (mask == 0x80)
                {                        
                    int newCh = SMStoASCII(ch, escch);
                    if (newCh != 0)
                    {
                        sb.append((char)newCh);
                    }
                    off = ch = 0;
                    mask = 1;
                    newData = newData >> 7;
                    if (++chCnt == data.length())
                    {
                        break;
                    }
                }
                ch |= (newData & mask);
                mask = mask << 1;
                ++off;
            }
        }
        if (mask == 0x80)
        {
            int newCh = SMStoASCII(ch, escch);
            if (newCh != 0)
            {
                sb.append((char)newCh);
            }
        }
        return sb.toString();
    }

    /** 
     Convert hex string to string data.
    */
    private static String decode8Bit(String data)
    {
        StringBuilder sb = new StringBuilder();
        for (int pos = 0; pos != data.length(); pos += 2)
        {
            sb.append((char) getInteger(data, pos));
        }
        return sb.toString();
    }

    /** 
     Decode UNICODE data.
    */
    private static String decodeUnicode(String data)
    {
        StringBuilder sb = new StringBuilder();
        for (int pos = 0; pos != data.length(); pos += 4)
        {
            int value = getInteger(data, pos) << 8;
            value |= getInteger(data, pos + 2);
            sb.append((char) value);
        }
        return sb.toString();
    }
    
    static void encode(String data, GXSMSMessage msg)
    {
        if (data.length() < 1)
        {
            throw new IllegalArgumentException("Invalid data.");
        }
        int index = 0;
        // Service Center Number length
        int ServCenterlen = getInteger(data, index);
        index += 2;
        //Type-of-address of the SMSC.
        int SMSCType = getInteger(data, index);
        index += 2;
        //Get Service Center number            
        if (ServCenterlen > 0)
        {
            int sz = 2 * ServCenterlen - 1;
            if (data.length() < index + sz)
            {
                throw new IllegalArgumentException("Invalid data.");
            }
            String serviceCenterNumber = data.substring(index, index + sz);
            if (SMSCType == 0x91) //If international format.
            {
                serviceCenterNumber = "+" + serviceCenterNumber;
            }
            msg.setServiceCenterNumber(serviceCenterNumber);
            index += sz;
        }
        //First octet of the SMS-DELIVER PDU
        int SMSDeliver = getInteger(data, index);
        index += 2;
        //Length of the sender number
        int SenderLen = getInteger(data, index);
        index += 2;
        SenderLen += SenderLen % 2;
        //Type-of-address of the sender number.
        int SenderType = getInteger(data, index);
        index += 2;
        //Get Sender number            
        String phoneNumber = getString(data, index, SenderLen, true);
        if (SenderType == 0x91) //If international format.
        {
            phoneNumber = "+" + phoneNumber;
        }
        msg.setPhoneNumber(phoneNumber);
        index += SenderLen;
        //TP-PID. Protocol identifier.
        int ProtocolID = getInteger(data, index);
        index += 2;
        //TP-DCS Data coding scheme		
        MessageCodeType type = MessageCodeType.forValue(getInteger(data, index));
        msg.setCodeType(type);
        index += 2;
        int year = getInteger(data, index);
        index += 2;
        if (year != 0xAA)
        {
            year += 2000;
            int month = getInteger(data, index);
            index += 2;
            int day = getInteger(data, index);
            index += 2;
            int hour = getInteger(data, index);
            index += 2;
            int minute = getInteger(data, index);
            index += 2;
            int second = getInteger(data, index);
            index += 2;
            int timezone = getInteger(data, index);
            index += 2;
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.set(year, month, day, hour, minute, second);            
            msg.setTime(calendar.getTime());          
        }
        //TP-UDL. User data length, length of message. 
        // The TP-DCS field indicated 7-bit data, so the length here is the number of septets (10). 
        // If the TP-DCS field were set to indicate 8-bit data or Unicode, the length would be the number of octets (9). 
        int DataLen = getInteger(data, index);
        index += 2;
        String buff = getString(data, index, 2 * DataLen, false); //data.Substring(index, DataLen);
        index += DataLen;
        //TP-UD. Message 8-bit octets representing 7-bit data.
        //When DataCodingScheme = 0, PDU code is coded from 7bit charactor (see GSM 03.38).
        if (type == MessageCodeType.Bits7)
        {
            msg.setData(decode7Bit(buff));
        }
        // When DataCodingScheme = 4, PDU code is coded using 8 bits codec...
        else if (type == MessageCodeType.Bits8)
        {
            msg.setData(decode8Bit(buff));
        }
        // When DataCodingScheme = 8, PDU code is coded from Unicode charactor (see GSM 03.38).
        else if (type == MessageCodeType.Unicode)
        {
            msg.setData(decodeUnicode(buff));
        }
        else
        {
            throw new IllegalArgumentException("Invalid data coding scheme");
        }
    }
}