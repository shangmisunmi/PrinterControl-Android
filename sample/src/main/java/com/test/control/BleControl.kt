package com.test.control

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import java.util.*

class BleControl(private val bluetoothDevice: BluetoothDevice): Control{

    private var socket: BluetoothSocket? = null

    override fun connect(context: Context, connectCallback: ConnectCallback) {
        if(bluetoothDevice.bondState == BluetoothDevice.BOND_NONE) {
            connectCallback.onfailed()
        } else {
            socket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString(Companion.PRINTER_UUID))
            if(socket == null) {
                connectCallback.onfailed()
            } else {
                socket?.connect()
                connectCallback.onSuccess()
            }
        }
    }

    override fun sendData(data: ByteArray): Int {
        if(socket == null) {
            return -1
        }
        socket?.outputStream?.write(data)
        return data.size
    }

    override fun recvData(data: ByteArray): Int {
        if(socket == null) {
            return -1
        }
        return socket?.inputStream?.read(data) ?:-1
    }

    override fun disconnect(context: Context) {
        socket?.apply {
            outputStream.close()
            inputStream.close()
            close()
        }
    }

    companion object {
        private const val PRINTER_UUID = "00001101-0000-1000-8000-00805F9B34FB"
    }
}