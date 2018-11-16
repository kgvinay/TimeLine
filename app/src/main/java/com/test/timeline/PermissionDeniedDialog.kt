package com.test.timeline

import android.app.AlertDialog
import android.app.Dialog
import android.support.v4.app.DialogFragment
import android.widget.Toast
import android.content.DialogInterface
import android.os.Bundle



class PermissionDeniedDialog : DialogFragment() {

    private val ARGUMENT_FINISH_ACTIVITY = "finish"

    private var mFinishActivity = false

    /**
     * Creates a new instance of this dialog and optionally finishes the calling Activity
     * when the 'Ok' button is clicked.
     */
    fun newInstance(finishActivity: Boolean): PermissionDeniedDialog {
        val arguments = Bundle()
        arguments.putBoolean(ARGUMENT_FINISH_ACTIVITY, finishActivity)

        val dialog = PermissionDeniedDialog()
        dialog.arguments = arguments
        return dialog
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        mFinishActivity = arguments!!.getBoolean(ARGUMENT_FINISH_ACTIVITY)

        return AlertDialog.Builder(activity)
                .setMessage(R.string.location_permission_denied)
                .setPositiveButton(android.R.string.ok, null)
                .create()
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        if (mFinishActivity) {
            Toast.makeText(activity, R.string.permission_required_toast,
                    Toast.LENGTH_SHORT).show()
            activity!!.finish()
        }
    }
}