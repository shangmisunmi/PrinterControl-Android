package com.test.ui

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.test.R
import com.test.control.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private var control: Control? = null
    private var receiver: BroadcastReceiver? = null
    private lateinit var mBluetoothAdapter: BluetoothAdapter
    private val service: ExecutorService = Executors.newSingleThreadExecutor()
    private val PM_SINGLE: String = Manifest.permission.ACCESS_FINE_LOCATION

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onDestroy() {
        super.onDestroy()
        receiver?.also {
            unregisterReceiver(it)
        }
        control?.disconnect(this)
    }

    fun usb(view: View) {
        control = UsbControl()
    }

    fun bluetooth(view: View) {
        receiver =  object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when(intent?.action) {
                    BluetoothDevice.ACTION_FOUND -> {
                        val device: BluetoothDevice?
                                = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        if(device?.name?.startsWith("CloudPrint") == true) {
                            mBluetoothAdapter.cancelDiscovery()
                            device.apply {
                                if(bondState == BluetoothDevice.BOND_NONE) {
                                    createBond()
                                }
                                if(bondState == BluetoothDevice.BOND_BONDED) {
                                    control = BleControl(this)
                                }
                            }
                        }
                    }
                }
            }
        }
        registerReceiver(receiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if(BluetoothAdapter.getDefaultAdapter() == null) {
            Toast.makeText(this, "Bluetooth unsupported", Toast.LENGTH_LONG).show()
            return
        } else {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if(!mBluetoothAdapter.isEnabled) {
                startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT)
            } else {
                searchBT()
            }
        }
    }

    private fun searchBT() {
        val ret = ContextCompat.checkSelfPermission(this, PM_SINGLE)
        if(ret != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(PM_SINGLE),10000)
            return
        }
        if(mBluetoothAdapter.isDiscovering) {
            mBluetoothAdapter.cancelDiscovery()
        }
        mBluetoothAdapter.startDiscovery()
    }

    fun net(view: View) {
        val nsdManager = getSystemService(Context.NSD_SERVICE) as NsdManager
        val discoveryListener = object : NsdManager.DiscoveryListener {

            override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
            }

            override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {
            }

            override fun onDiscoveryStarted(serviceType: String?) {
            }

            override fun onDiscoveryStopped(serviceType: String?) {
            }

            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                if (serviceInfo.serviceType == SERVICE_TYPE) {
                    println(serviceInfo)
                    control = NetControl(serviceInfo)
                    nsdManager.stopServiceDiscovery(this)
                }
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo?) {
            }

        }
        nsdManager.discoverServices(SERVICE_TYPE,  NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 10000) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                searchBT()
            } else {
                Toast.makeText(this, "Bluetooth PERMISSION DENIED", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode) {
            REQUEST_ENABLE_BT -> {
                if(resultCode == RESULT_OK) {
                    searchBT()
                } else {
                    Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_LONG).show()
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun connect(view: View) {
        control?.connect(this, object: ConnectCallback {
            override fun onSuccess() {
                println("onSuccess")
            }

            override fun onfailed() {
                println("onfailed")
            }
        })
    }

    fun print(view: View) {
        service.execute(object: Thread(){
            override fun run() {
                super.run()
                control?.sendData(rv)

            }
        })
    }

    companion object {
        private const val REQUEST_ENABLE_BT = 3
        private const val SERVICE_TYPE = "_afpovertcp._tcp."
        private val rv = byteArrayOf(
            0x1b,
            0x40,
            0x1b,
            0x61,
            0x01,
            0x1d,
            0x21,
            0x11,
            0xa3.toByte(),
            0xa3.toByte(),
            0x31,
            0x20,
            0x20,
            0xc3.toByte(),
            0xc0.toByte(),
            0xcd.toByte(),
            0xc5.toByte(),
            0xb2.toByte(),
            0xe2.toByte(),
            0xca.toByte(),
            0xd4.toByte(),
            0x0a,
            0x0a,
            0x1d,
            0x21,
            0x00,
            0xd4.toByte(),
            0xc1.toByte(),
            0xcf.toByte(),
            0xe3.toByte(),
            0xb8.toByte(),
            0xdb.toByte(),
            0xca.toByte(),
            0xbd.toByte(),
            0xc9.toByte(),
            0xd5.toByte(),
            0xc0.toByte(),
            0xb0.toByte(),
            0x28,
            0xb5.toByte(),
            0xda.toByte(),
            0x31,
            0xc1.toByte(),
            0xaa.toByte(),
            0x29,
            0x0a,
            0x1b,
            0x21,
            0x10,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x0a,
            0x2a,
            0x20,
            0x2a,
            0x20,
            0x2a,
            0x20,
            0x2a,
            0x20,
            0x2a,
            0x20,
            0x2a,
            0x20,
            0x20,
            0xd4.toByte(),
            0xa4.toByte(),
            0xb6.toByte(),
            0xa9.toByte(),
            0xb5.toByte(),
            0xa5.toByte(),
            0x20,
            0x20,
            0x2a,
            0x20,
            0x2a,
            0x20,
            0x2a,
            0x20,
            0x2a,
            0x20,
            0x2a,
            0x20,
            0x2a,
            0x0a,
            0xc6.toByte(),
            0xda.toByte(),
            0xcd.toByte(),
            0xfb.toByte(),
            0xcb.toByte(),
            0xcd.toByte(),
            0xb4.toByte(),
            0xef.toByte(),
            0xca.toByte(),
            0xb1.toByte(),
            0xbc.toByte(),
            0xe4.toByte(),
            0x3a,
            0x20,
            0x5b,
            0x31,
            0x38,
            0x3a,
            0x30,
            0x30,
            0x5d,
            0x0a,
            0x1d,
            0x21,
            0x00,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x0a,
            0x1b,
            0x61,
            0x00,
            0xcf.toByte(),
            0xc2.toByte(),
            0xb5.toByte(),
            0xa5.toByte(),
            0xca.toByte(),
            0xb1.toByte(),
            0xbc.toByte(),
            0xe4.toByte(),
            0x3a,
            0x30,
            0x31,
            0x2d,
            0x30,
            0x31,
            0x20,
            0x31,
            0x32,
            0x3a,
            0x30,
            0x30,
            0x0a,
            0x1b,
            0x21,
            0x10,
            0xb1.toByte(),
            0xb8.toByte(),
            0xd7.toByte(),
            0xa2.toByte(),
            0x3a,
            0xb1.toByte(),
            0xf0.toByte(),
            0xcc.toByte(),
            0xab.toByte(),
            0xc0.toByte(),
            0xb1.toByte(),
            0x0a,
            0x1d,
            0x21,
            0x00,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x0a,
            0xb2.toByte(),
            0xcb.toByte(),
            0xc3.toByte(),
            0xfb.toByte(),
            0x09,
            0x09,
            0x20,
            0x20,
            0x20,
            0xca.toByte(),
            0xfd.toByte(),
            0xc1.toByte(),
            0xbf.toByte(),
            0x09,
            0x20,
            0x20,
            0x20,
            0x20,
            0xd0.toByte(),
            0xa1.toByte(),
            0xbc.toByte(),
            0xc6.toByte(),
            0x09,
            0x0a,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x0a,
            0x1b,
            0x21,
            0x10,
            0xba.toByte(),
            0xec.toByte(),
            0xc9.toByte(),
            0xd5.toByte(),
            0xc8.toByte(),
            0xe2.toByte(),
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x78,
            0x31,
            0x09,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x31,
            0x32,
            0x0a,
            0x1d,
            0x21,
            0x00,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x0a,
            0xc5.toByte(),
            0xe4.toByte(),
            0xcb.toByte(),
            0xcd.toByte(),
            0xb7.toByte(),
            0xd1.toByte(),
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x35,
            0x0a,
            0xb2.toByte(),
            0xcd.toByte(),
            0xba.toByte(),
            0xd0.toByte(),
            0xb7.toByte(),
            0xd1.toByte(),
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x31,
            0x0a,
            0x5b,
            0xb3.toByte(),
            0xac.toByte(),
            0xca.toByte(),
            0xb1.toByte(),
            0xc5.toByte(),
            0xe2.toByte(),
            0xb8.toByte(),
            0xb6.toByte(),
            0x5d,
            0x20,
            0x2d,
            0xcf.toByte(),
            0xea.toByte(),
            0xbc.toByte(),
            0xfb.toByte(),
            0xb6.toByte(),
            0xa9.toByte(),
            0xb5.toByte(),
            0xa5.toByte(),
            0x0a,
            0xbf.toByte(),
            0xc9.toByte(),
            0xbf.toByte(),
            0xda.toByte(),
            0xbf.toByte(),
            0xc9.toByte(),
            0xc0.toByte(),
            0xd6.toByte(),
            0x3a,
            0x78,
            0x31,
            0x0a,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x0a,
            0x1b,
            0x21,
            0x10,
            0xba.toByte(),
            0xcf.toByte(),
            0xbc.toByte(),
            0xc6.toByte(),
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x20,
            0x31,
            0x38,
            0xd4.toByte(),
            0xaa.toByte(),
            0x0a,
            0x1b,
            0x40,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x0a,
            0x1d,
            0x21,
            0x11,
            0xd5.toByte(),
            0xc5.toByte(),
            0x2a,
            0x20,
            0x31,
            0x38,
            0x33,
            0x31,
            0x32,
            0x33,
            0x34,
            0x35,
            0x36,
            0x37,
            0x38,
            0x0a,
            0xb5.toByte(),
            0xd8.toByte(),
            0xd6.toByte(),
            0xb7.toByte(),
            0xd0.toByte(),
            0xc5.toByte(),
            0xcf.toByte(),
            0xa2.toByte(),
            0x0a,
            0x1d,
            0x21,
            0x00,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x2d,
            0x0a,
            0x0a,
            0x1b,
            0x40,
            0x1b,
            0x61,
            0x01,
            0x1d,
            0x21,
            0x11,
            0xa3.toByte(),
            0xa3.toByte(),
            0x31,
            0x20,
            0x20,
            0xc3.toByte(),
            0xc0.toByte(),
            0xcd.toByte(),
            0xc5.toByte(),
            0xb2.toByte(),
            0xe2.toByte(),
            0xca.toByte(),
            0xd4.toByte(),
            0x0a,
            0x1d,
            0x21,
            0x00,
            0x1b,
            0x40,
            0x0a,
            0x0a,
            0x0a,
            0x0a
        )
    }

}