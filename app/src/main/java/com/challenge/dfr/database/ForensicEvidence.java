package com.challenge.dfr.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "forensic_evidence")
public class ForensicEvidence {
    @PrimaryKey(autoGenerate = true)
    public int eid;

    @ColumnInfo(name="cid")
    public int cid;

    @ColumnInfo(name = "evidence_type")
    public String evidenceType;

    @ColumnInfo(name = "payload_type")
    public String payloadType;

    @ColumnInfo(name = "payload")
    public String payload;

    @ColumnInfo(name = "notes")
    public String notes;

    @ColumnInfo(name = "checksum")
    public String checksum;

    @ColumnInfo(name = "creation_date")
    public long created;
}