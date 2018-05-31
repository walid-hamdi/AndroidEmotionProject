package com.upfunstudio.emotionsocial.Helper

import android.graphics.Canvas
import android.graphics.Paint
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper

open class RecycleItemTouchHelper(dragDirs: Int, swipeDirs: Int,
                                  val listenner: RecycleItemTouchHelperListenner) :
        ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs) {


    override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?): Boolean {

        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {


        if (listenner != null) {
            listenner.onSwip(viewHolder, direction, viewHolder!!.adapterPosition)
        }
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        if (viewHolder != null) {

            ItemTouchHelper.Callback.getDefaultUIUtil().onSelected(viewHolder.itemView)

        }

    }


    override fun clearView(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?) {
        super.clearView(recyclerView, viewHolder)

        ItemTouchHelper.Callback.getDefaultUIUtil().clearView(viewHolder!!.itemView)

    }

    override fun onChildDraw(c: Canvas?, recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {


        // todo : change background item when delete
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            // Get RecyclerView item from the ViewHolder
            val itemView = viewHolder!!.itemView

            val p = Paint()
            if (dX > 0) {
                /* Set your color for positive displacement */

                // Draw Rect with varying right side, equal to displacement dX
                c!!.drawRect(itemView.left.toFloat(), itemView.top.toFloat(), dX,
                        itemView.bottom.toFloat(), p)
            } else {
                /* Set your color for negative displacement */

                // Draw Rect with varying left side, equal to the item's right side plus negative displacement dX
                c!!.drawRect(itemView.right.toFloat() + dX, itemView!!.top.toFloat(),
                        itemView!!.right.toFloat(), itemView.bottom.toFloat(), p)
            }



            ItemTouchHelper.Callback.getDefaultUIUtil().onDrawOver(c, recyclerView, viewHolder!!.itemView, dX, dY, actionState, isCurrentlyActive)

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)


        }
    }

    open interface RecycleItemTouchHelperListenner {

        fun onSwip(viewHolder: RecyclerView.ViewHolder?,
                   direction: Int,
                   position: Int)

    }


}
