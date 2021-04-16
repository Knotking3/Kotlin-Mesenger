package com.example.kotlinmessenger.messages

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.user_row_new_message.view.*

class NewMessageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        supportActionBar?.title = "Select User"

        fetchUsers()
    }

    companion object{
        val USER_KEY = "USER_KEY"
    }
    private fun fetchUsers(){
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot){
                val adapter = GroupAdapter<GroupieViewHolder>()
                p0.children.forEach{
                    val user = it.getValue(User::class.java)
                    if (user != null){
                        adapter.add(UserItem(user))
                    }
                }
                adapter.setOnItemClickListener{ item, view ->

                    val userItem = item as UserItem
                    val intent = Intent(view.context, ChatLogActivity::class.java)
                    //comeback here I changed thigns to get the put extra to work
                    intent.putExtra(USER_KEY, userItem.user)
                    startActivity(intent)

                    finish()

                }

                recyclerview_newmessage.adapter = adapter
            }
            override fun onCancelled(p0: DatabaseError){

            }
        })
    }
}



class UserItem(val user: User): Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.username_textview_newmessage.text = user.username
        //will be called in our list for each user
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.imageview_newmessage)
    }
    override fun getLayout(): Int {
        return R.layout.user_row_new_message
    }
}