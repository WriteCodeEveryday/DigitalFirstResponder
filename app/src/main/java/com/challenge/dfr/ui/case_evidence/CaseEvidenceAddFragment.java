package com.challenge.dfr.ui.case_evidence;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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

import com.challenge.dfr.R;
import com.challenge.dfr.database.ForensicCase;
import com.challenge.dfr.database.ForensicEvidence;
import com.challenge.dfr.hardware.ScannerManager;
import com.challenge.dfr.logical.CaseManager;
import com.challenge.dfr.logical.ChecksumManager;
import com.challenge.dfr.logical.ForensicsDatabaseManager;
import com.challenge.dfr.logical.ImageMetadataProcessor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class CaseEvidenceAddFragment extends Fragment {
    public static final int REQUEST_TAKE_PICTURE = 1337;
    public static final int REQUEST_OPEN_PICTURE = 1338;
    public static final int REQUEST_OPEN_TEXT = 1339;
    private static int evidence_original;

    HashMap<Integer, boolean[]> options = new HashMap<Integer, boolean[]>();
    View parent;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_case_evidence_options, container, false);

        options.put(R.string.nav_initial_screen, new boolean[] {true, true, false, false});
        options.put(R.string.nav_tool, new boolean[] {true, false, true, false});
        options.put(R.string.nav_add_hardware, new boolean[] {true, false, false, false});
        options.put(R.string.nav_add_manual_extraction, new boolean[] {true, false, false, false});
        options.put(R.string.nav_scan_id, new boolean[] {true, false, false, true});
        options.put(R.string.nav_scan_text, new boolean[] {false, false, false, true});

        parent = root;

        setupOptions();
        setupButtons();

        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PICTURE) {
            if (resultCode == Activity.RESULT_OK) {
                System.out.println("Importing: " + CaseManager.getEvidencePath());
                Bitmap in = BitmapFactory.decodeFile(CaseManager.getEvidencePath());
                System.out.println("Adding evidence");
                CaseManager.addEvidence(in, 1);
                System.out.println("Closing evidence");
                CaseManager.completeNewEvidence();
                System.out.println("Added image from camera.");
            }
        } else if (requestCode == REQUEST_OPEN_PICTURE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri photoUri = data.getData();
                System.out.println("Importing: " + photoUri);
                try {
                    Bitmap picture = MediaStore.
                            Images.Media.getBitmap(getContext().getContentResolver(), photoUri);
                    System.out.println("Adding evidence");
                    CaseManager.addEvidence(picture, 1);
                    System.out.println("Closing evidence");
                    CaseManager.completeNewEvidence();
                    System.out.println("Added image from image import.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (requestCode == REQUEST_OPEN_TEXT) {
            if (resultCode == Activity.RESULT_OK) {
                Uri textUri = data.getData();
                System.out.println("Importing: " + textUri);
                FileInputStream file = null;
                try {
                    file = new FileInputStream(getContext().getContentResolver()
                            .openFileDescriptor(textUri, "r").getFileDescriptor());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                StringBuffer fileContent = new StringBuffer("");
                byte[] buffer = new byte[1024];

                int n = -1;
                while (true)
                {
                    try {
                        if (!((n = file.read(buffer)) != -1)) break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    fileContent.append(new String(buffer, 0, n));
                }

                String parsed = fileContent.toString();
                System.out.println("Adding evidence: " + parsed);
                CaseManager.addEvidence(parsed);
                System.out.println("Closing evidence");
                CaseManager.completeNewEvidence();
                System.out.println("Added text from file import.");
            }
        }
    }

    private void hideOptions() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View root = parent;

                root.findViewById(R.id.scan_camera_layout).setVisibility(View.GONE);
                root.findViewById(R.id.import_picture_layout).setVisibility(View.GONE);
                root.findViewById(R.id.import_text_file_layout).setVisibility(View.GONE);
                root.findViewById(R.id.scanner_layout).setVisibility(View.GONE);
            }
        });
    }

    private void setupOptions() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View root = parent;

                final ForensicCase current = CaseManager.getCase();
                if (current != null) {
                    final int evidence_type = CaseManager.getEvidenceType();
                    evidence_original = evidence_type;
                    TextView evidence_option = root.findViewById(R.id.option_name);
                    evidence_option.setText(getResources().getString(evidence_type));

                    boolean[] valid_options = options.get(evidence_type);

                    root.findViewById(R.id.scan_camera_layout).setVisibility(View.VISIBLE);
                    root.findViewById(R.id.import_picture_layout).setVisibility(View.VISIBLE);
                    root.findViewById(R.id.import_text_file_layout).setVisibility(View.VISIBLE);
                    root.findViewById(R.id.scanner_layout).setVisibility(View.VISIBLE);

                    /* Hide / UnHide options */
                    if (!valid_options[0]) {
                        root.findViewById(R.id.scan_camera_layout).setVisibility(View.GONE);
                    }

                    if (!valid_options[1]) {
                        root.findViewById(R.id.import_picture_layout).setVisibility(View.GONE);
                    }

                    if (!valid_options[2]) {
                        root.findViewById(R.id.import_text_file_layout).setVisibility(View.GONE);
                    }

                    if (!valid_options[3]) {
                        root.findViewById(R.id.scanner_layout).setVisibility(View.GONE);
                    }

                    if (evidence_type ==  R.string.nav_scan_text) {
                        CaseManager.setScannerInputType("document");
                    } else {
                        CaseManager.setScannerInputType("image");
                    }
                }
            }
        });
    }

    private void setupButtons() {
        View root = parent;
        final int evidence_type = CaseManager.getEvidenceType();


        //Setup the done button for completing the evidence review.
        root.findViewById(R.id.done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        CaseManager.completeNewEvidence();
                        showEvidencePreviewUI();
                    }
                }).start();
            }
        });


        //Setup the picture taking.
        root.findViewById(R.id.scan_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        hideOptions();
                        CaseManager.acceptNewEvidence();

                        System.out.println("Starting camera.");
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                            Uri photoURI = FileProvider.getUriForFile(getContext(),
                                    "com.challenge.dr.fileprovider",
                                    createImageFile());
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            startActivityForResult(takePictureIntent, REQUEST_TAKE_PICTURE);
                        }

                        System.out.println("Waiting for result.");
                        while (CaseManager.isEvidencePending()) {
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            System.out.println("Still waiting for result.");
                        }

                        System.out.println("Showing preview.");
                        showEvidencePreviewUI();
                    }
                }).start();
            }
        });

        //Setup the import image.
        root.findViewById(R.id.import_picture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        hideOptions();
                        CaseManager.acceptNewEvidence();

                        System.out.println("Importing file.");
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        startActivityForResult(intent, REQUEST_OPEN_PICTURE);

                        System.out.println("Waiting for result.");
                        while (CaseManager.isEvidencePending()) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            System.out.println("Still waiting for result.");
                        }

                        System.out.println("Showing preview.");
                        showEvidencePreviewUI();
                    }
                }).start();
            }
        });

        //Setup the import text
        root.findViewById(R.id.import_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        hideOptions();
                        CaseManager.acceptNewEvidence();

                        System.out.println("Importing text file.");
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("text/*");
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        startActivityForResult(intent, REQUEST_OPEN_TEXT);

                        System.out.println("Waiting for result.");
                        while (CaseManager.isEvidencePending()) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            System.out.println("Still waiting for result.");
                        }

                        showEvidencePreviewUI();
                    }
                }).start();
            }
        });

        //Setup the scanner.
        root.findViewById(R.id.scanner).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        hideOptions();
                        System.out.println("Start scanner");

                        System.out.println("Connecting...");
                        Context ctx = getActivity().getApplicationContext();
                        ScannerManager.connect(ScannerManager.CONNECTION.WIFI, ctx);
                        while (!ScannerManager.isConnected()) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            System.out.println("Still Connecting...");
                        }
                        System.out.println("Connected");

                        if (evidence_type == R.string.nav_scan_id) {
                            System.out.println("Start ID scan");
                        } else if  (evidence_type == R.string.nav_scan_text){
                            System.out.println("Start document scan");
                        }

                        CaseManager.acceptNewEvidence();
                        ScannerManager.executeScan(
                                ScannerManager.createIConnector(ctx),
                                getActivity());

                        while (!CaseManager.isEvidencePending()) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            System.out.println("Waiting on scan to complete.");
                        }
                        System.out.println("Scan completed.");

                        showEvidencePreviewUI();
                    }
                }).start();
            }
        });
    }

    private void showEvidencePreviewUI() {
        final View root = parent;

        PdfDocument document = null;
        Bitmap evidence = null;
        final String text = CaseManager.getTextEvidence();
        if (CaseManager.getScannerInputType().equals("document")) {
            document = CaseManager.getDocument();
        } else {
            evidence = CaseManager.getSingleEvidenceBitmap();
        }

        if (document == null && evidence == null && text == null) {
            setupOptions();
        } else {
            final PdfDocument finalDocument = document;
            final Bitmap finalEvidence = evidence;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LinearLayout preview = root.findViewById(R.id.preview_layout);
                    preview.removeAllViews();

                    DisplayMetrics met = getContext()
                            .getResources()
                            .getDisplayMetrics();
                    int height =  (int) (met.heightPixels * 0.25);

                    TextView previewText = new TextView(getContext());
                    previewText.setText(R.string.preview_text);
                    preview.addView(previewText);

                    if (finalDocument != null) {
                        TextView doc_preview = new TextView(getContext());
                        doc_preview.setText(R.string.document_text);
                        doc_preview.setGravity(Gravity.CENTER);
                        doc_preview.setTextSize(20);
                        doc_preview.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    File output = createDocumentFile();
                                    FileOutputStream out = new FileOutputStream(output);
                                    finalDocument.writeTo(out);
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
                    } else if (finalEvidence != null) {
                        Bitmap image = finalEvidence;
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
                    } else if (text != null) {
                        EditText textView = new EditText(getContext());
                        textView.setText(text);
                        textView.setHeight(height);
                        textView.setFocusable(false);
                        textView.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                return true;
                            }
                        }); // Makes the preview not modifiable.
                        preview.addView(textView);
                    }

                    TextView notesText = new TextView(getContext());
                    notesText.setText(R.string.notes_text);
                    preview.addView(notesText);

                    String notesResult = "";
                    if (finalDocument != null || finalEvidence != null) {
                        Bitmap single = CaseManager.getSingleEvidenceBitmap();
                        String ocr = ImageMetadataProcessor.processOCR(single, getContext());
                        String lic = ImageMetadataProcessor.processLicense(single, getContext());
                        if (!ocr.equals("")) {
                            notesResult +=  "\n\nOCR: \n" + ocr;
                        }

                        if (!lic.equals("")) {
                            notesResult +=  "\n\nLIC: \n" + lic;
                        }
                    }

                    final EditText notesView = new EditText(getContext());
                    notesView.setHint(R.string.notes_text);
                    notesView.setMaxLines(3);
                    notesView.setText(notesResult);
                    preview.addView(notesView);

                    ((Button) root.findViewById(R.id.done)).setText(R.string.nav_save);
                    root.findViewById(R.id.done).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Date creation = new Date();
                                    ForensicCase current = CaseManager.getCase();
                                    ForensicEvidence created = new ForensicEvidence();
                                    created.created = creation.getTime();
                                    created.cid = CaseManager.getCase().cid;
                                    created.notes = notesView.getText().toString();
                                    created.evidenceType = getResources()
                                            .getString(evidence_original);

                                    if (finalDocument != null) {
                                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                        try {
                                            finalDocument.writeTo(stream);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        byte[] dataBytes = stream.toByteArray();

                                        created.payloadType = "document";
                                        created.payload = Base64.encodeToString(
                                                dataBytes, Base64.DEFAULT);
                                    } else if (finalEvidence != null) {
                                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                        finalEvidence.compress(Bitmap.CompressFormat.PNG,
                                                100, stream);
                                        byte[] dataBytes = stream.toByteArray();

                                        created.payloadType = "image";
                                        created.payload = Base64.encodeToString(
                                                dataBytes, Base64.DEFAULT);
                                    } else if (text != null) {
                                        created.payloadType = "text";
                                        created.payload = text; //No Base64 encoding.
                                    }

                                    ChecksumManager.submitWithEvidenceHash(created);

                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            //Go to view evidence view.
                                            NavController navController = Navigation
                                                    .findNavController(getActivity(), R.id.nav_host_fragment);
                                            navController.navigate(
                                                    R.id.action_navigation_case_evidence_options_to_navigation_case_evidence);
                                        }
                                    });
                                }
                            }).start();
                        }
                    });
                }
            });
        }
    }

    private File createImageFile() {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "PNG_" + timeStamp + "_";
        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".png",         /* suffix */
                    storageDir      /* directory */
            );
        }
        catch (IOException e) {

        }

        // Save a file: path for use with ACTION_VIEW intents
        CaseManager.setEvidencePath(image.getAbsolutePath());
        return image;
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
