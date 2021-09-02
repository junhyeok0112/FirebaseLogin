package org.techtown.firebasetest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import org.techtown.firebasetest.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {

    lateinit var binding : ActivityAuthBinding
    lateinit var activityLauncher : ActivityResultLauncher<Intent>      //액티비티에서 넘어온 결과를 처리하기 위해 만든 변수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setLauncher()                       //launcher 초기화

        if(MyApplication.checkAuth()){      //첫 화면에서 만약 MyApplication에서 등록된 user를 가져올 수 있으면 login 아니면 logout
            changeVisibility("login")
        } else{
            changeVisibility("logout")
        }

        binding.logoutBtn.setOnClickListener {
            // 로그아웃
            MyApplication.auth.signOut()
            MyApplication.email = null
            changeVisibility("logout")
        }

        //구글 로그인 메소드 공부
        binding.googleLoginBtn.setOnClickListener {
            //구글로그인
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.defalut_web_client_id))
                .requestEmail()
                .build()

            //구글의 인증 관리 앱 실행
            val signIntent = GoogleSignIn.getClient(this,gso).signInIntent
            activityLauncher.launch(signIntent)

        }


        binding.goSignInBtn.setOnClickListener{
            //회원가입 누르면 회원가입 폼으로 이동
            changeVisibility("signin")
        }

        binding.signBtn.setOnClickListener {
            //이메일 , 비밀번호 회원가입
            val email:String = binding.authEmailEditView.text.toString()
            val password:String = binding.authPasswordEditView.text.toString()

            MyApplication.auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this){task->
                    binding.authEmailEditView.text.clear()
                    binding.authPasswordEditView.text.clear()
                    if(task.isSuccessful){
                        //비밀번호는 최소 6자 이상
                        //메일 보내기
                        MyApplication.auth.currentUser?.sendEmailVerification()
                            ?.addOnCompleteListener{sendTask->
                                if(sendTask.isSuccessful){
                                    Toast.makeText(this, "회원 가입에 성공했습니다. 메일을 확인해주세요" , Toast.LENGTH_SHORT).show()
                                    changeVisibility("logout")
                                }else{
                                    Toast.makeText(this, "메일 전송 실패" , Toast.LENGTH_SHORT).show()
                                    changeVisibility("logout")
                                }
                            }
                    } else{
                        Toast.makeText(this, "회원 가입 실패패" , Toast.LENGTH_SHORT).show()
                        changeVisibility("logout")
                    }

                }
        }

        binding.loginBtn.setOnClickListener {
            //이메일 / 비밀번호 로그인
            val email:String = binding.authEmailEditView.text.toString()
            val password:String = binding.authPasswordEditView.text.toString()

            MyApplication.auth.signInWithEmailAndPassword(email , password)         //자동으로 auth의 currentUser가 셋팅됨
                .addOnCompleteListener(this){task->
                    binding.authEmailEditView.text.clear()
                    binding.authPasswordEditView.text.clear()
                    if(task.isSuccessful){
                        if(MyApplication.checkAuth()){
                            //로그인 성공
                            MyApplication.email = email
                            changeVisibility("login")
                        }else{
                            Toast.makeText(this,"전송 된 메일로 이메일 인증이 되지 않았습니다" , Toast.LENGTH_SHORT).show()
                            changeVisibility("logout")
                        }
                    } else{
                        Toast.makeText(this , "로그인 실패 " , Toast.LENGTH_SHORT).show()
                        changeVisibility("logout")
                    }

                }

        }


    }

    fun changeVisibility(mode: String){
        if(mode === "login"){
            binding.run {
                authMainTextView.text = "${MyApplication.email} 님 반갑습니다."
                logoutBtn.visibility= View.VISIBLE
                goSignInBtn.visibility= View.GONE
                googleLoginBtn.visibility= View.GONE
                authEmailEditView.visibility= View.GONE
                authPasswordEditView.visibility= View.GONE
                signBtn.visibility= View.GONE
                loginBtn.visibility= View.GONE
            }

        }else if(mode === "logout"){
            binding.run {
                authMainTextView.text = "로그인 하거나 회원가입 해주세요."
                logoutBtn.visibility = View.GONE
                goSignInBtn.visibility = View.VISIBLE
                googleLoginBtn.visibility = View.VISIBLE
                authEmailEditView.visibility = View.VISIBLE
                authPasswordEditView.visibility = View.VISIBLE
                signBtn.visibility = View.GONE
                loginBtn.visibility = View.VISIBLE
            }
        }else if(mode === "signin"){
            binding.run {
                logoutBtn.visibility = View.GONE
                goSignInBtn.visibility = View.GONE
                googleLoginBtn.visibility = View.GONE
                authEmailEditView.visibility = View.VISIBLE
                authPasswordEditView.visibility = View.VISIBLE
                signBtn.visibility = View.VISIBLE
                loginBtn.visibility = View.GONE
            }
        }
    }



    fun setLauncher(){
        activityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try{
                val account = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider
                    .getCredential(account.idToken , null)
                MyApplication.auth.signInWithCredential(credential)
                    .addOnCompleteListener(this){ task->
                        if(task.isSuccessful){
                            //구글 로그인 성공
                            MyApplication.email = account.email
                            changeVisibility("login")
                            Log.d("구글 로그인 성공" , "로그인 성공 : ${MyApplication.email}")
                        } else{
                            //구글 로그인 실패
                            changeVisibility("logout")
                            Log.d("구글 로그인 실패" , "로그인 실패 ")
                        }

                    }
            } catch (e:ApiException){
                Log.d("구글 로그인 실패 예외 처리" , "로그인 실패 예외처리 " , e)
                changeVisibility("logout")
            }

        }
    }
}