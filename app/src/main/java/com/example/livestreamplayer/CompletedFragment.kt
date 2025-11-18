package com.example.livestreamplayer

import com.example.livestreamplayer.TasksResponse
import com.example.livestreamplayer.DownloadTask
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.livestreamplayer.databinding.FragmentCompletedBinding
import kotlinx.coroutines.launch

class CompletedFragment : Fragment() {
    private var _binding: FragmentCompletedBinding? = null
    private val binding get() = _binding!!
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var completedAdapter: CompletedAdapter
    private lateinit var remoteDownloadApi: RemoteDownloadApi

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCompletedBinding.inflate(inflater, container, false)
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
        loadCompletedTasks()
    }

    private fun setupRecyclerView() {
        completedAdapter = CompletedAdapter(
            mutableListOf(),
            onDeleteClick = { context, task ->
                lifecycleScope.launch {
                    val response = remoteDownloadApi.deleteCompletedFile(task.id)
                    Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                    if (response.ok) {
                        loadCompletedTasks()
                    }
                }
            },
            onPlayClick = { context, task ->
                if (task.outputPath != null && task.outputPath.isNotEmpty()) {
                    val intent = Intent(context, PlayerActivity::class.java).apply {
                        putExtra(PlayerActivity.EXTRA_STREAM_URL, task.outputPath)
                        putExtra(PlayerActivity.EXTRA_STREAM_TITLE, task.channelTitle)
                    }
                    context.startActivity(intent)
                } else {
                    Toast.makeText(context, "文件路径无效", Toast.LENGTH_SHORT).show()
                }
            }
        )
        binding.recyclerViewCompleted.apply {
            adapter = completedAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun loadCompletedTasks() {
        lifecycleScope.launch {
            println(remoteDownloadApi)
            val response = remoteDownloadApi.getTasks("completed", 1, 100)
            if (response.ok) {
                completedAdapter.updateTasks(response.items)
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
