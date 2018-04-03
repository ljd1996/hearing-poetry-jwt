package com.poetry.hearing.requestCached;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

public class FastHttpDateFormat {
    protected static final SimpleDateFormat format;
    protected static final SimpleDateFormat[] formats;
    protected static final TimeZone gmtZone;
    protected static long currentDateGenerated;
    protected static String currentDate;
    protected static final HashMap<Long, String> formatCache;
    protected static final HashMap<String, Long> parseCache;

    public FastHttpDateFormat() {
    }

    public static String formatDate(long value, DateFormat threadLocalformat) {
        String cachedDate = null;
        Long longValue = value;

        try {
            cachedDate = (String)formatCache.get(longValue);
        } catch (Exception var12) {
            ;
        }

        if (cachedDate != null) {
            return cachedDate;
        } else {
            Date dateValue = new Date(value);
            String newDate;
            HashMap var7;
            if (threadLocalformat != null) {
                newDate = threadLocalformat.format(dateValue);
                var7 = formatCache;
                synchronized(formatCache) {
                    updateCache(formatCache, longValue, newDate);
                }
            } else {
                var7 = formatCache;
                synchronized(formatCache) {
                    newDate = format.format(dateValue);
                    updateCache(formatCache, longValue, newDate);
                }
            }

            return newDate;
        }
    }

    public static String getCurrentDate() {
        long now = System.currentTimeMillis();
        if (now - currentDateGenerated > 1000L) {
            SimpleDateFormat var2 = format;
            synchronized(format) {
                if (now - currentDateGenerated > 1000L) {
                    currentDateGenerated = now;
                    currentDate = format.format(new Date(now));
                }
            }
        }

        return currentDate;
    }

    private static Long internalParseDate(String value, DateFormat[] formats) {
        Date date = null;

        for(int i = 0; date == null && i < formats.length; ++i) {
            try {
                date = formats[i].parse(value);
            } catch (ParseException var5) {
                ;
            }
        }

        return date == null ? null : new Long(date.getTime());
    }

    public static long parseDate(String value, DateFormat[] threadLocalformats) {
        Long cachedDate = null;

        try {
            cachedDate = (Long)parseCache.get(value);
        } catch (Exception var9) {
            ;
        }

        if (cachedDate != null) {
            return cachedDate;
        } else {
            Long date;
            HashMap var4;
            if (threadLocalformats != null) {
                date = internalParseDate(value, threadLocalformats);
                var4 = parseCache;
                synchronized(parseCache) {
                    updateCache(parseCache, value, date);
                }
            } else {
                var4 = parseCache;
                synchronized(parseCache) {
                    date = internalParseDate(value, formats);
                    updateCache(parseCache, value, date);
                }
            }

            return date == null ? -1L : date;
        }
    }

    private static void updateCache(HashMap cache, Object key, Object value) {
        if (value != null) {
            if (cache.size() > 1000) {
                cache.clear();
            }

            cache.put(key, value);
        }
    }

    static {
        format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        formats = new SimpleDateFormat[]{new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US), new SimpleDateFormat("EEEEEE, dd-MMM-yy HH:mm:ss zzz", Locale.US), new SimpleDateFormat("EEE MMMM d HH:mm:ss yyyy", Locale.US)};
        gmtZone = TimeZone.getTimeZone("GMT");
        format.setTimeZone(gmtZone);
        formats[0].setTimeZone(gmtZone);
        formats[1].setTimeZone(gmtZone);
        formats[2].setTimeZone(gmtZone);
        currentDateGenerated = 0L;
        currentDate = null;
        formatCache = new HashMap();
        parseCache = new HashMap();
    }
}