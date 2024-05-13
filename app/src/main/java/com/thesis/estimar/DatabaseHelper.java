package com.thesis.estimar;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "CalculationHistory.db";
    private static final String TABLE_NAME = "calculation_history";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_CALCULATION = "calculation";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_CALCULATION + " TEXT"
                + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addCalculation(String calculation) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CALCULATION, calculation);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }


    public List<String> getCalculationHistory() {
        List<String> history = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        if (cursor.moveToFirst()) {
            do {
                // Assuming column order: id, calculation
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                @SuppressLint("Range") String calculation = cursor.getString(cursor.getColumnIndex(COLUMN_CALCULATION));
                // Construct history string with format: id, calculation
                String historyEntry = id + ", " + calculation;
                history.add(historyEntry);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return history;
    }
    // Add method to retrieve calculation history
}
