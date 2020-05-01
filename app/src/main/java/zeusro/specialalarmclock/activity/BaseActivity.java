package zeusro.specialalarmclock.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewConfiguration;

import java.lang.reflect.Field;

import zeusro.specialalarmclock.Alarm;
import zeusro.specialalarmclock.R;
import zeusro.specialalarmclock.receiver.AlarmServiceBroadcastReciever;

/**
 * Created by Z on 2015/11/16.
 * 可设置闹钟提醒的页面基类     可设置闹钟提醒、 可接收闹钟提醒服务
 */
public class BaseActivity extends AppCompatActivity implements android.view.View.OnClickListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.middle_title_actionbar);     // 定制XX信息显示页面布局 middle_title_actionbar.xml

        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // Ignore
        }
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {

    }


    /* 取消闹钟服务     取消闹钟服务广播的接收 */
    protected void  CancelAlarmServiceBroadcastReciever(){
        AlarmServiceBroadcastReciever reciever = new AlarmServiceBroadcastReciever();

        reciever.CancelAlarm(this);     // 取消闹钟服务
    }

    /**
     * 添加定时提醒及其 Reciever     设置闹钟服务
     */
    protected void CallAlarmServiceBroadcastReciever(Alarm alarm) {

//        Intent serviceIntent = new Intent(this, AlarmService.class);
//        this.startService(serviceIntent);
        AlarmServiceBroadcastReciever reciever = new AlarmServiceBroadcastReciever();
        reciever.setAlarm(this, alarm);     // 使用 AlarmManager 设置定时提醒     设置闹钟提醒时刻
//        bindService()

//        Intent serviceIntent = new Intent(this, AlarmServiceBroadcastReciever.class);
//        this.startService(serviceIntent);
    }
}
