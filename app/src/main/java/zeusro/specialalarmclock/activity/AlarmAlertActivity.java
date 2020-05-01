package zeusro.specialalarmclock.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import zeusro.specialalarmclock.Alarm;
import zeusro.specialalarmclock.R;
import zeusro.specialalarmclock.StaticWakeLock;
import zeusro.specialalarmclock.receiver.AlarmServiceBroadcastReciever;
import zeusro.specialalarmclock.view.SlideView;

/* 闹钟响铃提醒页面 */
/*   开启闹铃音或振动器， 如移动滑条到位则关闭 */
public class AlarmAlertActivity extends AppCompatActivity implements View.OnClickListener {
    private Alarm alarm;     // 闹钟项数据
    private MediaPlayer mediaPlayer;     // 闹铃音播放器
    private Vibrator vibrator;     // 振动器
    private boolean alarmActive;     // 本页面中闹钟激活状态


    @Override
    // 页面创建     设置滑条操作响应设置来电话时处理
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alert);     // 本页面布局 alert.xml     闹铃提示页面布局

        // 提取 闹钟数据 Alarm
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            alarm = (Alarm) bundle.getSerializable("alarm");
            if (null != alarm) {
                this.setTitle(alarm.getAlarmName());
                startAlarm();     // 开始闹钟的提醒， 振动和闹铃音
            }
        }
        // 设置滑条控件     设置滑块移动到位后关闭闹钟关闭闹铃音等
        SetSlideView();
        // 电话来电时处理， 开关闹铃音
        SetTelephonyStateChangedListener();
    }

    // 电话来电时处理， 开关闹铃音
    private void SetTelephonyStateChangedListener() {
        PhoneStateListener phoneStateListener = new PhoneStateListener() {     // 设置电话状态监听
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    // 来电话铃了， 暂停闹铃音
                    case TelephonyManager.CALL_STATE_RINGING:
                        Log.d(getClass().getSimpleName(), "Incoming call: " + incomingNumber);
                        try {
                            mediaPlayer.pause();     // 暂停闹铃音
                        } catch (IllegalStateException e) {
                        }
                        break;

                    // 电话呼叫状态闲置， 重新闹铃音
                    case TelephonyManager.CALL_STATE_IDLE:
                        Log.d(getClass().getSimpleName(), "Call State Idle");
                        try {
                            mediaPlayer.start();
                        } catch (IllegalStateException e) {
                        }
                        break;
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };

        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    // 设置滑条控件     设置滑块移动到位后关闭闹钟关闭闹铃音等
    private void SetSlideView() {
        SlideView slideView = (SlideView) findViewById(R.id.slider);     // 滑条控件中 滑块 id: slider
        slideView.setSlideListener(new SlideView.SlideListener() {     // 滑块响应处理
            @Override
            public void onDone() {     // 滑块移动到位
                AlarmServiceBroadcastReciever reciever = new AlarmServiceBroadcastReciever();
                reciever.CancelAlarm(AlarmAlertActivity.this);     // 关闭当前定时闹钟
                ReleaseRelease();     // 关闭闹铃音播放器
                Toast.makeText(AlarmAlertActivity.this, "早起啦", Toast.LENGTH_SHORT).show();
                Log.d("SHIT", String.valueOf(Build.VERSION.SDK_INT));

                // ?????: 下面什么操作？
                if (Build.VERSION.SDK_INT > 15) {
                    quit();
                    return;
                }
                int pid = android.os.Process.myPid();
                android.os.Process.killProcess(pid);
                System.exit(0);
            }
        });
    }

    @TargetApi(16)
    // 退出本页面
    protected void quit() {
        finishAffinity();
    }

    @Override
    // 页面显示时， 闹铃状态为激活
    protected void onResume() {
        super.onResume();
        alarmActive = true;
    }

    // 开始闹钟的提醒， 振动和闹铃音
    private void startAlarm() {
        if (alarm.getAlarmTonePath() != "") {
            mediaPlayer = new MediaPlayer();
            if (alarm.IsVibrate()) {
                // 开启振动器
                vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                long[] pattern = {1000, 200, 200, 200};
                vibrator.vibrate(pattern, 0);
            }
            try {
                // 播放闹铃音
                mediaPlayer.setVolume(1.0f, 1.0f);
                mediaPlayer.setDataSource(this, Uri.parse(alarm.getAlarmTonePath()));
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mediaPlayer.setLooping(true);
                mediaPlayer.prepare();
                mediaPlayer.start();

            } catch (Exception e) {
                mediaPlayer.release();
                alarmActive = false;
            }
        }

    }

    /**
     * 禁止返回取消闹钟
     */
    @Override
    public void onBackPressed() {
        // 闹铃非激活， 页面才响应返回键
        if (!alarmActive)
            super.onBackPressed();
    }

    /*
     * (non-Javadoc)
     * 页面到后台的暂停操作
     * @see android.app.Activity#onPause()
     */
    @Override
    protected void onPause() {
        super.onPause();
        StaticWakeLock.lockOff(this);
    }

    // 关闭释放闹铃音播放器
    protected void ReleaseRelease() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            finish();
        }
    }

    @Override
    // 本页面销毁
    protected void onDestroy() {
        try {
            // 退出振动器
            if (vibrator != null)
                vibrator.cancel();
        } catch (Exception e) {

        }
        try {
            // 关闭闹铃音播放器
            mediaPlayer.stop();
        } catch (Exception e) {

        }
        try {
            // 释放闹铃音播放器
            mediaPlayer.release();
        } catch (Exception e) {

        }
        super.onDestroy();
    }


    /**
     * Called when a view has been clicked.
     * 点击事件响应
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
    }
}
