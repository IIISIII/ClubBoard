package edu.sns.clubboard.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import edu.sns.clubboard.databinding.DialongRequestBinding

class RequestDialog: DialogFragment()
{
    private var _binding: DialongRequestBinding? = null
    private val binding get() = _binding!!

    private var onMenuButtonClickListener: OnMenuButtonClickListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = DialongRequestBinding.inflate(inflater, container, false)
        val view = binding.root

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.btnSubmit.setOnClickListener {
            onMenuButtonClickListener?.onSubmit(this, binding.inputDescription.text.toString())
        }

        binding.btnCancel.setOnClickListener {
            onMenuButtonClickListener?.onCancel(this)
        }

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    fun setOnMenuItemClickListener(listener: OnMenuButtonClickListener)
    {
        onMenuButtonClickListener = listener
    }

    interface OnMenuButtonClickListener
    {
        fun onSubmit(requestDialog: RequestDialog, description: String)

        fun onCancel(requestDialog: RequestDialog)
    }
}