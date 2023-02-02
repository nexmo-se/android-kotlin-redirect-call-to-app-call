package com.vonage.tutorial.voice.dialertophone;

import android.app.role.RoleManager
import android.content.Intent
import android.net.Uri
import android.telecom.CallRedirectionService
import android.telecom.PhoneAccountHandle
import android.util.Log


class VonageCallRedirectService : CallRedirectionService() {

    override fun onPlaceCall(
        handle: Uri,
        initialPhoneAccount: PhoneAccountHandle,
        allowInteractiveResponse: Boolean
    ) {
        // We can get the outgoing number from the handle parameter:
        val intent = Intent(this, CallActivity::class.java)



        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        intent.putExtra("phonenumber", handle.toString())

        startActivity(intent)
        cancelCall()
    }


}
