package gurux.sms;

/**
 * SMS info keep information from state of modem.
 * 
 * @author Gurux Ltd.
 *
 */
public class GXSMSSignalQualityInfo {

    /**
     * Received Signal Strength Indication.
     */
    private int rssi;
    /**
     * Bit Error Rate.
     */
    private int ber;

    /**
     * Constructor.
     */
    GXSMSSignalQualityInfo() {

    }

    /**
     * Get Received Signal Strength Indication.
     * 
     * @return Received Signal Strength Indication.
     */
    public final int getRssi() {
        return rssi;
    }

    /**
     * Set Received Signal Strength Indication.
     * 
     * @param value
     *            Received Signal Strength Indication.
     * 
     */
    final void setRssi(final int value) {
        rssi = value;
    }

    /**
     * Set Bit Error Rate.
     * 
     * @return Bit Error Rate.
     */
    public final int getBer() {
        return ber;
    }

    /**
     * Set Bit Error Rate.
     * 
     * @param value
     *            Bit Error Rate.
     * 
     */
    final void setBer(final int value) {
        ber = value;
    }

}
