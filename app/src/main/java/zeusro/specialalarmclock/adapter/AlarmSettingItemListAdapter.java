package zeusro.specialalarmclock.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import zeusro.specialalarmclock.Alarm;
import zeusro.specialalarmclock.AlarmPreference;
import zeusro.specialalarmclock.Key;
import zeusro.specialalarmclock.R;
import zeusro.specialalarmclock.Type;

/**
 * Created by Z on 2015/11/16.
 * 闹钟参数设置页面中子参数数据与编辑显示 Adapter
 *   为自动化显示编辑子参数， 抽象化组织各个子参数为表
 *   然后添加子参数显示编辑组件， 与显示关联数据， 设置修改监听处理函数等
 *
 * <del> 抽象化某个闹钟项的子参数表 Adapter
 * <del>   似乎为了方便页面显示， 对某个闹钟项的几个子参数抽象出 Key、 显示字符串、 夹带数据等
 */

public class AlarmSettingItemListAdapter extends BaseAdapter {

    private Context context;     // 本抽象化参数表的上下文，  关联的页面
    private Alarm alarm;     // 被参数化的闹钟项
    private List<AlarmPreference> preferences = new ArrayList<AlarmPreference>();     // 抽象化子参数表
    private final String[] repeatDays = {"一", "二", "三", "四", "五", "六", "日"};     // 闹钟要重复的周日子名称
    private String[] alarmTones;     // 闹铃音名称表
    private String[] alarmTonePaths;     // 闹铃音文件路径表

    // 初始化设置闹钟项 alarm 的抽象化子参数表， 建立本机闹铃音表
    public AlarmSettingItemListAdapter(Context context, Alarm alarm) {
        this.context = (context);

        Log.d("AlarmSettingItemListAdapter", "Loading Ringtones...");

        // 拷贝闹铃音名称与路径
        RingtoneManager ringtoneMgr = new RingtoneManager(getContext());
        ringtoneMgr.setType(RingtoneManager.TYPE_ALARM);
        Cursor alarmsCursor = ringtoneMgr.getCursor();
        alarmTones = new String[alarmsCursor.getCount() + 1];     // 根据闹铃音个数创建字符串
        alarmTones[0] = "静默模式";     // 第一个设置为静默
        alarmTonePaths = new String[alarmsCursor.getCount() + 1];
        alarmTonePaths[0] = "";
        if (alarmsCursor.moveToFirst()) {
            do {
                Log.d("ITEM", ringtoneMgr.getRingtone(alarmsCursor.getPosition()).getTitle(getContext()));
                Log.d("ITEM", ringtoneMgr.getRingtoneUri(alarmsCursor.getPosition()).toString());
                alarmTones[alarmsCursor.getPosition() + 1] = ringtoneMgr.getRingtone(alarmsCursor.getPosition()).getTitle(getContext());     // 闹铃音名称
                alarmTonePaths[alarmsCursor.getPosition() + 1] = ringtoneMgr.getRingtoneUri(alarmsCursor.getPosition()).toString();     // 闹铃音文件路径
            } while (alarmsCursor.moveToNext());
        }
        Log.d("AlarmSettingItemListAdapter", "Finished Loading " + alarmTones.length + " Ringtones.");
        alarmsCursor.close();

        setMathAlarm(alarm);     // 闹钟项数据转换为抽象化参数表
    }

    @Override
    /* 返回子参数个数 */
    public int getCount() {
        return preferences.size();
    }

    @Override
    /* 根据序号返回某项子参数 */
    public Object getItem(int position) {
        return preferences.get(position);
    }

    @Override
    /* 序号即 ID */
    public long getItemId(int position) {
        return position;
    }

    @Override
    /* 组装闹钟项一个子参数的编辑组件 */
    public View getView(int position, View convertView, ViewGroup parent) {
//        Log.d("tagText", "getViewAgain");
        AlarmPreference alarmPreference = (AlarmPreference) getItem(position);     // 根据序号返回某项子参数
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());     // 从给定的上下文中获取LayoutInflater
        switch (alarmPreference.getType()) {
            // 编辑闹钟名称
            case EditText:
                //标签
                if (null == convertView)
                    convertView = layoutInflater.inflate(R.layout.simple_edit_text, null);     // 从指定的 simple_edit_text.xml 资源中扩充新的视图层次结构
                final EditText editText = (EditText) convertView.findViewById(R.id.tagText);     // 编辑框 id: tagText

                editText.setText(alarm.getAlarmName());     // 闹钟名称
                editText.addTextChangedListener(new TextWatcher() {     // 修改监听
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        alarm.setAlarmName(s.toString());
                    }
                });
                break;

            // 编辑闹钟时间
            case TIME:
                if (null == convertView)
                    convertView = layoutInflater.inflate(R.layout.time_picker, null);     // 从指定的 time_picker.xml 资源中扩充新的视图层次结构
                final TimePicker timePicker1 = (TimePicker) convertView.findViewById(R.id.timePicker);     // 时间选取器 id: timePicker

                int oldHour = alarm.getAlarmTime().get(Calendar.HOUR_OF_DAY);     // 小时
                int oldMinute = alarm.getAlarmTime().get(Calendar.MINUTE);     // 分钟

                Toast tt = new Toast(getContext());
                //// FIXME: 2015/11/25 正式环境改回去
//                timePicker1.setCurrentHour(oldHour);
//                timePicker1.setCurrentMinute(oldMinute);
                notifyDataSetChanged();

                final Calendar newAlarmTime = Calendar.getInstance();     // 当前日历时间
                // 测试用， 时间选择器设置为当前时间， 闹铃时间也设置为当前时间
                //comment
                timePicker1.setCurrentHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
                timePicker1.setCurrentMinute(Calendar.getInstance().get(Calendar.MINUTE) + 1);
                newAlarmTime.set(Calendar.HOUR_OF_DAY, Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
                newAlarmTime.set(Calendar.MINUTE, Calendar.getInstance().get(Calendar.MINUTE) + 1);
                alarm.setAlarmTime(newAlarmTime);
                //comment
                if (timePicker1 != null) {
                    // 设置时间选择器的修改监听
                    timePicker1.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                        @Override
                        public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                            // 提取时间选择器设置的时间
                            newAlarmTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            newAlarmTime.set(Calendar.MINUTE, minute);
                            alarm.setAlarmTime(newAlarmTime);

                            // 更新抽象化参数表
                            setMathAlarm(alarm);
                            // XXXXX: 有必要回写？      更新时间选择器
                            timePicker1.setCurrentHour(hourOfDay);
                            timePicker1.setCurrentMinute(minute);//
                        }
                    });
                }
                break;

            // 编辑重复日子
            case MULTIPLE_ImageButton:
                if (null == convertView)
                    convertView = layoutInflater.inflate(R.layout.week_button, null);     // 从指定的 week_button.xml 资源中扩充新的视图层次结构
                // http://stackoverflow.com/questions/12596199/android-how-to-set-onclick-event-for-button-in-list-item-of-listview
                SetWeekButton((Button) convertView.findViewById(R.id.btn_Sunday), Calendar.SUNDAY);     // 设置重复日子按钮中的 SUNDAY， 颜色与点击响应
                SetWeekButton((Button) convertView.findViewById(R.id.btn_Monday), Calendar.MONDAY);
                SetWeekButton((Button) convertView.findViewById(R.id.btn_Tuesday), Calendar.TUESDAY);
                SetWeekButton((Button) convertView.findViewById(R.id.btn_Webnesday), Calendar.WEDNESDAY);
                SetWeekButton((Button) convertView.findViewById(R.id.btn_Thursday), Calendar.THURSDAY);
                SetWeekButton((Button) convertView.findViewById(R.id.btn_Friday), Calendar.FRIDAY);
                SetWeekButton((Button) convertView.findViewById(R.id.btn_Saturday), Calendar.SATURDAY);
                break;

            // 编辑闹钟是否有效     没找到这个 simple_list_item_checked 布局文件
            case BOOLEAN:
                if (null == convertView)
                    convertView = layoutInflater.inflate(android.R.layout.simple_list_item_checked, null);     // 从指定的 simple_list_item_checked.xml 资源中扩充新的视图层次结构
                CheckedTextView checkedTextView = (CheckedTextView) convertView.findViewById(android.R.id.text1);
                checkedTextView.setText(alarmPreference.getTitle());
                checkedTextView.setChecked((Boolean) alarmPreference.getValue());
                break;

            // 备注？     没找到这个 simple_list_item_2 布局文件
            default:
                if (null == convertView)
                    convertView = layoutInflater.inflate(android.R.layout.simple_list_item_2, null);

                TextView text1 = (TextView) convertView.findViewById(android.R.id.text1);
                text1.setTextSize(18);
                text1.setText(alarmPreference.getTitle());

                TextView text2 = (TextView) convertView.findViewById(android.R.id.text2);
                text2.setText(alarmPreference.getSummary());
                break;
        }

        return convertView;
    }


    /* 设置重复日子按钮， 颜色与点击响应     初始设置按钮上字体色， 设置按钮的点击响应， 字反色、 重复日子添加或删除 */
    final void SetWeekButton(Button button, final int dayOfWeek) {
        final Button week = button;
        if (week != null) {
            // 初始设置重复日子按钮的字体颜色
            Boolean isRepeat = alarm.IsRepeat(dayOfWeek);     // 周几是否在重复日子里
            if (isRepeat) {      // 选择为重复则灰底白字
                week.setTextColor(Color.WHITE);
                week.setBackgroundColor(Color.GRAY);
            } else {      // 非重复白底黑字
                week.setTextColor(Color.BLACK);
                week.setBackgroundColor(Color.WHITE);
            }

            // 设置按钮的点击响应     字反色， 重复日子添加或删除
            week.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {     // 按钮点击相应     字反色， 重复日子添加或删除
                    int oldButtonTextColor = week.getCurrentTextColor();     // XXXXX: 按钮响应是异步的， 这个 week 还一直关联正确？

                    //0 白色
                    if (oldButtonTextColor != -1) {  // 当前文本颜色为黑/-1

                        week.setTextColor(Color.WHITE);
                        week.setBackgroundColor(Color.GRAY);

                        //选中
                        if (alarm != null)
                            alarm.addDay(dayOfWeek);     // 加入重复
                        Log.d("data", String.valueOf(dayOfWeek));

                    } else {
                        int[] days = alarm.getDays();
                        //至少选择一项才允许取消
                        if (days != null && days.length > 0) {
                            week.setTextColor(Color.BLACK);
                            week.setBackgroundColor(Color.WHITE);
                            //为取消
                            if (alarm != null)
                                alarm.removeDay(dayOfWeek);     // 取消重复
                        }
                    }
                }
            });
        }

    }


    /* 闹钟项数据转换为抽象化参数表 */
    public void setMathAlarm(Alarm alarm) {
        this.alarm = alarm;
        preferences.clear();
//        preferences.add(new AlarmPreference(Key.ALARM_ACTIVE, context.getString(R.string.AlarmStatus), null, null, alarm.getAlarmActive(), Type.BOOLEAN));
        preferences.add(new AlarmPreference(Key.ALARM_NAME,     "标签", alarm.getAlarmName(), null, alarm.getAlarmName(), Type.EditText));
        preferences.add(new AlarmPreference(Key.ALARM_TIME,     "时间", alarm.getAlarmTimeString(), null, alarm.getAlarmTime(), Type.TIME));
        preferences.add(new AlarmPreference(Key.ALARM_REPEAT,   "重复", "重复", repeatDays, alarm.getDays(), Type.MULTIPLE_ImageButton));

        Uri alarmToneUri = Uri.parse(alarm.getAlarmTonePath());
        Ringtone alarmTone = RingtoneManager.getRingtone(getContext(), alarmToneUri);

        if (alarmTone instanceof Ringtone && !alarm.getAlarmTonePath().equalsIgnoreCase("")) {
            preferences.add(new AlarmPreference(Key.ALARM_TONE, "铃声", alarmTone.getTitle(getContext()), alarmTones, alarm.getAlarmTonePath(), Type.Ring));
        } else {
            preferences.add(new AlarmPreference(Key.ALARM_TONE, "铃声", getAlarmTones()[0], alarmTones, null, Type.Ring));
        }

        preferences.add(new AlarmPreference(Key.ALARM_VIBRATE, "振动", null, null, alarm.IsVibrate(), Type.BOOLEAN));
    }


    /* 返回关联上下文， 调用页面？ */
    public Context getContext() {
        return context;
    }


    /* 返回闹铃音名称表 */
    public String[] getAlarmTones() {
        return alarmTones;
    }


    /* 返回闹铃音路径表 */
    public String[] getAlarmTonePaths() {
        return alarmTonePaths;
    }

}
