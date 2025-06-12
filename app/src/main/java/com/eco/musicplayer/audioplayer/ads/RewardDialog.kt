package com.eco.musicplayer.audioplayer.ads

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.eco.musicplayer.audioplayer.music.R

class RewardDialog : DialogFragment() {

    var onRewardAccepted: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle(getString(R.string.reward_dialog_title))
            builder.setMessage(getString(R.string.reward_dialog_message))
                .setPositiveButton("Start") { dialog, id ->
                    onRewardAccepted?.invoke()
                }
                .setNegativeButton("Cancel") { dialog, id ->

                }
            builder.setCancelable(true)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    fun showDialog(parentFragmentManager : FragmentManager) {
        if (!isAdded || activity != null) {
            show(parentFragmentManager, "Reward")
        }
    }
}