package org.techtown.firebasetest

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.ch20_firebase.util.dateToString
import com.google.firebase.firestore.DocumentReference
import org.techtown.firebasetest.databinding.ActivityAddBinding
import java.io.File
import java.util.*

class AddActivity : AppCompatActivity() {

    lateinit var binding:ActivityAddBinding
    lateinit var registerLauncher: ActivityResultLauncher<Intent>       //startActivityForResult 대신
    lateinit var filePath:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setLauncher()

    }

    //메뉴 하면 생성
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add , menu)            //메뉴 화면 inflate 시키기
        return super.onCreateOptionsMenu(menu)
    }

    //옵션 클릭시 사진 가져오기 or 저장
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.menu_add_gallery){               //사진 가져오기 버튼이면
            val intent = Intent(Intent.ACTION_PICK)             //갤러리에서 사진 가져오기 실행
            intent.setDataAndType(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/*"
            )
            registerLauncher.launch(intent)                     //이미지 불러와서 이미지 뷰에 로딩시켜줌
        } else if(item.itemId == R.id.menu_add_save){           //저장 버튼을 눌렀을 경우
            if(binding.addImageView.drawable !== null && binding.addEditView.text.isNotEmpty()){
                //이미지와 텍스트 모두 입력했으면
                //업로드 하기전에 FireStore에 데이터 저장 후 document Id 값으로 업로드
                saveStore()
            } else{
                Toast.makeText(this,"이미지와 텍스트 모두 입력해주세요" , Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //다른 액티비티에서 받아온 결과 처리
    fun setLauncher(){          //Glide 다음에 공부해보기
        //이미지 뷰에 가져온 이미지 로딩
        registerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->

                Glide
                    .with(getApplicationContext())
                    .load(result.data?.data)
                    .apply(RequestOptions().override(250, 200))
                    .centerCrop()
                    .into(binding.addImageView)

                val cursor = contentResolver.query(result.data?.data as Uri,
                    arrayOf<String>(MediaStore.Images.Media.DATA), null, null, null);
                cursor?.moveToFirst().let {
                    filePath=cursor?.getString(0) as String
                }
        }
    }

    fun saveStore(){        //입력된 정보를 store에 올리기
        val data = mapOf(
            "email" to MyApplication.email,          //MyApplication에 있는 현재 유저의 이메일 가져옴
            "content" to binding.addEditView.text.toString(),   //입력된 텍스트 정보 저장
            "data" to dateToString(Date())          //현재 날짜
        )

        MyApplication.db.collection("new")
            .add(data)
            .addOnSuccessListener {
                //collection 새로 생성되면서 document도 생성하면서 안에 data 넣음 documentRef 리턴
                //store에 먼저 저장 후 스토리지에 documentID 이용해서 이미지 저장
                uploadImage(it.id)
            }.addOnFailureListener {
                Log.d("jun", "data 저장 실패" ,it)
            }
    }

    fun uploadImage(docId: String){     //문서 id 전달 받아서 올려야함
        //스토리지에 업로드하기
        val storage = MyApplication.storage

        //storageRef
        val storageRef = storage.reference
        //imgaes 폴더에 docId.jpg로 저장
        val imgRef = storageRef.child("images/${docId}.jpg")
        val file = Uri.fromFile(File(filePath))         //갤러리에서 가져온 파일경로를 이용해서 파일의 URi 전달
        imgRef.putFile(file)
            .addOnSuccessListener {
                Toast.makeText(this, "데이터가 저장 되었습니다." ,Toast.LENGTH_SHORT).show()
                finish()    // 저장 후 액태비티 종료
            }
            .addOnFailureListener{
                Log.d("jun" , "저장 실패 "+ it)
            }

    }


}