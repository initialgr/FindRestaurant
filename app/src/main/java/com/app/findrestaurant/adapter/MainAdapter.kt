package com.app.findrestaurant.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.app.findrestaurant.model.ModelMain
import com.app.findrestaurant.utils.OnItemClickCallback
import com.app.findrestaurant.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import kotlinx.android.synthetic.main.list_item_main.view.*

class MainAdapter (

    private val mContext: Context,
    private val items: List<ModelMain>) : RecyclerView.Adapter<MainAdapter.ViewHolder>() {

    private var rating = 0.0

    private var onItemClickCallback: OnItemClickCallback? = null

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback?) {
        this.onItemClickCallback = onItemClickCallback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_main, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = items[position]

        rating = data.aggregateRating

        Glide.with(mContext)
            .load(data.thumbResto)
            .transform(CenterCrop(), RoundedCorners(25))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(holder.imgResto)

        val newValue = rating.toFloat()
        holder.ratingResto.numStars = 5
        holder.ratingResto.stepSize = 0.5.toFloat()
        holder.ratingResto.rating = newValue

        holder.tvNameResto.text = data.nameResto
        holder.tvAddress.text = data.addressResto
        holder.tvRating.text = " |  " + data.aggregateRating + " " + data.ratingText
        holder.cvListMain.setOnClickListener {
            onItemClickCallback?.onItemMainClicked(data)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    //Class Holder
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var cvListMain: CardView = itemView.cvListMain
        var imgResto: ImageView = itemView.imgResto
        var tvNameResto: TextView = itemView.tvNameResto
        var tvAddress: TextView = itemView.tvAddress
        var tvRating: TextView = itemView.tvRating
        var ratingResto: RatingBar = itemView.rating_resto

    }
}
