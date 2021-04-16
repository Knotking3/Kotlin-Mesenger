package com.example.kotlinmessenger.messages

import android.os.*
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.models.ChatMessage
import com.example.kotlinmessenger.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.onesignal.OneSignal
import com.r0adkll.slidr.Slidr
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*
import org.json.JSONException
import org.json.JSONObject
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


class ChatLogActivity : AppCompatActivity() {

    companion object{
        val TAG = "ChatLog"
    }

    val adapter = GroupAdapter<GroupieViewHolder>()

    var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        Slidr.attach(this)

        recyclerview_chatlog.adapter = adapter

        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)

        supportActionBar?.title = toUser?.username

        listenForMessages()

        scrollUpForKeyboard()



        send_button_chatlog.setOnClickListener {

            performSendMessage()
        }
    }

    private fun scrollUpForKeyboard(){
        edittext_chatlog.setOnFocusChangeListener { view, b ->
            if(b){
                val handler = Handler()
                val runnable = Runnable {
                    recyclerview_chatlog.scrollToPosition(adapter.itemCount - 1)
                }
                handler.postDelayed(runnable, 300)            }
        }
        edittext_chatlog.setOnClickListener {
            val handler = Handler()
            val runnable = Runnable {
                recyclerview_chatlog.scrollToPosition(adapter.itemCount - 1)
            }
            handler.postDelayed(runnable, 100)
        }
    }




    private fun listenForMessages(){
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid

        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")
        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)

                if (chatMessage != null) {

                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        val currentUser = LatestMessegesActivity.currentUser ?: return
                        adapter.add(ChatFromItem(chatMessage.text, currentUser))
                    } else {
                        adapter.add(ChatToItem(chatMessage.text, toUser!!))

                    }
                }

                recyclerview_chatlog.scrollToPosition(adapter.itemCount - 1)
            }

            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }
        })



    }

    private fun performSendMessage(){
        val text = edittext_chatlog.text.toString()

        val fromId = FirebaseAuth.getInstance().uid
        val user = intent. getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user?.uid
        if (fromId == null) return
        if (toId == null) return
        if (text == "") return

//        val reference = FirebaseDatabase.getInstance().getReference("/messages").push()
        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()

        val toReference = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()


        val chatMessage = ChatMessage(
            reference.key!!,
            text,
            fromId,
            toId,
            System.currentTimeMillis() / 1000
        )

        reference.setValue(chatMessage)
            .addOnSuccessListener {
                edittext_chatlog.text.clear()
                recyclerview_chatlog.scrollToPosition(adapter.itemCount - 1)
            }

        toReference.setValue(chatMessage)

        val latestMessageReference = FirebaseDatabase.getInstance().getReference("latest-messages/$fromId/$toId")
        latestMessageReference.setValue(chatMessage)

        val latestMessageToRef = FirebaseDatabase.getInstance().getReference("latest-messages/$toId/$fromId")
        latestMessageToRef.setValue(chatMessage)


        val currentUser = LatestMessegesActivity.currentUser ?: return
        sendNotification(toId, currentUser.username, chatMessage.text)



    }
    private fun sendNotification(user: String, sender: String, message: String) {

        AsyncTask.execute {
            val SDK_INT = Build.VERSION.SDK_INT
            if (SDK_INT > 8) {
                val policy = StrictMode.ThreadPolicy.Builder()
                    .permitAll().build()
                StrictMode.setThreadPolicy(policy)
                val send_ID: String

                //This is a Simple Logic to Send Notification different Device Programmatically....


                send_ID = user//"Noah"
                val notification = "$sender: $message"
//
                try {
                    val jsonResponse: String
                    val url = URL("https://onesignal.com/api/v1/notifications")
                    val con: HttpURLConnection = url.openConnection() as HttpURLConnection
                    con.setUseCaches(false)
                    con.setDoOutput(true)
                    con.setDoInput(true)
                    con.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    con.setRequestProperty(
                        "Authorization",
                        "Basic ODJlYjZjMzAtODgxMC00NzUzLTg3ODctNTRhMDBlNmNhOGE1"
                    )
                    con.setRequestMethod("POST")
                    val strJsonBody = ("{"
                            + "\"app_id\": \"ba6a3177-153d-466f-a684-65a82247d576\","
                            + "\"filters\": [{\"field\": \"tag\", \"key\": \"User_ID\", \"relation\": \"=\", \"value\": \"" + send_ID + "\"}],"
                            + "\"data\": {\"foo\": \"bar\"},"
                            + "\"contents\": {\"en\": \"" + notification + "\"}"
                            + "}")
                    println("strJsonBody:\n$strJsonBody")
                    val sendBytes = strJsonBody.toByteArray(charset("UTF-8"))
                    con.setFixedLengthStreamingMode(sendBytes.size)
                    val outputStream: OutputStream = con.getOutputStream()
                    outputStream.write(sendBytes)
                    val httpResponse: Int = con.getResponseCode()
                    println("httpResponse: $httpResponse")
                    if (httpResponse >= HttpURLConnection.HTTP_OK
                        && httpResponse < HttpURLConnection.HTTP_BAD_REQUEST
                    ) {
                        val scanner = Scanner(con.getInputStream(), "UTF-8")
                        jsonResponse =
                            if (scanner.useDelimiter("\\A").hasNext()) scanner.next() else ""
                        scanner.close()
                    } else {
                        val scanner = Scanner(con.getErrorStream(), "UTF-8")
                        jsonResponse =
                            if (scanner.useDelimiter("\\A").hasNext()) scanner.next() else ""
                        scanner.close()
                    }
                    println("jsonResponse:\n$jsonResponse")
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
            }
        }
    }


}


class ChatFromItem(val text: String, val user: User): Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textview_from_row.text = text

        val uri = user.profileImageUrl
        val targetImageView = viewHolder.itemView.imageview_chat_from_row
        Picasso.get().load(uri).into(targetImageView)

    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}

class ChatToItem(val text: String, val user: User): Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textview_to_row.text = text

        //load our userimage into the left
        val uri = user.profileImageUrl
        val targetImageView = viewHolder.itemView.imageview_chat_to_row
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }


}