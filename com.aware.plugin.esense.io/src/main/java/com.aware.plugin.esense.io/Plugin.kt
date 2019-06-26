package com.aware.plugin.esense.io

import android.Manifest
import android.app.Service
import android.content.Intent
import com.aware.Aware
import com.aware.Aware_Preferences
import com.aware.utils.Aware_Plugin
import io.esense.esenselib.*

open class Plugin : Aware_Plugin(), ESenseConnectionListener, ESenseSensorListener, ESenseEventListener {

    companion object {
        interface AWARESensorObserver {
            /**
             * Callback when eSense device is found
             */
            fun onDeviceFound()

            /**
             * Callback when eSense device is not found
             */
            fun onDeviceNotFound()

            /**
             * Callback when eSense device is connected
             */
            fun onDeviceConnected()

            /**
             * Callback when eSense device is disconnected
             */
            fun onDeviceDisconnected()

            /**
             * Callback when eSense device reports data
             */
            fun onDataChanged(event: ESenseEvent?)

            /**
             * Callback when eSense voltage changed
             */
            fun onVoltageChanged(voltage: Double)

            /**
             * Callback when eSense button is pressed
             */
            fun onButtonChanged(pressed: Boolean)
        }

        var awareSensor: AWARESensorObserver? = null

        fun setSensorObserver(observer: AWARESensorObserver) {
            awareSensor = observer
        }

        fun getSensorObserver(): AWARESensorObserver {
            return awareSensor!!
        }
    }

    private lateinit var eSenseManager: ESenseManager

    override fun onCreate() {
        super.onCreate()
        TAG = "AWARE: eSense.io"

        REQUIRED_PERMISSIONS.add(Manifest.permission.BLUETOOTH)
        REQUIRED_PERMISSIONS.add(Manifest.permission.BLUETOOTH_ADMIN)
        REQUIRED_PERMISSIONS.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        REQUIRED_PERMISSIONS.add(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if (PERMISSIONS_OK) {

            DEBUG = Aware.getSetting(applicationContext, Aware_Preferences.DEBUG_FLAG).equals("true")
            Aware.setSetting(applicationContext, Settings.STATUS_PLUGIN_ESENSE, true)

            if (Aware.getSetting(applicationContext, Settings.FREQUENCY_PLUGIN_ESENSE).isEmpty())
                Aware.setSetting(applicationContext, Settings.FREQUENCY_PLUGIN_ESENSE, 1)

            if (Aware.getSetting(applicationContext, Settings.DEVICE_PLUGIN_ESENSE).isEmpty()) {
                startActivity(Intent(applicationContext, Settings::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            } else {
                if (!::eSenseManager.isInitialized && Aware.getSetting(applicationContext, Settings.DEVICE_PLUGIN_ESENSE).isNotEmpty()) {
                    eSenseManager = ESenseManager(Aware.getSetting(applicationContext, Settings.DEVICE_PLUGIN_ESENSE), applicationContext, this)
                    eSenseManager.connect(15000)
                }

                if (eSenseManager.isConnected) {
                    eSenseManager.registerEventListener(this)
                    eSenseManager.registerSensorListener(this, Aware.getSetting(applicationContext, Settings.FREQUENCY_PLUGIN_ESENSE).toInt())
                }
            }
        }

        return Service.START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        eSenseManager.unregisterEventListener()
        eSenseManager.unregisterSensorListener()

        Aware.setSetting(applicationContext, Settings.STATUS_PLUGIN_ESENSE, false)
    }

    override fun onDeviceNotFound(manager: ESenseManager?) {
        awareSensor?.onDeviceNotFound()

        println("eSense not found")
    }

    override fun onConnected(manager: ESenseManager?) {
        awareSensor?.onDeviceConnected()
        println("Connected ${manager?.deviceName}")

        startService(Intent(applicationContext, Plugin::class.java))
    }

    override fun onDisconnected(manager: ESenseManager?) {
        awareSensor?.onDeviceDisconnected()

        println("Disconnected ${manager?.deviceName}")
    }

    override fun onDeviceFound(manager: ESenseManager?) {
        awareSensor?.onDeviceFound()

        println("Found ${manager?.deviceName}")
    }

    override fun onSensorChanged(sensorEvent: ESenseEvent?) {
        awareSensor?.onDataChanged(sensorEvent)

        println("Event ${sensorEvent?.accel}")
    }

    override fun onBatteryRead(voltage: Double) {
        awareSensor?.onVoltageChanged(voltage)

        println("Battery $voltage")
    }

    override fun onButtonEventChanged(pressed: Boolean) {
        awareSensor?.onButtonChanged(pressed)

        println("Button pressed $pressed")
    }

    override fun onAdvertisementAndConnectionIntervalRead(minAdvertisementInterval: Int, maxAdvertisementInterval: Int, minConnectionInterval: Int, maxConnectionInterval: Int) {}
    override fun onDeviceNameRead(deviceName: String?) {}
    override fun onSensorConfigRead(config: ESenseConfig?) {}
    override fun onAccelerometerOffsetRead(offsetX: Int, offsetY: Int, offsetZ: Int) {}
}
