/*
  * The MIT License
  *
  * Original work sponsored and donated by National Board of e-Health (NSI), Denmark (http://www.nsi.dk)
  * Further enhancement before move to Codehaus sponsored and donated by Lakeside A/S (http://www.lakeside.dk)
  *
  * Copyright (c) to all contributors
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of
  * this software and associated documentation files (the "Software"), to deal in
  * the Software without restriction, including without limitation the rights to
  * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
  * of the Software, and to permit persons to whom the Software is furnished to do
  * so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all
  * copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  *
  * $HeadURL: https://svn.codehaus.org/mojo/trunk/mojo/chronos/chronos-report-maven-plugin/src/main/java/org/codehaus/mojo/chronos/common/model/ResponsetimeSample.java $
  * $Id: ResponsetimeSample.java 15770 2012-01-24 12:34:33Z soelvpil $
  */
package org.codehaus.mojo.chronos.common.model;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Contains info from a jmeter logentry.
 *
 * @author ksr@lakeside.dk
 */
public class ResponsetimeSample
{
    private final int responsetime;

    private final long timestamp;

    private final boolean success;

    private final String threadId;


    public ResponsetimeSample( int responsetime, long timestamp, boolean success, String threadId )
    {
        this.responsetime = responsetime;
        this.timestamp = timestamp;
        this.success = success;
        this.threadId = threadId;
    }

    /**
     * @return Returns the responsetime.
     * @see org.codehaus.mojo.chronos.common.model.ResponsetimeSample#getResponsetime()
     */
    public final int getResponsetime()
    {
        return responsetime;
    }

    /**
     * @return Returns the timestamp.
     * @see org.codehaus.mojo.chronos.common.model.ResponsetimeSample#getStartTime()
     */
    public final long getStartTime()
    {
        return timestamp;
    }

    public final long getEndTime()
    {
        return timestamp + responsetime;
    }

    /**
     * @return Returns the success.
     * @see org.codehaus.mojo.chronos.common.model.ResponsetimeSample#isSuccess()
     */
    public final boolean isSuccess()
    {
        return success;
    }

    /**
     * @return Returns the threadgroupId.
     * @see org.codehaus.mojo.chronos.common.model.ResponsetimeSample#getThreadId()
     */
    public final String getThreadId()
    {
        return threadId;
    }


}