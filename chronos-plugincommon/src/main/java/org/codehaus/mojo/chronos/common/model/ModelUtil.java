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
  * $HeadURL: https://svn.codehaus.org/mojo/trunk/sandbox/chronos/chronos-report-maven-plugin/src/main/java/org/codehaus/mojo/chronos/common/model/HistoricSamples.java $
  * $Id: HistoricSamples.java 14838 2011-10-14 11:00:31Z soelvpil $
  */
package org.codehaus.mojo.chronos.common.model;

import org.jfree.data.time.Millisecond;
import org.jfree.data.time.Second;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ModelUtil
{
    static final int IGNORED_YEAR = 1970;

    /**
     * Converst the specified number of milli seconds into a <code>Millisecond</code> object.
     *
     * @param millisecond The time represented in milli seconds.
     * @return The corresponding <code>Millisecond</code> instance.
     */
    public static Millisecond createMillis( long millisecond )
    {
        Date date = new Date( millisecond );
        return new Millisecond( date, TimeZone.getTimeZone( "GMT" ), Locale.getDefault() );
    }

    public static Second createSecond( long millisecond )
    {
        Date date = new Date( millisecond );
        return new Second( date, TimeZone.getTimeZone( "GMT" ), Locale.getDefault() );
    }

    /**
     * get the second immediately after the input argument.
     * Note that Second#next() returns incorrect result due to time zone differences.
     */
    public static Second next( Second second)
    {
        Date date = new Date( second.getFirstMillisecond() + 1000 );
        return new Second( date, TimeZone.getTimeZone( "GMT" ), Locale.getDefault() );
    }
}
