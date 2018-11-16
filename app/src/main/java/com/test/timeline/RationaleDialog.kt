package com.test.timeline

import android.Manifest
import android.support.v4.app.DialogFragment
import android.widget.Toast
import android.content.DialogInterface
import android.Manifest.permission
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.AlertDialog
import android.app.Dialog
import android.support.v4.app.ActivityCompat
import android.os.Bundle



 class RationaleDialog : DialogFragment() {

    private val ARGUMENT_PERMISSION_REQUEST_CODE = "requestCode"

    private val ARGUMENT_FINISH_ACTIVITY = "finish"

    private var mFinishActivity = false

    /**
     * Creates a new instance of a dialog displaying the rationale for the use of the location
     * permission.
     *
     *
     * The permission is requested after clicking 'ok'.
     *
     * @param requestCode    Id of the request that is used to request the permission. It is
     * returned to the
     * [android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback].
     * @param finishActivity Whether the calling Activity should be finished if the dialog is
     * cancelled.
     */
    fun newInstance(requestCode: Int, finishActivity: Boolean): RationaleDialog {
        val arguments = Bundle()
        arguments.putInt(ARGUMENT_PERMISSION_REQUEST_CODE, requestCode)
        arguments.putBoolean(ARGUMENT_FINISH_ACTIVITY, finishActivity)
        val dialog = RationaleDialog()
        dialog.arguments = arguments
        return dialog
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val arguments = arguments
        val requestCode = arguments!!.getInt(ARGUMENT_PERMISSION_REQUEST_CODE)
        mFinishActivity = arguments.getBoolean(ARGUMENT_FINISH_ACTIVITY)

        return AlertDialog.Builder(activity)
                .setMessage(R.string.permission_rationale_location)
                .setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener { dialog, which ->
                    // After click on Ok, request the permission.
                    ActivityCompat.requestPermissions(activity!!,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            requestCode)
                    // Do not finish the Activity while requesting permission.
                    mFinishActivity = false
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create()
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        if (mFinishActivity) {
            Toast.makeText(activity,
                    R.string.permission_required_toast,
                    Toast.LENGTH_SHORT)
                    .show()
            activity!!.finish()
        }
    }

}