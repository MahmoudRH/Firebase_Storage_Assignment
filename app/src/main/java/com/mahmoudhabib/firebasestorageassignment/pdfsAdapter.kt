package com.mahmoudhabib.firebasestorageassignment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mahmoudhabib.firebasestorageassignment.databinding.PdfItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PdfsAdapter(private val itemList: List<String>, val onClick: OnClick) :
    RecyclerView.Adapter<PdfsAdapter.ViewHolder>() {

    class ViewHolder(itemView: PdfItemBinding) : RecyclerView.ViewHolder(itemView.root) {
        val binding = itemView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = PdfItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.tvFileName.text = itemList[position]
        holder.binding.downloadBtn.setOnClickListener {
                onClick.clicked(position)
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    interface OnClick {
        fun clicked(position: Int)
    }
}