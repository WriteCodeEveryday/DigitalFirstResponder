package com.challenge.dfr.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ForensicCaseDao {
    @Query("SELECT * FROM forensic_case")
    List<ForensicCase> getCases();

    @Query("SELECT * FROM forensic_case WHERE cid = (:cid) LIMIT 1")
    ForensicCase fetchById(int cid);

    @Query("SELECT * FROM forensic_case WHERE case_number = (:number) LIMIT 1")
    ForensicCase fetchByCaseNumber(String number);

    @Insert
    void insert(ForensicCase new_case);

    @Update
    void update(ForensicCase existing_case);

    @Delete
    void delete(ForensicCase deleted_case);
}
