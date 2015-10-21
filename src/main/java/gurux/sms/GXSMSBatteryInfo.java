package gurux.sms;

/**
 * SMS info keep information from state of modem.
 * 
 * @author Gurux Ltd.
 *
 */
public class GXSMSBatteryInfo {

    /**
     * Battery capacity.
     */
    private int batteryCapacity;
    /**
     * Average power consumption.
     */
    private int averagePowerConsumption;

    /**
     * Constructor.
     */
    GXSMSBatteryInfo() {

    }

    /**
     * Gets battery capacity.
     * 
     * Battery capacity 0, 20, 40, 60, 80, 100 percent of remaining capacity (6
     * steps) 0 indicates that either the battery is exhausted or the capacity
     * value is not available.
     * 
     * @return Battery capacity
     */
    public final int getBatteryCapacity() {
        return batteryCapacity;
    }

    /**
     * Sets battery capacity.
     * 
     * @param value
     *            battery capacity.
     */
    final void setBatteryCapacity(final int value) {
        batteryCapacity = value;
    }

    /**
     * Gets average power consumption. Average power consumption i mA.
     * 
     * @return Average power consumption.
     */
    public final int getAveragePowerConsumption() {
        return averagePowerConsumption;
    }

    /**
     * Sets average power consumption. Average power consumption i mA.
     * 
     * @param value
     *            average power consumption.
     */
    final void setAveragePowerConsumption(final int value) {
        averagePowerConsumption = value;
    }

}
