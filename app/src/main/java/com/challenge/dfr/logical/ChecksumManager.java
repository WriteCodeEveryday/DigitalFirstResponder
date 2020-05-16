package com.challenge.dfr.logical;

import com.challenge.dfr.database.ForensicCase;
import com.challenge.dfr.database.ForensicEvidence;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class ChecksumManager {
    private static String SEPARATOR = "\0"; //Pretty Rare.
    public static String getUpdatedForensicCaseChecksum(ForensicCase current) {
        String caseChecksum = getForensicCaseChecksum(current);
        System.out.println("getUpdatedForensicCaseChecksum: " + caseChecksum);
        return caseChecksum;
    }

    public static String getUpdatedForensicEvidenceChecksum(ForensicEvidence evidence) {
        String evidenceChecksum = getForensicEvidenceChecksum(evidence);
        System.out.println("getUpdatedForensicEvidenceChecksum: " + evidenceChecksum);
        return evidenceChecksum;
    }

    //Should only be used for new ForensicEvidence items, not for updates.
    private static String getForensicEvidenceChecksum(ForensicEvidence evidence) {
        String value = "";
        value += evidence.evidenceType + SEPARATOR;
        value += evidence.payloadType + SEPARATOR;
        value += evidence.payload + SEPARATOR;
        value += evidence.notes;
        value = stringToMd5(value);

        System.out.println("Expected Evidence Checksum: " +
                (value.equals(evidence.checksum)) +
                " " + evidence.eid);

        return stringToMd5(value);
    }

    private static String getForensicCaseChecksum(ForensicCase current) {
        String value = "";
        value += current.caseNumber + SEPARATOR;
        value += current.examiner + SEPARATOR;
        value += current.location + SEPARATOR;
        value += current.notes;

        return stringToMd5(value);
    }

    private static String stringToMd5(String value) {
        //Push out an MD5 hash
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] array = md.digest(value.getBytes());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < array.length; ++i) {
            sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
        }
        return sb.toString();
    }

    public static void submitWithEvidenceHash(final ForensicEvidence created) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Adding hash");
                created.checksum = ChecksumManager
                        .getUpdatedForensicEvidenceChecksum(created);
                System.out.println("New checksum " + created.checksum);

                ForensicsDatabaseManager.instance()
                        .getForensicEvidenceDao().insert(created);
                System.out.println("Updated item");
            }
        }).start();
    }
}
