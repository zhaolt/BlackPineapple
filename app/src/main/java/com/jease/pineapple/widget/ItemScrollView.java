package com.jease.pineapple.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;

import com.jease.pineapple.R;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by zhaoliangtai on 2017/6/1.
 */

public class ItemScrollView extends LinearLayout implements View.OnClickListener {

    private static final String TAG = ItemScrollView.class.getSimpleName();

    /**
     * 选中模式禁用-普通模式
     */
    public static final int MODE_DEFAULT_SELECTION_DISABLED = -1;
    /**
     * 控件禁用模式-不可滑动
     */
    public static final int MODE_FORBID_USE = 2;


    private Scroller mScroller;

    private int mCurrentItem = 0;

    private int mItemMargin;

    private int mItemCount;

    private List<int[]> mChildArea;
    private List<Integer> mChildCenterX;

    private OnItemSelectedListener mOnItemSelectedListener;

    private List<String> mItemValues;

    private float mDownX;
    private float mMoveX;
    private float mLastMoveX;

    private int mPointerId;

    private int mMode = MODE_DEFAULT_SELECTION_DISABLED;

    private int mTextSize;

    private boolean isScale;

    public ItemScrollView(Context context) {
        this(context, null);
    }

    public ItemScrollView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        mOnItemSelectedListener = listener;
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.getTheme()
                .obtainStyledAttributes(
                        attrs, R.styleable.ItemScrollView, 0, 0);
        mItemMargin = (int) (typedArray.getDimension(R.styleable.ItemScrollView_item_interval,
                40f) / 2f);
        mTextSize = (int) typedArray.getDimension(R.styleable.ItemScrollView_android_textSize,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                        12f, getResources().getDisplayMetrics()));
        isScale = typedArray.getBoolean(R.styleable.ItemScrollView_item_scale, false);
        typedArray.recycle();
        mScroller = new Scroller(context);
        mItemValues = new ArrayList<>();
        mChildArea = new ArrayList<>();
        mChildCenterX = new ArrayList<>();
    }

    public void addIndicator(List<String> items) {
        if (null == items || items.isEmpty()) {
            return;
        }
        mItemValues.clear();
        removeAllViews();
        mItemValues.addAll(items);
        for (int i = 0; i < mItemValues.size(); i++) {
            TextView textView = new TextView(getContext());
            textView.setText(mItemValues.get(i));
            if (i == mCurrentItem) {
                textView.setTextColor(getResources().getColor(R.color.white));
            } else {
                textView.setTextColor(getResources().getColor(R.color.gray));
            }
            textView.setLines(1);
            LayoutParams ll = new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);
            ll.setMargins(mItemMargin, 0, mItemMargin, 0);
            ll.gravity = Gravity.CENTER;
            textView.setLayoutParams(ll);
            textView.setOnClickListener(this);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
            addView(textView);
        }
        mItemCount = mItemValues.size();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        for (int i = 0; i < getChildCount(); i++) {
            View childView = getChildAt(i);
            measureChild(childView, widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        View v = getChildAt(0);
        int delta = getWidth() / 2 - v.getLeft() - v.getWidth() / 2;
        for (int i = 0; i < getChildCount(); i++) {
            View v1 = getChildAt(i);
            v1.layout(v1.getLeft() + delta, v1.getTop(), v1.getRight() + delta, v1.getBottom());
        }
        mChildArea.clear();
        mChildCenterX.clear();
        int left = 0 - getChildAt(0).getWidth() / 2;
        int lastWHalf = 0;
        int loc = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View v1 = getChildAt(i);
            int vw = v1.getWidth() + mItemMargin * 2;
            int[] vArea = new int[2];
            vArea[0] = left;
            vArea[1] = left + vw;
            left += vw;
            mChildArea.add(vArea);

            if (i == 0) {
                mChildCenterX.add(0);
            } else {
                loc += lastWHalf + mItemMargin * 2 * i + v1.getWidth() / 2;
                mChildCenterX.add(loc);
            }
            lastWHalf = v1.getWidth() / 2;
        }
    }

    public void scrollLeft() {
        if (mCurrentItem >= mItemCount - 1) {
            mCurrentItem = mItemCount - 1;
            return;
        }
        View currentItem = getChildAt(mCurrentItem);
        int offset = (currentItem.getWidth() / 2 + mItemMargin * 2)
                + (getChildAt(mCurrentItem + 1).getWidth() / 2);
        mCurrentItem++;
        smoothScrollBy(offset, 0);
        Log.d(TAG, "scroll left offset: " + offset);
        updateSelectedItemColor(mCurrentItem);
    }


    public void scrollRight() {
        if (mCurrentItem <= 0) {
            mCurrentItem = 0;
            return;
        }
        View currentItem = getChildAt(mCurrentItem);
        int offset = -((currentItem.getWidth() / 2 + mItemMargin * 2)
                + (getChildAt(mCurrentItem - 1).getWidth() / 2));
        mCurrentItem--;
        smoothScrollBy(offset, 0);
        Log.d(TAG, "scroll right offset: " + offset);
        updateSelectedItemColor(mCurrentItem);
    }

    public void setMode(int mode) {
        if (mMode != mode) {
            mMode = mode;
            applyNewMode(mode);
        }
    }

    private void updateSelectedItemColor(int position) {
        for (int i = 0; i < mItemCount; i++) {
            TextView tv = (TextView) getChildAt(i);
            if (i == position) {
                tv.setTextColor(getResources().getColor(R.color.white));
                if (isScale) {
                    tv.animate().scaleX(1.2f).scaleY(1.2f).setDuration(200)
                            .setInterpolator(new AccelerateInterpolator()).start();
                }
            } else {
                tv.setTextColor(getResources().getColor(R.color.gray));
                if (isScale) {
                    tv.setScaleX(1f);
                    tv.setScaleY(1f);
                }
            }
        }
    }

    private void applyNewMode(int mode) {
        switch (mode) {
            case MODE_DEFAULT_SELECTION_DISABLED:
                setAlpha(1.0f);
                break;
            case MODE_FORBID_USE:
                setAlpha(0.3f);
                break;
        }
    }

    public void setItemIndex(int index) {
        if (index == mCurrentItem) {
            return;
        }
        if (index > mCurrentItem) {
            // 在右边
            int contentWidth = getContentItemWidth(mCurrentItem, index);
            int w = getChildAt(mCurrentItem).getWidth() / 2
                    + getChildAt(index).getWidth() / 2 + mItemMargin * 2;
            mCurrentItem = index;
            smoothScrollBy(contentWidth + w, 0);
        } else {
            // 在左边
            int contentWidth = getContentItemWidth(index, mCurrentItem);
            int w = getChildAt(mCurrentItem).getWidth() / 2
                    + getChildAt(index).getWidth() / 2 + mItemMargin * 2;
            mCurrentItem = index;
            smoothScrollBy(-(contentWidth + w), 0);
        }

        updateSelectedItemColor(mCurrentItem);
    }

    private void smoothScrollTo(int fx, int fy) {
        int dx = fx - mScroller.getFinalX();
        int dy = fy - mScroller.getFinalY();
        smoothScrollBy(dx, dy);
    }

    private void smoothScrollBy(int dx, int dy) {
        Log.d(TAG, "startX = " + mScroller.getFinalX() + ", distanceX = " + dx);
        mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), dx, dy);
        selectItem();
        invalidate();
    }

    private void smoothScrollBy(int startX, int startY, int dX, int dY) {
        mScroller.startScroll(startX, startY, dX, dY);
        selectItem();
        invalidate();
    }

    private void selectItem() {
        View v = getChildAt(mCurrentItem);
        if (!(v instanceof TextView)) {
            return;
        }
        TextView tv = (TextView) v;
        String value = tv.getText().toString().trim();
        int index = mItemValues.indexOf(value);
        if (mOnItemSelectedListener != null) {
            mOnItemSelectedListener.onItemSelected(tv, value, index);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN && ev.getEdgeFlags() != 0) {
            return false;
        }
        boolean isIntercept = false;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                isIntercept = !mScroller.isFinished();
                mPointerId = ev.getPointerId(0);
                mDownX = mLastMoveX = ev.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                int pointerIndex = ev.findPointerIndex(mPointerId);
                mMoveX = ev.getX(pointerIndex);
                float diff = Math.abs(mMoveX - mDownX);
                if (diff > 15) {
                    isIntercept = true;
                }
                if (isIntercept) {
                    mLastMoveX = mMoveX;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                break;
        }
        return isIntercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mMode == MODE_FORBID_USE) {
            return false;
        }
        final int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.i(TAG, "ACTION_DOWN");
                mDownX = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                mMoveX = event.getX();
                int scrolledX = (int) (mLastMoveX - mMoveX);
                scrollBy(scrolledX, 0);
                mLastMoveX = mMoveX;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (getScrollX() < mChildArea.get(0)[0]) {
                    int dX = -(getScrollX() - mChildCenterX.get(0));
                    mCurrentItem = 0;
                    smoothScrollBy(getScrollX(), 0, dX, 0);
                    updateSelectedItemColor(mCurrentItem);
                    return true;
                } else if (getScrollX() > mChildArea.get(mChildArea.size() - 1)[1]) {
                    int dX = -(getScrollX() - mChildCenterX.get(mChildCenterX.size() - 1));
                    mCurrentItem = mChildArea.size() - 1;
                    smoothScrollBy(getScrollX(), 0, dX, 0);
                    updateSelectedItemColor(mCurrentItem);
                    return true;
                }
                for (int i = 0; i < mChildArea.size(); i++) {
                    int[] vArea = mChildArea.get(i);
                    if (getScrollX() > vArea[0] && getScrollX() < vArea[1]) {
                        int dX = -(getScrollX() - mChildCenterX.get(i));
                        mCurrentItem = i;
                        smoothScrollBy(getScrollX(), 0, dX, 0);
                        updateSelectedItemColor(mCurrentItem);
                        return true;
                    }
                }
                Log.i(TAG, "ACTION_CANCEL | ACTION_UP");
                break;
        }
        return true;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            drawChild(canvas, v, getDrawingTime());
        }
    }

    @Override
    public void onClick(View v) {
        if (!(v instanceof TextView)) {
            return;
        }
        TextView tv = (TextView) v;
        String value = tv.getText().toString().trim();

        int index = mItemValues.indexOf(value);
        if (index == mCurrentItem) {
            return;
        }
        if (index > mCurrentItem) {
            // 在右边
            int contentWidth = getContentItemWidth(mCurrentItem, index);
            int w = getChildAt(mCurrentItem).getWidth() / 2
                    + getChildAt(index).getWidth() / 2 + mItemMargin * 2;
            mCurrentItem = index;
            smoothScrollBy(contentWidth + w, 0);
        } else {
            // 在左边
            int contentWidth = getContentItemWidth(index, mCurrentItem);
            int w = getChildAt(mCurrentItem).getWidth() / 2
                    + getChildAt(index).getWidth() / 2 + mItemMargin * 2;
            mCurrentItem = index;
            smoothScrollBy(-(contentWidth + w), 0);
        }

        updateSelectedItemColor(mCurrentItem);
    }

    private int getContentItemWidth(int startPosition, int endPosition) {
        int count = endPosition - startPosition - 1;
        if (count == 0) {
            return count;
        }
        int width = 0;
        for (startPosition += 1; startPosition < endPosition; startPosition++) {
            width += getChildAt(startPosition).getWidth() + mItemMargin * 2;
        }
        return width;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }


    public interface OnItemSelectedListener {
        void onItemSelected(View v, String value, int position);
    }

}
