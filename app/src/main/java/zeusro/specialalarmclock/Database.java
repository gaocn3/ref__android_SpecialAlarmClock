package zeusro.specialalarmclock;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Z on 2015/11/16.
 * 数据库存储
 */
public class Database extends SQLiteOpenHelper {
    static Database instance = null;     // 本应用数据库
    static SQLiteDatabase database = null;     // 下面的 SQLite 数据库

    static final String DATABASE_NAME = "DB";
    static final int DATABASE_VERSION = 1;

    public static final String ALARM_TABLE          = "alarm";     // 表名
    public static final String COLUMN_ALARM_ID      = "_id";     // 编号
    public static final String COLUMN_ALARM_ACTIVE  = "alarm_active";     // 闹钟项是否激活
    public static final String COLUMN_ALARM_TIME    = "alarm_time";     // 闹钟设置的时间
    public static final String COLUMN_ALARM_DAYS    = "alarm_days";     // 闹铃重复的日子
    public static final String COLUMN_ALARM_TONE    = "alarm_tone";     // 闹铃音文件
    public static final String COLUMN_ALARM_VIBRATE = "alarm_vibrate";     // 振动状态
    public static final String COLUMN_ALARM_NAME    = "alarm_name";     // 闹钟名

    /* 静态实例为空则初始化创建 SQLite 数据库， 存入静态实例     调用函数时 context 是页面实例 */
    public static void init(Context context) {
        if (null == instance) {
            instance = new Database(context);     // 用基类构建 SQLite 数据库
        }
    }

    /* 获取 SQLite 数据库 */
    public static SQLiteDatabase getDatabase() {
        if (null == database) {
            database = instance.getWritableDatabase();
        }
        return database;
    }

    /* SQLite 数据库关闭， 销毁数据库句柄 */
    public static void deactivate() {
        if (null != database && database.isOpen()) {
            database.close();
        }
        database = null;
        instance = null;
    }

    /* ADD_gaocn: 使用闹钟数据 alarm 封装一个数据库项 */
    private static ContentValues package_alarm(Alarm alarm) {
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_ALARM_ACTIVE, alarm.IsAlarmActive());     // active
        cv.put(COLUMN_ALARM_TIME, alarm.getAlarmTimeString());     // time

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = null;
            oos = new ObjectOutputStream(bos);
            oos.writeObject(alarm.getDays());
            byte[] buff = bos.toByteArray();

            cv.put(COLUMN_ALARM_DAYS, buff);     // days

        } catch (Exception e) {
        }
        cv.put(COLUMN_ALARM_TONE, alarm.getAlarmTonePath());     // tone
        cv.put(COLUMN_ALARM_VIBRATE, alarm.IsVibrate());      // vibrate
        cv.put(COLUMN_ALARM_NAME, alarm.getAlarmName());     // name

        return cv;
    }

    /* 数据库表插入一条新的闹钟项 alarm 条目      error: 第一条则是创建数据库表 */
    public static long create(Alarm alarm) {
        ContentValues cv = new ContentValues();

        if(false) {
//            cv.put(COLUMN_ALARM_ACTIVE, alarm.IsAlarmActive());
//            cv.put(COLUMN_ALARM_TIME, alarm.getAlarmTimeString());
//
//            try {
//                ByteArrayOutputStream bos = new ByteArrayOutputStream();
//                ObjectOutputStream oos = null;
//                oos = new ObjectOutputStream(bos);
//                oos.writeObject(alarm.getDays());
//                byte[] buff = bos.toByteArray();
//
//                cv.put(COLUMN_ALARM_DAYS, buff);
//
//            } catch (Exception e) {
//            }
//            cv.put(COLUMN_ALARM_TONE, alarm.getAlarmTonePath());
//            cv.put(COLUMN_ALARM_VIBRATE, alarm.IsVibrate());
//            cv.put(COLUMN_ALARM_NAME, alarm.getAlarmName());

        } else {
            cv = Database.package_alarm(alarm);
        }

        return getDatabase().insert(ALARM_TABLE, null, cv);
    }

    /* 数据库表更新一条闹钟项 alarm 条目     将闹钟数据 alarm 按 id 更新存入数据库表 */
    public static int update(Alarm alarm) {
        ContentValues cv = new ContentValues();

        if(false) {
//            cv.put(COLUMN_ALARM_ACTIVE, alarm.IsAlarmActive());     // active
//            cv.put(COLUMN_ALARM_TIME, alarm.getAlarmTimeString());     // time
//
//            try {
//                ByteArrayOutputStream bos = new ByteArrayOutputStream();
//                ObjectOutputStream oos = null;
//                oos = new ObjectOutputStream(bos);
//                oos.writeObject(alarm.getDays());
//                byte[] buff = bos.toByteArray();
//
//                cv.put(COLUMN_ALARM_DAYS, buff);     // days
//
//            } catch (Exception e) {
//            }
//            cv.put(COLUMN_ALARM_TONE, alarm.getAlarmTonePath());     // tone
//            cv.put(COLUMN_ALARM_VIBRATE, alarm.IsVibrate());      // vibrate
//            cv.put(COLUMN_ALARM_NAME, alarm.getAlarmName());     // name

        } else {
            cv = package_alarm(alarm);
        }
        return getDatabase().update(ALARM_TABLE, cv, "_id=" + alarm.getId(), null);     // 根据 ID 更新数据库项
    }

    /* 删除 id 相同的数据库项 */
    public static int deleteEntry(Alarm alarm) {
        return deleteEntry(alarm.getId());
    }

    /* 数据库表 ALARM_TABLE 根据 id 删除 */
    public static int deleteEntry(int id) {
        return getDatabase().delete(ALARM_TABLE, COLUMN_ALARM_ID + "=" + id, null);
    }

    /* 数据库删除表 ALARM_TABLE */
    public static int deleteAll() {
        return getDatabase().delete(ALARM_TABLE, "1", null);
    }





    /* ADD_gaocn: 由检索结果的当前游标 cursor 从数据库提取数据， 填写到闹钟数据 alarm 中 */
    private static Alarm make_alarm(Cursor cursor) {
        Alarm alarm = new Alarm();

        alarm.setId(cursor.getInt(0));     // id
        alarm.setAlarmActive(cursor.getInt(1) == 1);     // active
        alarm.setAlarmTime(cursor.getString(2));     // time
        byte[] repeatDaysBytes = cursor.getBlob(3);     // repeat_days

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(repeatDaysBytes);
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            int[] repeatDays;
            Object object = objectInputStream.readObject();
            if (object instanceof int[]) {
                repeatDays = (int[]) object;
                alarm.setDays(repeatDays);     // repeat_days
            }
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        alarm.setAlarmTonePath(cursor.getString(4));     // tone
        alarm.setVibrate(cursor.getInt(5) == 1);     // vibrate
        alarm.setAlarmName(cursor.getString(6));     // name

        return alarm;
    }

    /* 按 id 值返回第一个闹钟数据 */
    public static Alarm getAlarm(int id) {
        // 按 id 查询数据库
        String[] columns = new String[]{     // 查询返回的字段顺序
                COLUMN_ALARM_ID,
                COLUMN_ALARM_ACTIVE,
                COLUMN_ALARM_TIME,
                COLUMN_ALARM_DAYS,
                COLUMN_ALARM_TONE,
                COLUMN_ALARM_VIBRATE,
                COLUMN_ALARM_NAME
        };
        Cursor c = getDatabase().query(ALARM_TABLE, columns, COLUMN_ALARM_ID + "=" + id, null, null, null,
                null);
        Alarm alarm = null;

        if (c.moveToFirst()) {
//            alarm = new Alarm();
//            alarm.setId(c.getInt(1));
//            alarm.setAlarmActive(c.getInt(2) == 1);
//            alarm.setAlarmTime(c.getString(3));
//            byte[] repeatDaysBytes = c.getBlob(4);
//
//            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(repeatDaysBytes);
//            try {
//                ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
//                int[] repeatDays;
//                Object object = objectInputStream.readObject();
//                if (object instanceof int[]) {
//                    repeatDays = (int[]) object;
//                    alarm.setDays(repeatDays);
//                }
//            } catch (StreamCorruptedException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
//            }
//
//            alarm.setAlarmTonePath(c.getString(6));
//            alarm.setVibrate(c.getInt(7) == 1);
//            alarm.setAlarmName(c.getString(8));

            alarm = make_alarm(c);     // XXXXX: 也许有错， 调试时对比上面被注释的代码
        }

        c.close();
        return alarm;
    }

    /* 数据库一般检索， 返回检索结果的游标 */
    public static Cursor getCursor() {
        String[] columns = new String[]{     // 查询返回的字段顺序
                COLUMN_ALARM_ID,
                COLUMN_ALARM_ACTIVE,
                COLUMN_ALARM_TIME,
                COLUMN_ALARM_DAYS,
                COLUMN_ALARM_TONE,
                COLUMN_ALARM_VIBRATE,
                COLUMN_ALARM_NAME
        };
        return getDatabase().query(ALARM_TABLE, columns, null, null, null, null,
                null);
    }

    /* 用基类构建数据库 DATABASE_NAME     调用函数时 context 使用的是主页面实例 */
    Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    /* 创建数据库表 */
    public void onCreate(SQLiteDatabase db) {
        // 数据库建表 ALARM_TABLE， 设置相关字段
        db.execSQL("CREATE TABLE IF NOT EXISTS " + ALARM_TABLE + " ( "
                + COLUMN_ALARM_ID + " INTEGER primary key autoincrement, "
                + COLUMN_ALARM_ACTIVE + " INTEGER NOT NULL, "
                + COLUMN_ALARM_TIME + " TEXT NOT NULL, "
                + COLUMN_ALARM_DAYS + " BLOB NOT NULL, "
                + COLUMN_ALARM_TONE + " TEXT NOT NULL, "
                + COLUMN_ALARM_VIBRATE + " INTEGER NOT NULL, "
                + COLUMN_ALARM_NAME + " TEXT NOT NULL)");
    }

    @Override
    /* 数据库建立或更替 */
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ALARM_TABLE);
        onCreate(db);
    }

    /*  从数据库读取所有的闹钟项数组 */
    public static List<Alarm> getAll() {
        List<Alarm> alarms = new ArrayList<Alarm>();     // 数组
        Cursor cursor = Database.getCursor();     // 一般数据库检索， 返回游标

        if (cursor.moveToFirst()) {     // 数据库游标已移动到第一项
            do {
//                Alarm alarm = new Alarm();
//                alarm.setId(cursor.getInt(0));     // id
//                alarm.setAlarmActive(cursor.getInt(1) == 1);     // active
//                alarm.setAlarmTime(cursor.getString(2));     // time
//                byte[] repeatDaysBytes = cursor.getBlob(3);     // repeat_days
//
//                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(repeatDaysBytes);
//                try {
//                    ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
//                    int[] repeatDays;
//                    Object object = objectInputStream.readObject();
//                    if (object instanceof int[]) {
//                        repeatDays = (int[]) object;
//                        alarm.setDays(repeatDays);     // repeat_days
//                    }
//                } catch (StreamCorruptedException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } catch (ClassNotFoundException e) {
//                    e.printStackTrace();
//                }
//
//                alarm.setAlarmTonePath(cursor.getString(4));     // tone
//                alarm.setVibrate(cursor.getInt(5) == 1);     // vibrate
//                alarm.setAlarmName(cursor.getString(6));     // name
//
//                alarms.add(alarm);     // 数组添加一项

                alarms.add(make_alarm(cursor));     // XXXXX: 也许有错， 调试时对比上面被注释的代码

            } while (cursor.moveToNext());     // 向后移动游标
        }
        cursor.close();
        return alarms;
    }
}
