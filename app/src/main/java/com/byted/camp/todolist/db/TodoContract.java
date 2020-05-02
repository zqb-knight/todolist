package com.byted.camp.todolist.db;

import android.provider.BaseColumns;

/**
 * Created on 2019/1/22.
 *
 * @author xuyingyi@bytedance.com (Yingyi Xu)
 */
public final class TodoContract {

    // TODO 定义表结构和 SQL 语句常量
    //创建表格
    public static class TodoEntry implements BaseColumns{
        public static final String TABLE_NAME = "entry";
        public static final String LIST_CONTENT = "list_content";
        public static final String LIST_TIME = "list_time";
        public static final String List_STATE = "state";
    }

    //删除表格
    public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TodoEntry.TABLE_NAME;


    public static  final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TodoEntry.TABLE_NAME + " (" +
                    TodoEntry._ID + " INTEGER PRIMARY KEY," +
                    TodoEntry.LIST_CONTENT + " TEXT" +
                    TodoEntry.LIST_TIME + " TEXT," +
                    TodoEntry.List_STATE + " TEXT)";
    private TodoContract() {
    }

}
