package org.techtown.firebasetest.recycler

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ch20_firebase.model.ItemData
import org.techtown.firebasetest.MyApplication
import org.techtown.firebasetest.databinding.ItemMainBinding

class MyAdapter(val context:Context,val items : ArrayList<ItemData>) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    inner class MyViewHolder(val binding: ItemMainBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(item:ItemData){
            //데이터 바인딩 쓰면 좀 더 편하게 가능
            binding.itemEmailView.text = item.email
            binding.itemDateView.text = item.date
            binding.itemContentView.text = item.content
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemMainBinding.inflate(LayoutInflater.from(parent.context))
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = items[position]
        //리스트에 값 셋팅은 Main에서 실행될때 FireStore에서 값 가져와서 셋팅
        //데이터 셋팅
        holder.bind(item)

        //스토리지 이미지 다운로드
        val imgRef = MyApplication.storage.reference.child("images/${item.docId}.jpg")

        Glide.with(context)           //안되면 context 받아서 하는걸로 바꿔보기
            .load(imgRef)
            .into(holder.binding.itemImageView)
    }

    override fun getItemCount() = items.size
}