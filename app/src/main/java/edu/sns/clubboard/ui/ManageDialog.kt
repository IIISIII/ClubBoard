package edu.sns.clubboard.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import edu.sns.clubboard.databinding.DialogClubManagementBinding

class ManageDialog: DialogFragment()
{
    private var _binding: DialogClubManagementBinding? = null
    private val binding get() = _binding!!

    private var onMenuButtonClickListener: OnMenuButtonClickListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = DialogClubManagementBinding.inflate(inflater, container, false)
        val view = binding.root

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.inputFiirst.setOnClickListener {
            onMenuButtonClickListener?.onFirstItemClick(this)
        }

        binding.inputSecond.setOnClickListener {
            onMenuButtonClickListener?.onSecondItemClick(this)
        }

        binding.inputThird.setOnClickListener {
            onMenuButtonClickListener?.onThirdItemClick(this)
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
        fun onFirstItemClick(manageDialog: ManageDialog)

        fun onSecondItemClick(manageDialog: ManageDialog)

        fun onThirdItemClick(manageDialog: ManageDialog)
    }
}