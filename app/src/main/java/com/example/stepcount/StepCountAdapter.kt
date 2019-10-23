package com.example.stepcount

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.stepcount.model.StepCountModel
import kotlinx.android.synthetic.main.layout_item.view.*

class StepCountAdapter(private val context: MainActivity, private val stepArrayList: ArrayList<StepCountModel>) : RecyclerView.Adapter<StepCountAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StepCountAdapter.ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_item, parent, false))
    }

    override fun getItemCount(): Int {
        return stepArrayList.size
    }

    override fun onBindViewHolder(holder: StepCountAdapter.ViewHolder, position: Int) {
        holder.start.text = stepArrayList.get(position).startTime
        holder.end.text = stepArrayList.get(position).endTime
        holder.count.text = stepArrayList.get(position).count
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val start = view.tvStartTime
        val end = view.tvEndTime
        val count = view.tvCount
    }
}