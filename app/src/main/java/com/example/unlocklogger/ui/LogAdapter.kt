package com.example.unlocklogger.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.unlocklogger.R

class LogAdapter(
    private var logs: MutableList<String>, // 可变列表，方便删除操作
    private val onDeleteClicked: (index: Int) -> Unit // 删除回调函数
) : RecyclerView.Adapter<LogAdapter.LogViewHolder>() {

    class LogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val timestampTextView: TextView = view.findViewById(R.id.text_view_timestamp)
        val deleteButton: ImageButton = view.findViewById(R.id.button_delete_record)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_log_record, parent, false)
        return LogViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val logEntry = logs[position]
        holder.timestampTextView.text = logEntry

        // 设置删除按钮的点击监听器
        holder.deleteButton.setOnClickListener {
            // 调用外部回调，传入当前记录在列表中的索引
            onDeleteClicked(position)
        }
    }

    override fun getItemCount(): Int = logs.size

    /**
     * 更新列表数据，并在 UI 上反映变化。
     * @param newLogs 新的记录列表。
     */
    fun updateLogs(newLogs: List<String>) {
        logs.clear()
        logs.addAll(newLogs)
        // 通知 RecyclerView 数据集已改变
        notifyDataSetChanged() 
    }
    
    /**
     * 从列表中移除指定位置的记录，并通知适配器局部更新。
     */
    fun removeLogAt(position: Int) {
        if (position >= 0 && position < logs.size) {
            logs.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}