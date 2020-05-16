package com.challenge.dfr.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ForensicEvidenceDao {
    @Query("SELECT * FROM forensic_evidence WHERE cid = (:cid) ORDER BY creation_date ASC")
    List<ForensicEvidence> getEvidence(int cid);

    @Query("SELECT * FROM forensic_evidence WHERE cid = (:cid) AND eid = (:eid) LIMIT 1")
    ForensicEvidence fetchByEvidenceId(int cid, int eid);

    @Insert
    void insert(ForensicEvidence new_evidence);

    @Update
    void update(ForensicEvidence existing_evidence);

    @Delete
    void delete(ForensicEvidence deleted_evidence);
}
