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
import edu.sns.clubboard.R
import edu.sns.clubboard.databinding.DialogConfirmBinding

class ConfirmDialog: DialogFragment()
{
    private var _binding: DialogConfirmBinding? = null
    private val binding get() = _binding!!

    private var onConfirm: (() -> Unit)? = null

    private var inputText: String = ""

    private var contentText: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = DialogConfirmBinding.inflate(inflater, container, false)
        val view = binding.root

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding.dialogContent.text = contentText

        binding.inputConfirm.hint = inputText

        binding.inputConfirm.editText?.doAfterTextChanged {
            binding.btnConfirm.isEnabled = it.toString() == inputText
        }

        binding.btnCancel.setOnClickListener {
            this.dismiss()
        }

        binding.btnConfirm.setOnClickListener {
            onConfirm?.invoke()
            this.dismiss()
        }

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    fun show(activity: AppCompatActivity, inputText: String, onConfirm: () -> Unit)
    {
        this.inputText = inputText
        this.contentText = activity.getString(R.string.text_input_confirm, inputText)
        this.onConfirm = onConfirm
        this.show(activity.supportFragmentManager, "ConfirmDialog")
    }
}