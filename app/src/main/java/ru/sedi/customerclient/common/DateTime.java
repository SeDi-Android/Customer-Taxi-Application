package ru.sedi.customerclient.common;

import android.content.Context;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import ru.sedi.customer.R;

public class DateTime {

    public static final String TIME = "HH:mm";
    public static final String DATE = "dd.MM.yyyy";
    public static final String DATE_TIME = "dd.MM.yyyy HH:mm";
    public static final String FULL_DATE_TIME = "dd.MM.yyyy HH:mm:ss";
    public static final String WEB_DATE = "yyyy-MM-dd'T'HH:mm:ss";

    //------------------------------
    public static final long SECOND = 1000;
    public static final long MINUTE = SECOND * 60;
    public static final long HOUR = MINUTE * 60;
    public static final long DAY = HOUR * 24;
    //------------------------------

    private Calendar m_calendar;

    public DateTime() {
        Create(0, 0, 0, 0, 0, 0);

    }

    public DateTime(int year, int month, int day) {
        Create(year, month, day, 0, 0, 0);
    }

    public DateTime(int year, int month, int day, int hour, int minute, int second) {
        Create(year, month, day, hour, minute, second);
    }

    public DateTime(Date date) {
        m_calendar = Calendar.getInstance();
        m_calendar.setTime(date);
    }

    private void Create(int year, int month, int day, int hour, int minute, int second) {
        m_calendar = Calendar.getInstance();
        m_calendar.set(year, month, day, hour, minute, second);
    }

    //------------------------------

    public int getYear() {
        return m_calendar.get(Calendar.YEAR);
    }

    public void setYear(int year) {
        m_calendar.set(Calendar.YEAR, year);
    }

    public int getMonth() {
        return Integer.parseInt(String.format("%1$tm", m_calendar));
    }

    public void setMonth(int month) {
        m_calendar.set(Calendar.MONTH, month);
    }

    public int getDay() {
        return m_calendar.get(Calendar.DAY_OF_MONTH);
    }

    public int getDayOfWeek() {
        return m_calendar.get(Calendar.DAY_OF_WEEK);
    }

    public void setDay(int day) {
        m_calendar.set(Calendar.DAY_OF_MONTH, day);
    }

    public int getHour() {
        return m_calendar.get(Calendar.HOUR_OF_DAY);
    }

    public void setHour(int hour) {
        m_calendar.set(Calendar.HOUR_OF_DAY, hour);
    }

    public int getMinute() {
        return m_calendar.get(Calendar.MINUTE);
    }

    public void setMinute(int minute) {
        m_calendar.set(Calendar.MINUTE, minute);
    }

    public void addDay(int day) {
        m_calendar.add(Calendar.DAY_OF_MONTH, day);
    }

    public void addHour(int hour) {
        m_calendar.add(Calendar.HOUR_OF_DAY, hour);
    }

    public void addMinute(int minute) {
        m_calendar.add(Calendar.MINUTE, minute);
    }

    public long getTime() {
        return m_calendar.getTime().getTime();
    }

    public static DateTime Now() {
        Calendar calendar = Calendar.getInstance();
        return new DateTime(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
    }

    public static double DiffSecond(DateTime value1, DateTime value2) {
        try {
            return Math.abs((value1.m_calendar.getTimeInMillis() - value2.m_calendar.getTimeInMillis()) / 1000);
        } catch (Exception e) {
            return 0;
        }
    }

    public Date getDate() {
        return m_calendar.getTime();
    }

    public String toString(String format) {
        SimpleDateFormat df = new SimpleDateFormat(format);
        return df.format(m_calendar.getTime());
    }

    public static DateTime fromString(String input, String format) {
        SimpleDateFormat df = new SimpleDateFormat(format);
        try {
            Date date = df.parse(input);
            return new DateTime(date);
        } catch (ParseException e) {
            LogUtil.log(e);
            return DateTime.Now();
        }
    }

    public static DateTime nowWithCorrect(int h, int m) {
        DateTime now = Now();
        now.addHour(h);
        now.addMinute(m);
        return now;
    }

    public void addMonth(int i) {
        m_calendar.add(Calendar.MONTH, i);
    }

    public static String dateStringFromMins(Context context, int mins) {
        if(mins == 0){
            return context.getString(R.string.one_minute);
        }

        StringBuilder builder = new StringBuilder();

        long day = TimeUnit.MINUTES.toDays(mins);
        if (day > 0) {
            builder.append(" ").append(day).append(" ").append(context.getString(R.string.days));
            mins = (int) (mins - TimeUnit.DAYS.toMinutes(day));
        }

        long hours = TimeUnit.MINUTES.toHours(mins);
        if (hours > 0) {
            builder.append(" ").append(hours).append(" ").append(context.getString(R.string.hours));
            mins = (int) (mins - TimeUnit.HOURS.toMinutes(hours));
        }

        if (mins > 0) {
            builder.append(" ").append(mins).append(" ").append(context.getString(R.string.minutes));
        }

        return builder.toString();
    }
}