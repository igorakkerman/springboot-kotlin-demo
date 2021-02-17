import de.igorakkerman.demo.deviceconfig.application.Device
import de.igorakkerman.demo.deviceconfig.application.DeviceId

// Facade for persistence operations
interface DataStore {
    fun createDevice(device: Device)
    fun findDeviceById(deviceId: DeviceId): Device?
    fun updateDevice(device: Device)
    fun findAllDevices(): List<Device>
}

class NoSuchItemException(deviceId: DeviceId): RuntimeException(deviceId)
class ItemAreadyExistsException(deviceId: DeviceId): RuntimeException(deviceId)