package com.memoeslink.manager

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import junit.framework.Assert.assertNotNull
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class DeviceInstrumentedTest {
    @Rule
    @JvmField
    var permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_NETWORK_STATE,
            android.Manifest.permission.CHANGE_NETWORK_STATE,
            android.Manifest.permission.ACCESS_WIFI_STATE,
            android.Manifest.permission.CHANGE_WIFI_STATE
        )

    @Test
    fun getInfo_getAndroidId() {
        val info = getDevice().getInfo(InformationType.ANDROID_ID)
        println("getInfo_getAndroidId: $info")
        assertNotEquals(Device.DEFAULT_VALUE, info)
    }

    @Test
    fun getInfo_getAndroidVersion() {
        val info = getDevice().getInfo(InformationType.ANDROID_VERSION)
        println("getInfo_getAndroidVersion: $info")
        assertNotEquals(Device.DEFAULT_VALUE, info)
    }

    @Test
    fun getInfo_getBrand() {
        val info = getDevice().getInfo(InformationType.BRAND)
        println("getInfo_getBrand: $info")
        assertNotEquals(Device.DEFAULT_VALUE, info)
    }

    @Test
    fun getInfo_getBrandAndModel() {
        val info = getDevice().getInfo(InformationType.BRAND_AND_MODEL)
        println("getInfo_getBrandAndModel: $info")
        assertNotEquals(Device.DEFAULT_VALUE, info)
    }

    @Test
    fun getInfo_getManufacturerAndModel() {
        val info = getDevice().getInfo(InformationType.MANUFACTURER_AND_MODEL)
        println("getInfo_getManufacturerAndModel: $info")
        assertNotEquals(Device.DEFAULT_VALUE, info)
    }

    @Test
    fun getInfo_getProduct() {
        val info = getDevice().getInfo(InformationType.PRODUCT)
        println("getInfo_getProduct: $info")
        assertNotEquals(Device.DEFAULT_VALUE, info)
    }

    @Test

    fun getInfo_getNetworkName() {
        val info = getDevice().getInfo(InformationType.NETWORK_NAME)
        println("getInfo_getNetworkName: $info")
        assertNotEquals(Device.DEFAULT_VALUE, info)
    }

    @Test
    fun getInfo_getNetworkOperator() {
        val info = getDevice().getInfo(InformationType.NETWORK_OPERATOR)
        println("getInfo_getNetworkOperator: $info")
        assertNotEquals(Device.DEFAULT_VALUE, info)
    }

    @Test
    fun getInfo_getIpAddress() {
        val info = getDevice().getInfo(InformationType.IP_ADDRESS)
        println("getInfo_getIpAddress: $info")
        assertNotEquals(Device.DEFAULT_VALUE, info)
    }

    @Test
    fun getMaskedAndroidId() {
        val info = getDevice().getMaskedAndroidId()
        println("getMaskedAndroidId: $info")
        assertNotNull(info)
        assertNotEquals(Device.DEFAULT_VALUE, info)
    }

    @Test
    fun getAndroidId() {
        val info = getDevice().getAndroidId()
        println("getAndroidId: $info")
        assertNotNull(info)
        assertNotEquals(Device.DEFAULT_VALUE, info)
    }

    @Test
    fun getPseudoId() {
        val info = getDevice().getPseudoId()
        println("getPseudoId: $info")
        assertNotNull(info)
        assertNotEquals(Device.DEFAULT_VALUE, info)
    }

    @Test
    fun getAndroidVersionName() {
        val info = getDevice().getAndroidVersionName()
        println("getAndroidVersionName: $info")
        assertNotNull(info)
        assertNotEquals(Device.DEFAULT_VALUE, info)
    }

    @Test
    fun getNetworkName() {
        val info = getDevice().getNetworkName()
        println("getNetworkName: $info")
        assertNotNull(info)
        assertNotEquals(Device.DEFAULT_VALUE, info)
        assertNotEquals(Device.UNKNOWN_SSID, info)
    }

    @Test
    fun getNetworkOperator() {
        val info = getDevice().getNetworkOperator()
        println("getNetworkOperator: $info")
        assertNotNull(info)
        assertNotEquals(Device.DEFAULT_VALUE, info)
    }

    @Test
    fun getLocalIpAddress() {
        val info = getDevice().getIpAddress()
        println("getLocalIpAddress: $info")
        assertNotNull(info)
        assertNotEquals(Device.DEFAULT_VALUE, info)
    }

    fun getDevice(): Device = Device(InstrumentationRegistry.getInstrumentation().targetContext)
}