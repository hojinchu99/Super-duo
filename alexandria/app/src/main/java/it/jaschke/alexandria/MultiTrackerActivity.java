package it.jaschke.alexandria;

import android.Manifest;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiDetector;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import it.jaschke.alexandria.camera.CameraSourcePreview;
import it.jaschke.alexandria.camera.GraphicOverlay;

import android.telecom.ConnectionRequest;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by hojin on 15. 10. 28.
 */
public final class MultiTrackerActivity extends AppCompatActivity {
    private static final String TAG="MultiTracker";

    private static final int RC_HANDLE_GMS=9001;
    private static final int RC_HANDLE_CAMERA_PERM=2;

    private CameraSource mCameraSource=null;
    private GraphicOverlay mGraphicOverlay;
    private CameraSourcePreview mCameraSourcePreview;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mCameraSourcePreview=(CameraSourcePreview)findViewById(R.id.preview);
        mGraphicOverlay=(GraphicOverlay)findViewById(R.id.faceOverlay);

        int rc=ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);
        if(rc== PackageManager.PERMISSION_GRANTED){
            createCameraSource();
        }else{
            requestCameraPermission();
        }
    }

    private void requestCameraPermission(){
        Log.w(TAG, "Camera permission in not granted, Requesting permission");

        final String[] permissions= new String[]{
                                        Manifest.permission.CAMERA
                                    };
        if(!ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.CAMERA)){
            ActivityCompat.requestPermissions(this,permissions,RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity=this;

        View.OnClickListener listener =new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(thisActivity,permissions,RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                            Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok,listener)
                                                        .show();
    }

    private void createCameraSource(){
        final Context context=getApplicationContext();

        BarcodeDetector barcodeDetector=
            new BarcodeDetector.Builder(context)
                    .setBarcodeFormats(Barcode.EAN_13)
                    .build();

         BarcodeTrackerFactory barcodeTrackerFactory= new BarcodeTrackerFactory(mGraphicOverlay, new GraphicTracker.Callback() {
             @Override
             public void onFound(String barcodeValue) {
                 Log.d(TAG, "Barcode in Multitracker =" +barcodeValue);

                 Intent intent=new Intent(context,MainActivity.class);
                 intent.putExtra("barcodeValue",barcodeValue);
                 startActivity(intent);

             }
         });

        barcodeDetector.setProcessor(new MultiProcessor.Builder<>(barcodeTrackerFactory).build());

        MultiDetector multiDetector=new MultiDetector.Builder().add(barcodeDetector).build();

        if(!multiDetector.isOperational()){
            Log.w(TAG, "Detector dependencies are not yet available.");

            IntentFilter lowstorageIntentFilter=new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage=registerReceiver(null,lowstorageIntentFilter) !=null;

            if(hasLowStorage){
                Toast.makeText(this,R.string.low_storage_error,Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.low_storage_error));
            }
        }

        mCameraSource=new CameraSource.Builder(getApplicationContext(), multiDetector).setFacing(CameraSource.CAMERA_FACING_BACK).setRequestedPreviewSize(1600,1024).setRequestedFps(15.0f).build();
    }

    @Override
    protected void onResume() {
        super.onResume();

        startCameraSource();
    }


    private void startCameraSource(){
        int code=GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext());

        if(code != ConnectionResult.SUCCESS){
            Dialog dialog=GoogleApiAvailability.getInstance().getErrorDialog(this,code,RC_HANDLE_GMS);
            dialog.show();
        }

        if(mCameraSource != null){
            try {
                mCameraSourcePreview.start(mCameraSource,mGraphicOverlay);
            }catch (IOException e){
                Log.e(TAG, "Unable tot start camera source.",e);
                mCameraSource.release();
                mCameraSource=null;
            }
        }
    }




    @Override
    protected void onPause() {
        super.onPause();
        mCameraSourcePreview.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mCameraSource !=null){
            mCameraSource.release();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode != RC_HANDLE_CAMERA_PERM){
            Log.d(TAG, "Gto unexpected permission result: " +requestCode);
            super.onRequestPermissionsResult(requestCode,permissions,grantResults);
            return;
        }

        if(grantResults.length !=0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            createCameraSource();
            return;
        }

        Log.e(TAG, "Permission not granted: results le = " +grantResults.length +
                    " Result code = " +(grantResults.length>0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener=new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        };

        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setTitle("Multitracker sample").setMessage(R.string.no_camera_permission).setPositiveButton(R.string.ok,listener).show();

    }
}





















