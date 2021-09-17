package org.techtown.firebasetest

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


//서비스 컴포넌트에서 FCM 토큰과 메시지를 가져오는 기능 작성
class MyFirebaseMessageService : FirebaseMessagingService() {
    override fun onMessageReceived(p0: RemoteMessage) { //FCM서버로부터 메시지가 전달될 때 자동으로 후촐, 매개변수 객체의 data 프로퍼티로 메시지 얻을 수 있음
        super.onMessageReceived(p0)
        Log.d("jun" , "fcm notification....... ${p0.notification}")
        Log.d("jun" , "fcm data....... ${p0.data}")
    }

    override fun onNewToken(p0: String) {           //FCM서버로부터 토큰이 전달될 때 자동으로 호출
        super.onNewToken(p0)
        Log.d("jun", "fcm token..... ${p0}")
    }
}