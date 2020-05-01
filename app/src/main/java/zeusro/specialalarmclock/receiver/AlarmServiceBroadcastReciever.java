package zeusro.specialalarmclock.receiver;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import zeusro.specialalarmclock.Alarm;
import zeusro.specialalarmclock.Database;
import zeusro.specialalarmclock.service.SchedulingService;

//使设备保持唤醒状态     https://developer.android.google.cn/training/scheduling/wakelock?hl=zh-CN
//
//WakefulBroadcastReceiver 学习笔记     https://www.jianshu.com/p/5b8bfa6a6c37
//  > 简介
//  >   WakefulBroadcastReceiver 是一种特殊的广播接收器. 它可以自动创建和管理唤醒锁 PARTIAL_WAKE_LOCK 来执行任务. 确保耗时任务执行完毕之前设备不会休眠.
//  >   WakefulBroadcastReceiver 收到广播后一般会启动 Service (通常用 IntentService 来处理耗时任务), 同时确保设备在整个 Service 执行过程中保持唤醒状态. 不然的话, 对于耗时任务, 设备可能在你完成任务之前就休眠了.
//  >
//  > 注意点
//  >   通过 startWakefulService(Context, Intent) 启动 Service 而不是 startService(). WakefulBroadcastReceiver 启动 Service 的时候会自动创建唤醒锁, 并在 Intent 附上唤醒锁的 ID 来判断这个唤醒锁.
//  >   最后必须在 Service 中调用 completeWakefulIntent(intent) 释放唤醒锁.


/**
 *
 */
public class AlarmServiceBroadcastReciever extends WakefulBroadcastReceiver {

    Alarm alarm;     // 闹铃到时广播中包含的闹铃数据

    // The app's AlarmManager, which provides access to the system alarm services.
    private AlarmManager alarmMgr;     // 系统闹钟(定时提醒)管理
    // The pending intent that is triggered when the alarm fires.
    private PendingIntent alarmIntent;


    @Override
    // 接收到闹铃到时提醒广播的处理
    public void onReceive(Context context, Intent intent) {
        Log.d(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[2].getMethodName());
        try {
            //            11-27 13:43:39.020 14674-14674/zeusro.specialalarmclock D/TEST: null
//            11-27 13:43:39.020 14674-14674/zeusro.specialalarmclock D/TEST: android.intent.extra.ALARM_COUNT
//            11-27 13:43:39.020 14674-14674/zeusro.specialalarmclock D/TEST: 1
//            11-27 13:43:39.020 14674-14674/zeusro.specialalarmclock D/AlarmServiceBroadcastReciever: false
//            Log.d("TEST", String.valueOf(intent.getAction()));
//            Bundle bundle = intent.getExtras();

//            Set<String> keySet = bundle.keySet();
//            for (String key : keySet) {
//                Object value = bundle.get(key);
//                Log.d("TEST", key.toString());
//                Log.d("TEST", value.toString());
//            }
//            Log.d(this.getClass().getSimpleName(), String.valueOf(bundle.getSerializable("alarm") != null));
//            alarm = (Alarm) bundle.getSerializable("alarm");
//            if (alarm == null)
//                throw new Exception("参数没有啊混蛋");
            //            service.putExtra("alarm", alarm);
            // FIXME: 2015/11/27 通过对象传递找到对象而不是查数据库
//            Log.d(this.getClass().getSimpleName(), String.valueOf( intent.getSerializableExtra("alarm") != null));
//            alarm = (Alarm) intent.getSerializableExtra("alarm");

            alarm = getNext(context);     // 加载数据库中全部闹钟数据， 找到一个闹钟项
            Intent service = new Intent(context, SchedulingService.class);     // 闹钟到时执行的任务服务 SchedulingService： 打开闹铃提醒页面
            service.putExtra("alarm", alarm);     // 夹带闹钟数据

            // Start the service, keeping the device awake while it is launching.
            startWakefulService(context, service);     // 保持唤醒状态， 执行服务 打开闹铃提醒页面
            setResultCode(Activity.RESULT_OK);
        } catch (Exception e) {
            Log.wtf("WTF", e);
        }
    }


    /* 使用 AlarmManager 设置定时提醒     设置闹钟提醒时刻     context 调用者页面， alarm 闹钟项 */
    public void setAlarm(Context context, Alarm alarm) {
        Log.d(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[2].getMethodName());

        Intent intent = new Intent(context, AlarmServiceBroadcastReciever.class);     // 接受者 AlarmServiceBroadcastReciever
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        if (alarm == null)     // 设备上电启动完成后， 找到的第一个有效闹钟项数据
            alarm = getNext(context);
        if (alarm == null) {
            Log.d(context.getPackageName(), "没有闹钟");
            CancelAlarm(context);
            return;
        }
        intent.setAction("zeusro.action.alert");
        intent.putExtra("alarm", alarm);     // 夹带闹钟数据

        /* 比较当前时间与闹钟时间 */
        Calendar calendar = alarm.getAlarmTime();
        Calendar now = (Calendar) calendar.clone();
        now.set(Calendar.HOUR_OF_DAY, Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
        now.set(Calendar.MINUTE, Calendar.getInstance().get(Calendar.MINUTE));
        now.set(Calendar.SECOND, calendar.get(Calendar.SECOND));
        if (now.getTimeInMillis() > calendar.getTimeInMillis()) {     // 闹钟项时间已过期
            // XXXXX: 如果是上电后加载， 应该查找下一个闹钟项
            CancelAlarm(context);
            return;
        }

        /* 设置 AlarmManager 提醒时刻     无循环 */
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//            Log.d(this.getClass().getSimpleName(), calendar.getTime().toString());
//            alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),  AlarmManager.INTERVAL_FIFTEEN_MINUTES, alarmIntent);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);

        // Enable {@code SampleBootReceiver} to automatically restart the alarm when the
        // device is rebooted.
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();
        //可用状态
        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }


    /**
     * Cancels the alarm.
     * 关闭当前定时闹钟     取消闹钟服务
     * @param context
     */
    // BEGIN_INCLUDE(cancel_alarm)
    public void CancelAlarm(Context context) {
        if (null == alarmIntent) {
            Intent intent = new Intent(context, AlarmServiceBroadcastReciever.class);
            alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        }
        // If the alarm has been set, cancel it.
        if (alarmMgr != null) {
            alarmMgr.cancel(alarmIntent);
        }
        // Disable {@code SampleBootReceiver} so that it doesn't automatically restart the
        // alarm when the device is rebooted.
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }


    // 加载数据库中全部闹钟数据， 找到一个激活的有效闹钟项
    //   XXXXX: 这个没完工， 有序树应该作为类的成员进行加载、 顺序查找应该有 getRoot getNext 等
    private Alarm getNext(Context context) {
        Set<Alarm> alarmQueue = new TreeSet<Alarm>(new Comparator<Alarm>() {     // 有序树的比较器
            @Override
            public int compare(Alarm lhs, Alarm rhs) {
                int result = 0;
                long diff = lhs.getAlarmTime().getTimeInMillis() - rhs.getAlarmTime().getTimeInMillis();
                if (diff > 0) {
                    return 1;
                } else if (diff < 0) {
                    return -1;
                }
                return result;
            }
        });

        Database.init(context);     // 初始化创建数据库
        List<Alarm> alarms = Database.getAll();     // 从数据库读取所有的闹钟项数组

        for (Alarm alarm : alarms) {
            if (alarm.IsAlarmActive())
                alarmQueue.add(alarm);     // 将激活的闹钟项加入有序树
        }
        if (alarmQueue.iterator().hasNext()) {     // 如果迭代包含更多元素
            return alarmQueue.iterator().next();     // 返回迭代中的下一个元素
        } else {
            return null;
        }
    }
}
