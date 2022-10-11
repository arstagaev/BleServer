package com.arstagaev.bleservertest1

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.arstagaev.bleservertest1.ui.theme.BleServerTest1Theme
import java.util.*

@SuppressLint("MissingPermission")
class MainActivity : ComponentActivity() {

    val TAG = "ServerActivity"

    lateinit var mBluetoothManger: BluetoothManager
    lateinit var mBluetoothAdapter: BluetoothAdapter
    lateinit var mBluetoothLeAdvertiser: BluetoothLeAdvertiser
    lateinit var mGattServer: BluetoothGattServer
    val mAdvertiseCallback = AdvertiseCallback()

    var mDevices = mutableListOf<BluetoothDevice>()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        if (!hasPermission(Manifest.permission.BLUETOOTH_ADVERTISE)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestPermission(Manifest.permission.BLUETOOTH_ADVERTISE,4)
            }

        }
        setContent {
            BleServerTest1Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Button(onClick = {
                        if (isAllPermissionsEnabled()) {

                            mBluetoothManger = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager


                            mBluetoothAdapter = mBluetoothManger.adapter
                            mBluetoothAdapter.setName("SpaceX")
                            checker()
                        }

                    }) {
                        Text(text = "START! Become a BLE Server with one characteristic")
                    }
                }
            }
        }
    }

    fun checker() {
        // Check if Bluetooth is enable
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled) {
            var enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivity(enableBtIntent)
            finish()
            return
        }

        // Check if Bluetooth LE is supported
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            finish()
            return
        }

        // Check if Bluetooth Advertiser is supported
        if (!mBluetoothAdapter.isMultipleAdvertisementSupported) {
            finish()
            return
        }
        mBluetoothLeAdvertiser = mBluetoothAdapter.bluetoothLeAdvertiser

        var gattServerCallback = GattServerCallback()
        mGattServer = mBluetoothManger.openGattServer(this, gattServerCallback)
        setupServer()
        startAdvertising()
    }
    override fun onResume() {
        super.onResume()

    }

    private fun startAdvertising() {
        if (mBluetoothLeAdvertiser == null) {
            return
        }

        var setting = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
            .setConnectable(true)
            .setTimeout(0)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
            .build()

        var parcelUuid = ParcelUuid(Constants.SERVICE_UUID)
        var data = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(parcelUuid)
            .build()

        mBluetoothLeAdvertiser.startAdvertising(setting, data, mAdvertiseCallback)

    }


    private fun setupServer() {
        var service = BluetoothGattService(Constants.SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        var writeCharacteristic = BluetoothGattCharacteristic(
            Constants.CHARACTERISTIC_ECHO_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        service.addCharacteristic(writeCharacteristic)
        mGattServer.addService(service)

    }

    inner class GattServerCallback: BluetoothGattServerCallback() {

        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
            if (newState == BluetoothProfile.STATE_CONNECTED && device != null) {
                mDevices.add(device)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mDevices.remove(device)
            }
        }

        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice?,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic?,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            super.onCharacteristicWriteRequest(
                device,
                requestId,
                characteristic,
                preparedWrite,
                responseNeeded,
                offset,
                value
            )
            if (characteristic != null) {
                if (characteristic.uuid.equals(Constants.CHARACTERISTIC_ECHO_UUID)) {
                    mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
                    if (value != null) {
                        var messageString= String(value, charset("UTF-8"))
                        Log.d(TAG, "$messageString   ${value.decodeToString()}")
                        var length: Int = value.size
                        var reversed = ByteArray(length)
                        for (i in 0..(length - 1)) {
                            reversed[i] = value[length - (i + 1)]
                        }
                        characteristic.value = reversed
                        for (device in mDevices) {
                            mGattServer.notifyCharacteristicChanged(device, characteristic, false)
                        }
                    }
                }
            }
        }
    }

    inner class AdvertiseCallback: android.bluetooth.le.AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            Log.d(TAG,"Peripheral advertising started.")
        }
        override fun onStartFailure(errorCode: Int) {
            Log.d(TAG, "Peripheral advertising failed: $errorCode")
        }
    }

    override fun onPause() {
        super.onPause()
        stopAdvertising()
        stopServer()
    }

    private fun stopAdvertising() {
        if (mBluetoothLeAdvertiser != null) {
            mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback)
        }
    }

    private fun stopServer() {
        if (mGattServer != null) {
            mGattServer.close()
        }
    }


    private fun isAllPermissionsEnabled(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                requestPermission(Manifest.permission.BLUETOOTH_CONNECT,1)
                return false
            }
            if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
                requestPermission(Manifest.permission.BLUETOOTH_SCAN,2)
                return false
            }
            if (!hasPermission(Manifest.permission.BLUETOOTH_ADVERTISE)) {
                requestPermission(Manifest.permission.BLUETOOTH_ADVERTISE,4)
                return false
            }
        }

        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION,3)
            return false
        }

        return true
    }
}

fun Activity.requestPermission(permission: String, requestCode: Int) {
    ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
}

fun Context.hasPermission(permissionType: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permissionType) ==
            PackageManager.PERMISSION_GRANTED
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

class Constants {
    companion object {
        var SERVICE_STRING = "7D2EA28A-F7BD-485A-BD9D-92AD6ECFE93E"
        val SERVICE_UUID = UUID.fromString(SERVICE_STRING)

        var CHARACTERISTIC_ECHO_STRING = "7D2EBAAD-F7BD-485A-BD9D-92AD6ECFE93E"
        var CHARACTERISTIC_ECHO_UUID = UUID.fromString(CHARACTERISTIC_ECHO_STRING)

        var CHARACTERISTIC_TIME_STRING = "7D2EDEAD-F7BD-485A-BD9D-92AD6ECFE93E"
        var CHARACTERISTIC_TIME_UUID = UUID.fromString(CHARACTERISTIC_TIME_STRING)
        var CLIENT_CONFIGURATION_DESCRIPTOR_STRING = "00002902-0000-1000-8000-00805f9b34fb"
        var CLIENT_CONFIGURATION_DESCRIPTOR_UUID = UUID.fromString(CLIENT_CONFIGURATION_DESCRIPTOR_STRING)

        val CLIENT_CONFIGURATION_DESCRIPTOR_SHORT_ID = "2902"

        val SCAN_PERIOD: Long = 5000
    }
}