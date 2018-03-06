package com.upfunstudio.emotionsocial.Companion

import android.app.DialogFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.upfunstudio.emotionsocial.Dr.SetttingsDoctorActivity
import com.upfunstudio.emotionsocial.R
import com.upfunstudio.emotionsocial.User.SettingsUserActivity
import kotlinx.android.synthetic.main.update_dialoge.view.*

/**
 * Created by walido on 2/17/2018.
 */
class AdapterFramgentUpdate : DialogFragment() {


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val view = inflater!!.inflate(R.layout.update_dialoge,
                container, false)

        try {
            val update = this.arguments.getString("update", "")
            if (update == "updateDoctor") {

                view.changePhone.visibility = View.GONE
                // that mean appear update for doctor



                view.updateNowButton.setOnClickListener {

                    val mainProfileDoctor = activity as SetttingsDoctorActivity
                    mainProfileDoctor.checkUpdate(view.changeFullname.text.toString()
                            , view.chngeMail.text.toString(),
                            view.chnageCity.text.toString(),
                            view.chageLanguage.text.toString(),
                            view.chageSpec.text.toString())

                    dismiss()

                }
                view.CancelUpdate.setOnClickListener {
                    dismiss()

                }


            } else if (update == "updateUser") {

                // disappear spec because field especially for drs
                view.chageSpec.visibility = View.GONE
                // that mean appear update for user
                view.updateNowButton.setOnClickListener {

                    val mainProfileActivity = activity as SettingsUserActivity

                    // update info
                    mainProfileActivity.checkAndUpdate(view.changeFullname.text.toString()
                            , view.chngeMail.text.toString(),
                            view.changePhone.text.toString(),
                            view.chnageCity.text.toString(),
                            view.chageLanguage.text.toString())





                    dismiss()


                }
                view.CancelUpdate.setOnClickListener {
                    dismiss()
                }


            }


        } catch (ex: Exception) {
        }
        return view
    }


}
