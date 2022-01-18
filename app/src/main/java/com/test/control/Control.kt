package com.test.control

import android.content.Context


interface Control {

    fun connect(context: Context, connectCallback: ConnectCallback)

    fun sendData(data: ByteArray): Int

    fun recvData(data: ByteArray): Int

    fun disconnect(context: Context)
}