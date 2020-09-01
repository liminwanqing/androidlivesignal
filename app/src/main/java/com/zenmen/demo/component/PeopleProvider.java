package com.zenmen.demo.component;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import java.sql.SQLData;
import java.sql.SQLDataException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PeopleProvider extends ContentProvider {
    private SQLiteDatabase db;
    private DBOpenHelper dbOpenHelper;

    private static final int MULTIPLE_PEOPLE = 1;
    private static final int SINGLE_PEOPLE = 2;
    private static UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(People.AUTHORITY, People.PATH_MULTIPLE, MULTIPLE_PEOPLE);
        uriMatcher.addURI(People.AUTHORITY, People.PATH_SINGLE, SINGLE_PEOPLE);
    }

    @Override
    public boolean onCreate() {
        dbOpenHelper = new DBOpenHelper(this.getContext(), DBOpenHelper.DB_NAME, null, DBOpenHelper.DB_VERSION);
        db = dbOpenHelper.getWritableDatabase();
        if (db == null) {
            return false;
        } else {
            return true;
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(DBOpenHelper.DB_TABLE);
        switch (uriMatcher.match(uri)) {
            case SINGLE_PEOPLE:
                // 单条数据的处理
                qb.appendWhere(People.KEY_ID + "=" + uri.getPathSegments().get(1));
                break;
            default:
                break;
        }
        Cursor cursor = qb.query(db,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case MULTIPLE_PEOPLE:
                // 多条数据的处理
                return People.MIME_TYPE_MULTIPLE;
            case SINGLE_PEOPLE:
                // 单条数据的处理
                return People.MIME_TYPE_SINGLE;
            default:
                throw new IllegalArgumentException("Unkown uro:" + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        long id = db.insert(DBOpenHelper.DB_TABLE, null, values);
        if (id > 0) {
            Uri newUri = ContentUris.withAppendedId(People.CONTENT_URI, id);
            getContext().getContentResolver().notifyChange(newUri, null);
            return newUri;
        }
        throw new SQLException("failed to insert row into " + uri);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case MULTIPLE_PEOPLE:
                // 多条数据的处理
                count = db.delete(DBOpenHelper.DB_TABLE, selection, selectionArgs);
                break;
            case SINGLE_PEOPLE:
                // 单条数据的处理
                String segment = uri.getPathSegments().get(1);
                count = db.delete(DBOpenHelper.DB_TABLE, People.KEY_ID + "=" + segment, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI:" + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int count;
        switch (uriMatcher.match(uri)) {
            case MULTIPLE_PEOPLE:
                // 多条数据的处理
                count = db.update(DBOpenHelper.DB_TABLE, values, selection, selectionArgs);
                break;
            case SINGLE_PEOPLE:
                // 单条数据的处理
                String segment = uri.getPathSegments().get(1);
                count = db.update(DBOpenHelper.DB_TABLE, values, People.KEY_ID + "=" + segment, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknow URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
