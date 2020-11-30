package com.williamzabot.notascoloridas.ui.colors

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.williamzabot.notascoloridas.R
import com.williamzabot.notascoloridas.extensions.transformDrawable
import com.williamzabot.notascoloridas.ui.colors.model.Color
import kotlinx.android.synthetic.main.item_color.view.*

class ColorsAdapter(
    private val colors: List<Color>,
    private val onColorClickListener: (color: Color) -> Unit
) : RecyclerView.Adapter<ColorsAdapter.ColorViewHolder>() {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        context = parent.context
        return ColorViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_color, parent, false)
        )
    }

    override fun getItemCount() = colors.size

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) =
        holder.bind(colors[position])


    inner class ColorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val colorButton = itemView.color_button

        fun bind(color: Color) {
            colorButton.background = transformDrawable(context, color.drawableCircle)
            itemView.setOnClickListener {
                onColorClickListener.invoke(color)
            }
        }

    }

}