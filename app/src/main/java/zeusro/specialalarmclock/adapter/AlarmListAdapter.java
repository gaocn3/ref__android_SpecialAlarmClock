package zeusro.specialalarmclock.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import zeusro.specialalarmclock.Alarm;
import zeusro.specialalarmclock.Database;
import zeusro.specialalarmclock.R;
import zeusro.specialalarmclock.activity.AlarmActivity;

/**
 * Created by Z on 2015/11/16.
 * 闹钟数据表 Adapter， 关联到主页面的列表框供显示自动刷新           闹钟项数据列表 Adapter， 关联视图与数据
 *   可认为内存中的闹钟数据表
 *
 *   数据表 Adapter， 内存中的闹钟数据表。     启动页面的列表框视图关联这个 Adapter， 当数据表有变化时可供自动刷新显示。
 *   初始加载和数据变化时， 会操作数据库同步闹钟数据。
 */
public class AlarmListAdapter extends BaseAdapter {
    private AlarmActivity alarmActivity;     // 关联页面
    private List<Alarm> alarms = new ArrayList<Alarm>();     // 闹钟项数组

    public static final String ALARM_FIELDS[] = {Database.COLUMN_ALARM_ACTIVE, Database.COLUMN_ALARM_TIME, Database.COLUMN_ALARM_DAYS};

    /* 设置关联 Activity */
    public AlarmListAdapter(AlarmActivity alarmActivity) {
        this.alarmActivity = alarmActivity;
//		Database.init(alarmActivity);
//		alarms = Database.getAll();
    }

    @Override
    /* 闹钟项数 */
    public int getCount() {
        return alarms.size();
    }

    @Override
    /* 获取某项闹钟数据 */
    public Object getItem(int position) {
        return alarms.get(position);
    }

    @Override
    /* 闹钟项 ID 就是原样返回 position */
    public long getItemId(int position) {
        return position;
    }

    @Override
    /* 组装一个闹钟项的显示组件， 一行， 左侧复选框表示闹钟是否激活， 右侧两行， 上面闹钟时间， 下面闹钟重复周期 */
    public View getView(int position, View view, ViewGroup viewGroup) {
        if (null == view)
            view = LayoutInflater.from(alarmActivity).inflate(R.layout.list_element, null);     // 加载 list_element.xml 布局

        Alarm alarm = (Alarm) getItem(position);     // 提取指定项闹钟数据
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox_alarm_active);     // 提取复选框
        checkBox.setChecked(alarm.IsAlarmActive());     // 复选框表示闹钟项是否激活
        checkBox.setTag(position);     // checkBox 设置夹带数据 Tag 为闹钟项的列表中位置
        checkBox.setOnClickListener(alarmActivity);    // 设置复选框点击事件的监听者， alarmActivity

        TextView alarmTimeView = (TextView) view.findViewById(R.id.textView_alarm_time);
        alarmTimeView.setText(alarm.getAlarmTimeString());     // 闹钟时间字符串

        TextView alarmDaysView = (TextView) view.findViewById(R.id.textView_alarm_days);
        alarmDaysView.setText(alarm.getRepeatDaysString());     // 闹钟重复周期字符串

        return view;
    }

    /* 返回闹钟数据表 */
    public List<Alarm> getMathAlarms() {
        return alarms;
    }

    /* 拷贝闹钟数据表 */
    public void setMathAlarms(List<Alarm> alarms) {
        this.alarms = alarms;     // 闹钟项数组替换为新的 List 引用？
    }
}
