/*
 * Copyright (c) 2019.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.applicationmanager.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.util.AttributeSet
import android.widget.ImageView
import com.dertyp7214.applicationmanager.R
import com.dertyp7214.applicationmanager.core.getBitmap


class MaskedImageView : ImageView {

    private lateinit var mMask: Drawable

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.MaskedImageView)
            mMask =
                typedArray.getDrawable(R.styleable.MaskedImageView_mask) ?: resources.getDrawable(R.drawable.mask_full)
            typedArray.recycle()
        }
    }

    fun setMask(drawable: Drawable) {
        mMask = drawable
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        val original = when (drawable) {
            is ColorDrawable -> Bitmap.createBitmap(
                width,
                height,
                Bitmap.Config.ARGB_8888
            ).apply {
                eraseColor((drawable as ColorDrawable).color)
            }
            is VectorDrawable -> (drawable as VectorDrawable).getBitmap(context)
            else -> (drawable as BitmapDrawable).bitmap
        }
        val mask = Bitmap.createScaledBitmap(when (mMask) {
            is ColorDrawable -> Bitmap.createBitmap(
                layoutParams.width,
                layoutParams.height,
                Bitmap.Config.ARGB_8888
            ).apply {
                eraseColor((mMask as ColorDrawable).color)
            }
            is VectorDrawable -> (mMask as VectorDrawable).getBitmap(context)
            else -> (mMask as BitmapDrawable).bitmap
        }, original.width, original.height, false)

        val result = Bitmap.createBitmap(mask.width, mask.height, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(result)
        val paint = Paint(ANTI_ALIAS_FLAG)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        tempCanvas.drawBitmap(original, 0F, 0F, null)
        tempCanvas.drawBitmap(mask, 0F, 0F, paint)
        paint.xfermode = null

        canvas !!.drawBitmap(result, 0F, 0F, Paint())
    }
}