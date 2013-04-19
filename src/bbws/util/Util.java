/*
    Blackboard WebServices Helper
    Copyright (C) 2011-2013 Andrew Martin, Newcastle University

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bbws.util;

//java
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.SimpleDateFormat;

//javax
import javax.xml.ws.WebServiceException;

public class Util
{
    public static String checkAndTrimParam(String param) throws Exception
    {
	if(param!=null && !param.equalsIgnoreCase(""))
	{
	    return param.trim();
	}
	throw new Exception("Invalid parameter: '"+param+"'");
    }

    public static Boolean checkParam(String param)
    {
	if(param!=null && !param.equalsIgnoreCase(""))
	{
	    return true;
	}
	return false;
    }

    public static String getDateTimeFromCalendar(Calendar c)
    {
        try
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.format(c.getTime());
        }
        catch(Exception e)
        {
            return "";
        }
    }

    public static Calendar getCalendarObjFromDateTimeString(String dateTime)
    {
        Calendar cal = Calendar.getInstance();
        if(dateTime!=null && dateTime.equals(""));
        {
            try
            {
                cal.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateTime));
            }
            catch(Exception e)
            {
                throw new WebServiceException("Error: Invalid date specified");
            }
        }
        return cal;
    }

    public static Boolean handleNullValue(Boolean value)
    {
        if(value==null)
        {
            return false;
        }
        return value;
    }

    public static boolean isAMatch(Pattern pattern, String searchStr)
    {
        boolean match = false;
        Matcher matcher = pattern.matcher(searchStr);
        while (matcher.find())
        {
            //System.err.println("I found the text "+matcher.group()+" starting at index "+matcher.start()+" and ending at index "+matcher.end());
            match = true;
        }
        return match;
    }

    /**
     * This method is provided as a convenience to developers
     *
     * Usage: If creating new methods, replace...
     *
     * authoriseMethod(magKey,"bbMyMethod");
     *
     * with...
     *
     * authoriseMethod(magKey,getMethodName());
     *
     * @return Name of the current method
     */
    public static String getMethodName()
    {
        Throwable throwable = new Throwable();
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        StackTraceElement stackTraceElement = stackTrace[1];  // index is 1 to get the name of the method that called this method
        return stackTraceElement.getMethodName();
    }
}
