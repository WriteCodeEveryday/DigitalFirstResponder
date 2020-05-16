package com.challenge.dfr.logical;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.util.List;

public class ImageMetadataProcessor {
    public static String processOCR(Bitmap data, Context ctx) {
        TextRecognizer recognizer = new TextRecognizer.Builder(ctx).build();
        Frame frame = new Frame.Builder().setBitmap(data).build();
        SparseArray<TextBlock> text = recognizer.detect(frame);

        String output = "";
        for (int i = 0; i < text.size(); i++) {
            TextBlock current = text.valueAt(i);
            output += current.getValue() + "\n";
        }
        return output;
    }

    public static String processLicense(Bitmap data, Context ctx) {
        BarcodeDetector detector = new BarcodeDetector.Builder(ctx)
                .setBarcodeFormats(Barcode.PDF417 | Barcode.DRIVER_LICENSE | Barcode.QR_CODE)
                .build();
        Frame frame = new Frame.Builder().setBitmap(data).build();
        SparseArray<Barcode> barcodes = detector.detect(frame);

        String output = "";
        if (barcodes.size() > 0) {
            for (int i = 0; i < barcodes.size(); i++) {
                Barcode current = barcodes.valueAt(i);
                switch (current.valueFormat) {
                    case Barcode.PDF417:
                        String[] segments = current.rawValue.split("\\n");
                        String name = null;
                        for (int j = 0; j < segments.length; j++) {
                            String trimmed = segments[j].trim();
                            String extracted = null;
                            if (trimmed.startsWith("DAA") || (trimmed.contains("ANSI") && trimmed.contains("DAA"))) { // Full Name
                                extracted = trimmed.split("DAA")[1];
                                output += extracted + "\n";
                            } else if (trimmed.startsWith("DAC")) { // Last Name
                                extracted = trimmed.split("DAC")[1];
                                if (name == null) {
                                    name = extracted;
                                } else {
                                    name = extracted + " " + name;
                                    output += name + "\n";
                                    name = null;
                                }
                            } else if (trimmed.startsWith("DAB")) { // First Name
                                extracted = trimmed.split("DAB")[1];
                                if (name == null) {
                                    name = extracted;
                                } else {
                                    name = extracted + " " + name;
                                    output += name + "\n";
                                    name = null;
                                }
                            } else if (trimmed.startsWith("DAG")) { //address
                                extracted = trimmed.split("DAG")[1];
                                output += extracted + "\n";
                            } else if (trimmed.startsWith("DAI")) { // city
                                extracted = trimmed.split("DAI")[1];
                                output += extracted + "\n";
                            } else if (trimmed.startsWith("DAJ")) { // state
                                extracted = trimmed.split("DAJ")[1];
                                output += extracted + "\n";
                            } else if (trimmed.startsWith("DAK")) { // postalCode
                                extracted = trimmed.split("DAK")[1];
                                output += extracted + "\n";
                            }
                        }
                        break;
                    case Barcode.DRIVER_LICENSE:
                        Barcode.DriverLicense lic = current.driverLicense;
                        output += lic.firstName;
                        if (lic.middleName != null) {
                            output += " " + lic.middleName;
                        }
                        if (lic.lastName != null) {
                            output += " " + lic.lastName;
                        }
                        output += "\n";
                        output += lic.addressStreet + "\n";
                        output = lic.addressCity;
                        if (lic.addressState != null) {
                            output += ", " + lic.addressState;
                        }
                        if (lic.addressZip != null) {
                            output += " " + lic.addressZip.split("-")[0];
                        }
                        output += "\n\n";
                        break;
                }
            }
        }
        return output;
    }
}
