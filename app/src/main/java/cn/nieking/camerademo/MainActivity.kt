package cn.nieking.camerademo

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.widget.Toast
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Camera2Demo
 */
class MainActivity : AppCompatActivity() {

    private val mRxPermissions by lazy { RxPermissions(this) }
    private val mCameraManager by lazy { getSystemService(Context.CAMERA_SERVICE) as CameraManager }
    private var mInfoText: StringBuffer = StringBuffer("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mRxPermissions.request(Manifest.permission.CAMERA)
                .subscribe { granted ->
                    if (granted) {
                        if (hasCamera2(this)) {
                            inquireCamera()
                            openCamera()
                        }
                    } else {
                        Toast.makeText(this, "权限被拒绝！", Toast.LENGTH_LONG).show()
                    }
                }
    }

    /**
     * 打开相机
     */
    private fun openCamera() = try {
        mCameraManager.openCamera(
                mCameraManager.cameraIdList[0],
                object : CameraDevice.StateCallback() {
                    override fun onOpened(camera: CameraDevice?) {
                        // 获取相机设备
                        val mCameraDevice = camera
                    }

                    override fun onDisconnected(camera: CameraDevice?) {
                        // 关闭相机设备
                        camera?.close()
                    }

                    override fun onError(camera: CameraDevice?, error: Int) {
                        Log.e("CAMERA", error.toString())
                    }

                },
                null
        )
    } catch (e: SecurityException) {
        e.printStackTrace()
    }

    /**
     * 查询相机
     */
    private fun inquireCamera() {
        // 1.获取相机管理器
//        val mCameraManager = getSystemService(android.content.Context.CAMERA_SERVICE) as CameraManager
        try {
            // 2.获取相机列表
            val ids = mCameraManager.cameraIdList
            // 3.获取相机数量
            val countOfCameras = ids.size
            // 4.获取每个 ID 对应的镜头参数
            for (id in ids) {
                // 获取当前id对应的镜头设备参数
                val characteristics = mCameraManager.getCameraCharacteristics(id)
                // 获取当前镜头朝向（镜头朝向是角度数值，即与屏幕所成角度，前置90，后置270）
                val orientation = characteristics.get(CameraCharacteristics.LENS_FACING)
                // 判断当前镜头是前置镜头还是后置镜头
                if (orientation == CameraCharacteristics.LENS_FACING_FRONT) { // 前置镜头
                    // 前置镜头id
                    val faceFrontCameraId = id
                    // 前置镜头方向
                    val faceFrontCameraOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)
                    // 前置镜头参数
                    val frontCameraCharacteristics = characteristics

                    mInfoText.append("\n$faceFrontCameraId\n$faceFrontCameraOrientation\n$frontCameraCharacteristics\n")
                } else { // 后置镜头
                    // 后置镜头id
                    val facebackCameraId = id
                    // 后置镜头方向
                    val facebackCameraOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)
                    // 后置镜头参数
                    val backCameraCharacteristics = characteristics

                    mInfoText.append("\n$facebackCameraId\n$facebackCameraOrientation\n$backCameraCharacteristics\n")
                }
            }
            mInfoTv.text = mInfoText.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 判断是否支持 Camera2
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun hasCamera2(context: Context?): Boolean {
        if (context == null) return false
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return false
        try {
//            val mCameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val idList = mCameraManager.cameraIdList
            var notFull = true
            if (idList.isEmpty()) {
                notFull = false
            } else {
                for (str in idList) {
                    if (str == null || str.trim().isEmpty()) {
                        notFull = false
                        break
                    }
                    val characteristics = mCameraManager.getCameraCharacteristics(str)
                    val supportLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
                    if (supportLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
                        notFull = false
                        break
                    }
                }
            }
            return notFull
        } catch (ignore: Throwable) {
            return false
        }
    }
}
