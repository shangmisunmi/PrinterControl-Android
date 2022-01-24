package com.test

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.sunmi.externalprinterlibrary.api.ConnectCallback
import com.sunmi.externalprinterlibrary.api.SunmiPrinter
import com.sunmi.externalprinterlibrary.api.SunmiPrinterApi

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onDestroy() {
        super.onDestroy()
        SunmiPrinterApi.getInstance().disconnectPrinter(this)
    }

    fun setCloudPrinter(view: View) {
        SunmiPrinterApi.getInstance().setPrinter(SunmiPrinter.SunmiCloudPrinter)
    }

    fun setBTPrinter(view: View) {
        val macList = SunmiPrinterApi.getInstance().findBleDevice(this)
        if(macList.size > 0) {
            SunmiPrinterApi.getInstance().setPrinter(SunmiPrinter.SunmiBlueToothPrinter, macList[0])
        }
    }

    fun setNetPrinter(view: View) {
        val ip = "Printer Ip"
        SunmiPrinterApi.getInstance().setPrinter(SunmiPrinter.SunmiNetPrinter, ip)
    }

    fun connect(view: View) {
        if(!SunmiPrinterApi.getInstance().isConnected) {
           SunmiPrinterApi.getInstance().connectPrinter(this, object : ConnectCallback{

               override fun onFound() {
                   System.out.println("onFound")
               }

               override fun onUnfound() {
                   System.out.println("onUnfound")
               }

               override fun onConnect() {
                   System.out.println("onConnect")
               }

               override fun onDisconnect() {
                   System.out.println("onDisconnect")
               }

           })
        }
    }

    fun test(view: View) {
        if(SunmiPrinterApi.getInstance().isConnected) {
            SunmiPrinterApi.getInstance().printerInit()
            SunmiPrinterApi.getInstance().printText("123456789\n")
            SunmiPrinterApi.getInstance().printQrCode("123456789", 4, 0)
            SunmiPrinterApi.getInstance().lineWrap(3)
        }
    }


}