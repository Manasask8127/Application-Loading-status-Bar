package com.udacity

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.udacity.databinding.ActivityDetailBinding
import kotlinx.android.synthetic.main.activity_detail.*


class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail)
        setSupportActionBar(toolbar)

        val extras = intent.extras;
        if (extras != null) {
            val status = extras.getString("status")
            val filename = extras.getString("file")
            val notificationID = extras.getInt("notificationID")

            if (status != null && filename != null) {
                //cancel notification
                val notificationManager =
                    this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(notificationID)

                //updating view
                binding.contentDetail.fileBody.text = filename
                if (status.equals("Success"))
                    binding.contentDetail.statusBody.setTextColor(Color.GREEN)
                else
                    binding.contentDetail.statusBody.setTextColor(Color.RED)

                binding.contentDetail.statusBody.text = status


            }
        }

        binding.contentDetail.ok.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

}
