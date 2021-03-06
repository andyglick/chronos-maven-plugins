package org.codehaus.mojo.jmeterreport.it;

import junit.framework.TestCase;

public class AppJunitSample extends TestCase {
    static int count = 0;

    public AppJunitSample()
    {

    }
    
    public AppJunitSample(String name)
    {
        super(name);
    }

    public void setUp()
    {
    }

    public void tearDown()
    {
    }
    

    public void testApp() {
        long sleepTime = ++count;
        try {
            Thread.sleep(sleepTime/1000 + 50 *  ((int)Math.random()));
        }
        catch ( InterruptedException e )
        {
            Thread.currentThread().interrupt();
        }
    }
}
