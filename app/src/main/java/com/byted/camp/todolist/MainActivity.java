package com.byted.camp.todolist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.byted.camp.todolist.beans.Note;
import com.byted.camp.todolist.beans.State;
import com.byted.camp.todolist.db.TodoContract;
import com.byted.camp.todolist.db.TodoDbHelper;
import com.byted.camp.todolist.operation.activity.DatabaseActivity;
import com.byted.camp.todolist.operation.activity.DebugActivity;
import com.byted.camp.todolist.operation.activity.SettingActivity;
import com.byted.camp.todolist.ui.NoteListAdapter;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ADD = 1002;

    private RecyclerView recyclerView;
    private NoteListAdapter notesAdapter;
    private TodoDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        dbHelper = new TodoDbHelper(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(
                        new Intent(MainActivity.this, NoteActivity.class),
                        REQUEST_CODE_ADD);
            }
        });

        recyclerView = findViewById(R.id.list_todo);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        notesAdapter = new NoteListAdapter(new NoteOperator() {
            @Override
            public void deleteNote(Note note) {
                MainActivity.this.deleteNote(note);
            }

            @Override
            public void updateNote(Note note) {
                MainActivity.this.updateNode(note);
            }
        });
        recyclerView.setAdapter(notesAdapter);

        notesAdapter.refresh(loadNotesFromDatabase());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingActivity.class));
                return true;
            case R.id.action_debug:
                startActivity(new Intent(this, DebugActivity.class));
                return true;
            case R.id.action_database:
                startActivity(new Intent(this, DatabaseActivity.class));
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD
                && resultCode == Activity.RESULT_OK) {
            notesAdapter.refresh(loadNotesFromDatabase());
        }
    }

    private List<Note> loadNotesFromDatabase() {
        // TODO 从数据库中查询数据，并转换成 JavaBeans
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String[] projection = {
                BaseColumns._ID,
                TodoContract.TodoEntry.LIST_CONTENT,
                TodoContract.TodoEntry.LIST_TIME,
                TodoContract.TodoEntry.List_STATE
        };

        //String selection = TodoContract.TodoEntry.LIST_CONTENT + " = ?";
        //String[] selectionArgs = {"my_title"};
        String od = TodoContract.TodoEntry.LIST_TIME + " DESC";
        Cursor c = db.query(
                TodoContract.TodoEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                od
        );
        Log.i("de", "perfrom query data:");
        List<Note> notes = new ArrayList<>();
        while(c.moveToNext()){
            long id = c.getLong(c.getColumnIndexOrThrow(TodoContract.TodoEntry._ID));
            String content = c.getString(c.getColumnIndex(TodoContract.TodoEntry.LIST_CONTENT));
            String timeStr = c.getString(c.getColumnIndex(TodoContract.TodoEntry.LIST_TIME));
            int state = c.getInt(c.getColumnIndex(TodoContract.TodoEntry.List_STATE));
            //转换格式
            SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
            ParsePosition pos = new ParsePosition(0);
            Date time = formatter.parse(timeStr,pos);
            Note tmpNote = new Note(id);
            tmpNote.setContent(content);
            tmpNote.setDate(time);
            tmpNote.setState(State.TODO);
            notes.add(tmpNote);
        }
        c.close();
        return notes;
    }

    private void deleteNote(Note note) {
        // TODO 删除数据
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // Define 'where' part of query.
        String selection = TodoContract.TodoEntry.LIST_TIME+ " LIKE ?";
        // Specify arguments in placeholder order.
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss", Locale.ENGLISH);
        String timeStr = formatter.format(note.getDate());
        String[] selectionArgs = {timeStr};
        // Issue SQL statement.
        int deletedRows = db.delete(TodoContract.TodoEntry.TABLE_NAME, selection, selectionArgs);

        notesAdapter.refresh(loadNotesFromDatabase());//更新数据

    }

    private void updateNode(Note note) {
        // TODO 更新数据
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        String s;
        if(note.getState() == State.DONE)
            s = "0";
        else
            s = "1";

        values.put(TodoContract.TodoEntry.List_STATE, s);
        // Which row to update, based on the title
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss", Locale.ENGLISH);
        String timeStr = formatter.format(note.getDate());
        String selection = TodoContract.TodoEntry.LIST_TIME + " LIKE ?";
        String[] selectionArgs = {timeStr};
        int count = db.update(
                TodoContract.TodoEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        notesAdapter.refresh(loadNotesFromDatabase());//更新数据
    }


}
