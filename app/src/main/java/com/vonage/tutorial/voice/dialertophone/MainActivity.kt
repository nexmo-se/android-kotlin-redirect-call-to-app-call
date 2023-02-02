package com.vonage.tutorial.voice.dialertophone


import android.app.role.RoleManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    private lateinit var labelTextView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        labelTextView = findViewById(R.id.LabelStatusTextView)

        if (!isRedirection())
            roleAcquire(RoleManager.ROLE_CALL_REDIRECTION)
    }
    private fun isRedirection(): Boolean {
        return isRoleHeldByApp(RoleManager.ROLE_CALL_REDIRECTION)
    }

    private fun isRoleHeldByApp(roleName: String): Boolean {
        val roleManager: RoleManager? = getSystemService(RoleManager::class.java)
        return roleManager!!.isRoleHeld(roleName)

    }

    private fun roleAcquire(roleName: String) {
        val roleManager: RoleManager?
        if (roleAvailable(roleName)) {
            val startActivityIntent = registerForActivityResult(
                StartActivityForResult(),
                ActivityResultCallback {
                    // Add same code that you want to add in onActivityResult method
                })
            roleManager = getSystemService(RoleManager::class.java)
            val intent = roleManager.createRequestRoleIntent(roleName)
            startActivityIntent.launch(intent);
        } else {
            Toast.makeText(
                this,
                "Redirection call with role in not available",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    private fun roleAvailable(roleName: String): Boolean {
        val roleManager: RoleManager? = getSystemService(RoleManager::class.java)
        return roleManager!!.isRoleAvailable(roleName)
    }

}