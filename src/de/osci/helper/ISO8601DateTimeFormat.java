package de.osci.helper;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


/**
 * THIS SOFTWARE IS PROVIDED BY THE SKARINGA TEAM AND
 * CONTRIBUTORS ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * SKARINGA TEAM OR ITS CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 *
 * See http://skaringa.sourceforge.net/license.html for license details.
 *
 * Format and parse an ISO 8601 DateTimeFormat used in XML documents.
 * This lexical representation is the ISO 8601 extended format CCYY-MM-DDThh:mm:ss
 * where "CC" represents the century, "YY" the year, "MM" the month and "DD" the day,
 * preceded by an optional leading "-" sign to indicate a negative number.
 * If the sign is omitted, "+" is assumed.
 * The letter "T" is the date/time separator and "hh", "mm", "ss" represent hour, minute and second respectively.
 * This representation may be immediately followed by a "Z" to indicate Coordinated Universal Time (UTC) or,
 * to indicate the time zone, i.e. the difference between the local time and Coordinated Universal Time,
 * immediately followed by a sign, + or -, followed by the difference from UTC represented as hh:mm.
 *
 */
public class ISO8601DateTimeFormat extends DateFormat
{
  /**
   * Creates a new ISO8601DateTimeFormat object.
   */
  public ISO8601DateTimeFormat()
  {
    setCalendar(Calendar.getInstance());
  }

  /**
   * Creates a new ISO8601DateTimeFormat object.
   *
   * @param tz undocumented
   */
  public ISO8601DateTimeFormat(TimeZone tz)
  {
    setCalendar(Calendar.getInstance(tz));
  }

  /**
   * @see DateFormat#parse(String, ParsePosition)
   */
  public Date parse(String text, ParsePosition pos)
  {
    int i = pos.getIndex();

    try
    {
      int year = Integer.valueOf(text.substring(i, i + 4)).intValue();
      i += 4;

      if (text.charAt(i) != '-')
      {
        throw new NumberFormatException();
      }

      i++;

      int month = Integer.valueOf(text.substring(i, i + 2)).intValue() - 1;
      i += 2;

      if (text.charAt(i) != '-')
      {
        throw new NumberFormatException();
      }

      i++;

      int day = Integer.valueOf(text.substring(i, i + 2)).intValue();
      i += 2;

      if (text.charAt(i) != 'T')
      {
        throw new NumberFormatException();
      }

      i++;

      int hour = Integer.valueOf(text.substring(i, i + 2)).intValue();
      i += 2;

      if (text.charAt(i) != ':')
      {
        throw new NumberFormatException();
      }

      i++;

      int mins = Integer.valueOf(text.substring(i, i + 2)).intValue();
      i += 2;

      int secs = 0;

      if ((i < text.length()) && (text.charAt(i) == ':'))
      {
        // handle seconds flexible
        i++;
        secs = Integer.valueOf(text.substring(i, i + 2)).intValue();
        i += 2;
      }

      calendar.set(year, month, day, hour, mins, secs);

      int ms = 0;

      if (text.charAt(i) == '.') // milliseconds
      {
    	int start = ++i;
    	while (i < text.length() &&  Character.isDigit(text.charAt(i)))
    		i++;
        ms = Integer.parseInt(text.substring(start, i));

        if (i-start == 1)
        	ms *= 100;
        else if (i-start == 2)
        	ms *= 10;
        else if (i-start > 3)
        	throw new NumberFormatException("Too many milliseconds.");
      }
      calendar.set(Calendar.MILLISECOND, ms);
      i = parseTZ(i, text);
    }
    catch (NumberFormatException ex)
    {
      pos.setErrorIndex(i);
      return null;
    }
    catch (IndexOutOfBoundsException ex)
    {
      pos.setErrorIndex(i);
      return null;
    }
    finally
    {
      pos.setIndex(i);
    }

    return calendar.getTime();
  }

  /**
   * undocumented
   *
   * @param i undocumented
   * @param text undocumented
   *
   * @return undocumented
   *
   * @throws NumberFormatException undocumented
   */
  protected int parseTZ(int i, String text) throws NumberFormatException
  {
    if (i < text.length())
    {
      // check and handle the zone/dst offset
      int offset = 0;

      if (text.charAt(i) == 'Z')
      {
        offset = 0;
        i++;
      }
      else
      {
        int sign = 1;

        if (text.charAt(i) == '-')
        {
          sign = -1;
        }
        else if (text.charAt(i) != '+')
        {
          throw new NumberFormatException();
        }

        i++;

        int offset_h = Integer.valueOf(text.substring(i, i + 2)).intValue();
        i += 2;

        if (text.charAt(i) != ':')
        {
          throw new NumberFormatException();
        }

        i++;

        int offset_min = Integer.valueOf(text.substring(i, i + 2)).intValue();
        i += 2;
        offset = ((offset_h * 60) + offset_min) * 60000 * sign;
      }

      int offset_cal = calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET);
      calendar.add(Calendar.MILLISECOND, offset_cal - offset);
    }

    return i;
  }

  /**
   * @see DateFormat#format(Date, StringBuffer, FieldPosition)
   */
  public StringBuffer format(Date date, StringBuffer sbuf, FieldPosition fieldPosition)
  {
    calendar.setTime(date);
    writeCCYYMM(sbuf);
    sbuf.append('T');
    writehhmmss(sbuf);
    writeTZ(sbuf);

    return sbuf;
  }

  /**
   * undocumented
   *
   * @param sbuf undocumented
   */
  protected void writeTZ(StringBuffer sbuf)
  {
    int offset = calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET);

    if (offset == 0)
    {
      sbuf.append('Z');
    }
    else
    {
      int offset_h = offset / 3600000;
      int offset_min = (offset % 3600000) / 60000;

      if (offset >= 0)
      {
        sbuf.append('+');
      }
      else
      {
        sbuf.append('-');
        offset_h = 0 - offset_h;
        offset_min = 0 - offset_min;
      }

      appendInt(sbuf, offset_h, 2);
      sbuf.append(':');
      appendInt(sbuf, offset_min, 2);
    }
  }

  /**
   * undocumented
   *
   * @param sbuf undocumented
   */
  protected void writehhmmss(StringBuffer sbuf)
  {
    int hour = calendar.get(Calendar.HOUR_OF_DAY);
    appendInt(sbuf, hour, 2);
    sbuf.append(':');

    int mins = calendar.get(Calendar.MINUTE);
    appendInt(sbuf, mins, 2);
    sbuf.append(':');

    int secs = calendar.get(Calendar.SECOND);
    appendInt(sbuf, secs, 2);
  }

  /**
   * undocumented
   *
   * @param sbuf undocumented
   */
  protected void writeCCYYMM(StringBuffer sbuf)
  {
    int year = calendar.get(Calendar.YEAR);
    appendInt(sbuf, year, 4);

    String month;

    switch (calendar.get(Calendar.MONTH))
    {
    case Calendar.JANUARY:
      month = "-01-";

      break;

    case Calendar.FEBRUARY:
      month = "-02-";

      break;

    case Calendar.MARCH:
      month = "-03-";

      break;

    case Calendar.APRIL:
      month = "-04-";

      break;

    case Calendar.MAY:
      month = "-05-";

      break;

    case Calendar.JUNE:
      month = "-06-";

      break;

    case Calendar.JULY:
      month = "-07-";

      break;

    case Calendar.AUGUST:
      month = "-08-";

      break;

    case Calendar.SEPTEMBER:
      month = "-09-";

      break;

    case Calendar.OCTOBER:
      month = "-10-";

      break;

    case Calendar.NOVEMBER:
      month = "-11-";

      break;

    case Calendar.DECEMBER:
      month = "-12-";

      break;

    default:
      month = "-NA-";

      break;
    }

    sbuf.append(month);

    int day = calendar.get(Calendar.DAY_OF_MONTH);
    appendInt(sbuf, day, 2);
  }

  /**
   * undocumented
   *
   * @param buf undocumented
   * @param value undocumented
   * @param length undocumented
   */
  protected void appendInt(StringBuffer buf, int value, int length)
  {
    int len1 = buf.length();
    buf.append(value);

    int len2 = buf.length();

    for (int i = len2; i < (len1 + length); ++i)
    {
      buf.insert(len1, '0');
    }
  }

/*     public static void main(String[] args)
     {
       try
       {
         System.out.println(new ISO8601DateTimeFormat().format(new Date()));
         System.out.println(new ISO8601DateTimeFormat().parse("2005-04-04T13:00+02:00").getTime());
         System.out.println(new ISO8601DateTimeFormat().parse("2005-04-04T13:00:00Z").getTime());
         System.out.println(new ISO8601DateTimeFormat().parse("2005-04-04T13:00:00+02:00").getTime());
         System.out.println(new ISO8601DateTimeFormat().parse("2005-04-04T13:00:00.1").getTime());
         System.out.println(new ISO8601DateTimeFormat().parse("2005-04-04T13:00:00.12").getTime());
         System.out.println(new ISO8601DateTimeFormat().parse("2005-04-04T13:00:00.12Z").getTime());
         System.out.println(new ISO8601DateTimeFormat().parse("2005-04-04T13:00:00.123").getTime());
         System.out.println(new ISO8601DateTimeFormat().parse("2005-04-04T13:00:00.123+00:00").getTime());
     }
       catch (Exception ex)
       {
         ex.printStackTrace();
       }
     }
*/
}
