package com.example.facialrecognition_realtime;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceContour;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.util.List;
import java.util.Timer;

import Helper.GraphicOverlay;
import Helper.RectOverlay;
import dmax.dialog.SpotsDialog;

import static com.example.facialrecognition_realtime.R.id.camera_view;
import static com.example.facialrecognition_realtime.R.id.none;

public class MainActivity extends AppCompatActivity {

    Button face_detect_button;
    GraphicOverlay graphicOverlay;
    CameraView cameraView;
    AlertDialog alertDialog;
    //int counter=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        face_detect_button = findViewById(R.id.face_detect_btn);
        graphicOverlay = findViewById(R.id.graphic_overlay);
        cameraView = findViewById(camera_view);

        alertDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("wait.....")
                .setCancelable(false)
                .build();

        face_detect_button.setOnClickListener(view -> {
            cameraView.start();
            cameraView.captureImage();
            graphicOverlay.clear();
        });
        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                alertDialog.show();
                Bitmap bitmap = cameraKitImage.getBitmap();
                bitmap = Bitmap.createScaledBitmap(bitmap, cameraView.getWidth(), cameraView.getHeight(), false);
                cameraView.stop();
                processFace(bitmap);

            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });
    }

    private void processFace(Bitmap bitmap) {
        int rotationDegree=0;
        InputImage image = InputImage.fromBitmap(bitmap, rotationDegree);
        /*FaceDetectorOptions highAccuracyOpts =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                        .build();*/
        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                        .setMinFaceSize(0.15f)
                        .enableTracking()
                        .build();
        FaceDetectorOptions realtime =
                new FaceDetectorOptions.Builder()
                        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                        .build();
        FaceDetector detector = FaceDetection.getClient(options);

        Task<List<Face>>res=detector.process(image)
                        .addOnSuccessListener(
                                faces -> {
                                    // Task completed successfully
                                    int counter =0;
                                    for (Face face : faces) {
                                        Rect rect = face.getBoundingBox();
                                        RectOverlay rectOverlay = new RectOverlay(graphicOverlay, rect);
                                        graphicOverlay.add(rectOverlay);
                                        counter = counter + 1;
                                        /*
                                        float rotY = face.getHeadEulerAngleY();  // Head is rotated to the right rotY degrees
                                        float rotZ = face.getHeadEulerAngleZ();  // Head is tilted sideways rotZ degrees

                                        // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
                                        // nose available):
                                        FaceLandmark leftEar = face.getLandmark(FaceLandmark.LEFT_EAR);
                                        if (leftEar != null) {
                                            PointF leftEarPos = leftEar.getPosition();
                                        }

                                        // If contour detection was enabled:
                                        List<PointF> leftEyeContour =
                                                face.getContour(FaceContour.LEFT_EYE).getPoints();
                                        List<PointF> upperLipBottomContour =
                                                face.getContour(FaceContour.UPPER_LIP_BOTTOM).getPoints();

                                        // If classification was enabled:
                                        if (face.getSmilingProbability() != null) {
                                            float smileProb = face.getSmilingProbability();
                                        }
                                        if (face.getRightEyeOpenProbability() != null) {
                                            float rightEyeOpenProb = face.getRightEyeOpenProbability();
                                        }

                                        // If face tracking was enabled:
                                        if (face.getTrackingId() != null) {
                                            int id = face.getTrackingId();
                                        }*/
                                    }
                                    alertDialog.dismiss();
                                   // res(faces);

                                })
                        .addOnFailureListener(
                                e -> {
                                    // Task failed with an exception
                                    Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });

    }
/*
    private void faceResults(List<Face> faces) {

    }
*/
    @Override
    protected void onPause() {
        super.onPause();
        cameraView.stop();
    }
    boolean exit=false;
    Toast toast;
    @Override
    public void onBackPressed() {
        cameraView.start();
        if (exit){
            toast.cancel();
            super.onBackPressed();}
        else{
            exit=true;
            toast= Toast.makeText(MainActivity.this, "Press again to exit", Toast.LENGTH_SHORT);
            toast.show();
            Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    exit=false;
                                }
                            },
                1500);}

    }
    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }
}