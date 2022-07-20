package com.udacity

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.databinding.DataBindingUtil
import com.udacity.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

const val channelID = "notification_channel"
const val channelName="notification_name"

class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0
    private var checkedRadioButton=-1

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    private lateinit var binding:ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_main)
        setSupportActionBar(toolbar)

        createNotificationChannel()

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        binding.contentMain.customButton.setOnClickListener {
            download()
        }

        binding.contentMain.downloadableType.setOnCheckedChangeListener{
            _,checkID->
            checkedRadioButton=when(checkID){
                R.id.download_glide -> 0
                R.id.download_loadapp -> 1
                R.id.download_retrofit -> 2
                else -> -1
            }

        }
    }

    fun createNotificationChannel(){
           if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
               val channel= NotificationChannel(
                    channelID, channelName,NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description=description
                }
                notificationManager=this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            } else {
                TODO("VERSION.SDK_INT < O")
               Log.d("Manasa","Version less than O")
            }
    }

    private fun checkRadioButtonsChecked():Boolean{
        return if(checkedRadioButton ==-1)
        {
            Toast.makeText(this,"Please select the file to download",Toast.LENGTH_LONG).show()
            false
        }
        else
            true
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if(id==downloadID)
            {
                binding.contentMain.customButton.text=getString(R.string.button_loading)
                binding.contentMain.customButton.animateButton=ButtonState.Completed
                val downloadManager=getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                val query=DownloadManager.Query()
                query.setFilterById(id)
                val cursor=downloadManager.query(query)
                if(cursor.moveToFirst())
                {
                    val status=cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    val detailIntent=Intent()
                    when(status)
                    {
                        DownloadManager.STATUS_SUCCESSFUL->
                            detailIntent.putExtra("status","Sucess")
                        DownloadManager.STATUS_FAILED->
                            detailIntent.putExtra("status","Failed")
                        DownloadManager.ERROR_UNKNOWN->
                            detailIntent.putExtra("status","Unknown")

                    }
                    callNotificationToDisplayStatus(detailIntent)
                }
            }
        }
    }

    private fun download() {
        if (checkRadioButtonsChecked()) {
            binding.contentMain.customButton.text=getString(R.string.button_loading)
            binding.contentMain.customButton.animateButton=ButtonState.Loading
            val downloadFile=getDownloadFile()

            val request =
                DownloadManager.Request(Uri.parse(downloadFile[0]))
                    .setTitle(downloadFile[1])
                    .setDescription(downloadFile[2])
                    .setRequiresCharging(false)
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true)
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)

            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            downloadID =
                downloadManager.enqueue(request)// enqueue puts the download request in the queue.
            if(downloadID!=0L){
                binding.contentMain.customButton.isEnabled=false
            }
        }

    }

    private fun getDownloadFile():ArrayList<String>{
        val arrayList=ArrayList<String>()
        when(checkedRadioButton){
            0 -> {
                arrayList.add(0,GlideUrl)
                arrayList.add(1,getString(R.string.glide))
                arrayList.add(2,getString(R.string.glide_library_description))
            }
            1 -> {
                arrayList.add(0, LoadAppUrl)
                arrayList.add(1,getString(R.string.loadapp))
                arrayList.add(2,getString(R.string.loadapp_library_description))
            }
            2 -> {
                arrayList.add(0, RetrofitUrl)
                arrayList.add(1,getString(R.string.retrofit))
                arrayList.add(2,getString(R.string.retrofit_library_description))
            }
        }
        return arrayList
    }

    companion object {
//        private const val URL =
//            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
//        private const val CHANNEL_ID = "channelId"

        private const val GlideUrl="https://github.com/bumptech/glide/archive/refs/heads/master.zip"
        private const val LoadAppUrl="https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/refs/heads/master.zip"
        private const val RetrofitUrl="https://github.com/square/retrofit/archive/refs/heads/master.zip"
    }

    private fun callNotificationToDisplayStatus(intent: Intent){
        val downloadingFile=getDownloadFile()
        intent.putExtra("notificationID",downloadID.toInt())
        intent.putExtra("file",downloadingFile.get(1))
        createNotificationToDisplayStatus(this,intent,downloadingFile.get(1),downloadingFile.get(2),downloadID.toInt())
    }


    fun createNotificationToDisplayStatus(
        context: Context,
        intent: Intent,
        title:String,
        description:String,
        notificationID:Int
    ){
        pendingIntent=getIntentToShowDetail(context,intent)
        val builder=NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentTitle(title)
            .setContentText(description)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_assistant_black_24dp,"Check the Status",pendingIntent)
        with(NotificationManagerCompat.from(context)){
            notify(notificationID,builder.build())
        }
    }

    private fun getIntentToShowDetail(context: Context,detailIntent: Intent):PendingIntent{
        detailIntent.setClass(context,DetailActivity::class.java)
        val detailPendingIntent:PendingIntent=TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(detailIntent)
            getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT)
        }
        return detailPendingIntent
    }

}
