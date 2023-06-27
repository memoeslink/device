package com.memoeslink.manager

import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.SupplicantState
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import java.lang.reflect.Field

class Device(context: Context) : ContextWrapper(context) {

    fun getInfo(type: InformationType): String {
        when (type) {
            InformationType.ANDROID_ID -> return getString(R.string.device_id, getMaskedAndroidId())
            InformationType.ANDROID_VERSION -> {
                val name: String = getAndroidVersionName()
                return if (name.isEmpty()) getString(R.string.device_android)
                else getString(R.string.device_version, name, Build.VERSION.RELEASE)
            }

            InformationType.BRAND -> return getString(R.string.device_brand,
                Build.BRAND.takeUnless { it.isNullOrEmpty() } ?: DEFAULT_VALUE)

            InformationType.BRAND_AND_MODEL -> return getString(R.string.device,
                (Build.BRAND.takeUnless { it.isNullOrEmpty() }
                    ?: DEFAULT_VALUE) + Build.MODEL.takeUnless { it.isNullOrEmpty() }
                    .let { " $it" })

            InformationType.MANUFACTURER_AND_MODEL -> return getString(R.string.device,
                (Build.MANUFACTURER.takeUnless { it.isNullOrEmpty() }
                    ?: DEFAULT_VALUE) + Build.MODEL.takeUnless { it.isNullOrEmpty() }
                    .let { " $it" })

            InformationType.PRODUCT -> return getString(R.string.device,
                Build.PRODUCT.takeUnless { it.isNullOrEmpty() } ?: DEFAULT_VALUE)

            InformationType.NETWORK_NAME -> {
                val networkName = getNetworkName()
                return when {
                    networkName.isEmpty() -> getString(R.string.device_network_disconnected)
                    networkName in listOf(
                        DEFAULT_VALUE, UNKNOWN_SSID
                    ) -> getString(R.string.device_network_unknown_ssid)

                    else -> getString(R.string.device_network_ssid, networkName)
                }
            }

            InformationType.NETWORK_OPERATOR -> {
                val networkOperator = getNetworkOperator()
                return if (networkOperator.isBlank()) getString(R.string.device_network_operator_disconnected)
                else getString(R.string.device_network_operator, networkOperator)
            }

            InformationType.IP_ADDRESS -> {
                val ipAddress = getIpAddress()
                return if (ipAddress.isBlank()) getString(R.string.device_unknown_ip)
                else getString(R.string.device_ip, ipAddress)
            }

            else -> return DEFAULT_VALUE
        }
    }

    fun getMaskedAndroidId(): String {
        val androidId = getAndroidId()
        return when {
            androidId.isBlank() || androidId == DEFAULT_VALUE -> "*".repeat(32)
            androidId.length <= 4 -> androidId.padStart(32, '*')
            else -> androidId.replaceRange(
                0, androidId.length - 4, "*".repeat(androidId.length - 4)
            )
        }
    }

    fun getAndroidId(): String =
        Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
            .takeUnless { it.isNullOrBlank() }?.toMd5()?.uppercase() ?: getPseudoId()

    fun getPseudoId(): String = String.format(
        "0%c%c%c%c%c%c%c%c%c%c%c%c%c%c%c",
        Integer.toHexString(Build.BOARD.length % 16)[0],
        Integer.toHexString(Build.BOOTLOADER.length % 16)[0],
        Integer.toHexString(Build.BRAND.length % 16)[0],
        Integer.toHexString(Build.DEVICE.length % 16)[0],
        Integer.toHexString(Build.DISPLAY.length % 16)[0],
        Integer.toHexString(Build.FINGERPRINT.length % 16)[0],
        Integer.toHexString(Build.HARDWARE.length % 16)[0],
        Integer.toHexString(Build.HOST.length % 16)[0],
        Integer.toHexString(Build.ID.length % 16)[0],
        Integer.toHexString(Build.MANUFACTURER.length % 16)[0],
        Integer.toHexString(Build.MODEL.length % 16)[0],
        Integer.toHexString(Build.PRODUCT.length % 16)[0],
        Integer.toHexString(Build.TAGS.length % 16)[0],
        Integer.toHexString(Build.TYPE.length % 16)[0],
        Integer.toHexString(Build.USER.length % 16)[0]
    ).toMd5().uppercase()

    fun getAndroidVersionName(): String {
        if (VERSION_CODES.isNullOrEmpty()) return DEFAULT_VALUE

        for (versionCode in VERSION_CODES) {
            val versionName = versionCode.name
            var fieldValue = -1

            try {
                fieldValue = versionCode.getInt(Any())
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (fieldValue == Build.VERSION.SDK_INT) return versionName
        }

        try {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) packageManager.getPackageInfo(
                packageName, PackageManager.PackageInfoFlags.of(0)
            ).versionName
            else packageManager.getPackageInfo(packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return DEFAULT_VALUE
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        return network != null
    }

    @Suppress("DEPRECATION")
    fun getNetworkName(): String {
        if (!isNetworkAvailable()) return DEFAULT_VALUE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (CONNECTIVITY_MANAGER == null) {
                val request =
                    NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                        .build()
                CONNECTIVITY_MANAGER = getSystemService(ConnectivityManager::class.java)
                val networkCallback =
                    object : ConnectivityManager.NetworkCallback(FLAG_INCLUDE_LOCATION_INFO) {
                        override fun onCapabilitiesChanged(
                            network: Network, networkCapabilities: NetworkCapabilities
                        ) {
                            super.onCapabilitiesChanged(network, networkCapabilities)
                            val wifiInfo = networkCapabilities.transportInfo as WifiInfo?
                            CURRENT_NETWORK = wifiInfo?.ssid?.toSsidFormat() ?: DEFAULT_VALUE
                        }
                    }
                CONNECTIVITY_MANAGER?.registerNetworkCallback(request, networkCallback)
            }
        } else {
            val manager = getSystemService(WIFI_SERVICE) as WifiManager?
            val info = manager?.connectionInfo
            CURRENT_NETWORK =
                info?.takeIf { it.supplicantState == SupplicantState.COMPLETED }?.ssid?.toSsidFormat()
                    ?: DEFAULT_VALUE
        }
        return CURRENT_NETWORK
    }

    fun getNetworkOperator(): String {
        val manager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager?
        return if (manager?.simState == TelephonyManager.SIM_STATE_READY) manager.networkOperatorName
        else DEFAULT_VALUE
    }

    fun getIpAddress(): String {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE)

        if (connectivityManager is ConnectivityManager) {
            try {
                val link: LinkProperties =
                    connectivityManager.getLinkProperties(connectivityManager.activeNetwork) as LinkProperties
                return if (link.linkAddresses.isEmpty()) DEFAULT_IP_ADDRESS
                else link.linkAddresses[0].toString()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return DEFAULT_IP_ADDRESS
    }

    companion object {
        const val DEFAULT_VALUE = "?"
        const val DEFAULT_IP_ADDRESS = "192.168.1.1"
        const val UNKNOWN_SSID = "<unknown ssid>"
        val VERSION_CODES: Array<Field>? = Build.VERSION_CODES::class.java.fields
        private var CONNECTIVITY_MANAGER: ConnectivityManager? = null
        private var CURRENT_NETWORK: String = DEFAULT_VALUE
    }
}