package com.ryanrvldo.myreactiveform

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Patterns
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.functions.Function
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val emailStream = observableDataStream(ed_email) { email ->
            !Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }
        emailStream.subscribe { isValid -> showEmailExistAlert(isValid) }

        val passwordStream = observableDataStream(ed_password) { password ->
            password.length < 6
        }
        passwordStream.subscribe { isValid -> showPasswordMinimalAlert(isValid) }

        val passwordConfirmationStream = Observable.merge(
            observableDataStream(ed_password) { password ->
                password.toString() != ed_confirm_password.text.toString()
            },
            observableDataStream(ed_confirm_password) { confirmPassword ->
                confirmPassword.toString() != ed_password.text.toString()
            }
        )
        passwordConfirmationStream.subscribe { isValid ->
            showPasswordConfirmationAlert(isValid)
        }

        val invalidFieldsStream = Observable.combineLatest(
            emailStream,
            passwordStream,
            passwordConfirmationStream,
            { emailInvalid: Boolean, passwordInvalid: Boolean, passwordConfirmationInvalid: Boolean ->
                !emailInvalid && !passwordInvalid && !passwordConfirmationInvalid
            }
        )
        invalidFieldsStream.subscribe { isValid ->
            if (isValid) {
                btn_register.isEnabled = true
                btn_register.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent))
            } else {
                btn_register.isEnabled = false
                btn_register.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        android.R.color.darker_gray
                    )
                )
            }
        }
    }

    private fun observableDataStream(
        view: TextView,
        mapFunction: Function<CharSequence, Boolean>
    ): Observable<Boolean> =
        RxTextView.textChanges(view)
            .skipInitialValue()
            .map(mapFunction)

    private fun showEmailExistAlert(isValid: Boolean) {
        ed_email.error = if (isValid) getString(R.string.email_not_valid) else null
    }

    private fun showPasswordMinimalAlert(isValid: Boolean) {
        ed_password.error = if (isValid) getString(R.string.password_not_valid) else null
    }

    private fun showPasswordConfirmationAlert(isValid: Boolean) {
        ed_confirm_password.error = if (isValid) getString(R.string.password_not_same) else null
    }

}
