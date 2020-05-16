package com.challenge.dfr.ui.case_evidence;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.brother.ptouch.sdk.Printer;
import com.brother.ptouch.sdk.PrinterInfo;
import com.brother.ptouch.sdk.PrinterStatus;
import com.challenge.dfr.R;
import com.challenge.dfr.database.ForensicCase;
import com.challenge.dfr.database.ForensicEvidence;
import com.challenge.dfr.hardware.PrinterManager;
import com.challenge.dfr.hardware.ScannerManager;
import com.challenge.dfr.logical.CaseManager;
import com.challenge.dfr.logical.ChecksumManager;
import com.challenge.dfr.logical.ForensicsDatabaseManager;
import com.challenge.dfr.logical.ImageMetadataProcessor;
import com.challenge.dfr.logical.PrintableGenerator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class CaseEvidencePreviewFragment extends Fragment {
    View parent;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_case_evidence_preview, container, false);
        parent = root;

        ForensicCase current = CaseManager.getCase();
        ForensicEvidence evidence = CaseManager.getEvidence();

        setupOptions(current, evidence);

        return root;
    }

    private void setupOptions(final ForensicCase current, final ForensicEvidence evidence) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView option = parent.findViewById(R.id.option_name);
                option.setText(evidence.evidenceType);

                Button settings = parent.findViewById(R.id.printer_settings);
                Button print_evidence_tag = parent.findViewById(R.id.print_evidence_tag);

                boolean caseExistsOnDevice = evidence.checksum != null && !evidence.checksum.equals("");
                TextView checksum = parent.findViewById(R.id.checksum);
                if (caseExistsOnDevice) {
                    DisplayMetrics met = getContext()
                            .getResources()
                            .getDisplayMetrics();
                    int height =  (int) (met.heightPixels * 0.25);

                    LinearLayout preview = parent.findViewById(R.id.preview_layout);
                    preview.removeAllViews();

                    switch(evidence.payloadType) {
                        case "document":
                            TextView doc_preview = new TextView(getContext());
                            doc_preview.setText(R.string.document_text);
                            doc_preview.setGravity(Gravity.CENTER);
                            doc_preview.setTextSize(20);
                            doc_preview.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    try {
                                        byte[] pdfData = Base64.decode(evidence.payload,
                                                Base64.DEFAULT);
                                        File output = createDocumentFile();
                                        FileOutputStream out = new FileOutputStream(output);
                                        out.write(pdfData);
                                        out.close();
                                        System.out.println("Wrote file out.");

                                        Uri outputFileUri = FileProvider.getUriForFile(getActivity(),
                                                "com.challenge.dr.fileprovider", output);
                                        System.out.println("Previewing: " + outputFileUri);

                                        Intent intent = new Intent(Intent.ACTION_VIEW);
                                        intent.setDataAndType(outputFileUri, "application/pdf");
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY
                                                | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                        startActivity(intent);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            preview.addView(doc_preview);
                            break;
                        case "image":
                            byte[] imageData = Base64.decode(evidence.payload,
                                    Base64.DEFAULT);
                            Bitmap image = BitmapFactory
                                    .decodeByteArray(imageData, 0, imageData.length);
                            float ratio = (float) image.getWidth() / (float) image.getHeight();
                            int width = (int) (height * ratio);

                            image  = Bitmap.createScaledBitmap(image, width, height, true);

                            int size = 3;
                            Bitmap borderImage = Bitmap.createBitmap(
                                    image.getWidth() + size * 2,
                                    image.getHeight() + size * 2,
                                    image.getConfig());
                            Canvas canvas = new Canvas(borderImage);
                            canvas.drawColor(Color.BLACK);
                            canvas.drawBitmap(image, size, size, null);
                            image = borderImage;

                            ImageView imageView = new ImageView(getContext());
                            imageView.setImageBitmap(image);
                            preview.addView(imageView);
                            break;
                        case "text":
                            EditText textView = new EditText(getContext());
                            textView.setText(evidence.payload);
                            textView.setHeight(height);
                            textView.setFocusable(false);
                            textView.setOnLongClickListener(new View.OnLongClickListener() {
                                @Override
                                public boolean onLongClick(View v) {
                                    return true;
                                }
                            }); // Makes the preview not modifiable.
                            preview.addView(textView);
                            break;
                    }

                    settings.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            NavController navController = Navigation
                                    .findNavController(getActivity(), R.id.nav_host_fragment);
                            navController.navigate(
                                    R.id.action_navigation_case_evidence_preview_to_navigation_printer_settings);
                        }
                    });

                    print_evidence_tag.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            PrintableGenerator pr = new PrintableGenerator();
                            Bitmap image = pr.buildOutput(current, evidence, getContext());

                            DisplayMetrics met = getActivity().getApplicationContext()
                                    .getResources()
                                    .getDisplayMetrics();
                            float ratio = (float) image.getWidth() / (float) image.getHeight();
                            int height =  (int) (met.heightPixels * 0.2);
                            int width = (int) (height * ratio);
                            image  = Bitmap.createScaledBitmap(image, width, height, true);
                            final Bitmap printable = Bitmap.createBitmap(image);

                            int size = 3;
                            Bitmap borderImage = Bitmap.createBitmap(
                                    image.getWidth() + size * 2,
                                    image.getHeight() + size * 2,
                                    image.getConfig());
                            Canvas canvas = new Canvas(borderImage);
                            canvas.drawColor(Color.BLACK);
                            canvas.drawBitmap(image, size, size, null);

                            if (borderImage.getHeight() > borderImage.getWidth()) {
                                //Rotate the image 90 degrees and resize it.
                                Matrix counter = new Matrix();
                                counter.postRotate(90);
                                borderImage = Bitmap.createBitmap(borderImage,
                                        0, 0, borderImage.getWidth(),
                                        borderImage.getHeight(), counter, true);
                                height =  (int) (met.heightPixels * 0.2);
                                width = (int) (height * ratio);
                                borderImage  = Bitmap.createScaledBitmap(
                                        borderImage, height, width, true);
                            }
                            Bitmap imageWithBorder = borderImage;

                            LinearLayout temp = parent.findViewById(R.id.preview_layout);
                            temp.removeAllViews();
                            temp.setGravity(Gravity.CENTER);

                            ImageView preview = new ImageView(getContext());
                            preview.setImageBitmap(imageWithBorder);
                            preview.setOnClickListener(new View.OnClickListener(){
                                @Override
                                public void onClick(View v) {
                                    Printer temp = PrinterManager.getPrinter();
                                    temp.startCommunication();
                                    Bitmap recycled = Bitmap.createBitmap(printable);
                                    PrinterStatus result = temp.printImage(recycled);
                                    if (result.errorCode != PrinterInfo.ErrorCode.ERROR_NONE) {
                                        System.out.println("Error: " + result.errorCode);
                                    }
                                    temp.endCommunication();
                                }
                            });
                            temp.addView(preview);

                            TextView tap_to_print = new TextView(getContext());
                            tap_to_print.setText(R.string.tap_preview_to_print_text);
                            tap_to_print.setGravity(Gravity.CENTER);
                            tap_to_print.setTextSize(20);
                            temp.addView(tap_to_print);
                        }
                    });

                    String validatedChecksum = ChecksumManager.getUpdatedForensicEvidenceChecksum(evidence);
                    String checksumText = getResources().getText(R.string.checksum_text) + ": ";
                    if (validatedChecksum.equals(evidence.checksum)) {
                        checksumText += getResources().getText(R.string.checksum_success_text) + " ";
                    } else {
                        checksumText += getResources().getText(R.string.checksum_failure_text) + " ";
                    }
                    checksumText += validatedChecksum;
                    checksum.setText(checksumText);
                } else {
                    // Scanned tags won't have checksums
                    // unless they're part of the current case set.
                    // We'll hide those.
                    checksum.setVisibility(View.GONE);
                    settings.setVisibility(View.GONE);
                    print_evidence_tag.setVisibility(View.GONE);
                }


                LinearLayout notes = parent.findViewById(R.id.notes_layout);
                notes.removeAllViews();

                //Add notes for case only if the case isn't on the device.
                if (!caseExistsOnDevice) {
                    TextView caseNotes = new TextView(getContext());
                    caseNotes.setText(R.string.case_notes_text);
                    notes.addView(caseNotes);

                    EditText caseView = new EditText(getContext());
                    caseView.setText(current.notes);
                    caseView.setMaxLines(5);
                    caseView.setFocusable(false);
                    caseView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            return true;
                        }
                    }); // Makes the preview not modifiable.
                    notes.addView(caseView);
                }

                //Add notes for evidence for all.
                TextView evidenceNotes = new TextView(getContext());
                evidenceNotes.setText(R.string.evidence_notes_text);
                notes.addView(evidenceNotes);

                EditText evidenceView = new EditText(getContext());
                evidenceView.setText(evidence.notes);
                evidenceView.setMaxLines(5);
                evidenceView.setFocusable(false);
                evidenceView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        return true;
                    }
                }); // Makes the preview not modifiable.
                notes.addView(evidenceView);
            }
        });
    }

    private File createDocumentFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "PDF_" + timeStamp + "_";
        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File doc = null;
        try {
            doc = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".pdf",         /* suffix */
                    storageDir      /* directory */
            );
        }
        catch (IOException e) {

        }

        // Save a file: path for use with ACTION_VIEW intents
        CaseManager.setEvidencePath(doc.getAbsolutePath());
        return doc;
    }
}
