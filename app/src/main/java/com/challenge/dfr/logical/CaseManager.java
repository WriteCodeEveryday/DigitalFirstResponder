package com.challenge.dfr.logical;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;

import com.challenge.dfr.database.ForensicCase;
import com.challenge.dfr.database.ForensicEvidence;

import java.util.ArrayList;

public class CaseManager {
    private static ForensicCase current;
    private static ForensicEvidence current_evidence;
    private static String evidence_path;
    private static int evidence_type;

    private static boolean evidence_pending = false;
    private static ArrayList<Bitmap> evidence=  new ArrayList<Bitmap>();
    private static String evidence_text = null;
    private static String scanner_input_type = null;;

    public static void setCase(ForensicCase selected) {
        current = selected;
    }
    public static ForensicCase getCase() {
        return current;
    }

    public static void setEvidence(ForensicEvidence selected) {
        current_evidence = selected;
    }
    public static ForensicEvidence getEvidence() {
        return current_evidence;
    }

    public static void setEvidenceType(int type) { evidence_type = type; }
    public static int getEvidenceType() { return evidence_type; }

    public static void setScannerInputType(String input) { scanner_input_type = input; }
    public static String getScannerInputType() { return scanner_input_type; }

    public static void setEvidencePath(String path ) { evidence_path = path; }
    public static String getEvidencePath() { return evidence_path; }

    public static void acceptNewEvidence() {
        System.out.println("Awaiting incoming evidence");
        evidence = new ArrayList<Bitmap>();
        evidence_text = null;
        evidence_pending = true;
    }

    public static boolean isEvidencePending() {
        return evidence_pending;
    }

    public static void completeNewEvidence() {
        System.out.println("Evidence acquisition complete");
        evidence_pending = false;
    }

    public static void addEvidence(Bitmap input, int index) {
        System.out.println("Received a file: " + index);
        evidence.add(index - 1, input);
    }

    public static void addEvidence(String input) { evidence_text = input; }

    public static int getPendingEvidence() { return evidence.size(); }

    public static PdfDocument getDocument() {
        if (evidence.size() < 1) {
            return null;
        }

        PdfDocument doc = new PdfDocument();
        for (int i = 1; i <= evidence.size(); i++) { //Pages for PDF need to
            Bitmap image = evidence.get(i-1);
            PdfDocument.PageInfo info = new
                    PdfDocument.PageInfo.Builder(image.getWidth(), image.getHeight(), i).create();
            PdfDocument.Page page  = doc.startPage(info);

            Canvas canvas = page.getCanvas();
            canvas.drawBitmap(image, 0, 0, null);
            doc.finishPage(page);
        }

        return doc;
    }

    public static Bitmap getSingleEvidenceBitmap() {
        if (evidence.size() < 1) {
            return null;
        }

        Bitmap output = evidence.get(0);

        if (evidence.size() == 1) {
            return output;
        }

        for (int i = 1; i < evidence.size(); i++) {
            Bitmap incoming = evidence.get(i);
            int width = Math.max(output.getWidth(), incoming.getWidth());
            int height = output.getHeight() + incoming.getHeight();

            Bitmap outgoing  = Bitmap.createScaledBitmap(output, width, height, true);
            Canvas canvas = new Canvas(outgoing);
            canvas.drawBitmap(output, 0, 0, null);
            canvas.drawBitmap(output, 0, output.getHeight(), null);
            output = outgoing;
        }
        return output;
    }

    public static String getTextEvidence() {
        return evidence_text;
    }
}
