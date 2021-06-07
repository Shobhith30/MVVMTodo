package com.codinginflow.mvvmtodo.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeleteAllCompleted : DialogFragment() {
    private val viewmodel : DeleteAllCompletedViewModel by viewModels()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setTitle("Confirm Delete")
            .setMessage("Do you really want to delete all completed tasks?")
            .setPositiveButton("Yes",) { _, _ ->
                viewmodel.onConfirmClick()
            }
            .setNegativeButton("Cancel", null)
            .create()
    }

    }