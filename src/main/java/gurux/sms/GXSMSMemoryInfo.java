package gurux.sms;

/**
 * SMS info keep information from memory capacity.
 * 
 * @author Gurux Ltd.
 *
 */
public class GXSMSMemoryInfo {

    /**
     * SMSs in memory.
     */
    private int count;
    /**
     * Maximum SMS count.
     */
    private int maximum;

    /**
     * Constructor.
     */
    GXSMSMemoryInfo() {

    }

    /**
     * Gets amount of SMSs in memory.
     * 
     * @return Battery capacity
     */
    public final int getCount() {
        return count;
    }

    /**
     * Sets amount of SMSs in memory.
     * 
     * @param value
     *            battery capacity.
     */
    final void setCount(final int value) {
        count = value;
    }

    /**
     * Get maximum SMS count.
     * 
     * @return Maximum SMS count.
     */
    public final int getMaximum() {
        return maximum;
    }

    /**
     * Set maximum SMS count.
     * 
     * @param value
     *            Maximum SMS count.
     * 
     */
    final void setMaximum(final int value) {
        maximum = value;
    }

}
