package com.arstagaev.bleservertest1

//class ServerBleManager(private val context: Context,
//                       private val dataExchangeManager: BleManager.BleDataExchangeManager,
//                       private val callbackHandler: Handler) {
//
//    private val gson = Gson()
//
//    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
//    private val adapter: BluetoothAdapter? get() = BluetoothAdapter.getDefaultAdapter()
//    private val advertiser get() = adapter?.bluetoothLeAdvertiser
//
//    var gattServer: BluetoothGattServer? = null
//
//    private val readCharacteristic = BluetoothGattCharacteristic(
//        UUID.fromString(BleUuid.READ_UUID),
//        BluetoothGattCharacteristic.PROPERTY_READ,
//        BluetoothGattCharacteristic.PERMISSION_READ
//    )
//
//    private val writeCharacteristic = BluetoothGattCharacteristic(
//        UUID.fromString(BleUuid.WRITE_UUID),
//        BluetoothGattCharacteristic.PROPERTY_WRITE,
//        BluetoothGattCharacteristic.PERMISSION_WRITE
//    )
//
//    private val bleService = BluetoothGattService(
//        UUID.fromString(BleUuid.SERVICE_UUID),
//        BluetoothGattService.SERVICE_TYPE_PRIMARY
//    ).apply {
//        addCharacteristic(readCharacteristic)
//        addCharacteristic(writeCharacteristic)
//    }
//
//    private val advertiseSettings = AdvertiseSettings.Builder()
//        .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
//        .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
//        .setTimeout(0)
//        .setConnectable(true)
//        .build()
//
//    private val advertiseData = AdvertiseData.Builder()
//        .addServiceUuid(ParcelUuid.fromString(BleUuid.SERVICE_UUID))
//        .build()
//
//    private val advertiseCallback = object : AdvertiseCallback() { }
//
//    private val serverCallback = object : BluetoothGattServerCallback() {
//
//        override fun onCharacteristicReadRequest(device: BluetoothDevice?,
//                                                 requestId: Int,
//                                                 offset: Int,
//                                                 characteristic: BluetoothGattCharacteristic?) {
//            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
//            // send data to remote BLE client
////            callbackHandler.post {
////                val bleData = dataExchangeManager.getBleData()
////                val stringValue = gson.toJson(bleData)
////                val value = stringValue?.toByteArray()
////                characteristic?.value = value
////                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value)
////            }
//        }
//
//        override fun onCharacteristicWriteRequest(device: BluetoothDevice?,
//                                                  requestId: Int,
//                                                  characteristic: BluetoothGattCharacteristic?,
//                                                  preparedWrite: Boolean,
//                                                  responseNeeded: Boolean,
//                                                  offset: Int,
//                                                  value: ByteArray?) {
//            super.onCharacteristicWriteRequest(
//                device,
//                requestId,
//                characteristic,
//                preparedWrite,
//                responseNeeded,
//                offset,
//                value
//            )
//            // receive data from remote ble client
//            callbackHandler.post {
//                characteristic?.value = value
//                val bleData =
//                    gson.fromJson(characteristic?.getStringValue(offset), BleData::class.java)
//                if (bleData != null) dataExchangeManager.onDataReceived(bleData)
//                if (responseNeeded) {
//                    gattServer?.sendResponse(
//                        device,
//                        requestId,
//                        BluetoothGatt.GATT_SUCCESS,
//                        offset,
//                        null
//                    )
//                }
//            }
//        }
//    }
//
//    fun start() {
//        openServer()
//        startAdvertising()
//    }
//
//    fun stop() {
//        stopAdvertising()
//        closeServer()
//    }
//
//    private fun openServer() {
//        if (adapter.isBleOn && gattServer == null) {
//            gattServer = bluetoothManager.openGattServer(context, serverCallback)
//            gattServer?.addService(bleService)
//        }
//    }
//
//    private fun closeServer() {
//        gattServer?.clearServices()
//        gattServer?.close()
//        gattServer = null
//    }
//
//    private fun startAdvertising() {
//        if (adapter.isBleOn) {
//            advertiser?.startAdvertising(advertiseSettings, advertiseData, advertiseCallback)
//        }
//    }
//
//    private fun stopAdvertising() {
//        if (adapter.isBleOn) {
//            advertiser?.stopAdvertising(advertiseCallback)
//        }
//    }
//}