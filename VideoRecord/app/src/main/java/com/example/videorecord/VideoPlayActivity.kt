package com.example.videorecord

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import android.media.MediaPlayer
import android.media.MediaPlayer.OnPreparedListener
import android.os.Environment
import java.io.File


class VideoPlayActivity : AppCompatActivity() {

    // declaring a null variable for VideoView
    var simpleVideoView: VideoView? = null

    // declaring a null variable for MediaController
    var mediaControls: MediaController? = null

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_play)

        val videoUriinstring:String = intent.getStringExtra("video").toString()

        var video = Uri.parse(videoUriinstring);

        Toast.makeText(this,video.toString(),Toast.LENGTH_LONG).show()

        simpleVideoView = findViewById(R.id.simpleVideoView)

        if (mediaControls == null) {
            mediaControls = MediaController(this)
            mediaControls!!.setAnchorView(this.simpleVideoView)
        }


        simpleVideoView!!.setMediaController(mediaControls)
        simpleVideoView!!.setVideoURI(video)
        simpleVideoView!!.requestFocus()
        simpleVideoView!!.start()


        simpleVideoView!!.setOnCompletionListener {
            Toast.makeText(applicationContext, "Video completed",
                Toast.LENGTH_LONG).show()
        }

        simpleVideoView!!.setOnErrorListener { mp, what, extra ->
            Toast.makeText(applicationContext, "An Error Occured " +
                    "While Playing Video !!!", Toast.LENGTH_LONG).show()
            false
        }
    }
}
