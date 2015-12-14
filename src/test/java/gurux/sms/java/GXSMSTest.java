package gurux.sms.java;

import gurux.sms.GXSMS;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for terminal media.
 */
public class GXSMSTest extends TestCase {
    /**
     * Create the test case.
     *
     * @param testName
     *            Name of the test case.
     */
    public GXSMSTest(final String testName) {
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
    public final void testNativeLibrary() {
        GXSMS.getPortNames();
    }
}
