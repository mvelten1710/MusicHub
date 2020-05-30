package com.dibsey.musichub.logic


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.dibsey.musichub.R
import kotlinx.android.synthetic.main.dialog_layout.*
import kotlinx.android.synthetic.main.dialog_layout.view.*

class RenameDialogFragment : DialogFragment() {

    private var username: String = ""

    @SuppressLint("DefaultLocale")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{

        val view = inflater.inflate(R.layout.dialog_layout, container, false)

        if (dialog != null && dialog!!.window != null) {
            dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        }

        view.clipToOutline = true
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)

        view.agree.setOnClickListener {
            username = editText.text.toString()
            //If the username full fills the requirements
            if (username.isNotBlank() && username.isNotEmpty() && username.length > 1){
                with(sharedPref?.edit()){
                    this?.putString(getString(R.string.username_placeholder), username)
                    this?.commit()
                }
                val intent = Intent()
                intent.putExtra("username", resources.getString(R.string.username_placeholder, username))
                targetFragment?.onActivityResult(targetRequestCode, 1337, intent)

                dismiss()
            }else{
                editTextLayout.error = "Something went wrong... Try again"
            }
        }

        view.cancel.setOnClickListener {
            dismiss()
        }
        return view
    }


    companion object {
        @JvmStatic
        fun newInstance() =
            RenameDialogFragment()

    }
}
