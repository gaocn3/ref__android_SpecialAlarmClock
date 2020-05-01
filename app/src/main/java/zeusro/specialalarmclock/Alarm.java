package zeusro.specialalarmclock;

import android.media.RingtoneManager;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Z on 2015/11/16.
 * 一个闹钟的数据     包含闹钟项参数数据与操作
 *   基本上是单纯的闹钟项数据， 供各模块使用。
 */
public class Alarm implements Serializable {

    private int id;     // 编号
    private Boolean alarmActive = true;     // 闹钟项是否激活
    private Calendar alarmTime = Calendar.getInstance();     // 闹钟设置的时间     闹钟项中的日历时间
    private int[] days = {Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY,};     // 闹铃重复的日子     按天重复， 周里哪些日子需要重复闹钟
    private int days_num = days.length;
    private String alarmTonePath = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString();     // 闹铃音文件路径     TYPE_NOTIFICATION  TYPE_RINGTONE
    private Boolean vibrate = true;     // 振动状态     闹钟项是否振动
    private String alarmName = "极简闹钟";     // 闹钟名


    public Alarm() {
    }


    /* 闹铃重复的周日子里添加一天 */
        /* 闹铃重复的星期几日子里添加一天 */
        /* 按星期的某天循环，周日子里添加一天 */
    public void addDay(int day) {
        boolean contains = false;
        int[] temp = getDays();     // 获取已有的重复日子
        int num = 0;

        if(temp != null/* || days_num == 0*/) {
            for (int d : temp)
                if (d == day)
                    contains = true;     // 包含周里的某天

            num = temp.length;
        }

        if (!contains) {
            /*int[] result = new int[temp.length + 1];*/
            int[] result = new int[num + 1];

            /* 拷贝已有的重复日子 */
            for (int i = 0; i < num; i++) {
                result[i] = temp[i];
            }
            result[num] = day;     // 添加一个日子
            setDays(result);     // 设置重复日子
        }
    }

    /* 闹铃重复的周日子里删除一天 */
    public void removeDay(int day) {
        boolean contains = false;
        int[] temp = getDays();
        int num = temp == null ? 0 : temp.length;
        int[] result = null; //new int[num];
        int xiabiao = num;

        for (int i = 0; i < num; i++) {
            if (temp[i] == day) { // 找到指定的周日子 day
                contains = true;
                xiabiao = i;

                if(num == 1/* && i == 0*/) { // 删除的正好是余下的最后一个
//                    days_num = 0;
//                    setDays(null);
                    result = null;
//                    return;
                }
                else {
                    result = new int[num - 1];
                }

                break;     // my add: 只用找到第一个
            }
        }

        if (contains) { // 拷贝余下的周日子
            for (int i = 0; i < xiabiao; i++) {
                result[i] = temp[i];
            }
            for (int i = xiabiao + 1; i < num; i++) {
                result[i - 1] = temp[i];
            }

            setDays(result);
        }
    }

    /**
     * @return the alarmActive
     * 闹钟项是否激活
     */
    public Boolean IsAlarmActive() {
        return alarmActive;
    }

    /**
     * 这一天是否重复
     *     周几是否在重复的日子里
     * @param dayOfWeek
     * @return
     */
    public boolean IsRepeat(int dayOfWeek) {
        if (days == null || days.length < 1)
            return false;

        for (int i = 0; i < days.length; i++) {
            if (days[i] == dayOfWeek)
                return true;
        }

        return false;
    }

    /**
     * @param alarmActive the alarmActive to set
     * 设置激活状态
     */
    public void setAlarmActive(Boolean alarmActive) {
        this.alarmActive = alarmActive;
    }

    /**
     * @return the alarmTime
     * 返回闹钟设置的时间
     */
    public Calendar getAlarmTime() {
//        if (alarmTime.before(Calendar.getInstance()))
//            alarmTime.add(Calendar.DAY_OF_MONTH, 1);
        return alarmTime;
    }

    /**
     * @return the alarmTime
     * 拼装闹钟时间字符串
     */
    public String getAlarmTimeString() {
//        String time = "";
//        if (alarmTime.get(Calendar.HOUR_OF_DAY) <= 9)
//            time += "0";
//        time += String.valueOf(alarmTime.get(Calendar.HOUR_OF_DAY));
//        time += ":";
//
//        if (alarmTime.get(Calendar.MINUTE) <= 9)
//            time += "0";
//        time += String.valueOf(alarmTime.get(Calendar.MINUTE));
//        return time;

        return String.format("%02d:%02d", alarmTime.get(Calendar.HOUR_OF_DAY), alarmTime.get(Calendar.MINUTE));
    }

    /**
     * @param alarmTime the alarmTime to set
     * 设置闹钟时间
     */
    public void setAlarmTime(Calendar alarmTime) {
        this.alarmTime = alarmTime;
    }

    /**
     * @param alarmTime the alarmTime to set
     * 从字符串提取时间， 然后设置闹钟时间
     */
    public void setAlarmTime(String alarmTime) {
        String[] timePieces = alarmTime.split(":");
        Calendar newAlarmTime = Calendar.getInstance();
        newAlarmTime.set(Calendar.HOUR_OF_DAY,
                Integer.parseInt(timePieces[0]));
        newAlarmTime.set(Calendar.MINUTE, Integer.parseInt(timePieces[1]));
        newAlarmTime.set(Calendar.SECOND, 0);
        
        setAlarmTime(newAlarmTime);
    }


    /**
     * @return the repeatDays
     * 获取闹铃重复的周日子集合     获取已有的循环日子     <del>返回星期数字</>
     */
    public int[] getDays() {
        return days;
    }

    /**
     * @param days the repeatDays to set
     * 设置闹铃重复的周日子集合     <del>设置循环日子
     */
    public void setDays(int[] days) {
//        this.days = null;
        this.days = days;

        if(days  == null) {
            days_num = 0;
        }
        else {
            days_num = days.length;
        }
    }

    /**
     * @return the alarmTonePath
     * 获得闹铃音路径
     */
    public String getAlarmTonePath() {
        return alarmTonePath;
    }

    /**
     * @param alarmTonePath the alarmTonePath to set
     * 设置闹铃音路径
     */
    public void setAlarmTonePath(String alarmTonePath) {
        this.alarmTonePath = alarmTonePath;
    }

    /**
     * @return the vibrate
     * 振动状态
     */
    public Boolean IsVibrate() {
        return vibrate;
    }

    /**
     * @param vibrate the vibrate to set
     * 设置振动状态
     */
    public void setVibrate(Boolean vibrate) {
        this.vibrate = vibrate;
    }

    /**
     * @return the alarmName
     * 闹钟名
     */
    public String getAlarmName() {
        return alarmName;
    }

    /**
     * @param alarmName the alarmName to set
     * 设置闹钟名
     */
    public void setAlarmName(String alarmName) {
        this.alarmName = alarmName;
    }

    /* 返回 ID */
    public int getId() {
        return id;
    }
  
    /* 设置 ID */
    public void setId(int id) {
        this.id = id;
    }

    /* 根据闹钟设置时间与系统时间的间隔， 拼装字符串 */
    public String getTimeUntilNextAlarmMessage() {
        long timeDifference = getAlarmTime().getTimeInMillis() - System.currentTimeMillis();     // 闹钟设置的时间与系统时间的差值
        long days    = timeDifference / (1000 * 60 * 60 * 24);
        long hours   = timeDifference / (1000 * 60 * 60) - (days * 24);
        long minutes = timeDifference / (1000 * 60) - (days * 24 * 60) - (hours * 60);
        long seconds = timeDifference / (1000) - (days * 24 * 60 * 60) - (hours * 60 * 60) - (minutes * 60);
        String alert = "闹钟将会在";
        
        if (days > 0) {
            alert += String.format("%d 天 %d 小时 %d 分钟 %d 秒", days, hours, minutes, seconds);
        } else {
            if (hours > 0) {
                alert += String.format("%d 小时, %d 分钟 %d 秒", hours, minutes, seconds);
            } else {
                if (minutes > 0) {
                    alert += String.format("%d 分钟 %d 秒", minutes, seconds);
                } else {
                    alert += String.format("%d 秒", seconds);
                }
            }
        }
        alert += "提醒";
        
        return alert;
    }


    /* 拼装闹钟重复周日子的字符串 */
    public String getRepeatDaysString() {
        if (days == null/* || days_num < 1*/ || days.length < 1)
            return "只响一次";
        Map<Integer, String> map = new HashMap<>(7);
        map.put(Calendar.SUNDAY,    "周日");
        map.put(Calendar.MONDAY,    "周一");
        map.put(Calendar.TUESDAY,   "周二");
        map.put(Calendar.WEDNESDAY, "周三");
        map.put(Calendar.THURSDAY,  "周四");
        map.put(Calendar.FRIDAY,    "周五");
        map.put(Calendar.SATURDAY,  "周六");
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < days.length; i++) {
            if (map.containsKey(days[i])) {
                sb.append(" " + map.get(days[i]));
            }
        }
        return sb.toString();

    }
}
