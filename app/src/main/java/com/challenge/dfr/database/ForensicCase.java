package com.challenge.dfr.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "forensic_case")
public class ForensicCase {
    @PrimaryKey(autoGenerate = true)
    public int cid;

    @ColumnInfo(name = "case_number")
    public String caseNumber;

    @ColumnInfo(name = "examiner")
    public String examiner;

    @ColumnInfo(name = "location")
    public String location;

    @ColumnInfo(name = "notes")
    public String notes;

    @ColumnInfo(name = "checksum")
    public String checksum;

    @ColumnInfo(name = "creation_date")
    public long created;
}