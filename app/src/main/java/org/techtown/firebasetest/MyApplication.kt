package org.techtown.firebasetest

import androidx.multidex.MultiDexApplication
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


//앱 전역에서 파이어베이스 인증 객체를 이용하고자 Application을 상속받은 클래스
class MyApplication : MultiDexApplication(){        //MultiDexApplication 은 함수 개수의 제한을 풀어줌
    companion object{
        lateinit var auth : FirebaseAuth
        var email : String? = null
        fun checkAuth() : Boolean{                      //입력한 이메일이 인증 되어있는 이메일인지 확인하는 메소드
            val currentUser = auth.currentUser
            return currentUser?.let{                    //let 은 null이 아니면 실행
                email = currentUser.email
                if(currentUser.isEmailVerified){
                    true
                } else{
                    false
                }
            } ?: let{
                false
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        auth = Firebase.auth
    }
}