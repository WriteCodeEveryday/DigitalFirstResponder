package com.challenge.dfr.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {ForensicCase.class, ForensicEvidence.class}, version = 1)
public abstract class ForensicsDatabase extends RoomDatabase {
    public abstract ForensicCaseDao getForensicCaseDao();
    public abstract ForensicEvidenceDao getForensicEvidenceDao();
}
