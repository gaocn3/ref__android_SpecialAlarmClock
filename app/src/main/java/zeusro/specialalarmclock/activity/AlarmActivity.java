package zeusro.specialalarmclock.activity;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import zeusro.specialalarmclock.Alarm;
import zeusro.specialalarmclock.Database;
import zeusro.specialalarmclock.R;
import zeusro.specialalarmclock.adapter.AlarmListAdapter;
import zeusro.specialalarmclock.receiver.NotificationWakeUpReceiver;

/**
 * 主activity
 * 启动页面
 *   基类 BaseActivity 可设置闹钟提醒、 可接收闹钟提醒服务
 */
public class AlarmActivity extends BaseActivity {

    AlarmListAdapter alarmListAdapter;     // 内存中的闹钟数据表
    ListView mathAlarmListView;
    ImageButton add, setting;
    private boolean isExit;
    public final static int notificationId = 1;

    @Override
    /* 初始页面的创建     初始化页面显示， 初始化关联闹钟数据表与数据库， 关联界面组件的操作 */
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("AlarmActivity.java","onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);     // 布局文件 activity_home.xml

        // Toast 浮动显示快速消息， 自动消失，没有焦点的。
        Toast toast = Toast.makeText(this, R.string.Thank, Toast.LENGTH_SHORT);
        toast.show();     // 显示 Toast 信息

        SetlistView();     // 设置列表框， 设置闹钟数据表 Adapter、 设置长按删除和单击编辑操作     有几处间接地调用了数据库的初始化
        SetAddButton();     // 设置 Add 按钮的点击操作     阻塞调用页面 AlarmPreferencesActivity
        SetSettingButton();     // 设置 Setting 按钮的点击操作     发送广播通知 NotificationWakeUpReceiver
    }


    @Override
    /* Activity Stop */
    protected void onStop() {
        Log.d("AlarmActivity.java","onStop()");
        super.onStop();
    }

    @Override
    /* Activity Destroy */
    protected void onDestroy() {
        Log.d("AlarmActivity.java", "onDestroy()");
        super.onDestroy();
    }

    @Override
    /* Activity Pause */
    protected void onPause() {
        Log.d("AlarmActivity.java","onPause()");
        Database.deactivate();     // SQLite 数据库关闭， 销毁数据库句柄
        super.onPause();
    }

    @Override
    /* Activity Destroy */
    protected void onRestart() {
        Log.d("AlarmActivity.java", "onRestart()");
        super.onRestart();
    }

    @Override
    /* 页面还原     Activity Resume */
    protected void onResume() {
        Log.d("AlarmActivity.java","onResume");
        super.onResume();
        updateAlarmList();     // 从数据库加载闹钟项数据到内存表， 再刷新显示
    }


    @Override
    /* 似乎页面退出时返回结果用的     本程序没用？ */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Log.d("AlarmActivity.java", "onActivityResult()");
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("AlarmActivity.java -> onActivityResult()", String.valueOf(resultCode));

        switch (resultCode) {
            case RESULT_OK:
                Bundle b = data.getExtras();
                Alarm alarm = (Alarm) b.getSerializable("object");     // 回传的值
                if (alarm != null) {
                    Log.d("AlarmActivity.java -> onActivityResult() data = ", alarm.getAlarmName());
                }
                break;

            default:
                break;
        }
    }


    /**
     * Called when a view has been clicked.
     * 页面上点击激活与否某个闹钟项， 则更新数据库, 添加定时提醒
     *   闹钟列表中某项闹钟[的复选框]被点击， 本页面 Activity 监听到就响应， 本函数传入该闹钟项所在显示子组件 View v(list_element.xml 列表框中列表项布局)
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.checkBox_alarm_active) {     // 点击的组件是复选框
            CheckBox checkBox = (CheckBox) v;
            Alarm alarm = (Alarm) alarmListAdapter.getItem((Integer) checkBox.getTag());     // 取出UI组件夹带的闹钟位置序号， 再获取闹钟数据
              // 参考 AlarmListAdapter.java -> getView() 里 checkBox.setTag() 设置夹带数据 Tag 保存某个闹钟项在列表中的位置 position
              // 根据位置 position， alarmListAdapter 取出该项闹钟数据。

            alarm.setAlarmActive(checkBox.isChecked());     // 设置闹钟项是否激活
            Database.update(alarm);     // 数据库更新闹钟项
            AlarmActivity.this.CallAlarmServiceBroadcastReciever(alarm);     // 基类调用， 添加定时提醒及其 Reciever

            if (checkBox.isChecked()) {     // 闹钟项激活
                Toast.makeText(AlarmActivity.this, alarm.getTimeUntilNextAlarmMessage(), Toast.LENGTH_LONG).show();     // 提示距离闹钟时间
            }
        }
    }


    /* 设置 Add 按钮的点击操作     阻塞调用页面 AlarmPreferencesActivity  */
    private void SetAddButton() {
        add = (ImageButton) findViewById(R.id.Add);     // 启动页面底部 图形按钮 id: Add
        if (add != null) {
            add.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent newAlarmIntent = new Intent(getApplicationContext(), AlarmPreferencesActivity.class);     // 待切换去的页面 AlarmPreferencesActivity
                    startActivityForResult(newAlarmIntent, 0);     // 阻塞调用页面 AlarmPreferencesActivity
//                    startActivity(newAlarmIntent);
                }

            });
        }
    }


    /* 设置列表框， 设置闹钟数据表 Adapter、 设置长按删除和单击编辑操作     有几处间接地调用了数据库的初始化 */
    private void SetlistView() {
        mathAlarmListView = (ListView) findViewById(R.id.listView);     // 主页面 activity_home.xml 上列表框 id: listView

        if (mathAlarmListView != null) {
            /* 1. 列表框允许长按删除某项闹钟 */
            mathAlarmListView.setLongClickable(true);     // 允许长按操作
            mathAlarmListView.setOnItemLongClickListener(new OnItemLongClickListener() {     // 设置长按事件处理函数： 是否删除
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    final Alarm alarm = (Alarm) alarmListAdapter.getItem(position);     // 提取 position 位置处的闹钟项

                    /* 确认是否删除闹钟对话框 */
                    Builder dialog = new AlertDialog.Builder(AlarmActivity.this);     // 拼装提醒对话框
                    dialog.setTitle("删除");
                    dialog.setMessage("删除这个闹钟?");
                    dialog.setPositiveButton("取消", new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {     // 点击肯定 “取消” 按钮
                            dialog.dismiss();     // 关闭提醒对话框
                        }
                    });
                    dialog.setNegativeButton("好", new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {     // 点击否定 “确认” 按钮
                            Database.init(AlarmActivity.this);     // 初始化创建数据库
                            Database.deleteEntry(alarm);     // 删除 id 相同的数据库项
                            //取消
                            AlarmActivity.this.CancelAlarmServiceBroadcastReciever();     // 基类调用， 取消闹钟服务
                            updateAlarmList();     // 从数据库加载闹钟项数据到内存表， 再刷新显示
                        }
                    });
                    dialog.show();     // 弹出提醒对话框
                    return true;
                }
            });

            CallAlarmServiceBroadcastReciever(null);     // 基类调用.设置闹钟服务个值 null， 函数内部会提取下数据库...， 具体未知

            /* 2. 列表框关联 Adapter */
            alarmListAdapter = new AlarmListAdapter(this);     // 创建闹钟数据表 Adapter
            this.mathAlarmListView.setAdapter(alarmListAdapter);     // 设置 ListView 关联的适配器 Adapter。  注意之后 Adapter 中数组 ArrayList 可增添修改， 不能另外 new 分配， 否则不能驱动显示更新

            /* 3. 列表框允许单击编辑某项闹钟     切换去 AlarmPreferencesActivity 页面编辑 */
            mathAlarmListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {     // 设置点击事件处理函数： 编辑某项闹钟数据
                @Override
                public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
                    v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

                    Alarm alarm = (Alarm) alarmListAdapter.getItem(position);    // 提取 position 位置处的闹钟项

                    // 传递闹钟数据 Alarm
                    Intent intent = new Intent(AlarmActivity.this, AlarmPreferencesActivity.class);     // 待切换去页面 AlarmPreferencesActivity
                    intent.putExtra("alarm", alarm);     // 夹带传递数据 alarm
                    startActivityForResult(intent, 0);     // 阻塞调用页面 AlarmPreferencesActivity
                }

            });
        }
    }


    /* 从数据库加载闹钟项数据到内存表， 再刷新显示 */
    public void updateAlarmList() {
        Database.init(AlarmActivity.this);     // 初始化创建数据库
        final List<Alarm> alarms = Database.getAll();     // 从数据库读取已有的闹钟项数组
        alarmListAdapter.setMathAlarms(alarms);     // 设置闹钟数据表
            // XXXXX: 闹钟项数组替换为新的 List 引用？  没重新 .setAdapter(), 下面的 .notifyDataSetChanged() 会无效？      或者

        /* 利用 Activity.runOnUiThread(Runnable) 把或许耗时更新 ui 的代码创建在 Runnable(){run()} 中 */
        runOnUiThread(new Runnable() {
            @Override
            public void run() {     // 线程等内部运行
                // reload content
                AlarmActivity.this.alarmListAdapter.notifyDataSetChanged();     // 通知 Listview 其关联的 Adapter 数据有变化， 并刷新显示
                TextView text = (TextView) findViewById(R.id.textView);     // 启动页面顶部 文本框 id: textView
                if (alarms != null && alarms.size() > 0) {
                    text.setVisibility(View.GONE);     // text 控件隐藏
                } else {
                    text.setText(R.string.NoClockAlert);
                    text.setVisibility(View.VISIBLE);     // text 控件可见
                }
            }
        });
    }


    @Override
    /* 返回键响应， 2秒内连续2次按下返回键， 则退出本程序 */
    public void onBackPressed() {
        Timer tExit = null;
        if (isExit == false) {
            isExit = true; // 准备退出
            Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();     // 提示按返回键会退出
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false; // 取消退出
                }
            }, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务
        } else {     // 2秒内连续2次按下返回键， 则退出本程序
            //退出
            finish();
        }
    }

    /* 设置 Setting 按钮的点击操作     发送广播通知 NotificationWakeUpReceiver  */
    private void SetSettingButton() {
        setting = (ImageButton) findViewById(R.id.Setting);     // 启动页面底部 图形按钮 id: Setting
        if (setting != null) {
            setting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {     // 点击操作
                    CreateNotification(null);
//                    Toast.makeText(AlarmActivity.this, "该功能见鬼中", Toast.LENGTH_SHORT).show();
//                    finish();
                }

            });
        }
    }

    /* 发送广播， 通知 NotificationWakeUpReceiver     alarm 没用， 没实现 */
    private void CreateNotification(Alarm alarm) {
        Intent intent = new Intent();
        intent.setClass(this, NotificationWakeUpReceiver.class);
        sendBroadcast(intent);//发送广播事件
    }
}
