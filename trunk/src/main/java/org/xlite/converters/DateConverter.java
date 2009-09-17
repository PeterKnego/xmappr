/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite.converters;

import org.xlite.XliteException;
import org.xlite.XliteConfigurationException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Map;
import java.util.HashMap;

public class DateConverter extends ValueConverter {

    ThreadLocal threadLocal = new ThreadLocal();

    public boolean canConvert(Class type) {
        return java.util.Date.class.isAssignableFrom(type);
    }

    public Object fromValue(String value, String format, Class targetType, Object targetObject) {
        try {
            return getDateFormatter(format).parse(value);
        } catch (ParseException e) {
            throw new XliteException("Error: can not parse Date with given format. Input XML: '" + value +
                    "'. Configured format: '" + format + "'. ", e);
        }
    }

    public String toValue(Object object, String format) {
        return getDateFormatter(format).format(object);
    }

    private DateFormat getDateFormatter(String format) {
        if (format == null || format.length() == 0) {
            throw new XliteConfigurationException("Error: DateConverter must have a format value defined on the " +
                    "target field.");
        }
        // DateFormat is very slow to initialize and not thread safe, so we initialize one per thread and save it
        Map<String, DateFormat> formatters = (Map<String, DateFormat>) threadLocal.get();
        if (formatters == null) {
            formatters = new HashMap<String, DateFormat>();
            threadLocal.set(formatters);
        }
        DateFormat dateFormat = formatters.get(format);
        if (dateFormat == null) {
            try {
                dateFormat = new SimpleDateFormat(format);
            } catch (IllegalArgumentException e) {
                throw new XliteConfigurationException("Error: Wrong conversion format for DateConverter. ", e);
            }
            formatters.put(format, dateFormat);
        }
        return dateFormat;
    }
}
