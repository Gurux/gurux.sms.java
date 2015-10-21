package gurux.sms;

import gurux.sms.GXSMS;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for terminal media.
 */
public class GXSMSTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName
     *            name of the test case
     */
    public GXSMSTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(GXSMSTest.class);
    }

    /**
     * Test native library load.
     */
    public void testNativeLibrary() {
        GXSMS.getPortNames();
    }
}
