package com.aware.plugin.esense.io

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.CheckBoxPreference
import android.preference.EditTextPreference
import android.preference.PreferenceManager
import com.aware.Aware
import com.aware.ui.AppCompatPreferenceActivity

class Settings : AppCompatPreferenceActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        val STATUS_PLUGIN_ESENSE = "status_plugin_esense"
        val FREQUENCY_PLUGIN_ESENSE = "frequency_plugin_esense"
        val DEVICE_PLUGIN_ESENSE = "device_plugin_esense"
    }

    lateinit var status : CheckBoxPreference
    lateinit var frequency : EditTextPreference
    lateinit var device : EditTextPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences_esense)
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onResume() {
        super.onResume()
        status = findPreference(STATUS_PLUGIN_ESENSE) as CheckBoxPreference
        if (Aware.getSetting(this, STATUS_PLUGIN_ESENSE).isEmpty())
            Aware.setSetting(this, STATUS_PLUGIN_ESENSE, true)
        status.isChecked = Aware.getSetting(this, STATUS_PLUGIN_ESENSE) == "true"

        frequency = findPreference(FREQUENCY_PLUGIN_ESENSE) as EditTextPreference
        if (Aware.getSetting(this, FREQUENCY_PLUGIN_ESENSE).isEmpty())
            Aware.setSetting(this, FREQUENCY_PLUGIN_ESENSE, 1)
        frequency.summary = "${Aware.getSetting(this, FREQUENCY_PLUGIN_ESENSE)} Hz"

        device = findPreference(DEVICE_PLUGIN_ESENSE) as EditTextPreference
        if (Aware.getSetting(this, DEVICE_PLUGIN_ESENSE).isEmpty())
            Aware.setSetting(this, DEVICE_PLUGIN_ESENSE, "")
        device.summary = Aware.getSetting(this, DEVICE_PLUGIN_ESENSE)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        val pref = findPreference(key)
        when (pref.key) {
            STATUS_PLUGIN_ESENSE -> {
                Aware.setSetting(this, key, sharedPreferences!!.getBoolean(key, false))
                status.isChecked = sharedPreferences.getBoolean(key, false)
            }
            FREQUENCY_PLUGIN_ESENSE -> {
                Aware.setSetting(this, key, sharedPreferences!!.getString(key, "1"))
                pref.summary = "${Aware.getSetting(this, key)} Hz"
            }
            DEVICE_PLUGIN_ESENSE -> {
                Aware.setSetting(this, key, sharedPreferences!!.getString(key, ""))
                pref.summary = Aware.getSetting(this, key)
            }
        }

        if (status.isChecked) {
            Aware.startPlugin(applicationContext, "com.aware.plugin.esense.io")
        } else {
            Aware.stopPlugin(applicationContext, "com.aware.plugin.esense.io")
        }
    }
}