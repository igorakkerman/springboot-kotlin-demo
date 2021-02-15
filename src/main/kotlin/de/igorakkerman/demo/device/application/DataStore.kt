import de.igorakkerman.demo.device.application.Device
import de.igorakkerman.demo.device.application.DeviceId

// Facade for persistence operations
interface DataStore {
    fun createDevice(device: Device)
    fun findDeviceById(deviceId: DeviceId): Device?
    fun updateDevice(device: Device)
    fun findAllDevices(): List<Device>
}