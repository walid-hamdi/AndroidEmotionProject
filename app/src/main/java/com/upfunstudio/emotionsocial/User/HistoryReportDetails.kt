package com.upfunstudio.emotionsocial.User

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.upfunstudio.emotionsocial.R
import kotlinx.android.synthetic.main.activity_history_report_details.*

class HistoryReportDetails : AppCompatActivity() {


    private var fireStore: FirebaseFirestore? = null
    private var firebaseAuth: FirebaseAuth? = null


    private var anger: Double = 0.0
    private var fear: Double = 0.0
    private var joy: Double = 0.0
    private var sadness: Double = 0.0
    private var surprise: Double = 0.0
    private var emotion: String? = null
    private var tipGot: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_report_details)
        firebaseAuth = FirebaseAuth.getInstance()
        fireStore = FirebaseFirestore.getInstance()

        fullChartStateAndPassingInfo()

    }

    private fun fullChartStateAndPassingInfo() {

        try {
            // gettig data

            emotion = intent.extras.getString("emotion")
            tipGot = intent.extras.getString("tip")

            emotionTv.text = emotion
            tipTv.text = tipGot



            anger = intent.extras.getDouble("anger")
            fear = intent.extras.getDouble("fear")
            joy = intent.extras.getDouble("joy")
            sadness = intent.extras.getDouble("sadness")
            surprise = intent.extras.getDouble("surprise")

            chartPieDetails.setUsePercentValues(true)
            chartPieDetails.description.isEnabled = false
            chartPieDetails.setExtraOffsets(5f, 10f, 5f, 5f)
            chartPieDetails.dragDecelerationFrictionCoef = 0.99f
            chartPieDetails.isDrawHoleEnabled = true
            chartPieDetails.setHoleColor(Color.WHITE)
            chartPieDetails.transparentCircleRadius = 61f

            val arr = ArrayList<PieEntry>()
            arr.add(PieEntry(anger.toFloat() * 10, "anger"))
            arr.add(PieEntry(fear.toFloat() * 10, "fear"))
            arr.add(PieEntry(joy.toFloat() * 10, "joy"))
            arr.add(PieEntry(sadness.toFloat() * 10, "sadness"))
            arr.add(PieEntry(surprise.toFloat() * 10, "surprise"))

            chartPieDetails.animateY(1000, Easing.EasingOption.EaseInCubic)

            val set = PieDataSet(arr, "Emotions")
            set.sliceSpace = 1.5f
            set.selectionShift = 5f
            set.colors = ColorTemplate.MATERIAL_COLORS.toMutableList()

            val data = PieData(set)
            data.setValueTextSize(10f)
            data.setValueTextColor(Color.WHITE)

            chartPieDetails.data = data


        } catch (ex: Exception) {


        }


    }

    fun dropReport(view: View) {

        // todo : later delete report
    }

}
