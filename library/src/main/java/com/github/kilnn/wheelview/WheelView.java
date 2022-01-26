/*
 *  Android Wheel Control.
 *  https://code.google.com/p/android-wheel/
 *
 *  Copyright 2011 Yuri Kanivets
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.github.kilnn.wheelview;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import com.github.kilnn.wheelview.adapters.WheelViewAdapter;

import java.util.LinkedList;
import java.util.List;


/**
 * Numeric wheel view.
 *
 * @author Yuri Kanivets
 */
public class WheelView extends View {

    private final Paint mHighlightPaint;
    private final PorterDuffXfermode mXfermode;
    private final LinearLayout mItemsLayout; // Items layout
    private int mVisibleItems;// Count of visible items
    private boolean isCyclic;//是否可以循环滚动
    private boolean mDrawHighlight;

    private WheelViewAdapter mViewAdapter;
    private int mFirstItem;// The number of first item in layout
    private int mCurrentItem = 0;

    // Recycle
    private final WheelRecycle mRecycle = new WheelRecycle(this);

    //Listeners//
    private final List<OnWheelChangedListener> mChangingListeners = new LinkedList<>();
    private final List<OnWheelScrollListener> mScrollingListeners = new LinkedList<>();
    private final List<OnWheelClickedListener> mClickingListeners = new LinkedList<>();

    //Background,Shadow,Divider//
    private Drawable mCenterBackground;//滑轮中心区域的背景
    private Drawable mCenterForeground;//滑轮中心区域的前景(用于设置高光效果)
    private boolean isDrawShadows;//是否绘制滑轮上部和下部的阴影
    private GradientDrawable mTopShadow;
    private GradientDrawable mBottomShadow;
    private Drawable mDividerDrawable;//滑轮中心区域上下两条分割线

    // Scrolling
    private final WheelScroller mScroller;
    private boolean isScrollingPerformed;
    private int mScrollingOffset;

    //Temp
    private int mItemHeight = 0;//Item的高度，避免每次计算
    private final Rect mTempRect = new Rect();

    // Adapter listener
    private final DataSetObserver mDataObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            invalidateWheel(false);
        }

        @Override
        public void onInvalidated() {
            invalidateWheel(true);
        }
    };

    /**
     * Constructor
     */
    public WheelView(Context context) {
        this(context, null);
    }

    /**
     * Constructor
     */
    public WheelView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Constructor
     */
    public WheelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WheelView, defStyleAttr, 0);
        mVisibleItems = a.getInt(R.styleable.WheelView_visible_items, 5);
        isCyclic = a.getBoolean(R.styleable.WheelView_is_cyclic, false);
        mDrawHighlight = a.getBoolean(R.styleable.WheelView_draw_highlight, false);
        int highlightColor = a.getColor(R.styleable.WheelView_highlight_color, Color.WHITE);
        mCenterBackground = a.getDrawable(R.styleable.WheelView_center_background);
        mCenterForeground = a.getDrawable(R.styleable.WheelView_center_foreground);
        isDrawShadows = a.getBoolean(R.styleable.WheelView_is_draw_shadows, true);
        int shadowsColor = a.getColor(R.styleable.WheelView_shadows_color, Color.WHITE);
        setShadowColor(shadowsColor);
        mDividerDrawable = a.getDrawable(R.styleable.WheelView_divider);
        a.recycle();

        mHighlightPaint = new Paint();
        mHighlightPaint.setColor(highlightColor);
        mXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);

        // Scrolling listener
        WheelScroller.ScrollingListener scrollingListener = new WheelScroller.ScrollingListener() {
            @Override
            public void onStarted() {
                isScrollingPerformed = true;
                notifyScrollingListenersAboutStart();
            }

            @Override
            public void onScroll(int distance) {
                doScroll(distance);

                int height = getHeight();
                if (mScrollingOffset > height) {
                    mScrollingOffset = height;
                    mScroller.stopScrolling();
                } else if (mScrollingOffset < -height) {
                    mScrollingOffset = -height;
                    mScroller.stopScrolling();
                }
            }

            @Override
            public void onFinished() {
                if (isScrollingPerformed) {
                    notifyScrollingListenersAboutEnd();
                    isScrollingPerformed = false;
                }

                mScrollingOffset = 0;
                invalidate();
            }

            @Override
            public void onJustify() {
                if (Math.abs(mScrollingOffset) > WheelScroller.MIN_DELTA_FOR_SCROLLING) {
                    mScroller.scroll(mScrollingOffset, 0);
                }
            }
        };
        mScroller = new WheelScroller(getContext(), scrollingListener);
        mItemsLayout = new LinearLayout(getContext());
        mItemsLayout.setOrientation(LinearLayout.VERTICAL);
        mItemsLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    }

    /**
     * Set the the specified scrolling interpolator
     *
     * @param interpolator the interpolator
     */
    public void setInterpolator(Interpolator interpolator) {
        mScroller.setInterpolator(interpolator);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled() || getViewAdapter() == null) {
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!isScrollingPerformed) {
                    int distance = (int) event.getY() - getHeight() / 2;
                    if (distance > 0) {
                        distance += getItemHeight() / 2;
                    } else {
                        distance -= getItemHeight() / 2;
                    }
                    int items = distance / getItemHeight();
                    if (items != 0 && isValidItemIndex(mCurrentItem + items)) {
                        notifyClickListenersAboutClick(mCurrentItem + items);
                    }
                }
                break;
        }
        return mScroller.onTouchEvent(event);
    }

    /**
     * Scrolls the wheel
     *
     * @param delta the scrolling value
     */
    private void doScroll(int delta) {
        mScrollingOffset += delta;

        int itemHeight = getItemHeight();
        int count = mScrollingOffset / itemHeight;

        int pos = mCurrentItem - count;
        int itemCount = mViewAdapter.getItemsCount();

        int fixPos = mScrollingOffset % itemHeight;
        if (Math.abs(fixPos) <= itemHeight / 2) {
            fixPos = 0;
        }
        if (isCyclic && itemCount > 0) {
            if (fixPos > 0) {
                pos--;
                count++;
            } else if (fixPos < 0) {
                pos++;
                count--;
            }
            // fix position by rotating
            while (pos < 0) {
                pos += itemCount;
            }
            pos %= itemCount;
        } else {
            //
            if (pos < 0) {
                count = mCurrentItem;
                pos = 0;
            } else if (pos >= itemCount) {
                count = mCurrentItem - itemCount + 1;
                pos = itemCount - 1;
            } else if (pos > 0 && fixPos > 0) {
                pos--;
                count++;
            } else if (pos < itemCount - 1 && fixPos < 0) {
                pos++;
                count--;
            }
        }

        int offset = mScrollingOffset;
        if (pos != mCurrentItem) {
            setCurrentItem(pos, false);
        } else {
            invalidate();
        }

        // update offset
        mScrollingOffset = offset - count * itemHeight;
        if (mScrollingOffset > getHeight()) {
            mScrollingOffset = mScrollingOffset % getHeight() + getHeight();
        }
    }

    /**
     * Stops scrolling
     */
    public void stopScrolling() {
        mScroller.stopScrolling();
    }

    /**
     * Scroll the wheel
     *
     * @param itemsToScroll items to scroll
     * @param time          scrolling duration
     */
    public void scroll(int itemsToScroll, int time) {
        int distance = itemsToScroll * getItemHeight() - mScrollingOffset;
        mScroller.scroll(distance, time);
    }

    /**
     * Gets view adapter
     *
     * @return the view adapter
     */
    public WheelViewAdapter getViewAdapter() {
        return mViewAdapter;
    }

    /**
     * Sets view adapter. Usually new adapters contain different views, so
     * it needs to rebuild view by calling measure().
     *
     * @param viewAdapter the view adapter
     */
    public void setViewAdapter(WheelViewAdapter viewAdapter) {
        if (this.mViewAdapter != null) {
            this.mViewAdapter.unregisterDataSetObserver(mDataObserver);
        }
        this.mViewAdapter = viewAdapter;
        if (this.mViewAdapter != null) {
            this.mViewAdapter.registerDataSetObserver(mDataObserver);
        }
        invalidateWheel(true);
    }

    /**
     * Gets current value
     *
     * @return the current value
     */
    public int getCurrentItem() {
        return mCurrentItem;
    }

    /**
     * Sets the current item w/o animation. Does nothing when index is wrong.
     *
     * @param index the item index
     */
    public void setCurrentItem(int index) {
        setCurrentItem(index, false);
    }

    /**
     * Sets the current item. Does nothing when index is wrong.
     *
     * @param index    the item index
     * @param animated the animation flag
     */
    public void setCurrentItem(int index, boolean animated) {
        if (mViewAdapter == null || mViewAdapter.getItemsCount() == 0) {
            return; // throw?
        }

        int itemCount = mViewAdapter.getItemsCount();
        if (index < 0 || index >= itemCount) {
            if (isCyclic) {
                while (index < 0) {
                    index += itemCount;
                }
                index %= itemCount;
            } else {
                return; // throw?
            }
        }
        if (index != mCurrentItem) {
            if (animated) {
                int itemsToScroll = index - mCurrentItem;
                if (isCyclic) {
                    int scroll = itemCount + Math.min(index, mCurrentItem) - Math.max(index, mCurrentItem);
                    if (scroll < Math.abs(itemsToScroll)) {
                        itemsToScroll = itemsToScroll < 0 ? scroll : -scroll;
                    }
                }
                scroll(itemsToScroll, 0);
            } else {
                mScrollingOffset = 0;
                int old = mCurrentItem;
                mCurrentItem = index;
                notifyChangingListeners(old, mCurrentItem);
                invalidate();
            }
        }
    }

    /**
     * Invalidates wheel
     *
     * @param clearCaches if true then cached views will be clear
     */
    public void invalidateWheel(boolean clearCaches) {
        if (clearCaches) {
            mRecycle.clearAll();
            mItemsLayout.removeAllViews();
            mScrollingOffset = 0;
        } else {
            // cache all items
            mRecycle.recycleItems(mItemsLayout, mFirstItem, new ItemsRange());
        }
        invalidate();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        buildViewForMeasuring();
        int width = calculateWidth(widthSize, widthMode);
        int height = calculateHeight(heightSize, heightMode);
        setMeasuredDimension(width, height);
    }

    /**
     * Calculates control width and creates text layouts
     *
     * @param widthSize the input layout width
     * @param mode      the layout mode
     * @return the calculated control width
     */
    private int calculateWidth(int widthSize, int mode) {
        mItemsLayout.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        int width = mItemsLayout.getMeasuredWidth();

        int padding = getPaddingLeftCompat() + getPaddingRightCompat();


        if (mode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            width += padding;
            // Check against our minimum width
            width = Math.max(width, getSuggestedMinimumWidth());
            if (mode == MeasureSpec.AT_MOST && widthSize < width) {
                width = widthSize;
            }
        }
        mItemsLayout.measure(MeasureSpec.makeMeasureSpec(width - padding, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        return width;
    }

    private int calculateHeight(int heightSize, int mode) {
        int height;
        if (mode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            //使用第一个child的高度作为判断依据。外部使用时，应保证所有child高度是一致的
            View childView = mItemsLayout.getChildAt(0);
            if (childView != null) {
                mItemHeight = childView.getMeasuredHeight();
            }
            int desired = mItemHeight * mVisibleItems + getPaddingTop() + getPaddingBottom();
            height = Math.max(desired, getSuggestedMinimumHeight());
            if (mode == MeasureSpec.AT_MOST) {
                height = Math.min(height, heightSize);
            }
        }
        return height;
    }

    /**
     * Builds view for measuring
     */
    private void buildViewForMeasuring() {
        // clear all items
        mRecycle.recycleItems(mItemsLayout, mFirstItem, new ItemsRange());
        // add views
        int addItems = mVisibleItems / 2;
        for (int i = mCurrentItem + addItems; i >= mCurrentItem - addItems; i--) {
            if (addViewItem(i, true)) {
                mFirstItem = i;
            }
        }
    }

    /**
     * Adds view for item to items layout
     *
     * @param index the item index
     * @param first the flag indicates if view should be first
     * @return true if corresponding item exists and is added
     */
    private boolean addViewItem(int index, boolean first) {
        View view = getItemView(index);
        if (view != null) {
            if (first) {
                mItemsLayout.addView(view, 0);
            } else {
                mItemsLayout.addView(view);
            }
            return true;
        }
        return false;
    }

    /**
     * Returns view for specified item
     *
     * @param index the item index
     * @return item view or empty view if index is out of bounds
     */
    @Nullable
    private View getItemView(int index) {
        if (mViewAdapter == null || mViewAdapter.getItemsCount() == 0) {
            return null;
        }
        int count = mViewAdapter.getItemsCount();
        if (!isValidItemIndex(index)) {
            return mViewAdapter.getEmptyItem(mRecycle.getEmptyItem(), mItemsLayout);
        } else {
            while (index < 0) {
                index = count + index;
            }
        }
        index %= count;
        return mViewAdapter.getItem(index, mRecycle.getItem(), mItemsLayout);
    }

    /**
     * Checks whether item index is valid
     *
     * @param index the item index
     * @return true if item index is not out of bounds or the wheel is cyclic
     */
    private boolean isValidItemIndex(int index) {
        return mViewAdapter != null && mViewAdapter.getItemsCount() > 0 &&
                (isCyclic || index >= 0 && index < mViewAdapter.getItemsCount());
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        layout(r - l, b - t);
    }

    /**
     * Sets layouts width and height
     *
     * @param width  the layout width
     * @param height the layout height
     */
    private void layout(int width, int height) {
        mItemsLayout.layout(0, 0, width - getPaddingLeftCompat() - getPaddingRightCompat(), height - getPaddingTop() - getPaddingBottom());
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int centerTop = getHeight() / 2 - getItemHeight() / 2;
        int centerBottom = getHeight() / 2 + getItemHeight() / 2;
        int drawAreaLeft = getPaddingLeftCompat();
        int drawAreaRight = getWidth() - getPaddingRightCompat();
        int drawAreaTop = getPaddingTop();
        int drawAreaBottom = getHeight() - getPaddingBottom();

        if (mViewAdapter != null && mViewAdapter.getItemsCount() > 0) {
            updateView();

            //绘制Center背景
            if (mCenterBackground != null) {
                mTempRect.set(drawAreaLeft, centerTop, drawAreaRight, centerBottom);
                mCenterBackground.setBounds(mTempRect);
                mCenterBackground.draw(canvas);
            }

            //绘制Item
            int save = canvas.saveLayer(drawAreaLeft, drawAreaTop, drawAreaRight, drawAreaBottom, mHighlightPaint, Canvas.ALL_SAVE_FLAG);
            mHighlightPaint.setXfermode(null);
            canvas.save();
            int top = (mCurrentItem - mFirstItem) * getItemHeight() + (getItemHeight() - getHeight()) / 2;
            canvas.translate(drawAreaLeft, -top + mScrollingOffset);
            mItemsLayout.draw(canvas);
            canvas.restore();

            if (mDrawHighlight) {
                mHighlightPaint.setXfermode(mXfermode);
                mTempRect.set(drawAreaLeft, centerTop, drawAreaRight, centerBottom);
                canvas.clipRect(mTempRect);
                canvas.drawRect(mTempRect, mHighlightPaint);
                mHighlightPaint.setXfermode(null);
            }

            canvas.restoreToCount(save);

            //绘制Divider
            if (mDividerDrawable != null) {
                mTempRect.set(drawAreaLeft, centerTop, drawAreaRight, centerTop + mDividerDrawable.getIntrinsicHeight());
                mDividerDrawable.setBounds(mTempRect);
                mDividerDrawable.draw(canvas);

                mTempRect.set(drawAreaLeft, centerBottom - mDividerDrawable.getIntrinsicHeight(), drawAreaRight, centerBottom);
                mDividerDrawable.setBounds(mTempRect);
                mDividerDrawable.draw(canvas);
            }

            //绘制Center前景
            if (mCenterForeground != null) {
                mTempRect.set(drawAreaLeft, centerTop, drawAreaRight, centerBottom);
                mCenterForeground.setBounds(mTempRect);
                mCenterForeground.draw(canvas);
            }
        }

        if (isDrawShadows) {
            mTopShadow.setBounds(drawAreaLeft, drawAreaTop, drawAreaRight, centerTop);
            mTopShadow.draw(canvas);
            mBottomShadow.setBounds(drawAreaLeft, centerBottom, drawAreaRight, drawAreaBottom);
            mBottomShadow.draw(canvas);
        }
    }

    /**
     * Updates view. Rebuilds items and label if necessary, recalculate items sizes.
     */
    private void updateView() {
        if (rebuildItems()) {
            calculateWidth(getWidth(), MeasureSpec.EXACTLY);
            layout(getWidth(), getHeight());
        }
    }

    /**
     * Rebuilds wheel items if necessary. Caches all unused items.
     *
     * @return true if items are rebuilt
     */
    private boolean rebuildItems() {
        boolean updated;
        final ItemsRange range = getItemsRange();
        if (range == null) return false;

        int firstUpdate = mRecycle.recycleItems(mItemsLayout, mFirstItem, range);
        updated = mFirstItem != firstUpdate;
        mFirstItem = firstUpdate;

        if (!updated) {
            updated = mFirstItem != range.getFirst() || mItemsLayout.getChildCount() != range.getCount();
        }

        if (mFirstItem > range.getFirst() && mFirstItem <= range.getLast()) {
            for (int i = mFirstItem - 1; i >= range.getFirst(); i--) {
                if (!addViewItem(i, true)) {
                    break;
                }
                mFirstItem = i;
            }
        } else {
            mFirstItem = range.getFirst();
        }

        int first = mFirstItem;
        for (int i = mItemsLayout.getChildCount(); i < range.getCount(); i++) {
            if (!addViewItem(mFirstItem + i, false) && mItemsLayout.getChildCount() == 0) {
                first++;
            }
        }
        mFirstItem = first;

        return updated;
    }

    /**
     * Returns height of wheel item
     *
     * @return the item height
     */
    private int getItemHeight() {
        if (mItemHeight != 0) {
            return mItemHeight;
        }
        View childView = mItemsLayout.getChildAt(0);
        if (childView != null) {
            mItemHeight = childView.getMeasuredHeight();
            return mItemHeight;
        }
        return (getHeight() - getPaddingTop() - getPaddingBottom()) / mVisibleItems;
    }

    /**
     * Calculates range for wheel items
     *
     * @return the items range
     */
    @Nullable
    private ItemsRange getItemsRange() {
        if (getItemHeight() == 0) {
            return null;
        }

        int first = mCurrentItem;
        int count = 1;

        while (count * getItemHeight() < getHeight()) {
            first--;
            count += 2; // top + bottom items
        }

        if (mScrollingOffset != 0) {
            if (mScrollingOffset > 0) {
                first--;
            }
            count++;

            // process empty items above the first or below the second
            int emptyItems = mScrollingOffset / getItemHeight();
            first -= emptyItems;
            count += Math.asin(emptyItems);
        }
        return new ItemsRange(first, count);
    }

    private int getPaddingLeftCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return getPaddingStart();
        } else {
            return getPaddingLeft();
        }
    }

    private int getPaddingRightCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return getPaddingEnd();
        } else {
            return getPaddingRight();
        }
    }

    /**
     * Gets count of visible items
     *
     * @return the count of visible items
     */
    public int getVisibleItems() {
        return mVisibleItems;
    }

    /**
     * Sets the desired count of visible items.
     * Actual amount of visible items depends on wheel layout parameters.
     * To apply changes and rebuild view call measure().
     *
     * @param count the desired count for visible items
     */
    public void setVisibleItems(int count) {
        mVisibleItems = count;
    }

    /**
     * Tests if wheel is cyclic. That means before the 1st item there is shown the last one
     *
     * @return true if wheel is cyclic
     */
    public boolean isCyclic() {
        return isCyclic;
    }

    /**
     * Set wheel cyclic flag
     *
     * @param isCyclic the flag to set
     */
    public void setCyclic(boolean isCyclic) {
        this.isCyclic = isCyclic;
    }

    public void setCenterBackground(Drawable drawable) {
        this.mCenterBackground = drawable;
    }

    public void setCenterForeground(Drawable drawable) {
        this.mCenterForeground = drawable;
    }

    /**
     * Determine whether shadows are drawn
     *
     * @return true is shadows are drawn
     */
    public boolean isDrawShadows() {
        return isDrawShadows;
    }

    /**
     * Set whether shadows should be drawn
     *
     * @param drawShadows flag as true or false
     */
    public void setDrawShadows(boolean drawShadows) {
        this.isDrawShadows = drawShadows;
    }

    /**
     * Set the shadow gradient color
     *
     * @param start  gradient start
     * @param middle gradient middle
     * @param end    gradient end
     */
    public void setShadowColor(int start, int middle, int end) {
        int[] shadowColors = new int[]{start, middle, end};
        mTopShadow = new GradientDrawable(Orientation.TOP_BOTTOM, shadowColors);
        mBottomShadow = new GradientDrawable(Orientation.BOTTOM_TOP, shadowColors);
    }

    /**
     * Parser a color to gradient shadow
     *
     * @param color color value
     */
    public void setShadowColor(@ColorInt int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        int alpha = Color.alpha(color);

        int startAlpha = (int) (0.9 * alpha);
        int middleAlpha = (int) (0.75 * alpha);
        int endAlpha = (int) (0.6 * alpha);
        setShadowColor(
                Color.argb(startAlpha, r, g, b),
                Color.argb(middleAlpha, r, g, b),
                Color.argb(endAlpha, r, g, b)
        );
    }

    public void setDivider(@Nullable Drawable drawable) {
        mDividerDrawable = drawable;
    }

    public void setDrawHighlight(boolean drawHighlight) {
        this.mDrawHighlight = drawHighlight;
    }

    public void setHighlightColor(@ColorInt int color) {
        mHighlightPaint.setColor(color);
    }

    /**
     * Adds wheel changing listener
     *
     * @param listener the listener
     */
    public void addChangingListener(OnWheelChangedListener listener) {
        mChangingListeners.add(listener);
    }

    /**
     * Removes wheel changing listener
     *
     * @param listener the listener
     */
    public void removeChangingListener(OnWheelChangedListener listener) {
        mChangingListeners.remove(listener);
    }

    /**
     * Notifies changing listeners
     *
     * @param oldValue the old wheel value
     * @param newValue the new wheel value
     */
    protected void notifyChangingListeners(int oldValue, int newValue) {
        for (OnWheelChangedListener listener : mChangingListeners) {
            listener.onChanged(this, oldValue, newValue);
        }
    }

    /**
     * Adds wheel scrolling listener
     *
     * @param listener the listener
     */
    public void addScrollingListener(OnWheelScrollListener listener) {
        mScrollingListeners.add(listener);
    }

    /**
     * Removes wheel scrolling listener
     *
     * @param listener the listener
     */
    public void removeScrollingListener(OnWheelScrollListener listener) {
        mScrollingListeners.remove(listener);
    }

    /**
     * Notifies listeners about starting scrolling
     */
    protected void notifyScrollingListenersAboutStart() {
        for (OnWheelScrollListener listener : mScrollingListeners) {
            listener.onScrollingStarted(this);
        }
    }

    /**
     * Notifies listeners about ending scrolling
     */
    protected void notifyScrollingListenersAboutEnd() {
        for (OnWheelScrollListener listener : mScrollingListeners) {
            listener.onScrollingFinished(this);
        }
    }

    /**
     * Adds wheel clicking listener
     *
     * @param listener the listener
     */
    public void addClickingListener(OnWheelClickedListener listener) {
        mClickingListeners.add(listener);
    }

    /**
     * Removes wheel clicking listener
     *
     * @param listener the listener
     */
    public void removeClickingListener(OnWheelClickedListener listener) {
        mClickingListeners.remove(listener);
    }

    /**
     * Notifies listeners about clicking
     */
    protected void notifyClickListenersAboutClick(int item) {
        for (OnWheelClickedListener listener : mClickingListeners) {
            listener.onItemClicked(this, item);
        }
    }
}
