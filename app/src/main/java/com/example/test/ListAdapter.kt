package com.example.test

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import retrofit2.http.Path
import kotlin.random.Random

class ListAdapter(private val dataSet: Array<RealTimeResult>) :
    RecyclerView.Adapter<ListAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val routeTextView: TextView
        val directionTextView: TextView
        val etaTextView: TextView

        init {
            routeTextView = view.findViewById(R.id.route)
            directionTextView = view.findViewById(R.id.direction)
            etaTextView = view.findViewById(R.id.eta)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            LayoutInflater.from(viewGroup.context).inflate(R.layout.list_item, viewGroup, false)
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val temp = dataSet[position].routeName.split("-")
        viewHolder.routeTextView.text = temp[0]
        viewHolder.directionTextView.text = temp.last()
        if (dataSet[position].realTime) {
            viewHolder.etaTextView.text = dataSet[position].minute.toString()
        } else {
            viewHolder.etaTextView.text = "^" + dataSet[position].minute.toString()
        }
    }

    override fun getItemCount() = dataSet.size
}