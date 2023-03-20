package com.mahmoudhabib.firebasestorageassignment

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.mahmoudhabib.firebasestorageassignment.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class MainActivity : AppCompatActivity(), PdfsAdapter.OnClick {
    private val requestCode = 101
    private lateinit var pdfFileUri: Uri
    lateinit var binding: ActivityMainBinding
    private val filesList = mutableListOf<StorageReference>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val storage = Firebase.storage
        val ref = storage.reference

        ref.child("pdfFiles").listAll().addOnSuccessListener { list ->
            filesList.addAll(list.items)
            val adapter = PdfsAdapter(filesList.map { it.name }, this)
            binding.recyclerView.layoutManager = LinearLayoutManager(this)
            binding.recyclerView.adapter = adapter
            binding.selectButton.setOnClickListener {
                val i = Intent(Intent.ACTION_GET_CONTENT)
                i.type = "application/pdf"
                i.addCategory(Intent.CATEGORY_OPENABLE)
                startActivityForResult(i, requestCode)
            }
        }


        binding.uploadButton.setOnClickListener {

            binding.progressBar.visibility = View.VISIBLE

            ref.child("pdfFiles/" + UUID.randomUUID().toString()).putFile(pdfFileUri)
                .addOnProgressListener {
                    val progress = (100 * it.bytesTransferred) / it.totalByteCount
                    binding.progressBar.progress = progress.toInt()
                }
                .addOnSuccessListener {
                    binding.progressBar.visibility = View.GONE
                    binding.tvFileName.text = ""
                    Toast.makeText(this, "file uploaded Successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    binding.progressBar.visibility = View.GONE
                    binding.tvFileName.text = ""
                    Toast.makeText(this, "filed to upload", Toast.LENGTH_SHORT).show()
                }

        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            binding.uploadButton.isEnabled = true
            pdfFileUri = data?.data!!
            val uriString: String = pdfFileUri.toString()
            var pdfName: String? = null
            if (uriString.startsWith("content://")) {
                var myCursor: Cursor? = null
                try {
                    myCursor = applicationContext!!.contentResolver.query(
                        pdfFileUri, null, null, null, null
                    )
                    if (myCursor != null && myCursor.moveToFirst()) {
                        pdfName =
                            myCursor.getString(myCursor.run { getColumnIndex(OpenableColumns.DISPLAY_NAME) })
                        binding.tvFileName.text = pdfName
                    }
                } finally {
                    myCursor?.close()
                }
            }

        }
    }

    override fun clicked(position: Int) {
            filesList[position].downloadUrl.addOnSuccessListener {
                val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val request = DownloadManager.Request(it)
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                request.setDestinationInExternalFilesDir(
                    this@MainActivity,
                    DIRECTORY_DOWNLOADS,
                    "downloadedFile.pdf"
                )
                dm.enqueue(request)
            }
    }
}