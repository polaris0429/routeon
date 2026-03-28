package com.example.routeon

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.routeon.databinding.ActivityMainBinding
import com.kakaomobility.knsdk.KNSDK

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🚨 핵심 해결책: 화면(XML)을 그리기 '전'에 KNSDK 설치(install)를 무조건 먼저 해야 합니다!
        KNSDK.install(application, "$filesDir/knsdk")

        // 💡 SDK가 설치되었으니 이제 안심하고 화면을 그립니다.
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 화면이 다 그려지면 권한을 확인합니다.
        checkLocationPermission()
    }

    // 1. 위치 권한 확인 함수
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없으면 팝업을 띄워 요청
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // 이미 권한이 있으면 SDK 인증(초기화) 시작
            initKakaoNaviSDK()
        }
    }

    // 권한 요청 결과 처리
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 사용자가 [허용]을 눌렀을 때
                initKakaoNaviSDK()
            } else {
                // 사용자가 [거부]를 눌렀을 때
                Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_LONG).show()
                finish() // 앱 종료
            }
        }
    }

    // 2. 카카오 내비 SDK 인증(초기화) 함수
    private fun initKakaoNaviSDK() {
        KNSDK.initializeWithAppKey(
            aAppKey = "b57bc6d46e97f480deecdd3a8e4cd754",
            aClientVersion = "1.0",
            aAppUserId = "test_user",
            aLangType = com.kakaomobility.knsdk.KNLanguageType.KNLanguageType_KOREAN,
            aCompletion = { error ->
                if (error != null) {
                    Log.e("KNSDK", "🚨 초기화 실패: ${error.msg}")
                } else {
                    Log.d("KNSDK", "✅ 카카오 내비 SDK 인증 완벽 성공!")
                    startSafeDriving()
                }
            }
        )
    }

    // 3. 안전운행 모드 실행 함수
    private fun startSafeDriving() {
        runOnUiThread {
            val naviView = binding.naviView
            val guidance = KNSDK.sharedGuidance()

            if (guidance != null) {
                naviView.mapComponent.mapView.isVisibleTraffic = true
                naviView.initWithGuidance(
                    guidance,
                    null, // 목적지가 없으면 안전운행 모드!
                    com.kakaomobility.knsdk.KNRoutePriority.KNRoutePriority_Recommand,
                    com.kakaomobility.knsdk.KNRouteAvoidOption.KNRouteAvoidOption_None.value
                )
            } else {
                Log.e("KNSDK", "🚨 주행 엔진을 불러올 수 없습니다.")
            }
        }
    }
}