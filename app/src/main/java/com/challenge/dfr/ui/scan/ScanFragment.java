package com.challenge.dfr.ui.scan;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.challenge.dfr.R;
import com.challenge.dfr.database.ForensicCase;
import com.challenge.dfr.database.ForensicEvidence;
import com.challenge.dfr.logical.CameraPreview;
import com.challenge.dfr.logical.CaseManager;
import com.challenge.dfr.logical.ForensicsDatabaseManager;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.ByteArrayOutputStream;

public class ScanFragment extends Fragment {
    private boolean shouldScan = true;
    private boolean shouldNavigate = true;
    private String SEPARATOR = ";";
    private View parent;

    private Camera mCamera;
    private CameraPreview mPreview;
    private BarcodeDetector detector;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_scan_evidence, container, false);
        parent = root;
        startScan();
        return root;
    }

    public void startScan() {
        detector = new BarcodeDetector.Builder(getContext())
                .setBarcodeFormats(Barcode.PDF417)
                .build();

        mCamera = getCameraInstance();
        mCamera.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                if (!shouldScan) {
                    if (shouldNavigate) {
                        shouldNavigate = false;
                        NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
                        navController.navigate(R.id.action_navigation_scan_to_navigation_case_evidence_preview);
                    }
                    return; } //Send us out the other fragment.

                Camera.Parameters parameters = camera.getParameters();
                int width = parameters.getPreviewSize().width;
                int height = parameters.getPreviewSize().height;

                YuvImage yuv = new YuvImage(data, parameters.getPreviewFormat(), width, height, null);

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                yuv.compressToJpeg(new Rect(0, 0, width, height), 50, out);

                byte[] bytes = out.toByteArray();
                final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        onImage(bitmap);
                    }
                }).start();
            }
        });

        LinearLayout preview = (LinearLayout) parent.findViewById(R.id.scan_layout);
        mPreview = new CameraPreview(getContext(), mCamera);
        preview.addView(mPreview);
    }

    private void onImage(Bitmap bitmap) {
        if (!shouldScan) { return; }

        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        final SparseArray<Barcode> barcodes = detector.detect(frame);
        if (barcodes.size() > 0) {
            for (int i = 0; i < barcodes.size(); i++) {
                Barcode current = barcodes.valueAt(i);
                switch (current.valueFormat) {
                    case Barcode.PDF417:
                    case Barcode.TEXT:
                        String[] payload = current.rawValue.split(SEPARATOR);
                        if (payload[0].equals("DFR")) {
                            //Found a DFR tag.
                            shouldScan = false;
                        } else {
                            return;
                        }

                        //Case details
                        String caseNumber = payload[1];
                        int cid = Integer.parseInt(payload[3]);
                        String caseNotes = payload[5];

                        //Evidence details
                        int eid = Integer.parseInt(payload[4]);
                        String evidenceType = payload[2];
                        String evidenceNotes = payload[6];

                        //Choose the case.
                        ForensicCase dbCase = null;
                        dbCase = ForensicsDatabaseManager
                                .instance().getForensicCaseDao().fetchByCaseNumber(caseNumber);

                        if (dbCase == null) {
                            dbCase = ForensicsDatabaseManager
                                    .instance().getForensicCaseDao().fetchById(cid);
                        }

                        //Attempt to select it from the DB
                        ForensicEvidence dbEvidence = null;
                        if (dbCase != null) {
                            dbEvidence = ForensicsDatabaseManager
                                    .instance().getForensicEvidenceDao().fetchByEvidenceId(cid, eid);
                        }

                        //Recreate the case and evidence from scratch.
                        if (dbEvidence == null) {
                            dbEvidence = new ForensicEvidence();

                            dbEvidence.cid = cid;
                            dbEvidence.eid = eid;
                            dbEvidence.evidenceType = evidenceType;
                            dbEvidence.notes = evidenceNotes;

                            dbCase = new ForensicCase();
                            dbCase.cid = cid;
                            dbCase.caseNumber = caseNumber;
                            dbCase.notes = caseNotes;
                        }

                        //Set the case and evidence item.
                        CaseManager.setCase(dbCase);
                        CaseManager.setEvidence(dbEvidence);
                        break;
                }
            }
        }
    }

    /** A safe way to get an instance of the Camera object. */
    private Camera getCameraInstance(){
        Camera c = null;
        if (!checkCameraHardware(getContext())) {
            System.out.println("No Camera");
            return null;
        }

        try {
            c = Camera.open(); // attempt to get a Camera instance
            Camera.Parameters params = c.getParameters();
            if (params.getSupportedFocusModes().contains(
                    Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            } // Set Auto Focus.
            /*if (params.getSupportedFlashModes().contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            } // Set Flash On. */
            c.setParameters(params);

            // Set Contrast to Maximum.
            String parmListStr = params.flatten();
            String[] parms = parmListStr.split(";");
            int maxContrast = 0, curContrast = 0, newContrast = 0;
            for(String str:parms){
                if(str.contains("max-contrast=")){
                    String[] values = str.split("=");
                    maxContrast = Integer.getInteger(values[1]);
                } else if (str.contains("contrast=")){
                    String[] values = str.split("=");
                    curContrast = Integer.getInteger(values[1]);
                }
            }

            if (maxContrast > 0 && curContrast >= 0){
                //calculate contrast as per your needs and set it to camera parameters as below
                newContrast = (curContrast + 1) < maxContrast? (curContrast + 1): maxContrast;
                params.set("contrast", newContrast);
                c.setParameters(params);
            }
        }
        catch (Exception e){
            System.out.println("Camera Error: " + e);
        }
        return c; // returns null if camera is unavailable
    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }
}
