package org.codehaus.mojo.chronos.timersample;

import org.junit.Test;

public class TimerSampleReportOnlyTest
{
    @Test
    public void testFastMethod()
        throws InterruptedException
    {
        Thread.sleep( 10L );
    }

    @Test
    public void testSlowMethod()
        throws InterruptedException
    {
        Thread.sleep( 100 );
    }

    @Test
    public void testFluctuatingMethod()
        throws InterruptedException
    {
        Thread.sleep( ((long) Math.random()*100)  );
    }

}
