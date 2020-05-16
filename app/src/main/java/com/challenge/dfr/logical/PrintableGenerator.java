package com.challenge.dfr.logical;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;

import com.challenge.dfr.R;
import com.challenge.dfr.database.ForensicCase;
import com.challenge.dfr.database.ForensicEvidence;
import com.challenge.dfr.hardware.PrinterManager;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.HashMap;
import java.util.Map;

public class PrintableGenerator {
    private String SEPARATOR = ";";
    private String file = "";
    public PrintableGenerator() {
        build();
    }

    private void build() {
        String printerModel = PrinterManager.getModel();
        String printerMode = PrinterManager.getMode();
        if (printerMode != null && printerModel != null) {
            file = PrinterManager.dashToLower(PrinterManager.getModel().toLowerCase()) + "_" + PrinterManager.getMode();
        } else if (printerModel != null) {
            file = PrinterManager.dashToLower(PrinterManager.getModel().toLowerCase()) + "_label";
        } else {
            file = PrinterManager.dashToLower(PrinterManager.getSupportedModels()[0].toLowerCase()) + "_label";
        }
    }

    public Bitmap buildOutput(ForensicCase current, ForensicEvidence item, Context ctx) {
        Resources resources = ctx.getResources();
        float scale = 3;//resources.getDisplayMetrics().density;
        int resource = ctx.getResources().getIdentifier(file, "drawable", ctx.getPackageName());
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap bitmap = BitmapFactory.decodeResource(resources, resource, options);
        android.graphics.Bitmap.Config bitmapConfig = bitmap.getConfig();
        // set default bitmap config if none
        if(bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        // resource bitmaps are imutable,
        // so we need to convert it to mutable one
        Bitmap rotatedBitmap = bitmap.copy(bitmapConfig, true);

        boolean rotated = bitmap.getHeight() > bitmap.getWidth();
        int textCodeDimension = Math.max(bitmap.getWidth(), bitmap.getHeight());

        if (rotated) {
            // Matrixes for rotations.
            Matrix rotate = new Matrix();
            rotate.postRotate(90);

            rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), rotate, true);
        }

        //Paints for text and background
        Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
        if (PrinterManager.getMode() != null &&
                PrinterManager.getMode().equals("roll") &&
                PrinterManager.getModel() != null &&
                PrinterManager.getModel().contains("820")) {
            text.setColor(Color.RED);
        } else {
            text.setColor(Color.BLACK);
        }
        text.setFakeBoldText(true);
        text.setTextSize((int) textCodeDimension / 34);

        Paint bg = new Paint(Paint.ANTI_ALIAS_FLAG);
        bg.setStyle(Paint.Style.FILL);
        bg.setColor(Color.WHITE);

        String[] outputs = getPayload(current, item);
        Canvas canvas = new Canvas(rotatedBitmap);

        // draw text to the Canvas center
        Rect bounds = new Rect();

        Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>() {{
            put(EncodeHintType.MARGIN, 0);
            put(EncodeHintType.ERROR_CORRECTION, 2);
            //put(EncodeHintType.PDF417_COMPACT, false);
            //put(EncodeHintType.PDF417_COMPACTION, Compaction.TEXT);
        }};

        int y = rotatedBitmap.getHeight()/12;
        BitMatrix matrix = null;
        Bitmap pdf417 = null;
        try {
            System.out.println("Payload for PDF417: " + outputs[0]);
            matrix = new MultiFormatWriter().encode(outputs[0],
                    BarcodeFormat.PDF_417, rotatedBitmap.getWidth(), (int) (rotatedBitmap.getHeight()/2), hints);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            pdf417 = barcodeEncoder.createBitmap(matrix);
            canvas.drawBitmap(pdf417, (rotatedBitmap.getWidth()/2) - (pdf417.getWidth()/2), y, null);
            y += pdf417.getHeight(); //Move the next point down.
        } catch (WriterException e) {
            e.printStackTrace();
        }

        int x = rotatedBitmap.getWidth()/2;
        int margin = (int) (rotatedBitmap.getWidth() * 0.025);
        String appNameLine = ctx.getResources().getString(R.string.app_name);
        text.getTextBounds(appNameLine, 0, appNameLine.length(), bounds);
        y += bounds.height();
        canvas.drawText(appNameLine, x - (bounds.width()/2), y, text);

        text.setTextSize((int) textCodeDimension / 26);
        y = rotatedBitmap.getHeight() - margin;

        String evidenceLine2 = ctx.getResources().getString(R.string.evidence_type_text) +
                ": " + outputs[3];
        text.getTextBounds(evidenceLine2, 0, evidenceLine2.length(), bounds);
        canvas.drawText(evidenceLine2, x - (bounds.width()/2), y, text);

        String evidenceLine = ctx.getResources().getString(R.string.evidence_number_text) +
                ": " + outputs[2];
        text.getTextBounds(evidenceLine, 0, evidenceLine.length(), bounds);
        y -= bounds.height() * 1.25;
        canvas.drawText(evidenceLine, rotatedBitmap.getWidth() - bounds.width() - margin, y, text);

        String caseLine = ctx.getResources().getString(R.string.case_number_text) +
                ": " + outputs[1];
        text.getTextBounds(caseLine, 0, caseLine.length(), bounds);
        canvas.drawText(caseLine, margin, y, text);


        if (rotated) {
            Matrix counter = new Matrix();
            counter.postRotate(270);
            rotatedBitmap = Bitmap.createBitmap(rotatedBitmap, 0, 0, rotatedBitmap.getWidth(), rotatedBitmap.getHeight(), counter, true);
        }
        return rotatedBitmap;
    }

    private String[] getPayload(ForensicCase current, ForensicEvidence evidence) {
        String pdfPayload = "DFR" +  SEPARATOR + current.caseNumber +
                SEPARATOR + evidence.evidenceType + SEPARATOR + current.cid +
                SEPARATOR + evidence.eid + SEPARATOR + current.notes + SEPARATOR + evidence.notes;

        return new String[] { pdfPayload, current.caseNumber, "" + evidence.eid, evidence.evidenceType };
    }

    private Rect generateBackground(Rect bounds, int x, int y, float scale) {
        Rect background = new Rect(bounds.left, bounds.top, bounds.right, bounds.bottom);
        background.left += x * scale;
        background.right += x * scale;
        background.top += y * scale;
        background.bottom += y * scale;

        int xSize = background.left - background.right;
        int ySize = background.top - background.bottom;
        background.left += xSize * 0.1;
        background.right -= xSize * 0.1;
        background.top += ySize * 0.3;
        background.bottom -= ySize * 0.3;
        return background;
    }
}
