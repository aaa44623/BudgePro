package com.example.ahmed.testocr;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.camView) CameraView mCameraView;
    @BindView(R.id.cameraBtn) Button mCameraButton;
    @BindView(R.id.graphic_overlay) GraphicOverlay mGraphicOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //Camera View
        mCameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {

                Bitmap bitmap = cameraKitImage.getBitmap();
                bitmap=Bitmap.createScaledBitmap(bitmap,mCameraView.getWidth(),mCameraView.getHeight(), false);
                mCameraView.stop();

                recognizeText(bitmap);


            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });

        mCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGraphicOverlay.clear();
                mCameraView.start();
                mCameraView.captureImage();

            }
        });
    }

    private void recognizeText(Bitmap bitmap){

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

        FirebaseVisionTextRecognizer textRecognizer = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();

        textRecognizer.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText firebaseVisionText) {

                        drawTextResult(firebaseVisionText);

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                 e.getMessage();
            }
        });
    }

    private void drawTextResult(FirebaseVisionText firebaseVisionText){

        List<FirebaseVisionText.TextBlock> blocks = firebaseVisionText.getTextBlocks();

        if(blocks.size() == 0)
        {
            Toast.makeText(this, "NO Text found", Toast.LENGTH_SHORT).show();
            return;
        }

        mGraphicOverlay.clear();

        for(int i = 0; i < blocks.size(); i++)
        //Get Line
        {
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();

            for(int j=0; j < lines.size(); j++)
            {
                //GET Elements
                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();

                for(int k=0; k <elements.size(); k++)
                {
                    //Draw Elements
                    TextGraphic textGraphic = new TextGraphic(mGraphicOverlay,elements.get(k));
                    mGraphicOverlay.add(textGraphic);

                }
            }
        }

    }

    @Override
    protected void onResume(){
        super.onResume();
        mCameraView.start();
    }

    protected void onPause() {
        super.onPause();
        mCameraView.stop();
    }



}
