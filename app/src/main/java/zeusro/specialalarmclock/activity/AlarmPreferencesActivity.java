package zeusro.specialalarmclock.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import zeusro.specialalarmclock.Alarm;
import zeusro.specialalarmclock.AlarmPreference;
import zeusro.specialalarmclock.Database;
import zeusro.specialalarmclock.R;
import zeusro.specialalarmclock.adapter.AlarmSettingItemListAdapter;

/* 闹钟参数设置页面 */
/*   基类 BaseActivity 可设置闹钟提醒、 可接收闹钟提醒服务 */
public class AlarmPreferencesActivity extends BaseActivity {
    private Alarm alarm;     // 编辑的闹钟项
    private MediaPlayer mediaPlayer;     // 闹铃音试听播放器
    private ListAdapter listAdapter;     // 本页面列表框的数据源 Adapter
    private ListView listView;     // 本页面列表框
    private CountDownTimer alarmToneTimer;     // 闹铃音试听定时器

    @Override
    // 闹钟参数设置页面的创建     关联要设置的闹钟参数 Adapter、 设置列表框条目点击事件响应（设置振动状态与闹铃音）
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preferences);     // 本页面的布局文件      闹钟参数设置页面布局 preferences.xml

        Bundle bundle = getIntent().getExtras();
        // 提取 闹钟数据     待编辑闹钟参数
        if (bundle != null && bundle.containsKey("alarm")) {     // 前一个页面传递了 alarm 数据     来自待编辑？ 列表框点击？
            //更新数据
            alarm = ((Alarm) bundle.getSerializable("alarm"));
        } else {
            alarm = (new Alarm());     // 新建闹钟数据     来自新建闹钟项？
        }

        // 本页面待设置的参数表 Adapter     本页面列表框的关联数据源 Adapter
        if (bundle != null && bundle.containsKey("adapter")) {     // 如果有其它调用传来的关联数据 Adapter
            setListAdapter((AlarmSettingItemListAdapter) bundle.getSerializable("adapter"));
        } else {
            setListAdapter(new AlarmSettingItemListAdapter(this, alarm));     // 新设置列表框关联数据源 Adapter
        }


        // 列表框中条目点击事件监听， 响应振动状态与闹铃音两种参数的设置
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {     // 本页面列表框中列表项点击事件监听
            @Override
            public void onItemClick(AdapterView<?> l, View v, int position, long id) {     // 列表项点击处理
                final AlarmSettingItemListAdapter alarmPreferenceListAdapter = (AlarmSettingItemListAdapter) listAdapter;     // 待设置的参数表
                final AlarmPreference alarmPreference = (AlarmPreference) alarmPreferenceListAdapter.getItem(position);     // 提取点击选择的参数
                AlertDialog.Builder alert;

                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

                switch (alarmPreference.getType()) {     // 选择参数的类型
                    // 布尔型参数
                    case BOOLEAN:
                        // 读复选框的值
                        CheckedTextView checkedTextView = (CheckedTextView) v;
                        boolean checked = !checkedTextView.isChecked();
                        ((CheckedTextView) v).setChecked(checked);
                        switch (alarmPreference.getKey()) {
                            // 振动状态
                            case ALARM_VIBRATE:
                                alarm.setVibrate(checked);     // 设置振动状态
                                if (checked) {
                                    // 振动1秒， 反馈选择操作
                                    Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                                    vibrator.vibrate(1000);
                                }
                                break;
                        }

                        // 保存参数值
                        alarmPreference.setValue(checked);
                        break;

                    // 闹铃音
                    case Ring:
                        alert = new AlertDialog.Builder(AlarmPreferencesActivity.this);
                        alert.setTitle(alarmPreference.getTitle());     // 本参数名称 “铃声”

                        // 拷贝闹铃音名称表
                        CharSequence[] items = new CharSequence[alarmPreference.getOptions().length];
                        for (int i = 0; i < items.length; i++)
                            items[i] = alarmPreference.getOptions()[i];

                        // android.app.AlertDialog.Builder.setItems() 设置要在对话框中显示的项目列表作为内容，将通过提供的监听器向您通知所选项目。
                        alert.setItems(items, new DialogInterface.OnClickListener() {     // 对话框中闹铃音名称列表项点击响应

                            @Override
                            public void onClick(DialogInterface dialog, int which) {     // 选择了某个闹铃音
                                alarm.setAlarmTonePath(alarmPreferenceListAdapter.getAlarmTonePaths()[which]);     // 提取选中闹铃音文件的路径
                                // 播放3秒该闹铃音
                                if (alarm.getAlarmTonePath() != null) {
                                    if (mediaPlayer == null) {
                                        mediaPlayer = new MediaPlayer();
                                    } else {
                                        if (mediaPlayer.isPlaying())
                                            mediaPlayer.stop();
                                        mediaPlayer.reset();
                                    }
                                    try {
                                        // mediaPlayer.setVolume(1.0f, 1.0f);
                                        mediaPlayer.setVolume(0.2f, 0.2f);
                                        mediaPlayer.setDataSource(AlarmPreferencesActivity.this, Uri.parse(alarm.getAlarmTonePath()));
                                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);     // STREAM_SYSTEM  STREAM_RING  STREAM_MUSIC
                                        mediaPlayer.setLooping(false);
                                        mediaPlayer.prepare();
                                        mediaPlayer.start();

                                        // Force the mediaPlayer to stop after 3
                                        // seconds...
                                        if (alarmToneTimer != null)
                                            alarmToneTimer.cancel();
                                        alarmToneTimer = new CountDownTimer(3000, 3000) {     // 定时器的响应函数
                                            @Override
                                            public void onTick(long millisUntilFinished) {

                                            }

                                            @Override
                                            public void onFinish() {
                                                try {
                                                    // 3秒到时则停止试听
                                                    if (mediaPlayer.isPlaying())
                                                        mediaPlayer.stop();
                                                } catch (Exception e) {

                                                }
                                            }
                                        };
                                        alarmToneTimer.start();
                                    } catch (Exception e) {
                                        try {
                                            if (mediaPlayer.isPlaying())
                                                mediaPlayer.stop();
                                        } catch (Exception e2) {

                                        }
                                    }
                                }
                                alarmPreferenceListAdapter.setMathAlarm(alarm);     // 更新闹钟项数据为抽象化参数表
                                alarmPreferenceListAdapter.notifyDataSetChanged();     // 通知显示模块数据已变化
                            }

                        });
                        alert.show();
                        break;

                    default:
                        break;
                }
            }
        });
    }


    @Override
    /* 返回键处理     保存闹钟参数到数据库表、 设置定时提醒服务、 提示下、 关闭音频播放器 */
    public void onBackPressed() {
//        String data = "data";
//        Log.d(data, alarm.getAlarmName());
//        Log.d(data, alarm.getAlarmTime().toString());
//        Log.d(data, String.valueOf(alarm.getDays().length));
//        Log.d(data, alarm.getAlarmTonePath());
//        Log.d(data, String.valueOf(alarm.getVibrate()));


//保存闹钟信息
//        int[] days = alarm.getDays();
//        if (days == null || days.length < 1) {
//            //todo: 当任何一天都不重复时,只提醒一次
//        }

        Database.init(getApplicationContext());     // 初始化创建数据库
        if (alarm.getId() < 1) {     // id < 1 表示新的闹钟项数据      表示数据库表中没这项闹钟数据， 需要建立？
            Database.create(alarm);     // 数据库表插入新的闹钟项 alarm
        } else {
            Database.update(alarm);     // 更新
        }

        CallAlarmServiceBroadcastReciever(alarm);     // 基类调用， 添加定时提醒及其 Reciever

        Toast.makeText(AlarmPreferencesActivity.this, alarm.getTimeUntilNextAlarmMessage(), Toast.LENGTH_LONG).show();     // 信息提示 闹钟设置时间与系统时间的间隔

        //跨activity传值,用于测试
//        Intent resultIntent = new Intent();
//        Bundle bundle = new Bundle();
//        bundle.putSerializable("object", alarm);
//        resultIntent.putExtras(bundle);
//        setResult(RESULT_OK, resultIntent);

        ReleaseMusicPlayer();     // 关闭播放器

        super.onBackPressed();
        finish();
    }

    @Override
    /* ？？退出本页面， 保存返回数据 */
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("alarm", alarm);
//        outState.putSerializable("adapter", (AlarmSettingItemListAdapter) listAdapter);
    }


    @Override
    /* 暂停音乐播放     直接释放播放器？ */
    protected void onPause() {
        super.onPause();
        try {
            ReleaseMusicPlayer();
        } catch (Exception e) {
        }
        // setListAdapter(null);
    }


    /* 设置列表框关联数据源 Adapter */
    public void setListAdapter(ListAdapter listAdapter) {
        this.listAdapter = listAdapter;

        getListView().setAdapter(listAdapter);
    }

    /* 获取本页面的列表框 */
    public ListView getListView() {
        if (listView == null)
            listView = (ListView) findViewById(android.R.id.list);     // 本页面列表框 id: list
        return listView;
    }


    @Override
    /* 点击的响应 */
    public void onClick(View v) {
        super.onClick(v);
    }

    /* 关闭播放器     释放播放器 */
    private void ReleaseMusicPlayer() {
        if (mediaPlayer != null)
            mediaPlayer.release();
        mediaPlayer = null;
    }
}


