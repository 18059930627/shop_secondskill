package com.qf.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @Author chenzhongjun
 * @Date 2019/12/28
 */
public class DateUtil {

    /**
     * 得到当前时间的下n个小时的时间
     *
     * @param n
     * @return
     */
    public static Date getNextNDate(int n) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, n);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date time = calendar.getTime();
        return time;
    }


    /**
     * 将时间类型按照自己的格式生成字符串
     *
     * @param time
     * @param pattern
     * @return
     */
    public static String date2String(Date time, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(time);
    }

}
