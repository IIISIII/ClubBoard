package edu.sns.clubboard.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import edu.sns.clubboard.databinding.DialogErrorBinding

class ErrorDialog: DialogFragment()
{
    private var _binding: DialogErrorBinding? = null
    private val binding get() = _binding!!

    private var onConfirm: (() -> Unit)? = null

    private var contentText: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = DialogErrorBinding.inflate(inflater, container, false)
        val view = binding.root

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)

        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(false)

        binding.textContent.text = contentText
        binding.btnOk.setOnClickListener {
            onConfirm?.invoke()
            this.dismiss()
        }

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    fun show(activity: AppCompatActivity, message: String, onConfirm: () -> Unit)
    {
        this.contentText = message
        this.onConfirm = onConfirm
        this.show(activity.supportFragmentManager, "ErrorDialog")
    }
}