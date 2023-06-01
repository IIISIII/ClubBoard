package edu.sns.clubboard.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import edu.sns.clubboard.databinding.DialogLoadingBinding

class LoadingDialog: DialogFragment()
{
    private var _binding: DialogLoadingBinding? = null
    private val binding get() = _binding!!

    private var isLoading = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = DialogLoadingBinding.inflate(inflater, container, false)
        val view = binding.root

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(false)

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun show(manager: FragmentManager, tag: String?) {
        if(isLoading)
            return
        isLoading = true
        super.show(manager, tag)
    }

    override fun dismiss() {
        if(!isLoading)
            return
        isLoading = false
        super.dismiss()
    }
}