package com.test.control

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import java.net.Socket

class NetControl(private val serviceInfo: NsdServiceInfo): Control{

    private val resolveListener: NsdManager.ResolveListener = object: NsdManager.ResolveListener{
        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            connectCallback.onfailed()
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            client = Socket(serviceInfo.host.hostAddress, serviceInfo.port)
            client?.soTimeout = 2000
            if(client?.isConnected == true) {
                connectCallback.onSuccess()
            } else {
                connectCallback.onfailed()
            }
        }
    }

    private lateinit var connectCallback: ConnectCallback

    private var client: Socket? = null

    override fun connect(context: Context, connectCallback: ConnectCallback) {
        this.connectCallback = connectCallback
        val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
        nsdManager.resolveService(serviceInfo, resolveListener)
    }

    override fun sendData(data: ByteArray): Int {
        if(client == null) {
            return -1
        }
        client?.outputStream?.write(data)
        return data.size
    }

    override fun recvData(data: ByteArray): Int {
        if(client == null) {
            return -1
        }
        return client?.inputStream?.read(data) ?:-1
    }

    override fun disconnect(context: Context) {
        client?.apply {
            outputStream.close()
            inputStream.close()
            close()
        }
    }
}