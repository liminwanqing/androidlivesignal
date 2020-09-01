package com.zenmen.demo.component;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.zenmen.demo.R;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class DataActivity extends AppCompatActivity {
    private ContentResolver resolver;
    private TextView nameEt, ageEt, heightEt, labelTv, displayTv, idEt;
    private Button addBtn, queryBtn, queryAllBtn, deleteBtn, deleteAllBtn, updateBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_main);
        resolver = this.getContentResolver();
        initView();
        initEvent();
    }

    private void initView() {
        nameEt = findViewById(R.id.nameEt);
        ageEt  = findViewById(R.id.ageEt);
        heightEt = findViewById(R.id.heightEt);
        labelTv = findViewById(R.id.labelTv);
        displayTv = findViewById(R.id.displayTv);
        idEt = findViewById(R.id.idEt);

        addBtn = findViewById(R.id.addBtn);
        queryBtn = findViewById(R.id.queryBtn);
        queryAllBtn = findViewById(R.id.queryBtn);
        deleteBtn = findViewById(R.id.deleteBtn);
        deleteAllBtn = findViewById(R.id.deleteAllBtn);
        updateBtn = findViewById(R.id.updateBtn);
    }

    private void initEvent() {
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues values = new ContentValues();
                values.put(People.KEY_NAME, nameEt.getText().toString());
                values.put(People.KEY_AGE, Integer.parseInt(ageEt.getText().toString()));
                values.put(People.KEY_HEIGHT, Float.parseFloat(heightEt.getText().toString()));
                Uri newUri = resolver.insert(People.CONTENT_URI, values);
                labelTv.setText("添加成功，URI:" + newUri);
            }
        });

        queryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(People.CONTENT_URI_STRING + "/" + idEt.getText().toString());
                Cursor cursor = resolver.query(uri,
                        new String[]{People.KEY_ID, People.KEY_NAME, People.KEY_AGE, People.KEY_HEIGHT},
                        null, null, null);
                if (cursor == null) {
                    labelTv.setText("数据库中没有数据");
                    return;
                }
                labelTv.setText("数据库：" + String.valueOf(cursor.getCount()) + "条记录");
                String msg = "";
                if (cursor.moveToFirst()) {
                    do {
                        msg += "ID: " + cursor.getString(cursor.getColumnIndex(People.KEY_ID)) + ",";
                        msg += "姓名: " + cursor.getString(cursor.getColumnIndex(People.KEY_NAME)) + ",";
                        msg += "年龄: " + cursor.getInt(cursor.getColumnIndex(People.KEY_AGE)) + ",";
                        msg += "身高: " + cursor.getFloat(cursor.getColumnIndex(People.KEY_HEIGHT)) + "\n";
                    } while (cursor.moveToNext());
                }
                displayTv.setText(msg);
            }
        });

        queryAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor cursor = resolver.query(People.CONTENT_URI,
                        new String[]{People.KEY_ID, People.KEY_NAME, People.KEY_AGE, People.KEY_HEIGHT},
                        null, null, null);
                if (cursor == null) {
                    labelTv.setText("数据库中没有数据");
                    return;
                }
                labelTv.setText("数据库：" + String.valueOf(cursor.getCount()) + "条记录");
                String msg = "";
                if (cursor.moveToFirst()) {
                    do {
                        msg += "ID: " + cursor.getString(cursor.getColumnIndex(People.KEY_ID)) + ",";
                        msg += "姓名: " + cursor.getString(cursor.getColumnIndex(People.KEY_NAME)) + ",";
                        msg += "年龄: " + cursor.getInt(cursor.getColumnIndex(People.KEY_AGE)) + ",";
                        msg += "身高: " + cursor.getFloat(cursor.getColumnIndex(People.KEY_HEIGHT)) + "\n";
                    } while (cursor.moveToNext());
                }
                displayTv.setText(msg);
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(People.CONTENT_URI_STRING + "/" + idEt.getText().toString());
                int count = resolver.delete(uri, null, null);
                String msg = count + " 条数据被删除";
                labelTv.setText(msg);
            }
        });

        deleteAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resolver.delete(People.CONTENT_URI, null, null);
                String msg = "数据全部删除";
                labelTv.setText(msg);
            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues values = new ContentValues();
                values.put(People.KEY_NAME, nameEt.getText().toString());
                values.put(People.KEY_AGE, Integer.parseInt(ageEt.getText().toString()));
                values.put(People.KEY_HEIGHT, Float.parseFloat(heightEt.getText().toString()));
                Uri uri = Uri.parse(People.CONTENT_URI_STRING + "/" + idEt.getText().toString());
                int result = resolver.update(uri, values, null, null);
                String msg = "更新ID为" + idEt.getText().toString() + "的数据" + (result > 0 ? "成功" : "失败");
                labelTv.setText(msg);
            }
        });
    }
}
