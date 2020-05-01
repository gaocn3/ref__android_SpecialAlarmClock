package zeusro.specialalarmclock.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import zeusro.specialalarmclock.Alarm;
import zeusro.specialalarmclock.activity.AlarmAlertActivity;
import zeusro.specialalarmclock.receiver.AlarmServiceBroadcastReciever;

/* 闹钟到时执行的任务服务 SchedulingService： 打开闹铃提醒页面 */
public class SchedulingService extends IntentService {
    public SchedulingService() {
        super("SchedulingService");
    }

    @Override
    /* 服务执行函数     打开闹钟提醒页面 AlarmAlertActivity */
    protected void onHandleIntent(Intent intent) {
        Log.d(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[2].getMethodName());
        final Alarm alarm = (Alarm) intent.getExtras().getSerializable("alarm");     // Intent 中提取扩展数据的映射

        // 打开闹铃提醒页面
        Intent alarmAlertActivityIntent = new Intent(getApplicationContext(), AlarmAlertActivity.class);     // 将 AlarmAlertActivity 闹钟提醒页面封装到 Intent
        alarmAlertActivityIntent.putExtra("alarm", alarm);
        alarmAlertActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(alarmAlertActivityIntent);     // 打开闹钟提醒页面 AlarmAlertActivity     在非 Activity 的类中调用 startActivity(intent)

        AlarmServiceBroadcastReciever.completeWakefulIntent(intent);     // WakefulBroadcastReceiver 是一种特殊的广播接收器， 在 Service 中调用 completeWakefulIntent(intent) 释放唤醒锁
    }

}
