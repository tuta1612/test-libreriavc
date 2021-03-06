package com.example.mylibrary

import android.Manifest
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import pub.devrel.easypermissions.EasyPermissions
import android.util.Log
import android.view.View
import com.opentok.android.*
import pub.devrel.easypermissions.AfterPermissionGranted
import kotlinx.android.synthetic.main.activity_videochat.*
import com.opentok.android.Subscriber




class VideoChatActivity : AppCompatActivity(){
    //https://test-tokbox-assistcard.herokuapp.com/

    //private val API_KEY = "46256142"
    //private val SESSION_ID = "2_MX40NjI1NjE0Mn5-MTU0OTMwNTI4NTI1MH5rOVFDYTQvNVlIdlBmMXpBWDJVaGo5MVJ-fg"
    //private val TOKEN = "T1==cGFydG5lcl9pZD00NjI1NjE0MiZzaWc9Njc4N2UwMjljNGZiNTEzODE3MmZkYjM3Zjc2ZDQwOWM0ZjM2MDRjNjpzZXNzaW9uX2lkPTJfTVg0ME5qSTFOakUwTW41LU1UVTBPVE13TlRJNE5USTFNSDVyT1ZGRFlUUXZOVmxJZGxCbU1YcEJXREpWYUdvNU1WSi1mZyZjcmVhdGVfdGltZT0xNTQ5NTU0MjQ1Jm5vbmNlPTAuMDExNTUxNTkyOTE1ODk1MzU5JnJvbGU9cHVibGlzaGVyJmV4cGlyZV90aW1lPTE1NTIxNDYyNDUmaW5pdGlhbF9sYXlvdXRfY2xhc3NfbGlzdD0="
    private val LOG_TAG = VideoChatActivity::class.java.simpleName
    private val RC_SETTINGS_SCREEN_PERM = 123


    val tokboxApiKey by lazy { intent.getStringExtra("API_KEY") }
    val tokboxSessionId by lazy { intent.getStringExtra("SESSION_ID") }
    val tokboxToken by lazy { intent.getStringExtra("TOKEN") }

    private val tokboxSession by lazy { Session.Builder(this, tokboxApiKey, tokboxSessionId).build() }
    private val tokboxPublisher by lazy { Publisher.Builder(this).build() }
    private var tokboxSuscriber :Subscriber? = null

    companion object {
        const val RC_VIDEO_APP_PERM = 124

        @JvmStatic fun newIntent(ctx: Context, apiKey: String, sessionId: String, token: String): Intent{
            val intent = Intent(ctx, VideoChatActivity::class.java)
            intent.putExtra("API_KEY", apiKey)
            intent.putExtra("SESSION_ID", sessionId)
            intent.putExtra("TOKEN", token)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_videochat)
        requestPermissions()

        btn_cam_switch.setOnClickListener {
            tokboxPublisher.cycleCamera()
        }
        btn_cam_on.setOnClickListener {
            tokboxPublisher.publishVideo = true
            btn_cam_on.visibility = View.GONE
            btn_cam_off.visibility = View.VISIBLE
        }
        btn_cam_off.setOnClickListener {
            tokboxPublisher.publishVideo = false
            btn_cam_on.visibility = View.VISIBLE
            btn_cam_off.visibility = View.GONE
        }
        btn_mic_on.setOnClickListener {
            tokboxPublisher.publishAudio = true
            btn_mic_on.visibility = View.GONE
            btn_mic_off.visibility = View.VISIBLE
        }
        btn_mic_off.setOnClickListener {
            tokboxPublisher.publishAudio= false
            btn_mic_on.visibility = View.VISIBLE
            btn_mic_off.visibility = View.GONE
        }
        btn_call_end.setOnClickListener {
            tokboxSession.disconnect()
            finish()
        }
    }

    override fun onPause() {
        tokboxSession.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        tokboxSession.onResume()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    @AfterPermissionGranted(Companion.RC_VIDEO_APP_PERM)
    private fun requestPermissions() {
        val perms = arrayOf<String>(Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        if (EasyPermissions.hasPermissions(this, *perms)) {
            // initialize view objects from your layout


            // initialize and connect to the session
            tokboxSession.setSessionListener(tokboxSesionListener)
            tokboxSession.connect(tokboxToken)

        } else {
            EasyPermissions.requestPermissions(
                this,
                "This app needs access to your camera and mic to make video calls",
                RC_VIDEO_APP_PERM,
                *perms
            )
        }
    }

    val tokboxSuscriberVideoListener: SubscriberKit.VideoListener by lazy {
        object : SubscriberKit.VideoListener {
            override fun onVideoDataReceived(p0: SubscriberKit?) {
            }

            override fun onVideoEnabled(p0: SubscriberKit?, p1: String?) {
            }

            override fun onVideoDisableWarning(p0: SubscriberKit?) {
            }

            override fun onVideoDisableWarningLifted(p0: SubscriberKit?) {
            }

            override fun onVideoDisabled(p0: SubscriberKit?, p1: String?) {
            }
        }
    }

    val tokboxPublisherListener :PublisherKit.PublisherListener by lazy {
        object : PublisherKit.PublisherListener{
            override fun onStreamCreated(p0: PublisherKit?, p1: Stream?) {
                Log.i(LOG_TAG, "Publisher onStreamCreated")
            }

            override fun onStreamDestroyed(p0: PublisherKit?, p1: Stream?) {
                Log.i(LOG_TAG, "Publisher onStreamDestroyed")
            }

            override fun onError(p0: PublisherKit?, opentokError: OpentokError?) {
                Log.e(LOG_TAG, "Publisher error: " + opentokError?.message)
            }
        }
    }

    val tokboxSesionListener: Session.SessionListener by lazy {
        object : Session.SessionListener{
            override fun onConnected(p0: Session?) {
                Log.i(LOG_TAG, "Session Connected")

                tokboxPublisher.setPublisherListener(tokboxPublisherListener)

                publisher_container.addView(tokboxPublisher.view)
                tokboxSession.publish(tokboxPublisher)
            }

            override fun onStreamReceived(p0: Session?, stream: Stream?) {
                Log.i(LOG_TAG, "Stream Received")

                Log.i(LOG_TAG, "Stream Received")

                if (tokboxSuscriber == null) {
                    tokboxSuscriber = Subscriber.Builder(this@VideoChatActivity, stream).build()

                    tokboxSuscriber?.setVideoListener(tokboxSuscriberVideoListener)
                    tokboxSession.subscribe(tokboxSuscriber)
                    subscriber_container.addView(tokboxSuscriber?.view)
                }
            }

            override fun onStreamDropped(p0: Session?, p1: Stream?) {
                Log.i(LOG_TAG, "Stream Dropped")
                if (tokboxSuscriber!= null) {
                    tokboxSuscriber = null
                    subscriber_container.removeAllViews()
                }
            }

            override fun onError(p0: Session?, opentokError: OpentokError?) {
                Log.e(LOG_TAG, "Session error: " + opentokError?.message)
            }

            override fun onDisconnected(p0: Session?) {
                Log.i(LOG_TAG, "Session Disconnected")
            }
        }
    }

}
