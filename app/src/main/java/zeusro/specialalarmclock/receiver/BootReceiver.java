package zeusro.specialalarmclock.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

// 设备启动完成消息的处理
public class BootReceiver extends BroadcastReceiver {
    public BootReceiver() {
    }

    AlarmServiceBroadcastReciever alarm = new AlarmServiceBroadcastReciever();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[2].getMethodName());
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            alarm.setAlarm(context, null);     // 使用 AlarmManager 设置定时提醒
        }
    }
}
