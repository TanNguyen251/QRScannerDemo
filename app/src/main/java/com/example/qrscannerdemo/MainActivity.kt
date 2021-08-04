package com.example.qrscannerdemo

import android.Manifest.permission.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceHolder.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.util.isNotEmpty
import com.example.qrscannerdemo.databinding.ActivityMainBinding
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector

class MainActivity : AppCompatActivity() {
    lateinit var barcodeDetector: BarcodeDetector
    lateinit var cameraSource: CameraSource
    lateinit var binding: ActivityMainBinding

    lateinit var surfaceCallBack: Callback
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }

    private fun init(){
        barcodeDetector = BarcodeDetector.Builder(applicationContext).setBarcodeFormats(Barcode.QR_CODE).build()
        val processor = object: Detector.Processor<Barcode>{
            override fun release() {
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                if (detections.detectedItems.isNotEmpty()) {
                    val barcode = detections.detectedItems
                    if (barcode?.size() ?: 0 > 0) {
                        binding.textScanResult.text = barcode.valueAt(0).displayValue
                        // show barcode content value
                        //Toast.makeText(this@MainActivity,  barcode?.valueAt(0)?.displayValue ?: "", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        createSurfaceCallback()
        barcodeDetector.setProcessor(processor)

        cameraSource = CameraSource.Builder(applicationContext,barcodeDetector)
            .setAutoFocusEnabled(true).build()
        binding.cameraSurfaceView.holder.addCallback(surfaceCallBack)
    }

    private fun createSurfaceCallback(){
        surfaceCallBack = object : Callback {

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource.stop()
            }

            override fun surfaceCreated(holder: SurfaceHolder) {
                // check camera permission for api version 23
                if(ContextCompat.checkSelfPermission(this@MainActivity, CAMERA)== PackageManager.PERMISSION_GRANTED )
                    cameraSource.start(holder)
                else requestPermissions( arrayOf(CAMERA),1001)
            }

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        CAMERA
                    ) != PackageManager.PERMISSION_GRANTED
                )
                cameraSource.start(binding.cameraSurfaceView.holder)
            } else {
                Toast.makeText(this, getString(R.string.scanner_permission), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        init()
    }

    override fun onDestroy() {
        super.onDestroy()
        barcodeDetector.release()
        cameraSource.stop()
        cameraSource.release()
    }
}