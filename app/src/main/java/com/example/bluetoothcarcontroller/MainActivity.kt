package com.example.bluetoothcarcontroller

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.io.OutputStream
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private val sppUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    private lateinit var statusText: TextView
    private lateinit var btnConnect: Button
    private lateinit var btnForward: Button
    private lateinit var btnBackward: Button
    private lateinit var btnLeft: Button
    private lateinit var btnRight: Button
    private lateinit var btnStop: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        btnConnect = findViewById(R.id.btnConnect)
        btnForward = findViewById(R.id.btnForward)
        btnBackward = findViewById(R.id.btnBackward)
        btnLeft = findViewById(R.id.btnLeft)
        btnRight = findViewById(R.id.btnRight)
        btnStop = findViewById(R.id.btnStop)

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        btnConnect.setOnClickListener {
            checkPermissionsAndConnect()
        }

        setupHoldButton(btnForward, "F")
        setupHoldButton(btnBackward, "B")
        setupHoldButton(btnLeft, "L")
        setupHoldButton(btnRight, "R")

        btnStop.setOnClickListener {
            sendCommand("S")
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupHoldButton(button: Button, command: String) {
        button.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> sendCommand(command)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> sendCommand("S")
            }
            true
        }
    }

    private fun checkPermissionsAndConnect() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 101)
                return
            }
        }
        connectToHc05()
    }

    @SuppressLint("MissingPermission")
    private fun connectToHc05() {
        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
            Toast.makeText(this, "Please turn on Bluetooth", Toast.LENGTH_SHORT).show()
            return
        }

        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter!!.bondedDevices
        val hc05Device = pairedDevices?.firstOrNull { it.name?.contains("HC-05", ignoreCase = true) == true }

        if (hc05Device == null) {
            Toast.makeText(this, "HC-05 not found in paired devices. Pair it in Settings first.", Toast.LENGTH_LONG).show()
            return
        }

        Thread {
            try {
                bluetoothSocket?.close()
                bluetoothSocket = hc05Device.createRfcommSocketToServiceRecord(sppUuid)
                bluetoothAdapter?.cancelDiscovery()
                bluetoothSocket?.connect()
                outputStream = bluetoothSocket?.outputStream

                runOnUiThread {
                    statusText.text = "Status: Connected to ${hc05Device.name}"
                    Toast.makeText(this, "Connected to HC-05!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    statusText.text = "Status: Connection Failed"
                    Toast.makeText(this, "Connection failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun sendCommand(cmd: String) {
        if (outputStream != null) {
            try {
                outputStream?.write(cmd.toByteArray())
            } catch (e: IOException) {
                e.printStackTrace()
                statusText.text = "Status: Disconnected"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            bluetoothSocket?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
