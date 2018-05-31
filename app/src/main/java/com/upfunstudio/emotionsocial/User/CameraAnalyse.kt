package com.upfunstudio.emotionsocial.User

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.affectiva.android.affdex.sdk.Frame
import com.affectiva.android.affdex.sdk.detector.CameraDetector
import com.affectiva.android.affdex.sdk.detector.Detector
import com.affectiva.android.affdex.sdk.detector.Face
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.upfunstudio.emotionsocial.R
import kotlinx.android.synthetic.main.activity_camera_analyse.*


class CameraAnalyse : AppCompatActivity() {

    private var mInterstitialAd: InterstitialAd? = null
    private var emotionResult: String? = null
    private var detector: CameraDetector? = null

    private var anger: Double = 0.0
    private var fear: Double = 0.0
    private var joy: Double = 0.0
    private var sadness: Double = 0.0
    private var surprise: Double = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_analyse)

        checkPermissio()

        loadAds()


    }

    private val CAMERAPERMIS = 1
    private fun checkPermissio() {

        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(arrayOf(android.Manifest.permission.CAMERA), CAMERAPERMIS)


                return

            }


        }
        initCamera()


    }

    private fun loadAds() {
        // support ads
        MobileAds.initialize(this, getString(R.string.ad_first_id))
        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd!!.adUnitId = getString(R.string.ad_secod_id)
        mInterstitialAd!!.loadAd(AdRequest.Builder().addTestDevice("walido").build())

    }


    private fun initCamera() {


        detector = CameraDetector(this,
                CameraDetector.CameraType.CAMERA_FRONT,
                cameraView,
                1,
                Detector.FaceDetectorMode.LARGE_FACES)


        // get result from detetorClass
        detector!!.setImageListener(DetectorClass())
        // classify 5 emotios
        detector!!.detectJoy = true
        detector!!.detectAnger = true
        detector!!.detectSurprise = true
        detector!!.detectFear = true
        detector!!.detectSadness = true
        // allow all emoji
        // disappointed
        // flushed
        // kissing
        // scream
        // smiley
        detector!!.setDetectAllEmojis(true)



        detector!!.setMaxProcessRate(2f)
        try {

                detector!!.start()


        } catch (e: IllegalStateException) {

            Toast.makeText(this, "${e.message}", Toast.LENGTH_LONG).show()
            Log.i("tag", e.message)
        }
    }


    fun getReportEvent(view: View) {

        val intent = Intent(this,
                ReportDetailsActivity::class.java)

        // emotion result
        intent.putExtra("emotion", emotionResult)

        // all emotions detected
        intent.putExtra("anger", anger)
        intent.putExtra("fear", fear)
        intent.putExtra("joy", joy)
        intent.putExtra("sadness", sadness)
        intent.putExtra("surprise", surprise)

        // start the report activity
        startActivity(intent)

            detector!!.stop()


        finish()

    }

    inner class DetectorClass : Detector.ImageListener {

        override fun onImageResults(face: MutableList<Face>?, frame: Frame?, p2: Float) {


            // to avoid the frames unused send :  power efficet
            detector!!.setSendUnprocessedFrames(true)


            if (face == null)
                return

            if (face.size == 0) {
                // failed to get face frame
                report.isEnabled = false
                report.text = getString(R.string.prepare_face_msg)

            } else {
                // frame ready to detect
                report.isEnabled = true
                //report.setBackgroundResource(R.drawable.button_background)
                report.text = getString(R.string.get_report)
                val item1 = face[0]


                anger = item1.emotions.anger.toDouble()
                fear = item1.emotions.fear.toDouble()
                joy = item1.emotions.joy.toDouble()
                sadness = item1.emotions.sadness.toDouble()
                surprise = item1.emotions.surprise.toDouble()


                if (anger > fear &&
                        anger > joy &&
                        anger > sadness &&
                        anger > surprise) {
                    emotionResult = " Anger"


                } else if (fear > anger &&
                        fear > joy &&
                        fear > sadness &&
                        fear > surprise) {
                    emotionResult = " Fear"


                } else if (joy > anger &&
                        joy > fear &&
                        joy > sadness &&
                        joy > surprise) {
                    emotionResult = " Joy"

                } else if (sadness > anger &&
                        sadness > fear && sadness > joy &&
                        sadness > surprise) {
                    emotionResult = " Sadness"


                } else if (surprise > anger &&
                        surprise > fear &&
                        surprise > joy &&
                        surprise > sadness) {
                    emotionResult = " Surprise"


                }


            }
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

    override fun onRestart() {
        super.onRestart()
        check()

    }

    override fun onResume() {
        super.onResume()
        check()


    }

    private fun check() {
        if (mInterstitialAd!!.isLoaded) {
            mInterstitialAd!!.show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {


        when (requestCode) {

            CAMERAPERMIS -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initCamera()
                } else {

                    Toast.makeText(this,
                            "Deny access to the camera!",
                            Toast.LENGTH_LONG).show()
                }

            }

        }




        super.onRequestPermissionsResult(requestCode, permissions, grantResults)


    }


}
