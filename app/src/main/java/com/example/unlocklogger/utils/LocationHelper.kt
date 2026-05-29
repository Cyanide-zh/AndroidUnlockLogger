object LocationHelper {
    private const val TIMEOUT_MS = 40000L //单位毫秒

    @SuppressLint("MissingPermission")
    fun requestSingleLocation(context: Context, callback: (String) -> Unit) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val handler = Handler(Looper.getMainLooper())

        // 🚨 调试日志：确认 GPS 是否真的开启了
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        Log.d("LocationHelper", "GPS 状态: $isGpsEnabled")

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                handler.removeCallbacksAndMessages(null) // 成功回调，取消超时
                val result = "Lat=${location.latitude}, Lon=${location.longitude}, Acc=${location.accuracy}"
                Log.i("LocationHelper", "GPS 定位成功: $result")
                callback(result)
                locationManager.removeUpdates(this)
            }
            // 🚨 添加状态监控
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                Log.d("LocationHelper", "GPS 状态变更: $status")
            }
            override fun onProviderDisabled(provider: String) {
                Log.e("LocationHelper", "GPS 被用户关闭")
                callback("Error: GPS disabled")
            }
        }

        val timeoutRunnable = Runnable {
            // 🚨 这里必须输出，如果没有输出，说明 Handler 被系统掐断了
            Log.e("LocationHelper", "【严重】GPS 超时触发")
            locationManager.removeUpdates(listener)
            callback("Error: GPS Timeout")
        }
        
        handler.postDelayed(timeoutRunnable, TIMEOUT_MS)
        
        try {
            Log.d("LocationHelper", "开始请求纯 GPS 定位...")
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, listener, null)
        } catch (e: Exception) {
            Log.e("LocationHelper", "请求异常: ${e.message}")
        }
    }
}
