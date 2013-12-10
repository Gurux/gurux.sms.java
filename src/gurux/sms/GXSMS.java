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

import gurux.common.*;
import gurux.io.*;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/** 
 The GXSMS component determines methods that make 
 * the communication possible using serial port connection. 
*/
public class GXSMS implements IGXMedia
{   
    //Values are saved if port is not open and user try to set them.
    int m_BaudRate = 9600;
    int m_DataBits = 8;    
    StopBits m_StopBits = StopBits.ONE;
    Parity m_Parity = Parity.NONE;       

    int m_ConnectionWaitTime = 3000;
    String m_PIN;
    boolean m_Server;
    String[] m_InitializeCommands;
    String m_PhoneNumber;
    long m_Closing = 0;
    int m_WriteTimeout;
    int m_ReadTimeout;
    private static boolean Initialized;
    int m_ReadBufferSize;
    GXReceiveThread Receiver;
    private SMSReceiveThread m_SMSReceiver;
    int m_SMSCheckInterval;
    private boolean m_SupportDirectSend;
    int m_hWnd;
    String m_PortName;    
    GXSynchronousMediaBase m_syncBase;
    public long m_BytesReceived = 0;
    private long m_BytesSend = 0;
    private int m_Synchronous = 0;
    private TraceLevel m_Trace = TraceLevel.OFF;
    private Object privateEop;        
    private int ConfigurableSettings;
    private List<IGXMediaListener> MediaListeners = new ArrayList<IGXMediaListener>();
    private final Object m_baseLock = new Object();
    private boolean privateAutoDelete;
    private MemoryType privateMemory = MemoryType.values()[0];
    private int m_MessagesSent;    
    private int m_MessagesReceived;

    /** 
     Constructor.
    */
    public GXSMS()
    {   
        m_PhoneNumber = "";
        initialize();
        m_ReadBufferSize = 256;        
        m_syncBase = new GXSynchronousMediaBase(m_ReadBufferSize);
        setConfigurableSettings(AvailableMediaSettings.All.getValue());
    }
    
    static void initialize()
    {
        if (!Initialized)
        {
            try
            {
                System.loadLibrary("gurux.serial.java");
            }
            catch(java.lang.UnsatisfiedLinkError ex)
            {
                throw new RuntimeException("Failed to locate gurux.serial.java.dll");            
            }
            Initialized = true;        
        }
    }
    
    /** 
    Gets an array of serial port names for the current computer.

    @return 
    */
    public static String[] getPortNames()
    {                
        initialize();
        return NativeCode.getPortNames();
    }

    /*
     * Get baud rates supported by given serial port.
     */
    public static int[] getAvailableBaudRates(String portName)
    {                
        return new int[]{300, 600, 1800, 2400, 4800, 9600, 19200, 38400};
    }
    
    
    /** 
     Destructor.
    */
    @Override
    @SuppressWarnings("FinalizeDeclaration")    
    protected void finalize() throws Throwable
    {
        super.finalize();
        if (isOpen())
        {
            close();
        }
    }    

    /** 
     What level of tracing is used.
    */
    @Override
    public final TraceLevel getTrace()
    {
        return m_Trace;
    }
    @Override
    public final void setTrace(TraceLevel value)
    {
        m_Trace = m_syncBase.Trace = value;
    }
    
    private void NotifyPropertyChanged(String info)
    {
        for (IGXMediaListener listener : MediaListeners) 
        {
            listener.onPropertyChanged(this, new PropertyChangedEventArgs(info));
        }
    }         

    void notifyError(RuntimeException ex)
    {
        for (IGXMediaListener listener : MediaListeners) 
        {
            listener.onError(this, ex);
            if (m_Trace.ordinal() >= TraceLevel.ERROR.ordinal())
            {
                listener.onTrace(this, new TraceEventArgs(TraceTypes.ERROR, ex));
            }
        }
    }
    
    void notifyReceived(ReceiveEventArgs e)
    {
        for (IGXMediaListener listener : MediaListeners) 
        {
            listener.onReceived(this, e);
        }
    }
    
    void notifyTrace(TraceEventArgs e)
    {
        for (IGXMediaListener listener : MediaListeners) 
        {                
            listener.onTrace(this, e);
        }
    }
       
    /** <inheritdoc cref="IGXMedia.ConfigurableSettings"/>
    */
    @Override
    public final int getConfigurableSettings()
    {            
        return ConfigurableSettings;
    }
    @Override
    public final void setConfigurableSettings(int value)
    {
        this.ConfigurableSettings = value;
    }

    @Override 
    public boolean properties(javax.swing.JFrame parent)
    {
        GXSettings dlg = new GXSettings(parent, true, this);
        dlg.pack();
        dlg.setVisible(true);    
        return dlg.Accepted;
    }
    
    /**    
     Displays the copyright of the control, user license, and version information, in a dialog box. 
    */
    public final void aboutBox()
    {
        throw new UnsupportedOperationException();
    }

    /** 
     Sends data asynchronously. <br/>
     No reply from the receiver, whether or not the operation was successful, is expected.

     @param data Data to send to the device.
     @param receiver Not used.
     Reply data is received through OnReceived event.<br/>     		
     @see OnReceived OnReceived
     @see Open Open
     @see Close Close 
    */    
    public final void send(GXSMSMessage msg)
    {
        send(msg, null);
    }
    
    /** 
     Sends data asynchronously. <br/>
     No reply from the receiver, whether or not the operation was successful, is expected.

     @param data Data to send to the device.
     @param receiver Not used.
     Reply data is received through OnReceived event.<br/>     		
     @see OnReceived OnReceived
     @see Open Open
     @see Close Close 
    */
    @Override
    public final void send(Object data, String receiver)
    {
        if (data instanceof GXSMSMessage)
        {
            GXSMSMessage msg = (GXSMSMessage) data;
            synchronized (m_baseLock)
            {
                if (m_Trace == TraceLevel.VERBOSE)
                {
                    notifyTrace(new TraceEventArgs(TraceTypes.SENT, 
                            msg.getPhoneNumber() + " : " + msg.getData()));
                }
                //Reset last position if Eop is used.
                synchronized (m_syncBase.m_ReceivedSync)
                {
                    m_syncBase.m_LastPosition = 0;
                }
                //Use default phone number if new is not set.
                String number = m_PhoneNumber;                    
                if (msg.getPhoneNumber() != null && 
                        !msg.getPhoneNumber().equals(""))
                {
                    number = msg.getPhoneNumber();
                }
                if (number == null || number.equals(""))
                {
                    throw new IllegalArgumentException("Invalid phone number.");
                }
                sendMessage(msg.getData(), number, msg.getCodeType());                    
            }
        }
        else if (data instanceof String)
        {
            if (receiver == null || receiver.equals(""))
            {
                throw new IllegalArgumentException("Invalid receiver phone number.");
            }
            GXSMSMessage msg = new GXSMSMessage();
            msg.setData(data.toString());
            msg.setPhoneNumber(receiver);
            send(msg, null);
        }
        else
        {
            throw new IllegalArgumentException("Invalid data to send");
        }
    }

    private void NotifyMediaStateChange(MediaState state)
    {
        for (IGXMediaListener listener : MediaListeners) 
        {                
            if (m_Trace.ordinal() >= TraceLevel.ERROR.ordinal())
            {
                listener.onTrace(this, new TraceEventArgs(TraceTypes.INFO, state));
            }
            listener.onMediaStateChange(this, new MediaStateEventArgs(state));
        }
    }
  
    private String getError(String reply)
    {
        int pos = reply.indexOf("ERROR:");
        if (pos != -1)
        {
            return reply.substring(pos + 6).trim();
        }
        return reply.trim();
    }
    
    /*
     * Open serial port and call to phone number.
     */
    @Override    
    public final void open() throws Exception
    {
        close();
        try
        {         
            if (m_PortName == null || m_PortName == "")
            {
                throw new IllegalArgumentException("Serial port is not selected.");
            }
            synchronized (m_syncBase.m_ReceivedSync)
            {
                m_syncBase.m_LastPosition = 0;
            }
            NotifyMediaStateChange(MediaState.OPENING);
            if (m_Trace.ordinal() >= TraceLevel.INFO.ordinal())
            {
                String eop = "None";
                if (getEop() instanceof byte[])
                {
                }
                else if (getEop() != null)
                {
                    eop = getEop().toString();
                }
                notifyTrace(new TraceEventArgs(TraceTypes.INFO, "Settings: Port: " + this.getPortName() + " Baud Rate: " + getBaudRate() + " Data Bits: " + (new Integer(getDataBits())).toString() + " Parity: " + getParity().toString() + " Stop Bits: " + getStopBits().toString()));
            }             
            long tmp[] = new long[1];
            m_hWnd = NativeCode.openSerialPort(m_PortName, tmp);            
            //If user has change values before open.
            if (m_BaudRate != 9600)
            {
                setBaudRate(m_BaudRate);
            }
            if (m_DataBits != 8)
            {
                setDataBits(m_DataBits);
            }
            if (m_Parity != Parity.NONE)
            {
                setParity(m_Parity);
            }
            if (m_StopBits != StopBits.ONE)
            {
                setStopBits(m_StopBits);
            }                               
            m_Closing = tmp[0];
            Receiver = new GXReceiveThread(this, m_hWnd);
            setRtsEnable(true);
            setDtrEnable(true);            
            Receiver.start();            
            Thread.sleep(100);
            try
            {
                //Send AT
                synchronized (m_baseLock)
                {
                    if (getInitializeCommands() != null)
                    {
                        for (String it : getInitializeCommands())
                        {
                            sendCommand(it + "\r\n", true);
                        }
                    }
                    String reply;
                    //Send AT
                    reply = sendCommand("AT\r", false);
                    if (reply.compareToIgnoreCase("OK") != 0)
                    {
                        reply = sendCommand("AT\r", false);
                        if (reply.compareToIgnoreCase("OK") != 0)
                        {
                            reply = sendCommand("+++", "+++", true);
                            if (reply.equals(""))
                            {
                                throw new RuntimeException("Invalid reply.");
                            }
                            reply = sendCommand("AT\r", true);
                            if (reply.compareToIgnoreCase("OK") != 0)
                            {
                                throw new RuntimeException("Invalid reply.");
                            }
                        }
                    }
                    //Enable error reporting. It's OK if this fails.
                    sendCommand("AT+CMEE\r", false);
                    //Enable verbode error code,
                    reply = sendCommand("AT+CMEE=2\r", false);
                    if (!reply.equals("OK"))
                    {
                        //Enable numeric error codes
                        sendCommand("AT+CMEE=1\r", false);
                    }
                    reply = sendCommand("AT+CPIN=?\r", false);
                    boolean pinSupported = reply.equals("OK");
                    //Is PIN Code supported.		
                    if (pinSupported)
                    {
                        //Check PIN-Code
                        reply = sendCommand("AT+CPIN?\r", false);
                        if (reply.contains("ERROR:"))
                        {
                            throw new RuntimeException("Failed to read PIN code.\r\n" + getError(reply));
                        }
                        //If PIN code is needed.
                        if (!reply.equals("+CPIN: READY"))
                        {
                            if (m_PIN == null || m_PIN.equals(""))
                            {
                                throw new RuntimeException("PIN is needed.");
                            }
                            reply = sendCommand(String.format("AT+CPIN=\"%1$s\"\r", m_PIN), false);
                            if (!reply.equals("OK"))
                            {
                                throw new RuntimeException("Failed to set PIN code." + getError(reply));
                            }
                            //Ask PIN Code again.
                            reply = sendCommand("AT+CPIN?\r", false);
                            if (!reply.equals("OK"))
                            {
                                throw new RuntimeException("Failed to set PIN code." + getError(reply));
                            }
                        }
                    }
                    //Is direct SMS sending supported.
                    reply = sendCommand("at+cmgs=?\r", false);
                    m_SupportDirectSend = reply.contains("OK");
                    //Start SMS checker.
                    if (m_SMSCheckInterval != 0)
                    {
                        m_SMSReceiver = new SMSReceiveThread(this);
                        m_SMSReceiver.start();
                    }
                }
            }
            catch (RuntimeException ex)
            {
                close();
                throw ex;
            }
            NotifyMediaStateChange(MediaState.OPEN);
        }
        catch (Exception ex)
        {
            close();
            throw ex;
        }        
    }
  
    private void sendBytes(byte[] value)
    {
        synchronized (m_baseLock)
        {
            if (m_Trace == TraceLevel.VERBOSE)
            {
                notifyTrace(new TraceEventArgs(TraceTypes.SENT, value));
            }
            m_BytesSend += value.length;
            //Reset last position if Eop is used.
            synchronized (m_syncBase.m_ReceivedSync)
            {
                m_syncBase.m_LastPosition = 0;
            }
            NativeCode.write(m_hWnd, value, m_WriteTimeout);
        }
    }

    private String sendCommand(String cmd, boolean throwError)
    {
        return sendCommand(cmd, null, throwError);
    }

    private String sendCommand(String cmd, String eop, boolean throwError)
    {
        ReceiveParameters<String> p = new ReceiveParameters<String>(String.class);        
        p.setWaitTime(m_ConnectionWaitTime);
        p.setEop(eop != null ? eop : "\r\n");
        if (p.getEop().equals(""))
        {
            p.setEop(null);
            p.setCount(cmd.length());
        }
        try 
        {
            sendBytes(cmd.getBytes("ASCII"));
        } 
        catch (UnsupportedEncodingException ex) 
        {
            throw new RuntimeException(ex.getMessage());
        }
        StringBuilder sb = new StringBuilder();
        int index = -1;
        String reply = "";
        while (index == -1)
        {
            if (!receive(p))
            {
                if (throwError)
                {
                    throw new RuntimeException("Failed to receive answer from the modem. Check serial port.");
                }
                return "";
            }
            sb.append(p.getReply());
            reply = sb.toString();
            //Remove echo.                
            if (sb.length() >= cmd.length() && reply.startsWith(cmd))
            {
                sb.delete(0, cmd.length());
                reply = sb.toString();
                //Remove echo and return if we are not expecting reply.
                if (eop != null && eop.equals(""))
                {
                    return "";
                }
            }
            if (eop != null)
            {
                index = reply.lastIndexOf(eop);
            }
            else if (reply.length() > 5)
            {
                index = reply.lastIndexOf("\r\nOK\r\n");
                if (index == -1)
                {
                    index = reply.lastIndexOf("ERROR:");
                }
                //If there is a message before OK show it.
                else if (index != 0)
                {
                    reply = reply.substring(0, index);
                    index = 0;
                }
            }
            p.setReply(null);
        }
        if (index != 0 & eop == null)
        {
            reply = reply.substring(0, 0) + reply.substring(0 + index);
        }
        reply = reply.trim();
        return reply;
    }
    
    /** 
     * <inheritdoc cref="IGXMedia.Close"/>        
    */    
    @Override       
    public final void close()
    {       
        if (m_hWnd != 0)
        {                
            try
            {
                NotifyMediaStateChange(MediaState.CLOSING);
                if (m_SMSReceiver != null)
                {
                    Receiver.interrupt();                    
                    Receiver = null;
                }
            }
            catch (RuntimeException ex)
            {
                notifyError(ex);
                throw ex;
            }
            finally
            {                 
                if (Receiver != null)
                {
                    Receiver.interrupt();                    
                    Receiver = null;
                }                
                try
                {
                    NativeCode.closeSerialPort(m_hWnd, m_Closing);
                }
                catch (java.lang.Exception e)
                {
                    //Ignore all errors on close.                    
                }
                m_hWnd = 0;    
                NotifyMediaStateChange(MediaState.CLOSED);
                m_BytesSend = m_BytesReceived = 0;
                m_syncBase.m_ReceivedSize = 0;
            }
        }                
    }

    /** 
    Used baud rate for communication.

    Can be changed without disconnecting.
  */ 
    public final int getBaudRate()
    {
        if (m_hWnd == 0)
        {
            return m_BaudRate;        
        }
        return NativeCode.getBaudRate(m_hWnd);
    }
    public final void setBaudRate(int value)
    {       
        boolean change = getBaudRate() != value;            
        if (change)
        {
            if (m_hWnd == 0)
            {
                m_BaudRate = value;        
            }
            else
            {
                NativeCode.setBaudRate(m_hWnd, value);
            }
            NotifyPropertyChanged("BaudRate");
        }
    }
        
     /** 
    Gets or sets the phone number.    
  */ 
    public final String getPhoneNumber()
    {
        return m_PhoneNumber;
    }
    public final void setPhoneNumber(String value)
    {       
        boolean change = m_PhoneNumber == null || m_PhoneNumber.equalsIgnoreCase(value);
        m_PhoneNumber = value;
        if (change)
        {
            NotifyPropertyChanged("BaudRate");
        }
    }
    
    /** 
     PIN Code.
    */    
    public final String getPINCode()
    {
        return m_PIN;
    }
    public final void setPINCode(String value)
    {
        m_PIN = value;
    }

    /*
     * Get or set how long (ms) modem answer is waited when connection is made.
     */
    public final int getConnectionWaitTime()
    {
        return m_ConnectionWaitTime;
    }
    
    public final void setConnectionWaitTime(int value)
    {       
        boolean change = m_ConnectionWaitTime != value;
        m_ConnectionWaitTime = value;
        if (change)
        {
            NotifyPropertyChanged("ConnectionWaitTime");
        }
    }    
    
    /** 
     True if the port is in a break state; otherwise, false.
    */
    public final boolean getBreakState()
    {
        return NativeCode.getBreakState(m_hWnd);
    }
    public final void setBreakState(boolean value)
    {
        boolean change;
        change = getBreakState() != value;
        if (change)
        {
            NativeCode.setBreakState(m_hWnd, value);
            NotifyPropertyChanged("BreakState");
        }
    }

    /* 
     * Gets the number of bytes in the receive buffer.
    */
    public final int getBytesToRead()
    {
        return NativeCode.getBytesToRead(m_hWnd);
    }

    /* 
     * Gets the number of bytes in the send buffer.
     */ 
    public final int getBytesToWrite()
    {
        return NativeCode.getBytesToWrite(m_hWnd);
    }

    /* 
    * Gets the state of the Carrier Detect line for the port.
    */
    public final boolean getCDHolding()
    {
        return NativeCode.getCDHolding(m_hWnd);
    }

    /* 
     * Gets the state of the Clear-to-Send line.
     */    
    public final boolean getCtsHolding()
    {
        return NativeCode.getCtsHolding(m_hWnd);
    }
   
    /** 
     * Gets or sets the standard length of data bits per byte.   
     */
    public final int getDataBits()
    {
        if (m_hWnd == 0)
        {
            return m_DataBits;
        }
        return NativeCode.getDataBits(m_hWnd);
    }

    public final void setDataBits(int value)
    {
        boolean change;
        change = getDataBits() != value;        
        if (change)
        {
            if (m_hWnd == 0)
            {
                m_DataBits = value;
            }
            else
            {
                NativeCode.setDataBits(m_hWnd, value);
            }
            NotifyPropertyChanged("DataBits");
        }
    }
    
        /** 
         Gets the state of the Data Set Ready (DSR) signal.
        */
        public final boolean getDsrHolding()
        {
            return NativeCode.getDsrHolding(m_hWnd);
        }

        /* 
         * Gets or sets a value that enables the Data Terminal Ready 
         * (DTR) signal during serial communication.        
        */
        public final boolean getDtrEnable()
        {
            return NativeCode.getDtrEnable(m_hWnd);
        }
        public final void setDtrEnable(boolean value)
        {
            boolean change;
            change = getDtrEnable() != value;
            NativeCode.setDtrEnable(m_hWnd, value);
            if (change)
            {                
                NotifyPropertyChanged("DtrEnable");
            }
        }
        
        /* 
         * Gets or sets the handshaking protocol for serial port transmission of data.
        */
        public final Handshake getHandshake()
        {
            return Handshake.values()[NativeCode.getHandshake(m_hWnd)];
        }
        public final void setHandshake(Handshake value)
        {
            boolean change;
            change = getHandshake() != value;
            if (change)
            {
                NativeCode.setHandshake(m_hWnd, value.ordinal());
                NotifyPropertyChanged("Handshake");
            }
        }

    /** <inheritdoc cref="IGXMedia.IsOpen"/>
     <seealso char="Connect">Open
     <seealso char="Close">Close
    */
    @Override
    public final boolean isOpen()
    {
        return m_hWnd != 0;
    }
    
    /** 
     * Gets or sets the parity-checking protocol.
     */
    public final Parity getParity()
    {
        if (m_hWnd == 0)
        {
            return m_Parity;
        }
        return Parity.values()[NativeCode.getParity(m_hWnd)];
    }

    public final void setParity(Parity value)
    {        
        boolean change;
        change = getParity() != value;        
        if (change)
        {
            if (m_hWnd == 0)
            {
                m_Parity = value;
            }
            else
            {
                NativeCode.setParity(m_hWnd, value.ordinal());
            }
            NotifyPropertyChanged("Parity");
        }
    }

    /** 
     Gets or sets the port for communications, including but not limited to all available COM ports.
    */
    public final String getPortName()
    {
        return m_PortName;
    }
                
    public final void setPortName(String value)
    {
        boolean change;
        change = !value.equals(m_PortName);
        m_PortName = value;
        if (change)
        {
            NotifyPropertyChanged("PortName");
        }
    }

    /*
     * Gets or sets the size of the System.IO.Ports.SerialPort input buffer.
     */ 
    public final int getReadBufferSize()
    {
        return m_ReadBufferSize;
    }
    public final void setReadBufferSize(int value)
    {
        boolean change;
        change = getReadBufferSize() != value;
        if (change)
        {
            m_ReadBufferSize = value;
            NotifyPropertyChanged("ReadBufferSize");
        }
    }

    /* 
    * Gets or sets the number of milliseconds before a time-out occurs when a read operation does not finish.
    */ 
    public final int getReadTimeout()
    {
        return m_ReadTimeout;
    }
    public final void setReadTimeout(int value)
    {
        boolean change = m_ReadTimeout != value;
        m_ReadTimeout = value;
        if (change)
        {
            NotifyPropertyChanged("ReadTimeout");
        }
    }
    
    /* 
     * Gets or sets a value indicating whether the 
     * Request to Send (RTS) signal is enabled during serial communication.
    */
    public final boolean getRtsEnable()
    {
        return NativeCode.getRtsEnable(m_hWnd);
    }
    public final void setRtsEnable(boolean value)
    {
        boolean change;
        change = getRtsEnable() != value;
        NativeCode.setRtsEnable(m_hWnd, value);
        if (change)
        {            
            NotifyPropertyChanged("RtsEnable");
        }
    }

    /** 
     Gets or sets the standard number of stopbits per byte.    
    */
     public final StopBits getStopBits()
    {
        if (m_hWnd == 0)
        {
            return m_StopBits;
        }
        return StopBits.values()[NativeCode.getStopBits(m_hWnd)];
    }
    public final void setStopBits(StopBits value)
    {
        boolean change;
        change = getStopBits() != value;
        if (change)
        {
            if (m_hWnd == 0)
            {
                m_StopBits = value;
            }
            else
            {
                NativeCode.setStopBits(m_hWnd, value.ordinal());
            }
            NotifyPropertyChanged("StopBits");
        }
    }

    /* 
     * Gets or sets the number of milliseconds before a time-out 
     * occurs when a write operation does not finish.
     */
    public final int getWriteTimeout()
    {
        return m_WriteTimeout;
    }
    public final void setWriteTimeout(int value)
    {
        boolean change = m_WriteTimeout != value;
        if (change)
        {
            m_WriteTimeout = value;
            NotifyPropertyChanged("WriteTimeout");
        }
    }
    
    private void sendMessage(String message, String receiver, MessageCodeType type)
    {
        if (receiver == null || receiver.equals(""))
        {
            throw new IllegalArgumentException("Invalid receiver");
        }
        //Remove spaces.
        receiver = receiver.replace(" ", "").replace("-", "").replace("(", "").replace(")", "").trim();
        //Send EOF
        sendBytes(new byte[] {26});
        //Code PDU.
        String data = GXSMSPdu.Code(receiver, message, type);
        long len = (data.length() / 2) - 1;
        String cmd;
        if (!m_SupportDirectSend) //Save SMS before send.
        {
            cmd = String.format("AT+CMGW=%1$s\r", len);
        }
        else
        {
            cmd = String.format("AT+CMGS=%1$s\r", len);
        }
        String reply = sendCommand(cmd, new String(new char[] {(char)0x20}), false);
        if (!reply.equals(">"))
        {
            throw new RuntimeException("Short message send failed.");
        }
        reply = sendCommand(data, "", false);        
        reply = sendCommand(new String(new char[]{26}), false);
        if (!reply.startsWith("+CMGW:"))
        {
            throw new RuntimeException("Short message send failed.\r\n" + getError(reply));
        }
    }

    @Override
    public final <T> boolean receive(ReceiveParameters<T> args)
    {
        return m_syncBase.receive(args);
    }

    /** 
     Sent byte count.

     @see BytesReceived BytesReceived
     @see ResetByteCounters ResetByteCounters
    */
    @Override
    public final long getBytesSent()
    {
        return m_BytesSend;
    }

    /** 
     Received byte count.

     @see BytesSent BytesSent
     @see ResetByteCounters ResetByteCounters
    */
    @Override
    public final long getBytesReceived()
    {
        return m_BytesReceived;
    }

    /** 
     Resets BytesReceived and BytesSent counters.

     @see BytesSent BytesSent
     @see BytesReceived BytesReceived
    */
    @Override
    public final void resetByteCounters()
    {
        m_BytesSend = m_BytesReceived = 0;
    }
   
    /** 
     Media settings as a XML string.
    */
    @Override
    public final String getSettings()
    {        
        return null;
        //TODO:
    }
    
    @Override
    public final void setSettings(String value)
    {   
        //TODO:
    }
    
    @Override
    public final void copy(Object target)
    {
        GXSMS tmp = (GXSMS)target;
        setPortName(tmp.getPortName());
        setBaudRate(tmp.getBaudRate()); 
        setStopBits(tmp.getStopBits());
        setParity(tmp.getParity());
        setDataBits(tmp.getDataBits());
    }

    @Override
    public String getName()
    {
        return getPortName();
    }

    @Override
    public String getMediaType()
    {
        return "SMS";
    }

    /** <inheritdoc cref="IGXMedia.Synchronous"/>
    */
    @Override
    public final Object getSynchronous()
    {
        synchronized (this)
        {
            int[] tmp = new int[]{m_Synchronous};
            GXSync obj = new GXSync(tmp);
            m_Synchronous = tmp[0];
            return obj;
        }
    }

    /** <inheritdoc cref="IGXMedia.IsSynchronous"/>
    */
    @Override
    public final boolean getIsSynchronous()
    {
        synchronized (this)
        {
            return m_Synchronous != 0;
        }
    }

    /** <inheritdoc cref="IGXMedia.ResetSynchronousBuffer"/>
    */
    @Override
    public final void resetSynchronousBuffer()
    {
        synchronized (m_syncBase.m_ReceivedSync)
        {
            m_syncBase.m_ReceivedSize = 0;
        }
    }

    /** <inheritdoc cref="IGXMedia.Validate"/>
    */
    @Override
    public final void validate()
    {
        if (getPortName() == null || getPortName().length() == 0)
        {
            throw new RuntimeException("Invalid port name.");
        }
    }    

    /*
     * Get or set modem initial settings.
     */
    public String[] getInitializeCommands()
    {
        return m_InitializeCommands;
    }
    
    public final void setInitializeCommands(String[] value)
    {
        m_InitializeCommands = value;
    }
        
    /*
     * Eop is not used.
     */
    @Override
    public final Object getEop()
    {
        return privateEop;
    }

    @Override
    public final void setEop(Object value)
    {
        privateEop = value;
    }

    @Override
    public void addListener(IGXMediaListener listener) 
    {        
        MediaListeners.add(listener);       
    }

    @Override
    public void removeListener(IGXMediaListener listener) 
    {
        MediaListeners.remove(listener);
    }      
    
    /** 
     Test is modem available
    */
    public final boolean test()
    {
        try
        {
            if (sendCommand("AT\r", false).equalsIgnoreCase("OK"))
            {
                return true;
            }
            return false;
        }
        catch (RuntimeException ex)
        {
            return false;
        }
    }

    /** 
     Are messages removed after read from the SIM or phone memory.

     Default is false.

    */
    public final boolean getAutoDelete()
    {
        return privateAutoDelete;
    }
    public final void setAutoDelete(boolean value)
    {
        privateAutoDelete = value;
    }

    /** 
     Used memory type. Default value is MemoryType.Sim.

     Default is false.

    */

    public final MemoryType getMemory()
    {
        return privateMemory;
    }
    public final void setMemory(MemoryType value)
    {
        privateMemory = value;
    }

    private String deleteMessage(int index)
    {
        String reply = sendCommand(String.format("AT+CMGD=%1$s\r", index), false);
        return reply;
    }

    /** 
     Delete SMS from the selected index.

     @return 
    */
    public final void delete(int index)
    {
        String reply = deleteMessage(index);
        if (!reply.equals("OK"))
        {
            throw new RuntimeException(String.format("Delete failed from index %1$s.\r\n%2$s", index, reply));
        }
    }

    /** 
     Delete ALL SMS from the phone.

     @return 
    */
    public final void deleteAll()
    {
        int[] count = new int[1];
        int[] maximum = new int[1];
        getMemoryCapacity(count, maximum);
        //If there are no messages to remove.
        if (count[0] == 0)
        {
            return;
        }
        synchronized (m_baseLock)
        {
            for (int pos = 1; pos != maximum[0] + 1; ++pos)
            {
                String reply = deleteMessage(pos);
                if (!reply.equals("OK"))
                {
                    throw new RuntimeException(String.format("Delete failed from index %1$s.\r\n%2$s", pos, reply));
                }
            }
        }
    }

    /** 
     Read all messages from the selected memory.

     @return 
    */
    public final GXSMSMessage[] read()
    {
        int[] count = new int[1];
        int[] maximum = new int[1];
        getMemoryCapacity(count, maximum);
        //If there are no messages to read.
        if (count[0] == 0)
        {
            return new GXSMSMessage[0];
        }
        java.util.ArrayList<GXSMSMessage> messages = new java.util.ArrayList<GXSMSMessage>();
        synchronized (m_baseLock)
        {
            for (int pos = 1; pos != maximum[0] + 1; ++pos)
            {
                String reply = sendCommand(String.format("AT+CMGR=%1$s\r", pos), false);
                if (reply.startsWith("+CMGR:"))
                {
                    reply = reply.substring(0, 0) + reply.substring(0 + 6);
                    String[] tmp = reply.split("[,]", -1);
                    GXSMSMessage msg = new GXSMSMessage();
                    msg.setIndex(pos);
                    String status = tmp[0].replace("\"", "").trim();
                    if (status.startsWith("STO"))
                    {
                        if (status.contains("UNSENT"))
                        {
                            msg.setStatus(MessageStatus.Unsent);
                        }
                        else if (status.contains("UNREAD"))
                        {
                            msg.setStatus(MessageStatus.Unread);
                        }
                        else if (status.contains("Read"))
                        {
                            msg.setStatus(MessageStatus.Read);
                        }
                        else if (status.contains("Sent"))
                        {
                            msg.setStatus(MessageStatus.Sent);
                        }
                    }
                    else
                    {
                        msg.setStatus(MessageStatus.forValue(Integer.parseInt(status)));
                    }
                    //If this is not a empty message
                    if (tmp.length != 1)
                    {                     
                        String[] m = tmp[2].split("\r\n");
                        if (m.length != 2)
                        {
                            continue;
                        }
                        GXSMSPdu.encode(m[1], msg);
                        //If this message is not read yet.
                        if (msg.getStatus() == MessageStatus.Unread && msg.getPhoneNumber().equals(""))
                        {
                            continue;
                        }
                    }
                    messages.add(msg);
                    //If all messages are read.
                    if (messages.size() == count[0])
                    {
                        break;
                    }  
                }
            }
        }
        return messages.toArray(new GXSMSMessage[0]);
    }

    /** 
     Returns used and maximum SMS capacity.

     @param count SMS in memory
     @param maximum Maximum SMS count.
     @return 
    */
    public final void getMemoryCapacity(int[] count, int[] maximum)
    {
        if (count == null || count.length != 1)
        {
            {
                throw new IllegalArgumentException("count is invalid.");
            }
        }
        if (maximum == null || maximum.length != 1)
        {
            throw new IllegalArgumentException("maximum is invalid.");
        }
        synchronized (m_baseLock)
        {
            String reply = sendCommand("AT+CPMS?\r", false);
            //for read/delete", "for write/send" , "for receive
            //"SM": SIM message store
            //"ME": ME message store
            //"MT": any of the storages associated with ME
            if (reply.startsWith("ERROR:"))
            {
                throw new RuntimeException("ReadSMSCapacity failed.\r\n" + getError(reply));
            }
            int ret = reply.lastIndexOf("\"SM\",");
            if (ret == -1)
            {
                throw new RuntimeException("ReadSMSCapacity failed.");
            }
            String[] results = reply.split(",");
            if (results.length != 9)
            {
                throw new RuntimeException("ReadSMSCapacity failed.");
            }
            count[0] = Integer.parseInt(results[1]);
            maximum[0] = Integer.parseInt(results[2]);
        }
    }

    /** 
     Returns network state.

     @return 
    */
    public final NetworkState getNetworkState()
    {
        synchronized (m_baseLock)
        {
            String reply = sendCommand("AT+CREG=?\r", false);
            if (reply.startsWith("ERROR"))
            {
                return NetworkState.Denied;
            }
            reply = sendCommand("AT+CREG?\r", false);
            if (reply.startsWith("ERROR:"))
            {
                throw new RuntimeException("GetNetworkState failed:\r\n" + getError(reply));
            }

            int ret = reply.lastIndexOf("+CREG:");
            if (ret == -1)
            {
                throw new RuntimeException("GetNetworkState failed.");
            }
            String[] results = reply.split(",|:");
            if (results.length != 3)
            {
                throw new RuntimeException("GetSignalQuality failed.");
            }
            return NetworkState.forValue(Integer.parseInt(results[2]));
        }
    }

    /** 
     Returns received signal strength indication (RSSI) and channel bit error rate (BER).

     @param rssi Received Signal Strength Indication
     @param ber Bit Error Rate
    */
    public final void getSignalQuality(int[] rssi, int[] ber)
    {
        if (rssi == null || rssi.length != 1)
        {
            throw new IllegalArgumentException("rssi is invalid.");
        }
        if (ber == null || ber.length != 1)
        {
            throw new IllegalArgumentException("ber is invalid.");
        }
        synchronized (m_baseLock)
        {
            //If modem don't support this.
            String reply = sendCommand("AT+CSQ=?\r", false);
            if (reply.startsWith("ERROR"))
            {
                return;
            }
            reply = sendCommand("AT+CSQ\r", false);
            if (reply.contains("ERROR:"))
            {
                throw new RuntimeException("GetSignalQuality failed:\r\n" + getError(reply));
            }

            int ret = reply.lastIndexOf("CSQ:");
            if (ret == -1)
            {
                throw new RuntimeException("GetSignalQuality failed.");
            }
            String[] results = reply.split(",|:");
            if (results.length != 3)
            {
                throw new RuntimeException("GetSignalQuality failed.");
            }
            rssi[0] = Integer.parseInt(results[1].trim());
            ber[0] = Integer.parseInt(results[2].trim());
        }
    }

    /** 
     Returns battery charge level and avarage power consumption.


     Battery capacity 0, 20, 40, 60, 80, 100 percent of remaining capacity (6 steps)
     0 indicates that either the battery is exhausted or the capacity value is not available.
     Average power consumption i mA.

     @param batteryCapacity
     @param averagePowerConsumption
    */
    public final void getBatteryCharge(int[] batteryCapacity, int[] averagePowerConsumption)
    {            
        if (batteryCapacity == null || batteryCapacity.length != 1)
        {
            throw new IllegalArgumentException("batteryCapacity is invalid.");
        }
        if (averagePowerConsumption == null || averagePowerConsumption.length != 1)
        {
            throw new IllegalArgumentException("averagePowerConsumption is invalid.");
        }
        synchronized (m_baseLock)
        {
            //If modem don't support this.
            String reply = sendCommand("AT^SBC=?\r", false);
            if (reply.startsWith("ERROR"))
            {
                return;
            }
            reply = sendCommand("AT^SBC?\r", false);
            if (reply.equals(""))
            {
                throw new RuntimeException("GetBatteryCharge failed.");
            }

            int ret = reply.lastIndexOf("SBC:");
            if (ret == -1)
            {
                throw new RuntimeException("GetBatteryCharge failed.");
            }
            String[] results = reply.split(",|:");
            if (results.length != 4)
            {
                throw new RuntimeException("GetBatteryCharge failed.");
            }
            batteryCapacity[0]= Integer.parseInt(results[2]);
            averagePowerConsumption[0] = Integer.parseInt(results[3]);
        }
    }

    /** 
     Return all available information from the modem.

     @return 
    */
    public final String[] getInfo()
    {
        java.util.ArrayList<String> info = new java.util.ArrayList<String>();
        synchronized (m_baseLock)
        {
            //Get manufacturer.
            String reply = sendCommand("AT+CGMI\r", false);
            if (!reply.startsWith("ERROR:"))
            {
                info.add("Manufacturer: " + reply);
            }
            else
            {
                info.add("Manufacturer: Unknown");
            }
            //Get Model.
            reply = sendCommand("AT+CGMM\r", false);
            if (!reply.startsWith("ERROR:"))
            {
                info.add("Model: " + reply);
            }
            else
            {
                info.add("Model: Unknown");
            }
            //Get supported features.
            reply = sendCommand("AT+GCAP\r", false);
            if (!reply.startsWith("ERROR:"))
            {
                info.add("Features: " + reply);
            }
            //Serial port speed.
            reply = sendCommand("AT+IPR?\r", false);
            int pos = reply.indexOf("+IPR:");
            if (pos != -1)
            {
                info.add("Serial port speed: " + reply.substring(pos + 5));
            }
        }
       return info.toArray(new String[info.size()]);
    }

    /** 
     Amount of sent messages.
    */   
    public final int getMessagesSent()
    {
        return m_MessagesSent;
    }
    private void setMessagesSent(int value)
    {
        m_MessagesSent = value;
    }

    /** 
     Amount of received messages.
    */
    public final int getMessagesReceived()
    {
        return m_MessagesReceived;
    }
    private void setMessagesReceived(int value)
    {
        m_MessagesReceived = value;
    }

    /** 
     Resets sent and received message counters.
    */
    public final void resetMessageCounters()
    {
        setMessagesReceived(0);
        setMessagesSent(0);
    }              
}