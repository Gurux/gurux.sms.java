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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import gurux.common.GXSync;
import gurux.common.GXSynchronousMediaBase;
import gurux.common.IGXMedia;
import gurux.common.IGXMediaListener;
import gurux.common.MediaStateEventArgs;
import gurux.common.PropertyChangedEventArgs;
import gurux.common.ReceiveEventArgs;
import gurux.common.ReceiveParameters;
import gurux.common.TraceEventArgs;
import gurux.common.enums.MediaState;
import gurux.common.enums.TraceLevel;
import gurux.common.enums.TraceTypes;
import gurux.io.BaudRate;
import gurux.io.Handshake;
import gurux.io.NativeCode;
import gurux.io.Parity;
import gurux.io.StopBits;
import gurux.sms.enums.AvailableMediaSettings;
import gurux.sms.enums.MemoryType;
import gurux.sms.enums.MessageCodeType;
import gurux.sms.enums.MessageStatus;
import gurux.sms.enums.NetworkState;

/**
 * The GXSMS component determines methods that make the communication possible
 * using serial port connection.
 */
public class GXSMS implements IGXMedia, AutoCloseable {

    /**
     * How long is waited before AT command is sent after connection is made.
     */
    static final int INITIALIZE_SLEEP = 100;
    /**
     * Initialized connection wait time.
     */
    static final int INITIALIZE_CONNECTION_WAIT_TIME = 30000;

    /**
     * Initialized command wait time.
     */
    static final int INITIALIZE_COMMAND_WAIT_TIME = 3000;
    /**
     * Amount of default data bits.
     */
    static final int DEFAULT_DATA_BITS = 8;

    /**
     * Used baud rate.
     */
    private BaudRate baudRate = BaudRate.BAUD_RATE_9600;
    /**
     * Amount of data bits.
     */
    private int dataBits = DEFAULT_DATA_BITS;
    /**
     * Used stop bits.
     */
    private StopBits stopBits = StopBits.ONE;
    /**
     * Used parity.
     */
    private Parity parity = Parity.NONE;

    /**
     * How long connection can take.
     */
    private int connectionWaitTime = INITIALIZE_CONNECTION_WAIT_TIME;

    /**
     * How long command reply can take.
     */
    private int commadWaitTime = INITIALIZE_COMMAND_WAIT_TIME;

    /**
     * Read buffer size.
     */
    static final int DEFUALT_READ_BUFFER_SIZE = 256;

    /**
     * PIN code.
     */
    private String pin;

    /**
     * Initialize commands for the modem..
     */
    private String[] initializeCommands;
    /**
     * Phone number where SMS messages are send in default.
     */
    private String phoneNumber;

    /**
     * Serial port closing handle.
     */
    private long closing = 0;
    /**
     * Write timeout.
     */
    private int writeTimeout;
    /**
     * Read timeout.
     */
    private int readTimeout;
    /**
     * In modem initialized.
     */
    private static boolean initialized;
    /**
     * Read buffer size.
     */
    private int readBufferSize;
    /**
     * Receiver thread.
     */
    private GXReceiveThread receiver;
    /**
     * Serial port handle.
     */
    private int hWnd;
    /**
     * Serial port name.
     */
    private String portName;
    /**
     * Synchronous class.
     */
    private GXSynchronousMediaBase syncBase;
    /**
     * Bytes send.
     */
    private long bytesSend = 0;
    /**
     * Synchronous counter.
     */
    private int synchronous = 0;
    /**
     * Trace level.
     */
    private TraceLevel trace = TraceLevel.OFF;

    /**
     * SMS receiver thread.
     */
    private SMSReceiveThread smsReceiver;

    /**
     * SMS check interval in seconds.
     */
    private int checkInterval;

    /**
     * Is modem supporting direct sending.
     */
    private boolean supportDirectSend;

    /**
     * Configurable settings.
     */
    private int configurableSettings;
    /**
     * Media listeners.
     */
    private List<IGXMediaListener> mediaListeners =
            new ArrayList<IGXMediaListener>();

    /**
     * Lock serial port so only one command is send at the time.
     */
    private final Object baseLock = new Object();

    /**
     * Are messages removed directly after read.
     */
    private boolean autoDelete;

    /**
     * Used memory type to save SMSs.
     */
    private MemoryType memory = MemoryType.UNKNOWN;
    /**
     * SMS messages send.
     */
    private int messagesSent;
    /**
     * SMS messages received.
     */
    private int messagesReceived;

    /**
     * Constructor.
     */
    public GXSMS() {
        phoneNumber = "";
        initialize();
        readBufferSize = DEFUALT_READ_BUFFER_SIZE;
        syncBase = new GXSynchronousMediaBase(readBufferSize);
        setConfigurableSettings(AvailableMediaSettings.ALL.getValue());
    }

    /**
     * Constructor.
     * 
     * @param port
     *            Serial port.
     * @param baudRateValue
     *            Baud rate.
     * @param dataBitsValue
     *            Data bits.
     * @param parityValue
     *            Parity.
     * @param stopBitsValue
     *            Stop bits.
     */
    public GXSMS(final String port, final BaudRate baudRateValue,
            final int dataBitsValue, final Parity parityValue,
            final StopBits stopBitsValue) {
        phoneNumber = "";
        initialize();
        readBufferSize = DEFUALT_READ_BUFFER_SIZE;
        syncBase = new GXSynchronousMediaBase(readBufferSize);
        setConfigurableSettings(AvailableMediaSettings.ALL.getValue());
        setPortName(port);
        setBaudRate(baudRateValue);
        setDataBits(dataBitsValue);
        setParity(parityValue);
        setStopBits(stopBitsValue);
    }

    /**
     * Returns synchronous class used to communicate synchronously.
     * 
     * @return Synchronous class.
     */
    final GXSynchronousMediaBase getSyncBase() {
        return syncBase;
    }

    /**
     * Get handle for closing.
     * 
     * @return Handle for closing.
     */
    final long getClosing() {
        return closing;
    }

    /**
     * Set handle for closing.
     * 
     * @param value
     *            Handle for closing.
     */
    final void setClosing(final long value) {
        closing = value;
    }

    /**
     * Is Windows operating system.
     * 
     * @param os
     *            Operating system name.
     * @return True if Windows.
     */
    static boolean isWindows(final String os) {
        return (os.indexOf("win") >= 0);
    }

    /**
     * Is Mac operating system.
     * 
     * @param os
     *            Operating system name.
     * @return True if Mac.
     */
    static boolean isMac(final String os) {
        return (os.indexOf("mac") >= 0);
    }

    /**
     * Is Unix operating system.
     * 
     * @param os
     *            Operating system name.
     * @return True if Unix.
     */
    static boolean isUnix(final String os) {
        return (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0
                || os.indexOf("aix") >= 0);
    }

    /**
     * Is Solaris operating system.
     * 
     * @param os
     *            Operating system name.
     * @return True if Solaris.
     */
    static boolean isSolaris(final String os) {
        return (os.indexOf("sunos") >= 0);
    }

    /**
     * Initialize Gurux serial port library.
     */
    static void initialize() {
        if (!initialized) {
            String path;
            String os = System.getProperty("os.name").toLowerCase();
            boolean is32Bit =
                    System.getProperty("sun.arch.data.model").equals("32");
            if (isWindows(os)) {
                if (is32Bit) {
                    path = "win32";
                } else {
                    path = "win64";
                }
            } else if (isUnix(os)) {
                if (System.getProperty("os.arch").indexOf("arm") != -1) {
                    if (is32Bit) {
                        path = "arm32";
                    } else {
                        path = "arm64";
                    }
                } else {
                    if (is32Bit) {
                        path = "linux86";
                    } else {
                        path = "linux64";
                    }
                }
            } else {
                throw new RuntimeException("Invald operating system. " + os);
            }
            File file;
            try {
                file = File.createTempFile("gurux.sms.java", ".dll");
            } catch (IOException e1) {
                throw new RuntimeException(
                        "Failed to load file. " + path + "/gurux.sms.java");
            }
            try (InputStream in = GXSMS.class.getResourceAsStream("/" + path
                    + "/" + System.mapLibraryName("gurux.sms.java"))) {
                Files.copy(in, file.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
                System.load(file.getAbsolutePath());
            } catch (Exception e) {
                throw new RuntimeException("Failed to load file. " + path
                        + "/gurux.sms.java" + e.toString());
            }
        }
    }

    /**
     * Gets an array of serial port names for the current computer.
     * 
     * @return Collection of available serial ports.
     */
    public static String[] getPortNames() {
        initialize();
        return NativeCode.getPortNames();
    }

    /**
     * Get baud rates supported by given serial port.
     * 
     * @param portName
     *            Name of serial port.
     * @return Collection of available baud rates.
     */
    public static BaudRate[] getAvailableBaudRates(final String portName) {
        return new BaudRate[] { BaudRate.BAUD_RATE_300, BaudRate.BAUD_RATE_600,
                BaudRate.BAUD_RATE_1800, BaudRate.BAUD_RATE_2400,
                BaudRate.BAUD_RATE_4800, BaudRate.BAUD_RATE_9600,
                BaudRate.BAUD_RATE_19200, BaudRate.BAUD_RATE_38400 };
    }

    /**
     * Get SMS check interval.
     * 
     * @return SMS check interval in seconds.
     */
    public final int getSMSCheckInterval() {
        return checkInterval;
    }

    /**
     * Set SMS check interval.
     * 
     * @param value
     *            SMS check interval in seconds.
     */
    public final void setSMSCheckInterval(final int value) {
        checkInterval = value;
    }

    @Override
    protected final void finalize() throws Throwable {
        super.finalize();
        if (isOpen()) {
            close();
        }
    }

    @Override
    public final TraceLevel getTrace() {
        return trace;
    }

    @Override
    public final void setTrace(final TraceLevel value) {
        trace = value;
        syncBase.setTrace(value);
    }

    /**
     * Notify that property has changed.
     * 
     * @param info
     *            Name of changed property.
     */
    private void notifyPropertyChanged(final String info) {
        for (IGXMediaListener listener : mediaListeners) {
            listener.onPropertyChanged(this,
                    new PropertyChangedEventArgs(info));
        }
    }

    /**
     * Notify clients from error occurred.
     * 
     * @param ex
     *            Occurred error.
     */
    final void notifyError(final RuntimeException ex) {
        for (IGXMediaListener listener : mediaListeners) {
            listener.onError(this, ex);
            if (trace.ordinal() >= TraceLevel.ERROR.ordinal()) {
                listener.onTrace(this,
                        new TraceEventArgs(TraceTypes.ERROR, ex));
            }
        }
    }

    /**
     * Notify clients from new data received.
     * 
     * @param e
     *            Received event argument.
     */
    final void notifyReceived(final ReceiveEventArgs e) {
        for (IGXMediaListener listener : mediaListeners) {
            listener.onReceived(this, e);
        }
    }

    /**
     * Notify clients from trace events.
     * 
     * @param e
     *            Trace event argument.
     */
    final void notifyTrace(final TraceEventArgs e) {
        for (IGXMediaListener listener : mediaListeners) {
            listener.onTrace(this, e);
        }
    }

    @Override
    public final int getConfigurableSettings() {
        return configurableSettings;
    }

    @Override
    public final void setConfigurableSettings(final int value) {
        configurableSettings = value;
    }

    @Override
    public final boolean properties(final JFrame parent) {
        GXSettings dlg = new GXSettings(parent, true, this);
        dlg.pack();
        dlg.setVisible(true);
        return dlg.isAccepted();
    }

    /**
     * Displays the copyright of the control, user license, and version
     * information, in a dialog box.
     */
    public final void aboutBox() {
        throw new UnsupportedOperationException();
    }

    /**
     * Sends SMS message asynchronously.
     * 
     * @param msg
     *            SMS message to send.
     * @see IGXMediaListener#onReceived IGXMediaListener#onReceived
     * @see open open
     * @see close close
     */
    public final void send(final GXSMSMessage msg) {
        send(msg, null);
    }

    @Override
    public final void send(final Object data, final String target) {
        if (data instanceof GXSMSMessage) {
            GXSMSMessage msg = (GXSMSMessage) data;
            synchronized (baseLock) {
                if (trace == TraceLevel.VERBOSE) {
                    notifyTrace(new TraceEventArgs(TraceTypes.SENT,
                            msg.getPhoneNumber() + " : " + msg.getData()));
                }
                // Reset last position.
                synchronized (syncBase.getSync()) {
                    syncBase.resetLastPosition();
                }
                // Use default phone number if new is not set.
                String number = phoneNumber;
                if (msg.getPhoneNumber() != null
                        && !msg.getPhoneNumber().equals("")) {
                    number = msg.getPhoneNumber();
                }
                if (number == null || number.equals("")) {
                    throw new IllegalArgumentException("Invalid phone number.");
                }
                sendMessage(msg.getData(), number, msg.getCodeType());
            }
        } else if (data instanceof String) {
            if (receiver == null || receiver.equals("")) {
                throw new IllegalArgumentException(
                        "Invalid receiver phone number.");
            }
            GXSMSMessage msg = new GXSMSMessage();
            msg.setData(data.toString());
            msg.setPhoneNumber(target);
            send(msg, null);
        } else {
            throw new IllegalArgumentException("Invalid data to send");
        }
    }

    /**
     * Notify client from media state change.
     * 
     * @param state
     *            New media state.
     */
    private void notifyMediaStateChange(final MediaState state) {
        for (IGXMediaListener listener : mediaListeners) {
            if (trace.ordinal() >= TraceLevel.ERROR.ordinal()) {
                listener.onTrace(this,
                        new TraceEventArgs(TraceTypes.INFO, state));
            }
            listener.onMediaStateChange(this, new MediaStateEventArgs(state));
        }
    }

    /**
     * Get error from the string.
     * 
     * @param reply
     *            Reply string from the modem.
     * @return Error string if exists.
     */
    private static String getError(final String reply) {

        int pos = reply.indexOf(GXSMSPdu.ERROR);
        if (pos != -1) {
            return reply.substring(pos + GXSMSPdu.ERROR.length()).trim();
        }
        return reply.trim();
    }

    /*
     * Open serial port and call to phone number.
     */
    @Override
    public final void open() throws Exception {
        close();
        try {
            if (portName == null || portName == "") {
                throw new IllegalArgumentException(
                        "Serial port is not selected.");
            }
            synchronized (syncBase.getSync()) {
                syncBase.resetLastPosition();
            }
            notifyMediaStateChange(MediaState.OPENING);
            if (trace.ordinal() >= TraceLevel.INFO.ordinal()) {
                notifyTrace(new TraceEventArgs(TraceTypes.INFO,
                        "Settings: Port: " + this.getPortName() + " Baud Rate: "
                                + getBaudRate() + " Data Bits: "
                                + (new Integer(getDataBits())).toString()
                                + " Parity: " + getParity().toString()
                                + " Stop Bits: " + getStopBits().toString()));
            }
            long[] tmp = new long[1];
            hWnd = NativeCode.openSerialPort(portName, tmp);
            // If user has change values before open.
            if (baudRate != BaudRate.BAUD_RATE_9600) {
                setBaudRate(baudRate);
            }
            if (dataBits != DEFAULT_DATA_BITS) {
                setDataBits(dataBits);
            }
            if (parity != Parity.NONE) {
                setParity(parity);
            }
            if (stopBits != StopBits.ONE) {
                setStopBits(stopBits);
            }
            closing = tmp[0];
            receiver = new GXReceiveThread(this, hWnd);
            setRtsEnable(true);
            setDtrEnable(true);
            receiver.start();
            // CHECKSTYLE:OFF
            Thread.sleep(100);
            // CHECKSTYLE:ON
            try {
                // Send AT
                synchronized (baseLock) {
                    if (getInitializeCommands() != null) {
                        for (String it : getInitializeCommands()) {
                            sendCommand(it + "\r\n", true);
                        }
                    }
                    String reply;
                    // Send AT
                    reply = sendCommand("AT\r", false);
                    if (reply.compareToIgnoreCase("OK") != 0) {
                        reply = sendCommand("AT\r", false);
                        if (reply.compareToIgnoreCase("OK") != 0) {
                            reply = sendCommand("+++", "+++", true);
                            if (reply.equals("")) {
                                throw new RuntimeException("Invalid reply.");
                            }
                            reply = sendCommand("AT\r", true);
                            if (reply.compareToIgnoreCase("OK") != 0) {
                                throw new RuntimeException("Invalid reply.");
                            }
                        }
                    }
                    // Enable error reporting. It's OK if this fails.
                    sendCommand("AT+CMEE\r", false);
                    // Enable verbode error code,
                    reply = sendCommand("AT+CMEE=2\r", false);
                    if (!reply.equals("OK")) {
                        // Enable numeric error codes
                        sendCommand("AT+CMEE=1\r", false);
                    }
                    reply = sendCommand("AT+CPIN=?\r", false);
                    boolean pinSupported = reply.equals("OK");
                    // Is PIN Code supported.
                    if (pinSupported) {
                        // Check PIN-Code
                        reply = sendCommand("AT+CPIN?\r", false);
                        if (reply.contains("ERROR:")) {
                            throw new RuntimeException(
                                    "Failed to read PIN code.\r\n"
                                            + getError(reply));
                        }
                        // If PIN code is needed.
                        if (!reply.equals("+CPIN: READY")) {
                            if (pin == null || pin.equals("")) {
                                throw new RuntimeException("PIN is needed.");
                            }
                            reply = sendCommand(
                                    String.format("AT+CPIN=\"%1$s\"\r", pin),
                                    false);
                            if (!reply.equals("OK")) {
                                throw new RuntimeException(
                                        "Failed to set PIN code."
                                                + getError(reply));
                            }
                            // Ask PIN Code again.
                            reply = sendCommand("AT+CPIN?\r", false);
                            if (!reply.equals("OK")) {
                                throw new RuntimeException(
                                        "Failed to set PIN code."
                                                + getError(reply));
                            }
                        }
                    }
                    // Is direct SMS sending supported.
                    reply = sendCommand("at+cmgs=?\r", false);
                    supportDirectSend = reply.contains("OK");
                    // Start SMS checker.
                    if (checkInterval != 0) {
                        smsReceiver = new SMSReceiveThread(this);
                        smsReceiver.start();
                    }
                }
            } catch (RuntimeException ex) {
                close();
                throw ex;
            }
            notifyMediaStateChange(MediaState.OPEN);
        } catch (Exception ex) {
            close();
            throw ex;
        }
    }

    /**
     * Write bytes to the serial port.
     * 
     * @param value
     *            Bytes to send.
     */
    private void sendBytes(final byte[] value) {
        synchronized (baseLock) {
            if (trace == TraceLevel.VERBOSE) {
                notifyTrace(new TraceEventArgs(TraceTypes.SENT, value));
            }
            bytesSend += value.length;
            // Reset last position if Eop is used.
            synchronized (syncBase.getSync()) {
                syncBase.resetLastPosition();
            }
            NativeCode.write(hWnd, value, writeTimeout);
        }
    }

    /**
     * Send command to the serial port.
     * 
     * @param cmd
     *            Command to send.
     * @param throwError
     *            Is error thrown if there is no reply.
     * @return Received reply from the modem.
     */
    private String sendCommand(final String cmd, final boolean throwError) {
        return sendCommand(cmd, null, throwError);
    }

    /**
     * Send command to the serial port.
     * 
     * @param cmd
     *            Command to send.
     * @param eop
     *            End of packet if used.
     * @param throwError
     *            Is error thrown if there is no reply.
     * @return Received reply from the modem.
     */
    private String sendCommand(final String cmd, final String eop,
            final boolean throwError) {
        final int expectedMinimumByteCount = 5;
        ReceiveParameters<String> p =
                new ReceiveParameters<String>(String.class);
        p.setWaitTime(commadWaitTime);
        if (eop != null) {
            p.setEop(eop);
        } else {
            p.setEop("\r\n");
        }

        if (p.getEop().equals("")) {
            p.setEop(null);
            p.setCount(cmd.length());
        }
        try {
            sendBytes(cmd.getBytes("ASCII"));
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getMessage());
        }
        StringBuilder sb = new StringBuilder();
        int index = -1;
        String reply = "";
        while (index == -1) {
            if (!receive(p)) {
                if (throwError) {
                    throw new RuntimeException(
                            "Failed to receive answer from the modem. "
                                    + "Check serial port.");
                }
                return "";
            }
            sb.append(p.getReply());
            reply = sb.toString();
            // Remove echo.
            if (sb.length() >= cmd.length() && reply.startsWith(cmd)) {
                sb.delete(0, cmd.length());
                reply = sb.toString();
                // Remove echo and return if we are not expecting reply.
                if (eop != null && eop.equals("")) {
                    return "";
                }
            }
            if (eop != null) {
                index = reply.lastIndexOf(eop);
            } else if (reply.length() > expectedMinimumByteCount) {
                index = reply.lastIndexOf("\r\nOK\r\n");
                if (index == -1) {
                    index = reply.lastIndexOf("ERROR:");
                } else if (index != 0) {
                    // If there is a message before OK show it.
                    reply = reply.substring(0, index);
                    index = 0;
                }
            }
            p.setReply(null);
        }
        if (index != 0 & eop == null) {
            reply = reply.substring(0, 0) + reply.substring(0 + index);
        }
        reply = reply.trim();
        return reply;
    }

    @Override
    public final void close() {
        if (hWnd != 0) {
            try {
                notifyMediaStateChange(MediaState.CLOSING);
                if (smsReceiver != null) {
                    receiver.interrupt();
                    receiver = null;
                }
            } catch (RuntimeException ex) {
                notifyError(ex);
                throw ex;
            } finally {
                if (receiver != null) {
                    receiver.interrupt();
                    receiver = null;
                }
                try {
                    NativeCode.closeSerialPort(hWnd, closing);
                } catch (java.lang.Exception e) {
                    // Ignore all errors on close.
                }
                hWnd = 0;
                notifyMediaStateChange(MediaState.CLOSED);
                bytesSend = 0;
                this.receiver.resetBytesReceived();
                syncBase.resetReceivedSize();
            }
        }
    }

    /**
     * Used baud rate for communication. Can be changed without disconnecting.
     * 
     * @return Used baud rate.
     */
    public final BaudRate getBaudRate() {
        if (hWnd == 0) {
            return baudRate;
        }
        return BaudRate.forValue(NativeCode.getBaudRate(hWnd));
    }

    /**
     * Set new baud rate.
     * 
     * @param value
     *            New baud rate.
     */
    public final void setBaudRate(final BaudRate value) {
        boolean change = getBaudRate() != value;
        if (change) {
            if (hWnd == 0) {
                baudRate = value;
            } else {
                NativeCode.setBaudRate(hWnd, value.getValue());
            }
            notifyPropertyChanged("BaudRate");
        }
    }

    /**
     * Gets the phone number.
     * 
     * @return Phone number.
     */
    public final String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Sets the phone number.
     * 
     * @param value
     *            Phone number.
     */
    public final void setPhoneNumber(final String value) {
        boolean change =
                phoneNumber == null || phoneNumber.equalsIgnoreCase(value);
        phoneNumber = value;
        if (change) {
            notifyPropertyChanged("BaudRate");
        }
    }

    /**
     * Get PIN Code.
     * 
     * @return PIN Code.
     */
    public final String getPINCode() {
        return pin;
    }

    /**
     * Set PIN Code.
     * 
     * @param value
     *            PIN Code.
     */
    public final void setPINCode(final String value) {
        pin = value;
    }

    /**
     * Gets how long (milliseconds) modem answer is waited when connection is
     * made.
     * 
     * @return Wait time in milliseconds.
     */
    public final int getConnectionWaitTime() {
        return connectionWaitTime;
    }

    /**
     * Sets how long (milliseconds) modem answer is waited when connection is
     * made.
     * 
     * @param value
     *            Wait time in milliseconds.
     */
    public final void setConnectionWaitTime(final int value) {
        boolean change = connectionWaitTime != value;
        connectionWaitTime = value;
        if (change) {
            notifyPropertyChanged("ConnectionWaitTime");
        }
    }

    /**
     * Gets how long (milliseconds) modem answer is waited when command is send
     * for the modem.
     * 
     * @return Wait time in milliseconds.
     */
    public final int getCommandWaitTime() {
        return commadWaitTime;
    }

    /**
     * Sets how long (milliseconds) modem answer is waited when command is send
     * for the modem.
     * 
     * @param value
     *            Wait time in milliseconds.
     */
    public final void setCommandWaitTime(final int value) {
        boolean change = commadWaitTime != value;
        commadWaitTime = value;
        if (change) {
            notifyPropertyChanged("CommadWaitTime");
        }
    }

    /**
     * Get break state.
     * 
     * @return True if the port is in a break state; otherwise, false.
     */
    public final boolean getBreakState() {
        return NativeCode.getBreakState(hWnd);
    }

    /**
     * Set break state.
     * 
     * @param value
     *            True if the port is in a break state; otherwise, false.
     */
    public final void setBreakState(final boolean value) {
        boolean change;
        change = getBreakState() != value;
        if (change) {
            NativeCode.setBreakState(hWnd, value);
            notifyPropertyChanged("BreakState");
        }
    }

    /**
     * Gets the number of bytes in the receive buffer.
     * 
     * @return Amount of read bytes.
     */
    public final int getBytesToRead() {
        return NativeCode.getBytesToRead(hWnd);
    }

    /**
     * Gets the number of bytes in the send buffer.
     * 
     * @return Amount of bytes to write in the send buffer.
     */
    public final int getBytesToWrite() {
        return NativeCode.getBytesToWrite(hWnd);
    }

    /**
     * Gets the state of the Carrier Detect line for the port.
     * 
     * @return Is Carrier Detect in holding state.
     */
    public final boolean getCDHolding() {
        return NativeCode.getCDHolding(hWnd);
    }

    /**
     * Gets the state of the Clear-to-Send line.
     * 
     * @return Clear-to-Send state.
     */
    public final boolean getCtsHolding() {
        return NativeCode.getCtsHolding(hWnd);
    }

    /**
     * Gets the standard length of data bits per byte.
     * 
     * @return Amount of data bits.
     */
    public final int getDataBits() {
        if (hWnd == 0) {
            return dataBits;
        }
        return NativeCode.getDataBits(hWnd);
    }

    /**
     * Sets the standard length of data bits per byte.
     * 
     * @param value
     *            Amount of data bits.
     */
    public final void setDataBits(final int value) {
        boolean change;
        change = getDataBits() != value;
        if (change) {
            if (hWnd == 0) {
                dataBits = value;
            } else {
                NativeCode.setDataBits(hWnd, value);
            }
            notifyPropertyChanged("DataBits");
        }
    }

    /**
     * Gets the state of the Data Set Ready (DSR) signal.
     * 
     * @return Is Data Set Ready set.
     */
    public final boolean getDsrHolding() {
        return NativeCode.getDsrHolding(hWnd);
    }

    /**
     * Get is Data Terminal Ready (DTR) signal enabled.
     * 
     * @return Is DTR enabled.
     */
    public final boolean getDtrEnable() {
        return NativeCode.getDtrEnable(hWnd);
    }

    /**
     * Set is Data Terminal Ready (DTR) signal enabled.
     * 
     * @param value
     *            Is DTR enabled.
     */
    public final void setDtrEnable(final boolean value) {
        boolean change;
        change = getDtrEnable() != value;
        NativeCode.setDtrEnable(hWnd, value);
        if (change) {
            notifyPropertyChanged("DtrEnable");
        }
    }

    /**
     * Gets the handshaking protocol for serial port transmission of data.
     * 
     * @return Used handshake protocol.
     */
    public final Handshake getHandshake() {
        return Handshake.values()[NativeCode.getHandshake(hWnd)];
    }

    /**
     * Sets the handshaking protocol for serial port transmission of data.
     * 
     * @param value
     *            Handshake protocol.
     */
    public final void setHandshake(final Handshake value) {
        boolean change;
        change = getHandshake() != value;
        if (change) {
            NativeCode.setHandshake(hWnd, value.ordinal());
            notifyPropertyChanged("Handshake");
        }
    }

    @Override
    public final boolean isOpen() {
        return hWnd != 0;
    }

    /**
     * Gets the parity-checking protocol.
     * 
     * @return Used parity.
     */
    public final Parity getParity() {
        if (hWnd == 0) {
            return parity;
        }
        return Parity.values()[NativeCode.getParity(hWnd)];
    }

    /**
     * Sets the parity-checking protocol.
     * 
     * @param value
     *            Used parity.
     */
    public final void setParity(final Parity value) {
        boolean change;
        change = getParity() != value;
        if (change) {
            if (hWnd == 0) {
                parity = value;
            } else {
                NativeCode.setParity(hWnd, value.ordinal());
            }
            notifyPropertyChanged("Parity");
        }
    }

    /**
     * Gets the port for communications, including but not limited to all
     * available COM ports.
     * 
     * @return Used serial port
     */
    public final String getPortName() {
        return portName;
    }

    /**
     * Sets the port for communications, including but not limited to all
     * available COM ports.
     * 
     * @param value
     *            Used serial port.
     */
    public final void setPortName(final String value) {
        boolean change;
        change = !value.equals(portName);
        portName = value;
        if (change) {
            notifyPropertyChanged("PortName");
        }
    }

    /**
     * Gets the size of the serial port input buffer.
     * 
     * @return Size of input buffer.
     */
    public final int getReadBufferSize() {
        return readBufferSize;
    }

    /**
     * Sets the size of the serial port input buffer.
     * 
     * @param value
     *            Size of input buffer.
     */
    public final void setReadBufferSize(final int value) {
        boolean change;
        change = getReadBufferSize() != value;
        if (change) {
            readBufferSize = value;
            notifyPropertyChanged("ReadBufferSize");
        }
    }

    /**
     * Gets the number of milliseconds before a time-out occurs when a read
     * operation does not finish.
     * 
     * @return Read timeout.
     */
    public final int getReadTimeout() {
        return readTimeout;
    }

    /**
     * Sets the number of milliseconds before a time-out occurs when a read
     * operation does not finish.
     * 
     * @param value
     *            Read timeout.
     */
    public final void setReadTimeout(final int value) {
        boolean change = readTimeout != value;
        readTimeout = value;
        if (change) {
            notifyPropertyChanged("ReadTimeout");
        }
    }

    /**
     * Gets a value indicating whether the Request to Send (RTS) signal is
     * enabled during serial communication.
     * 
     * @return Is RTS enabled.
     */
    public final boolean getRtsEnable() {
        return NativeCode.getRtsEnable(hWnd);
    }

    /**
     * Sets a value indicating whether the Request to Send (RTS) signal is
     * enabled during serial communication.
     * 
     * @param value
     *            Is RTS enabled.
     */
    public final void setRtsEnable(final boolean value) {
        boolean change;
        change = getRtsEnable() != value;
        NativeCode.setRtsEnable(hWnd, value);
        if (change) {
            notifyPropertyChanged("RtsEnable");
        }
    }

    /**
     * Gets the standard number of stop bits per byte.
     * 
     * @return Used stop bits.
     */
    public final StopBits getStopBits() {
        if (hWnd == 0) {
            return stopBits;
        }
        return StopBits.values()[NativeCode.getStopBits(hWnd)];
    }

    /**
     * Sets the standard number of stop bits per byte.
     * 
     * @param value
     *            Used stop bits.
     */
    public final void setStopBits(final StopBits value) {
        boolean change;
        change = getStopBits() != value;
        if (change) {
            if (hWnd == 0) {
                stopBits = value;
            } else {
                NativeCode.setStopBits(hWnd, value.ordinal());
            }
            notifyPropertyChanged("StopBits");
        }
    }

    /**
     * Gets the number of milliseconds before a time-out occurs when a write
     * operation does not finish.
     * 
     * @return Used time out.
     */
    public final int getWriteTimeout() {
        return writeTimeout;
    }

    /**
     * Sets the number of milliseconds before a time-out occurs when a write
     * operation does not finish.
     * 
     * @param value
     *            Used time out.
     */
    public final void setWriteTimeout(final int value) {
        boolean change = writeTimeout != value;
        if (change) {
            writeTimeout = value;
            notifyPropertyChanged("WriteTimeout");
        }
    }

    /**
     * Send message.
     * 
     * @param message
     *            Message to send.
     * @param target
     *            Receiver.
     * @param type
     *            Message code type.
     */
    private void sendMessage(final String message, final String target,
            final MessageCodeType type) {
        String receiver2 = target;
        if (receiver == null || receiver.equals("")) {
            throw new IllegalArgumentException("Invalid receiver");
        }
        // Remove spaces.
        receiver2 = receiver2.replace(" ", "").replace("-", "").replace("(", "")
                .replace(")", "").trim();
        // Send EOF
        // CHECKSTYLE:OFF
        sendBytes(new byte[] { 26 });
        // CHECKSTYLE:ON
        // Code PDU.
        String data = GXSMSPdu.code(target, message, type);
        long len = (data.length() / 2) - 1;
        String cmd;
        // Save SMS before send.
        if (!supportDirectSend) {
            cmd = String.format("AT+CMGW=%1$s\r", len);
        } else {
            cmd = String.format("AT+CMGS=%1$s\r", len);
        }
        // CHECKSTYLE:OFF
        String reply =
                sendCommand(cmd, new String(new char[] { (char) 0x20 }), false);
        // CHECKSTYLE:ON
        if (!reply.equals(">")) {
            throw new RuntimeException("Short message send failed.");
        }
        reply = sendCommand(data, "", false);
        // CHECKSTYLE:OFF
        reply = sendCommand(new String(new char[] { 26 }), false);
        // CHECKSTYLE:ON
        if (!reply.startsWith("+CMGW:")) {
            throw new RuntimeException(
                    "Short message send failed.\r\n" + getError(reply));
        }
    }

    @Override
    public final <T> boolean receive(final ReceiveParameters<T> args) {
        return syncBase.receive(args);
    }

    @Override
    public final long getBytesSent() {
        return bytesSend;
    }

    @Override
    public final long getBytesReceived() {
        return receiver.getBytesReceived();
    }

    @Override
    public final void resetByteCounters() {
        bytesSend = 0;
        receiver.resetBytesReceived();
    }

    @Override
    public final String getSettings() {
        StringBuilder sb = new StringBuilder();
        String nl = System.getProperty("line.separator");
        if (pin != null && !pin.isEmpty()) {
            sb.append("<PIN>");
            sb.append(pin);
            sb.append("</PIN>");
            sb.append(nl);
        }
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            sb.append("<Number>");
            sb.append(phoneNumber);
            sb.append("</Number>");
            sb.append(nl);
        }
        if (checkInterval != 0) {
            sb.append("<Interval>");
            sb.append(checkInterval);
            sb.append("</Interval>");
            sb.append(nl);
        }
        if (portName != null && !portName.isEmpty()) {
            sb.append("<Port>");
            sb.append(portName);
            sb.append("</Port>");
            sb.append(nl);
        }
        if (baudRate != BaudRate.BAUD_RATE_9600) {
            sb.append("<BaudRate>");
            sb.append(String.valueOf(baudRate.getValue()));
            sb.append("</BaudRate>");
            sb.append(nl);
        }
        if (stopBits != StopBits.ONE) {
            sb.append("<StopBits>");
            sb.append(String.valueOf(stopBits.ordinal()));
            sb.append("</StopBits>");
            sb.append(nl);
        }
        if (parity != Parity.NONE) {
            sb.append("<Parity>");
            sb.append(String.valueOf(parity.ordinal()));
            sb.append("</Parity>");
            sb.append(nl);
        }
        if (dataBits != DEFAULT_DATA_BITS) {
            sb.append("<DataBits>");
            sb.append(String.valueOf(dataBits));
            sb.append("</DataBits>");
            sb.append(nl);
        }
        if (initializeCommands != null && initializeCommands.length != 0) {
            sb.append("<Init>");
            for (String it : initializeCommands) {
                sb.append(it);
                sb.append(';');
            }
            // Remove last ;
            sb.setLength(sb.length() - 1);
            sb.append("</Init>");
            sb.append(nl);
        }
        return sb.toString();
    }

    @Override
    public final void setSettings(final String value) {
        // Reset to default values.
        phoneNumber = "";
        pin = "";
        checkInterval = 0;
        portName = "";
        baudRate = BaudRate.BAUD_RATE_9600;
        stopBits = StopBits.ONE;
        parity = Parity.NONE;
        dataBits = DEFAULT_DATA_BITS;
        initializeCommands = new String[0];

        if (value != null && !value.isEmpty()) {
            try {
                DocumentBuilderFactory factory =
                        DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                StringBuilder sb = new StringBuilder();
                if (value.startsWith("<?xml version=\"1.0\"?>")) {
                    sb.append(value);
                } else {
                    String nl = System.getProperty("line.separator");
                    sb.append("<?xml version=\"1.0\"?>\r\n");
                    sb.append(nl);
                    sb.append("<Net>");
                    sb.append(value);
                    sb.append(nl);
                    sb.append("</Net>");
                }
                InputSource is =
                        new InputSource(new StringReader(sb.toString()));
                Document doc = builder.parse(is);
                doc.getDocumentElement().normalize();
                NodeList nList = doc.getChildNodes();
                if (nList.getLength() != 1) {
                    throw new IllegalArgumentException(
                            "Invalid XML root node.");
                }
                nList = nList.item(0).getChildNodes();
                for (int pos = 0; pos < nList.getLength(); ++pos) {
                    Node it = nList.item(pos);
                    if (it.getNodeType() == Node.ELEMENT_NODE) {
                        if ("Port".equalsIgnoreCase(it.getNodeName())) {
                            setPortName(it.getFirstChild().getNodeValue());
                        } else if ("BaudRate"
                                .equalsIgnoreCase(it.getNodeName())) {
                            setBaudRate(BaudRate.forValue(Integer.parseInt(
                                    it.getFirstChild().getNodeValue())));
                        } else if ("StopBits"
                                .equalsIgnoreCase(it.getNodeName())) {
                            setStopBits(StopBits.values()[Integer.parseInt(
                                    it.getFirstChild().getNodeValue())]);
                        } else if ("Parity"
                                .equalsIgnoreCase(it.getNodeName())) {
                            setParity(Parity.values()[Integer.parseInt(
                                    it.getFirstChild().getNodeValue())]);
                        } else if ("DataBits"
                                .equalsIgnoreCase(it.getNodeName())) {
                            setDataBits(Integer.parseInt(
                                    it.getFirstChild().getNodeValue()));
                        } else if ("Number"
                                .equalsIgnoreCase(it.getNodeName())) {
                            setPhoneNumber(it.getFirstChild().getNodeValue());
                        } else if ("PIN".equalsIgnoreCase(it.getNodeName())) {
                            setPINCode(it.getFirstChild().getNodeValue());
                        } else if ("Interval"
                                .equalsIgnoreCase(it.getNodeName())) {
                            setSMSCheckInterval(Integer.parseInt(
                                    it.getFirstChild().getNodeValue()));
                        } else if ("Init".equalsIgnoreCase(it.getNodeName())) {
                            initializeCommands = it.getFirstChild()
                                    .getNodeValue().split("[;]");
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    @Override
    public final void copy(final Object target) {
        GXSMS tmp = (GXSMS) target;
        setPortName(tmp.getPortName());
        setBaudRate(tmp.getBaudRate());
        setStopBits(tmp.getStopBits());
        setParity(tmp.getParity());
        setDataBits(tmp.getDataBits());
    }

    @Override
    public final String getName() {
        return getPortName();
    }

    @Override
    public final String getMediaType() {
        return "SMS";
    }

    @Override
    public final Object getSynchronous() {
        synchronized (this) {
            int[] tmp = new int[] { synchronous };
            GXSync obj = new GXSync(tmp);
            synchronous = tmp[0];
            return obj;
        }
    }

    @Override
    public final boolean getIsSynchronous() {
        synchronized (this) {
            return synchronous != 0;
        }
    }

    @Override
    public final void resetSynchronousBuffer() {
        synchronized (syncBase.getSync()) {
            syncBase.resetReceivedSize();
        }
    }

    @Override
    public final void validate() {
        if (getPortName() == null || getPortName().length() == 0) {
            throw new RuntimeException("Invalid port name.");
        }
    }

    /**
     * Get modem initial settings.
     * 
     * @return Initialize commands.
     */
    public final String[] getInitializeCommands() {
        return initializeCommands;
    }

    /**
     * Set modem initial settings.
     * 
     * @param value
     *            Initialize commands.
     */
    public final void setInitializeCommands(final String[] value) {
        initializeCommands = value;
    }

    /**
     * End of packet is not used with SMS.
     */
    @Override
    public final Object getEop() {
        return null;
    }

    /**
     * End of packet is not used with SMS.
     */
    @Override
    public final void setEop(final Object value) {
    }

    @Override
    public final void addListener(final IGXMediaListener listener) {
        mediaListeners.add(listener);
    }

    @Override
    public final void removeListener(final IGXMediaListener listener) {
        mediaListeners.remove(listener);
    }

    /**
     * Test is modem available.
     * 
     * @return True, if modem is working.
     */
    public final boolean test() {
        try {
            if (sendCommand("AT\r", false).equalsIgnoreCase("OK")) {
                return true;
            }
            return false;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    /**
     * Gets are messages removed after read from the SIM or phone memory.
     * Default is false.
     * 
     * @return Is auto delete used.
     */
    public final boolean getAutoDelete() {
        return autoDelete;
    }

    /**
     * Sets are messages removed after read from the SIM or phone memory.
     * Default is false.
     * 
     * @param value
     *            Is auto delete used.
     */
    public final void setAutoDelete(final boolean value) {
        autoDelete = value;
    }

    /**
     * Get used memory type. Default value is MemoryType.SIM.
     * 
     * @return Used memory type.
     */
    public final MemoryType getMemory() {
        return memory;
    }

    /**
     * Sets used memory type.
     * 
     * @param value
     *            Used memory type.
     */
    public final void setMemory(final MemoryType value) {
        memory = value;
    }

    /**
     * Delete message from give index.
     * 
     * @param index
     *            Index where message is removed.
     * @return Delete command to send to the modem.
     */
    private String deleteMessage(final int index) {
        String reply =
                sendCommand(String.format("AT+CMGD=%1$s\r", index), false);
        return reply;
    }

    /**
     * Delete SMS from the selected index.
     * 
     * @param index
     *            Index where message is removed.
     */
    public final void delete(final int index) {
        String reply = deleteMessage(index);
        if (!reply.equals("OK")) {
            throw new RuntimeException(String.format(
                    "Delete failed from index %1$s.\r\n%2$s", index, reply));
        }
    }

    /**
     * Delete ALL SMS from the phone.
     */
    public final void deleteAll() {
        GXSMSMemoryInfo info = getMemoryCapacity();
        // If there are no messages to remove.
        if (info.getCount() == 0) {
            return;
        }
        synchronized (baseLock) {
            for (int pos = 1; pos != info.getMaximum() + 1; ++pos) {
                String reply = deleteMessage(pos);
                if (!reply.equals("OK")) {
                    throw new RuntimeException(String.format(
                            "Delete failed from index %1$s.\r\n%2$s", pos,
                            reply));
                }
            }
        }
    }

    /**
     * Read all messages from the selected memory.
     * 
     * @return Collection of SMS messages from the device.
     */
    public final GXSMSMessage[] read() {
        final String expectedReply = "+CMGR:";
        GXSMSMemoryInfo info = getMemoryCapacity();
        // If there are no messages to read.
        if (info.getCount() == 0) {
            return new GXSMSMessage[0];
        }
        java.util.ArrayList<GXSMSMessage> messages =
                new java.util.ArrayList<GXSMSMessage>();
        synchronized (baseLock) {
            for (int pos = 1; pos != info.getMaximum() + 1; ++pos) {
                String reply = sendCommand(String.format("AT+CMGR=%1$s\r", pos),
                        false);
                if (reply.startsWith(expectedReply)) {
                    reply = reply.substring(0, 0)
                            + reply.substring(0 + expectedReply.length());
                    String[] tmp = reply.split("[,]", -1);
                    GXSMSMessage msg = new GXSMSMessage();
                    msg.setIndex(pos);
                    String status = tmp[0].replace("\"", "").trim();
                    if (status.startsWith("STO")) {
                        if (status.contains("UNSENT")) {
                            msg.setStatus(MessageStatus.NOT_SENT);
                        } else if (status.contains("UNREAD")) {
                            msg.setStatus(MessageStatus.NOT_READ);
                        } else if (status.contains("Read")) {
                            msg.setStatus(MessageStatus.READ);
                        } else if (status.contains("Sent")) {
                            msg.setStatus(MessageStatus.SENT);
                        }
                    } else {
                        msg.setStatus(MessageStatus
                                .forValue(Integer.parseInt(status)));
                    }
                    // If this is not a empty message
                    if (tmp.length != 1) {
                        String[] m = tmp[2].split("\r\n");
                        if (m.length != 2) {
                            continue;
                        }
                        GXSMSPdu.encode(m[1], msg);
                        // If this message is not read yet.
                        if (msg.getStatus() == MessageStatus.NOT_READ
                                && msg.getPhoneNumber().equals("")) {
                            continue;
                        }
                    }
                    messages.add(msg);
                    // If all messages are read.
                    if (messages.size() == info.getCount()) {
                        break;
                    }
                }
            }
        }
        return messages.toArray(new GXSMSMessage[0]);
    }

    /**
     * Returns used and maximum SMS capacity.
     * 
     * @return SMS memory info class where information is filled.
     */
    public final GXSMSMemoryInfo getMemoryCapacity() {
        final int expectedArraySize = 9;
        synchronized (baseLock) {
            String reply = sendCommand("AT+CPMS?\r", false);
            // for read/delete", "for write/send" , "for receive
            // "SM": SIM message store
            // "ME": ME message store
            // "MT": any of the storages associated with ME
            if (reply.startsWith("ERROR:")) {
                return null;
            }
            int ret = reply.lastIndexOf("\"SM\",");
            if (ret == -1) {
                throw new RuntimeException("ReadSMSCapacity failed.");
            }
            String[] results = reply.split(",");
            if (results.length != expectedArraySize) {
                throw new RuntimeException("ReadSMSCapacity failed.");
            }
            GXSMSMemoryInfo info = new GXSMSMemoryInfo();
            info.setCount(Integer.parseInt(results[1]));
            info.setMaximum(Integer.parseInt(results[2]));
            return info;
        }
    }

    /**
     * Returns network state.
     * 
     * @return Network state.
     */
    public final NetworkState getNetworkState() {
        synchronized (baseLock) {
            String reply = sendCommand("AT+CREG=?\r", false);
            if (reply.startsWith("ERROR")) {
                return NetworkState.DENIED;
            }
            reply = sendCommand("AT+CREG?\r", false);
            if (reply.startsWith("ERROR:")) {
                throw new RuntimeException(
                        "GetNetworkState failed:\r\n" + getError(reply));
            }

            int ret = reply.lastIndexOf("+CREG:");
            if (ret == -1) {
                throw new RuntimeException("GetNetworkState failed.");
            }
            String[] results = reply.split(",|:");
            // CHECKSTYLE:OFF
            if (results.length != 3) {
                throw new RuntimeException("GetSignalQuality failed.");
            }
            // CHECKSTYLE:ON
            return NetworkState.forValue(Integer.parseInt(results[2]));
        }
    }

    /**
     * Returns received signal strength indication (RSSI) and channel bit error
     * rate (BER).
     * 
     * @return Signal quality information.
     */
    public final GXSMSSignalQualityInfo getSignalQuality() {
        final int expectedArraySize = 3;
        GXSMSSignalQualityInfo info = new GXSMSSignalQualityInfo();
        synchronized (baseLock) {
            // If modem don't support this.
            String reply = sendCommand("AT+CSQ=?\r", false);
            if (reply.startsWith("ERROR")) {
                return info;
            }
            reply = sendCommand("AT+CSQ\r", false);
            if (reply.contains("ERROR:")) {
                throw new RuntimeException(
                        "GetSignalQuality failed:\r\n" + getError(reply));
            }

            int ret = reply.lastIndexOf("CSQ:");
            if (ret == -1) {
                throw new RuntimeException("GetSignalQuality failed.");
            }
            String[] results = reply.split(",|:");
            if (results.length != expectedArraySize) {
                throw new RuntimeException("GetSignalQuality failed.");
            }
            info.setRssi(Integer.parseInt(results[1].trim()));
            info.setBer(Integer.parseInt(results[2].trim()));
            return info;
        }
    }

    /**
     * Returns battery charge level and average power consumption.
     * 
     * @return Info class where battery capacity and average power consumption
     *         are filled. Null is returned if
     */
    public final GXSMSBatteryInfo getBatteryCharge() {
        final int expectedArraySize = 4;
        GXSMSBatteryInfo info = new GXSMSBatteryInfo();
        synchronized (baseLock) {
            // If modem don't support this.
            String reply = sendCommand("AT^SBC=?\r", false);
            if (reply.startsWith("ERROR")) {
                return info;
            }
            reply = sendCommand("AT^SBC?\r", false);
            if (reply.equals("")) {
                throw new RuntimeException("GetBatteryCharge failed.");
            }

            int ret = reply.lastIndexOf("SBC:");
            if (ret == -1) {
                throw new RuntimeException("GetBatteryCharge failed.");
            }
            String[] results = reply.split(",|:");
            if (results.length != expectedArraySize) {
                throw new RuntimeException("GetBatteryCharge failed.");
            }
            // CHECKSTYLE:OFF
            info.setBatteryCapacity(Integer.parseInt(results[2]));
            info.setAveragePowerConsumption(Integer.parseInt(results[3]));
            // CHECKSTYLE:ON
        }
        return info;
    }

    /**
     * Return all available information from the modem.
     * 
     * @return String array of modem settings.
     */
    public final String[] getInfo() {
        final int expectedMinimumSize = 5;
        java.util.ArrayList<String> info = new java.util.ArrayList<String>();
        synchronized (baseLock) {
            // Get manufacturer.
            String reply = sendCommand("AT+CGMI\r", false);
            if (!reply.startsWith("ERROR:")) {
                info.add("Manufacturer: " + reply);
            } else {
                info.add("Manufacturer: Unknown");
            }
            // Get Model.
            reply = sendCommand("AT+CGMM\r", false);
            if (!reply.startsWith("ERROR:")) {
                info.add("Model: " + reply);
            } else {
                info.add("Model: Unknown");
            }
            // Get supported features.
            reply = sendCommand("AT+GCAP\r", false);
            if (!reply.startsWith("ERROR:")) {
                info.add("Features: " + reply);
            }
            // Serial port speed.
            reply = sendCommand("AT+IPR?\r", false);
            int pos = reply.indexOf("+IPR:");
            if (pos != -1) {
                info.add("Serial port speed: "
                        + reply.substring(pos + expectedMinimumSize));
            }
        }
        return info.toArray(new String[info.size()]);
    }

    /**
     * Get amount of sent messages.
     * 
     * @return Amount of sent messages.
     */
    public final int getMessagesSent() {
        return messagesSent;
    }

    /**
     * Set amount of messages sent.
     * 
     * @param value
     *            Amount of sent messages.
     */
    final void setMessagesSent(final int value) {
        messagesSent = value;
    }

    /**
     * Gets amount of received messages.
     * 
     * @return Amount of received messages.
     */
    public final int getMessagesReceived() {
        return messagesReceived;
    }

    /**
     * Sets amount of received messages.
     * 
     * @param value
     *            Amount of received messages.
     */
    private void setMessagesReceived(final int value) {
        messagesReceived = value;
    }

    /**
     * Resets sent and received message counters.
     */
    public final void resetMessageCounters() {
        setMessagesReceived(0);
        setMessagesSent(0);
    }
}