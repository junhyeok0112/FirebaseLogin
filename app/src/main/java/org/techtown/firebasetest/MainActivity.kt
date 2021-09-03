package org.techtown.firebasetest

import android.content.ClipData
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ch20_firebase.model.ItemData
import com.example.ch20_firebase.util.myCheckPermission
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import org.techtown.firebasetest.databinding.ActivityMainBinding
import org.techtown.firebasetest.recycler.MyAdapter

class MainActivity : AppCompatActivity() {

    lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        myCheckPermission(this)         //파일 접근 권한 허용

        //+ 버튼 눌렀을 때 사진 추가할 수 있는 액티비티 실행
        binding.addFab.setOnClickListener {
            if(MyApplication.checkAuth()){      //로그인되어 있으면 추가 화면 실행
                startActivity(Intent(this,AddActivity::class.java))
            } else{
                Toast.makeText(this , "인증을 먼저 해주세요", Toast.LENGTH_LONG).show()
            }
        }

    }

    override fun onStart() {
        super.onStart()
        if(!MyApplication.checkAuth()){
            binding.logoutTextView.visibility = View.VISIBLE
            binding.mainRecyclerView.visibility = View.GONE
        } else{
            binding.logoutTextView.visibility = View.GONE
            binding.mainRecyclerView.visibility = View.VISIBLE
            makeRecycler()
        }

    }

    //옵션 메뉴 생성 -> 공부해야할것
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main , menu)
        return super.onCreateOptionsMenu(menu)
    }

    //옵션 메뉴 클릭했을 때 로그인 화면 띄우기
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        startActivity(Intent(this, AuthActivity::class.java))
        return super.onOptionsItemSelected(item)
    }

    fun makeRecycler(){

        MyApplication.db.collection("new")
            .get()
            .addOnSuccessListener {
                //news 컬렉션의 모든 문서들의 리스트를 가져옴 (it 으로 나타남) 그 문서들을 각각 data로 표현하고
                //data(문서)를 ItemData로 객체호 하여서 저장 ( 문서 가져와서 객체에 담기)
                var items : ArrayList<ItemData> = ArrayList()
                for(data in it){
                    val item:ItemData = data.toObject(ItemData::class.java)
                    item.docId = data.id            //문서 아이디도 저장 -> 이 아이디로 스토리지에서 이미지 불러옴
                    items.add(item)
                }
                binding.mainRecyclerView.layoutManager = LinearLayoutManager(this)
                binding.mainRecyclerView.adapter = MyAdapter(this,items)
                Log.d("jun", "데이터 불러오기 완료 : ${items.size}")
            }.addOnFailureListener {
                Log.d("jun" , "데이터 불러오기 실패" , it)
                Toast.makeText(this,"서버로부터 데이터를 불러오지 못했습니다" ,Toast.LENGTH_LONG).show()
            }


    }
}