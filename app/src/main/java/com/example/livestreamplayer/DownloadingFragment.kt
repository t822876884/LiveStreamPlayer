package com.example.livestreamplayer

import com.example.livestreamplayer.TasksResponse
import com.example.livestreamplayer.DownloadTask
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.livestreamplayer.databinding.FragmentDownloadingBinding
import kotlinx.coroutines.launch

class DownloadingFragment : Fragment() {
    private var _binding: FragmentDownloadingBinding? = null
    private val binding get() = _binding!!
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var downloadingAdapter: DownloadingAdapter
    private lateinit var remoteDownloadApi: RemoteDownloadApi

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDownloadingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferenceManager = PreferenceManager(requireContext())
        remoteDownloadApi = RemoteDownloadApi(requireContext())
        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        loadDownloadingTasks()
    }

    private fun setupRecyclerView() {
        downloadingAdapter = DownloadingAdapter(
            mutableListOf(),
            onCancelClick = { context, task ->
                lifecycleScope.launch {
                    val response = remoteDownloadApi.cancelDownload(task.id)
                    Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                    if (response.ok) {
                        loadDownloadingTasks()
                    }
                }
            },
            onPlayClick = { context, task ->
                Toast.makeText(context, "文件正在下载中，无法播放", Toast.LENGTH_SHORT).show()
            }
        )
        binding.recyclerViewDownloading.apply {
            adapter = downloadingAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun loadDownloadingTasks() {
        lifecycleScope.launch {
            val response = remoteDownloadApi.getTasks("downloading", 1, 100)
            if (response.ok) {
                downloadingAdapter.updateTasks(response.items)
            } else {
                Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
