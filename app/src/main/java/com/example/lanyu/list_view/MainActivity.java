package com.example.lanyu.list_view;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.lanyu.swaplist.*;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ListView listView;
    ArrayAdapter<MySpannableString> mAdapter;
    ArrayList<MySpannableString> mList;
    EditText editText;
    Button button;
    SQLiteDatabase db;
    int mPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.list);
        editText = (EditText) findViewById(R.id.edit);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp = editText.getText().toString().trim();
                if (!temp.equals("")) {
                    MySpannableString s = new MySpannableString(temp);
                    mAdapter.insert(s, 0);
                    Save_Data(mAdapter);
                    editText.setText("");
                    Toast.makeText(MainActivity.this, "已创建事件：" + temp, Toast.LENGTH_SHORT).show();
                }
            }
        });
        Init_Data();
        Get_Data();

        SharedPreferences preferences = getSharedPreferences("use_info", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        if (preferences.getInt("first", 0) == 0) {
            Log.e("pre", String.valueOf(preferences.getInt("first", 0)));
            mList.add(new MySpannableString("在顶部输入框输入事件"));
            mList.add(new MySpannableString("点击添加按钮可以把时间添加到列表中"));
            MySpannableString s = new MySpannableString("在事件上直接滑动可以标记为已完成");
            s.setSpan(new StrikethroughSpan(), 0, s.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            s.setSpan(new ForegroundColorSpan(Color.parseColor("#CCCCCC")), 0, s.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            s.setTag(1);
            mList.add(s);
            editor.putInt("first", 1);
            editor.commit();
        }

        mAdapter = new ArrayAdapter<MySpannableString>(
                this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                mList);
        listView.setAdapter(mAdapter);
        Save_Data(mAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPosition = position;
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("警告");
                builder.setMessage("是否删除本条记录?");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Delete_Data(mAdapter.getItem(mPosition));
                        mAdapter.remove(mAdapter.getItem(mPosition));
                        Save_Data(mAdapter);
                        mAdapter.notifyDataSetChanged();
                        Toast.makeText(MainActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.create().show();
            }
        });

        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(
                        listView,
                        new SwipeDismissListViewTouchListener.DismissCallbacks() {
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }

                            @Override
                            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                                MySpannableString s = null;
                                int p = 0;
                                for (int position : reverseSortedPositions) {
                                    s = mAdapter.getItem(position);
                                    p = position;
                                    mAdapter.remove(s);
                                }
                                if (s.getTag() == 0) {
                                    MySpannableString spannableString = new MySpannableString(s);
                                    spannableString.setSpan(new StrikethroughSpan(), 0, s.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#CCCCCC")), 0, s.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                                    spannableString.setTag(1);
                                    mAdapter.add(spannableString);
                                } else {
                                    mAdapter.insert(s, p);
                                }
                                Toast.makeText(MainActivity.this, "已完成事件:" + s, Toast.LENGTH_SHORT).show();
                                mAdapter.notifyDataSetChanged();
                                Save_Data(mAdapter);
                            }
                        });
        listView.setOnTouchListener(touchListener);
        listView.setOnScrollListener(touchListener.makeScrollListener());
    }

    private void Init_Data() {
        mList = new ArrayList<MySpannableString>();
        db = SQLiteDatabase.openOrCreateDatabase("/data/data/com.example.lanyu.list_view/data.db", null);
        String sql = "create table if not exists schedule(line VARCHAR(50),flag INTEGER)";
        //Log.e("Init", sql);
        db.execSQL(sql);
    }

    private void Get_Data() {
        Cursor c = db.query("schedule", null, null, null, null, null, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            //Log.e("Get",String.valueOf(i));
            String line = c.getString(c.getColumnIndex("line"));
            //Log.e("Get", line);
            int flag = Integer.valueOf(c.getString(c.getColumnIndex("flag")));
            //Log.e("Get", String.valueOf(flag));
            MySpannableString s = new MySpannableString(line);
            s.setTag(flag);
            //Log.e("Get", String.valueOf(c.getCount()));
            if (flag == 1) {
                s.setSpan(new StrikethroughSpan(), 0, s.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                s.setSpan(new ForegroundColorSpan(Color.parseColor("#CCCCCC")), 0, s.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            }
            mList.add(s);
            c.moveToNext();
        }

    }

    private void Insert_Data(MySpannableString s) {
        Delete_Data(s);
        String sql = "insert into schedule(line,flag) values('" + s.toString() + "','" + s.getTag() + "')";
        Log.e("Insert", sql);
        db.execSQL(sql);
    }

    private void Delete_Data(MySpannableString s) {
        db.delete("schedule", "line=?", new String[]{s.toString()});
    }

    private void Save_Data(ArrayAdapter<MySpannableString> adapter) {
        for (int i = 0; i < adapter.getCount(); i++) {
            Log.e("Save", String.valueOf(adapter.getCount()));
            Insert_Data(adapter.getItem(i));
        }
    }
}
