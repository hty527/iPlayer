package com.android.videoplayer.utils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @time 2016/11/14 9:06
 * @des $时间格式化器
 */
public class DateParseUtil {

    public static final String ENG_DATE_FROMAT = "EEE, d MMM yyyy HH:mm:ss z";
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    public static final String YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String YYYY = "yyyy";
    public static final String MM = "MM";
    public static final String DD = "dd";

    private static final DecimalFormat decimalFormat = new DecimalFormat();


    public static String formatW(int vaule){
        if(vaule>=10000){
            float l = vaule/10000.0f;

            return format(l,"#.#'W'");
        }
        return String.valueOf(vaule);
    }

    public static String format(float vaule,String pattern){
        decimalFormat.applyPattern(pattern);
        return decimalFormat.format(vaule);
    }
    /**
     * @param date
     * @return
     * @描述 —— 格式化日期对象
     */
    public static Date date2date(Date date, String formatStr) {
        SimpleDateFormat sdf = new SimpleDateFormat(formatStr);
        String str = sdf.format(date);
        try {
            date = sdf.parse(str);
        } catch (Exception e) {
            return null;
        }
        return date;
    }

    /**
     * @param date
     * @return
     * @创建日期 2012-7-13
     * @创建时间 下午12:24:19
     * @描述 —— 时间对象转换成字符串
     */
    public static String date2string(Date date, String formatStr) {
        String strDate = "";
        SimpleDateFormat sdf = new SimpleDateFormat(formatStr);
        strDate = sdf.format(date);
        return strDate;
    }

    /**
     * @return
     * @创建日期 2012-7-13
     * @创建时间 下午12:24:19
     * @描述 —— sql时间对象转换成字符串
     */
    public static String timestamp2string(Timestamp timestamp, String formatStr) {
        String strDate = "";
        SimpleDateFormat sdf = new SimpleDateFormat(formatStr);
        strDate = sdf.format(timestamp);
        return strDate;
    }

    /**
     * @param dateString
     * @param formatStr
     * @return
     * @创建日期 2012-7-13
     * @创建时间 下午1:09:24
     * @描述 —— 字符串转换成时间对象
     */
    public static Date string2date(String dateString, String formatStr) {
        Date formateDate = null;
        DateFormat format = new SimpleDateFormat(formatStr);
        try {
            formateDate = format.parse(dateString);
        } catch (ParseException e) {
            return null;
        }
        return formateDate;
    }

    /**
     * @param date
     * @return
     * @创建日期 2012-10-10
     * @创建时间 上午09:18:36
     * @描述 —— Date类型转换为Timestamp类型
     */
    public static Timestamp date2timestamp(Date date) {
        if (date == null)
            return null;
        return new Timestamp(date.getTime());
    }

    /**
     * @创建日期 2012-9-13
     * @创建时间 下午05:02:57
     * @描述 —— 获得当前年份
     */
    public static String getNowYear() {
        SimpleDateFormat sdf = new SimpleDateFormat(YYYY);
        return sdf.format(new Date());
    }

    /**
     * @return
     * @创建日期 2012-9-13
     * @创建时间 下午05:03:15
     * @描述 —— 获得当前月份
     */
    public static String getNowMonth() {
        SimpleDateFormat sdf = new SimpleDateFormat(MM);
        return sdf.format(new Date());
    }

    /**
     * @return
     * @创建日期 2013-01-24
     * @创建时间 08:41:47
     * @描述 —— 获得当前日期中的日
     */
    public static String getNowDay(){
        SimpleDateFormat sdf = new SimpleDateFormat(DD);
        return sdf.format(new Date());
    }

    /**
     * @param time
     * @retur
     * @创建日期 2012-6-17
     * @创建时间 上午10:19:31
     * @描述 —— 指定时间距离当前时间的中文信息
     */
    public static String getNow(long time) {
        Calendar cal = Calendar.getInstance();
        long timel = cal.getTimeInMillis() - time;
        if (timel / 1000 < 60) {
            return "1分钟以内";
        } else if (timel / 1000 / 60 < 60) {
            return timel / 1000 / 60 + "分钟前";
        } else if (timel / 1000 / 60 / 60 < 24) {
            return timel / 1000 / 60 / 60 + "小时前";
        } else {
            return getTimeForString(time);
        }
    }

    public static String getTimeForString(long time){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(time);
    }

    public static int getTodayWeekSundy() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date(System.currentTimeMillis()));
        return c.get(Calendar.DAY_OF_WEEK);
    }
}
