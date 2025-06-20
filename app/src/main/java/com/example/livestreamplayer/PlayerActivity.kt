// 粘贴上一个回答中 PlayerActivity.kt 的完整代码
package com.example.livestreamplayer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.livestreamplayer.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayerBinding
    private var player: ExoPlayer? = null

    companion object {
        const val EXTRA_STREAM_URL = "extra_stream_url"
        const val EXTRA_STREAM_TITLE = "extra_stream_title"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val streamUrl = intent.getStringExtra(EXTRA_STREAM_URL)
        val streamTitle = intent.getStringExtra(EXTRA_STREAM_TITLE)
        title = streamTitle
        if (streamUrl != null) {
            initializePlayer(streamUrl)
        }
    }

    private fun initializePlayer(url: String) {
        player = ExoPlayer.Builder(this).build().also { exoPlayer ->
            binding.playerView.player = exoPlayer
            val mediaItem = MediaItem.fromUri(url)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.playWhenReady = true
            exoPlayer.prepare()
        }
    }

    public override fun onStart() {
        super.onStart()
        if (player == null) {
            val streamUrl = intent.getStringExtra(EXTRA_STREAM_URL)
            if (streamUrl != null) {
                initializePlayer(streamUrl)
            }
        }
    }

    public override fun onResume() {
        super.onResume()
        if (player == null) {
            val streamUrl = intent.getStringExtra(EXTRA_STREAM_URL)
            if (streamUrl != null) {
                initializePlayer(streamUrl)
            }
        }
    }

    public override fun onPause() {
        super.onPause()
        releasePlayer()
    }

    public override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    private fun releasePlayer() {
        player?.let {
            it.release()
            player = null
            binding.playerView.player = null
        }
    }
}