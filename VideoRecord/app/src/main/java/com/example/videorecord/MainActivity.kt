package com.example.videorecord

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private val VIDEO_CAPTURE = 101

    var check:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val Button = findViewById<Button>(R.id.button)

        Button.setOnClickListener {

            openCameraToCaptureVideo()

        }
    }
    private fun openCameraToCaptureVideo() {
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) { // First check if camera is available in the device
            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 5);
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1000);
            startActivityForResult(intent, VIDEO_CAPTURE);
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode == Activity.RESULT_OK && requestCode == VIDEO_CAPTURE) {
            if (intent?.data != null) {


                val uriPathHelper = URIPathHelper()
                val videoFullPath = uriPathHelper.getPath(this, intent.data!!) // Use this video path according to your logic
                // if you want to play video just after recording it to check is it working (optional)
                if (videoFullPath != null) {

                    if(VIDEO_CAPTURE!=null) {
                    val intent = Intent(this@MainActivity,VideoPlayActivity::class.java)
                    intent.putExtra("video",videoFullPath.toString())
                    startActivity(intent)
                    }
//                    Toast.makeText(this@MainActivity,videoFullPath.toString(),Toast.LENGTH_LONG).show()
//                    playVideoInDevicePlayer(videoFullPath);
                }
            }
        }
    }
    fun playVideoInDevicePlayer(videoPath: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoPath))
        intent.setDataAndType(Uri.parse(videoPath), "video/mp4")
        startActivity(intent)

    }

}