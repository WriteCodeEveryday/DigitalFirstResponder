package com.challenge.dfr.logical;

import android.content.Context;

import androidx.room.Room;

import com.challenge.dfr.database.ForensicsDatabase;

public class ForensicsDatabaseManager {
    private static ForensicsDatabase db;

    public static ForensicsDatabase instance(Context ctx) {
        if (db == null) {
            db = Room.databaseBuilder(ctx,
                    ForensicsDatabase.class, "dfr").build();
        }
        return instance();
    }

    public static ForensicsDatabase instance() {
        return db;
    }
}
