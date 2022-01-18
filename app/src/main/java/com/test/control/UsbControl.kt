package com.test.control

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.*

class UsbControl : Control{

    private lateinit var connectCallback: ConnectCallback

    private var usbInterface: UsbInterface? = null
    private var connection: UsbDeviceConnection? = null
    private var outEndpoint: UsbEndpoint? = null
    private var inEndPoint:UsbEndpoint? = null
    private val usbReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if(Companion.ACTION_USB_PERMISSION == intent.action) {
                synchronized(this) {
                    val device : UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    if(intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        device?.also {
                            initUsbDevice(context, it)
                        }
                    } else {
                        connectCallback.onfailed()
                    }
                }
            }
        }
    }

    /**
     * 初始化usb设备
     */
    private fun initUsbDevice(context: Context, usbDevice: UsbDevice) {
        val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        usbDevice.getInterface(0).also {
            usbInterface = it
            for (index in 0 until it.endpointCount) {
                it.getEndpoint(index).also { endPoint ->
                    if(endPoint.direction == UsbConstants.USB_DIR_IN) {
                        inEndPoint = endPoint
                    } else if(endPoint.direction == UsbConstants.USB_DIR_OUT) {
                        outEndpoint = endPoint
                    }
                }
            }
            manager.openDevice(usbDevice).apply {
                claimInterface(it, true)
                connection = this
                connectCallback.onSuccess()
            }
        }
    }

    /**
     * look for printer by traverse device list
     */
    private fun findUsbPrinter(context: Context): UsbDevice? {
        val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        manager.deviceList.values.forEach {
            if(it.deviceClass == 0) {
                if(it.getInterface(0).interfaceClass == 7) {
                    return it
                }
            }
        }
        return null
    }

    /**
     * A printer type USB device is connected by default
     * And you can also specify PID and vid to connect device
     */
    override fun connect(context: Context, connectCallback: ConnectCallback) {
        this.connectCallback = connectCallback
        val usbDevice = findUsbPrinter(context)
        if(usbDevice == null) {
            connectCallback.onfailed()
        } else {
            val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager
            if(manager.hasPermission(usbDevice)) {
                initUsbDevice(context, usbDevice)
            } else {
                val permissionIntent = PendingIntent.getBroadcast(context, 0, Intent(Companion.ACTION_USB_PERMISSION), 0)
                val filter = IntentFilter(Companion.ACTION_USB_PERMISSION)
                context.registerReceiver(usbReceiver, filter)
                manager.requestPermission(usbDevice, permissionIntent)
            }
        }
    }

    override fun sendData(data: ByteArray): Int {
        if (outEndpoint == null) {
            return -1
        }
        return connection?.bulkTransfer(outEndpoint, data, data.size, 0)?:-1
    }

    override fun recvData(data: ByteArray): Int {
        if(inEndPoint != null) {
            return -1
        }
        return connection?.bulkTransfer(inEndPoint, data, data.size, 0)?:-1
    }

    override fun disconnect(context: Context) {
        connection?.releaseInterface(usbInterface)
        connection?.close()
        context.unregisterReceiver(usbReceiver)
    }

    companion object {
        private const val ACTION_USB_PERMISSION = "com.test.example.USB_PERMISSION"
    }


}