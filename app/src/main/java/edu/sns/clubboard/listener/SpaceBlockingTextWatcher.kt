package edu.sns.clubboard.listener

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

class SpaceBlockingTextWatcher(val editText: EditText): TextWatcher
{
    private var cursorPosition = 0

    override fun afterTextChanged(editable: Editable?)
    {

    }

    override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int)
    {
        cursorPosition = after
    }

    override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int)
    {
        charSequence?.run {
            if(this.contains(" ")) {
                editText.setText(this.toString().replace(" ", ""))
                editText.setSelection(cursorPosition)
            }
        }
    }
}