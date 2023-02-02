package com.vonage.tutorial.voice.dialertophone

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.nexmo.client.*
import com.nexmo.client.request_listener.NexmoApiError
import com.nexmo.client.request_listener.NexmoConnectionListener.ConnectionStatus
import com.nexmo.client.request_listener.NexmoRequestListener
import java.util.*

class CallActivity : AppCompatActivity() {
    private lateinit var client: NexmoClient
    var onGoingCall: NexmoCall? = null
    var authToken: String = "JWT"

    private lateinit var startCallButton: Button
    private lateinit var endCallButton: Button
    private lateinit var connectionStatusTextView: TextView
    private lateinit var phonenumber: String
    private lateinit var sanitizedNumber: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)

        // request permissions
        val callsPermissions = arrayOf(Manifest.permission.RECORD_AUDIO)
        ActivityCompat.requestPermissions(this, callsPermissions, 123)

        //get phonenumber from passed bundle
        val bundle = intent.extras
        if (bundle != null) {
            phonenumber = bundle.getString("phonenumber").toString()
            sanitizedNumber = phonenumber.replace("+","").replace("%2B","").replace("tel:","")
            bundle.remove("phonenumber")
        }else{
            Toast.makeText(
                this,
                "Use this via the Dialer",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }

        // init views
        startCallButton = findViewById(R.id.makeCallButton)
        endCallButton = findViewById(R.id.endCallButton)
        connectionStatusTextView = findViewById(R.id.ConnectionStatusTextView)

        startCallButton.setOnClickListener {
            startCall()
        }

        endCallButton.setOnClickListener {
            hangup()
        }

        // init client
        client = NexmoClient.Builder().build(this)
        if(!client.isConnected){
            client.setConnectionListener { connectionStatus, _ ->
                Log.i("dtp","Connecting to Vonage")
                runOnUiThread { connectionStatusTextView.text = "" }

                if (connectionStatus == ConnectionStatus.CONNECTED) {

                    runOnUiThread {
                        startCallButton.visibility = View.VISIBLE

                        connectionStatusTextView.text = buildString {
                            append("Ready to Call ")
                            append(sanitizedNumber)
                        }
                    }

                    return@setConnectionListener
                }else if(connectionStatus == ConnectionStatus.DISCONNECTED)  {
                    runOnUiThread { connectionStatusTextView.text = "Cannot Connect To Vonage"}
                }else{
                    runOnUiThread { connectionStatusTextView.text = "Connecting"}
                }
            }
        }else{
            runOnUiThread {
                startCallButton.visibility = View.VISIBLE
                connectionStatusTextView.text = buildString {
                    append("Ready to Call ")
                    append(sanitizedNumber)
                }
            }
        }

        client.login(authToken)
    }


    fun startCall() {
        client.serverCall(sanitizedNumber, null, object : NexmoRequestListener<NexmoCall> {
            override fun onSuccess(call: NexmoCall?) {
                runOnUiThread {
                    endCallButton.visibility = View.VISIBLE
                    startCallButton.visibility = View.INVISIBLE
                    connectionStatusTextView.text = buildString {
                        append("In a Call with ")
                        append(sanitizedNumber)
                    }
                }

                onGoingCall = call
                onGoingCall?.addCallEventListener(object : NexmoCallEventListener {
                    override fun onMemberStatusUpdated(callStatus: NexmoCallMemberStatus, callMember: NexmoMember) {
                        if (callStatus == NexmoCallMemberStatus.COMPLETED || callStatus == NexmoCallMemberStatus.CANCELLED) {
                            onGoingCall = null

                            runOnUiThread {
                                endCallButton.visibility = View.INVISIBLE
                                startCallButton.visibility = View.VISIBLE
                            }
                        }
                    }

                    override fun onMuteChanged(nexmoMediaActionState: NexmoMediaActionState, callMember: NexmoMember) {}

                    override fun onEarmuffChanged(nexmoMediaActionState: NexmoMediaActionState, callMember: NexmoMember) {}

                    override fun onDTMF(dtmf: String, callMember: NexmoMember) {}
                    override fun onLegTransfer(
                        event: NexmoLegTransferEvent?,
                        member: NexmoMember?
                    ) {
                        TODO("Not yet implemented")
                    }
                })
            }

            override fun onError(apiError: NexmoApiError) {
                Log.i("DTPERR", apiError.toString())
            }
        })
    }

    private fun hangup() {
        onGoingCall?.hangup(object : NexmoRequestListener<NexmoCall> {
            override fun onSuccess(call: NexmoCall?) {
                onGoingCall = null
                moveTaskToBack(true)
            }

            override fun onError(apiError: NexmoApiError) {
                moveTaskToBack(true)
            }
        })
    }

}
