import de.igorakkerman.demo.deviceconfig.application.Device
import de.igorakkerman.demo.deviceconfig.application.DeviceId

class DeviceService(private val dataStore: DataStore) {
    fun createDevice(device: Device) = dataStore.createDevice(device)
    fun findDeviceById(deviceId: DeviceId): Device? = dataStore.findDeviceById(deviceId)
    fun updateDevice(device: Device) = dataStore.updateDevice(device)
    fun findAllDevices(): List<Device> = dataStore.findAllDevices()
}
