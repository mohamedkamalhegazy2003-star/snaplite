package com.snaplite.app

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.snaplite.app.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private val adapter = ResultAdapter { openStream(it.url) }

    private val notifPerm =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        NewPipe.init(DownloaderImpl.instance)

        b.list.layoutManager = LinearLayoutManager(this)
        b.list.adapter = adapter

        b.searchBtn.setOnClickListener { go() }
        b.input.setOnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_SEARCH) { go(); true } else false
        }

        if (Build.VERSION.SDK_INT >= 33) {
            notifPerm.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        // Link shared from another app (e.g. "Share" -> SnapLite)
        if (intent?.action == Intent.ACTION_SEND) {
            intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
                b.input.setText(it)
                go()
            }
        }
    }

    private fun go() {
        val text = b.input.text.toString().trim()
        if (text.isEmpty()) return
        if (text.startsWith("http://") || text.startsWith("https://")) openStream(text)
        else search(text)
    }

    private fun search(query: String) = lifecycleScope.launch {
        loading(true)
        try {
            val items = withContext(Dispatchers.IO) {
                val ex = ServiceList.YouTube.getSearchExtractor(query)
                ex.fetchPage()
                ex.initialPage.items.filterIsInstance<StreamInfoItem>()
            }
            adapter.submit(items)
            if (items.isEmpty()) toast(getString(R.string.no_results))
        } catch (e: Exception) {
            toast(getString(R.string.error_generic, e.message ?: e.javaClass.simpleName))
        } finally {
            loading(false)
        }
    }

    private fun openStream(url: String) = lifecycleScope.launch {
        loading(true)
        try {
            val info = withContext(Dispatchers.IO) { StreamInfo.getInfo(url) }
            showQualityDialog(info)
        } catch (e: Exception) {
            toast(getString(R.string.error_generic, e.message ?: e.javaClass.simpleName))
        } finally {
            loading(false)
        }
    }

    private data class Option(val label: String, val url: String, val ext: String, val mime: String)

    private fun showQualityDialog(info: StreamInfo) {
        val options = mutableListOf<Option>()

        info.videoStreams
            .filter { !it.isVideoOnly && it.content.isNotEmpty() }
            .sortedByDescending { it.height }
            .forEach {
                val ext = it.format?.suffix ?: "mp4"
                options += Option("🎬 ${it.resolution}  (${ext.uppercase()})", it.content, ext,
                    it.format?.mimeType ?: "video/mp4")
            }

        info.audioStreams
            .filter { it.content.isNotEmpty() }
            .sortedByDescending { it.averageBitrate }
            .forEach {
                val ext = it.format?.suffix ?: "m4a"
                options += Option("🎵 ${it.averageBitrate}kbps  (${ext.uppercase()})", it.content, ext,
                    it.format?.mimeType ?: "audio/mp4")
            }

        if (options.isEmpty()) {
            toast(getString(R.string.no_results)); return
        }

        AlertDialog.Builder(this)
            .setTitle("${getString(R.string.choose_quality)}\n${info.name}")
            .setItems(options.map { it.label }.toTypedArray()) { _, i ->
                enqueue(info.name, options[i])
            }
            .show()
    }

    private fun enqueue(title: String, o: Option) {
        val safe = title.replace(Regex("[\\\\/:*?\"<>|]"), "_").take(80)
        val req = DownloadManager.Request(o.url.toUri())
            .setTitle(safe)
            .setMimeType(o.mime)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "SnapLite/$safe.${o.ext}")
        (getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(req)
        toast(getString(R.string.download_started))
    }

    private fun loading(on: Boolean) {
        b.progress.visibility = if (on) View.VISIBLE else View.GONE
    }

    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_LONG).show()
}
