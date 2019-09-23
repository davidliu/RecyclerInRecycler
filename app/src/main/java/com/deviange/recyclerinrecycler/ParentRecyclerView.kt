package com.deviange.recyclerinrecycler

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import androidx.core.view.NestedScrollingParent3
import androidx.core.view.NestedScrollingParentHelper
import androidx.dynamicanimation.animation.FloatValueHolder
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs
import kotlin.math.min

class ParentRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr),
    NestedScrollingParent3 {

    private var springAnimation: SpringAnimation? = null
    private val parentHelper = NestedScrollingParentHelper(this)
    private val pagerSnapHelper = PagerSnapHelper()
    private val velocityTracker = VelocityTracker.obtain()

    init {
        pagerSnapHelper.attachToRecyclerView(this)

        addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                for (i in 0 until childCount) {
                    val v = getChildAt(i)
                    val factor = abs(min(0, v.top)).toFloat() / v.height
                    val targetScale = 0.95f
                    val newScale = 1 * (1 - factor) + (targetScale * factor)
                    v.pivotY = v.height.toFloat()
                    v.pivotX = v.width.toFloat() / 2
                    v.scaleX = newScale
                    v.scaleY = newScale
                }
            }
        })
    }

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        stopAnimateIfNeeded()
        return true
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        parentHelper.onNestedScrollAccepted(child, target, axes, type)
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        // In between pages
        if (target.top != 0) {
            val negSign = if (dy < 0) -1 else 1
            val scrollY = negSign * min(
                abs(dy),
                abs(target.top)
            )
            scrollBy(0, scrollY)
            consumed[1] = scrollY
        } else if (!target.canScrollVertically(dy)) {
            scrollBy(0, dy)
            consumed[1] = dy
        }
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {

    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int
    ) {
    }

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {

        val vX = velocityX.toInt()
        val vY = velocityY.toInt()
        if (target.top != 0 || !target.canScrollVertically(vY)) {
            val viewHolder = findContainingViewHolder(target)
                ?: throw IllegalStateException("Prefling from non-child?")
            val curPosition = viewHolder.adapterPosition
            val snapPosition = pagerSnapHelper.findTargetSnapPosition(layoutManager, vX, vY).let {
                // We need to make sure that we can't skip a page.
                Math.min(Math.max(it, curPosition - 1), curPosition + 1)
            }

            val scrollY = when {
                curPosition == snapPosition -> viewHolder.itemView.top
                curPosition < snapPosition -> height + viewHolder.itemView.top
                curPosition > snapPosition -> -height + viewHolder.itemView.top
                else -> 0 // not possible.
            }

            springAnimate(scrollY, velocityY)
            return true
        }
        return super.onNestedPreFling(target, velocityX, velocityY)
    }

    private fun stopAnimateIfNeeded() {
        springAnimation?.let {
            it.cancel()
            springAnimation = null
        }
    }

    private fun springAnimate(scrollY: Int, velocityY: Float = 0f) {
        stopAnimateIfNeeded()
        var last = 0
        springAnimation = SpringAnimation(FloatValueHolder(0f), scrollY.toFloat())
            .apply {
                addUpdateListener { _, value, _ ->
                    val step = (value - last).toInt()
                    scrollBy(0, step)
                    last += step
                }
                addEndListener { _, cancelled, _, _ ->
                    if (!cancelled) {
                        scrollBy(0, scrollY - last)
                    }
                }
                spring.dampingRatio = DAMPING
                spring.stiffness = STIFFNESS
                setStartVelocity(velocityY)
                start()
            }

    }

    override fun onStopNestedScroll(target: View, type: Int) {
        parentHelper.onStopNestedScroll(target, type)

        if (springAnimation == null) {
            val viewHolder = findContainingViewHolder(target)!!
            springAnimate(viewHolder.itemView.top)
        }
    }

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        when (e.action) {
            MotionEvent.ACTION_DOWN -> velocityTracker.clear()
        }
        velocityTracker.addMovement(e)
        return false
    }

    override fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        // ignore, we need the intercepts.
    }

    companion object {
        val DAMPING = SpringForce.DAMPING_RATIO_NO_BOUNCY
        val STIFFNESS = (SpringForce.STIFFNESS_MEDIUM + SpringForce.STIFFNESS_LOW) / 2f
    }
}