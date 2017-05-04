package com.example.flowlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * A FlowLayout is a view group that can automatically change to next line
 * when there is no place in the current line
 *
 * @author anrou_hu
 */

public class FlowLayout extends ViewGroup {

    private static final int UNSETTLED_MAX_LINE = -1;
    private static final int INVALID_INDEX = -1;

    private int mGravity;
    private int mMaxLine;

    //the horizontal spacing between the child views
    private int mChildHorizontalSpacing;

    //the vertical spacing between the child views
    private int mChildVerticalSpacing;

    //used for temp remembering remaining vertical spacing
    private int mRemainVerticalSpacing;

    //used for temp remembering remaining horizontal spacing of every line
    private ArrayList<Integer> mRemainHorizontalSpacings = new ArrayList<>();


    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }


    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FlowLayout);
        try {
            mChildHorizontalSpacing = a.getDimensionPixelSize(R.styleable.FlowLayout_childHorizontalSpacing, 0);
            mChildVerticalSpacing = a.getDimensionPixelSize(R.styleable.FlowLayout_childVerticalSpacing, 0);
            mGravity = a.getInt(R.styleable.FlowLayout_android_gravity, Gravity.CENTER_HORIZONTAL);
            mMaxLine = a.getInt(R.styleable.FlowLayout_maxLine, UNSETTLED_MAX_LINE);
        } finally {
            a.recycle();
        }
    }


    /**
     * Measure the view and its child views.
     * Child view will change to next line automatically when the child view can't fit in current line, if needed.
     * However, if user wants to limit the count of line, sets maxLine.
     *
     *
     * Below positions are all relative positions. (padding is excluded)
     * We'll change all the positions to absolute positions when onLayout().
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //measure content size
        int widthSize = MeasureSpec.getSize(widthMeasureSpec) - getPaddingRight() - getPaddingLeft();
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        boolean willHeightGrow = widthMode != MeasureSpec.UNSPECIFIED;
        boolean isHeightFixed = heightMode != MeasureSpec.UNSPECIFIED;

        //for record layout height and width
        int height = 0;
        int width = 0;

        //for recording where the next child view position
        int nextXPos = 0;
        int nextYPos = 0;

        //current position in line (index position starts from 0)
        int currentPos = 0;

        //current line index starts from 0
        int lineIndex = 0;



        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);


            //2. To add spacing between the previous child view and the next child view if needed
            if (currentPos > 0 && nextXPos + mChildHorizontalSpacing + child.getMeasuredWidth() <= widthSize) {
                nextXPos += mChildHorizontalSpacing;
            }

            //3. To do new next line if needed (if willHeightGrow == false, there is no need to change line)
            if (willHeightGrow && nextXPos + child.getMeasuredWidth() > widthSize) {
                //To calculate current line remain spacing (it will be used in setting of horizontal gravity)
                mRemainHorizontalSpacings.add(lineIndex, widthSize - nextXPos);
                lineIndex++;

                //To break, if next line is exceeding max line
                if (mMaxLine != UNSETTLED_MAX_LINE && lineIndex >= mMaxLine) {
                    break;
                }

                //initial currentPos and nextXPos, and update nextYPos (adds a childVerticalSpacing)
                currentPos = 0;
                nextXPos = 0;
                nextYPos = height + mChildVerticalSpacing;
            }

            //4. To set LayoutParams to child view
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            lp.x = nextXPos;
            lp.y = nextYPos;
            lp.pos = currentPos;
            lp.lineIndex = lineIndex;


            //5. To update nextXPos and currentPos for next child view, and update layout height and width
            nextXPos += child.getMeasuredWidth();
            height = Math.max(height, nextYPos + child.getMeasuredHeight());
            width = Math.max(width, nextXPos);

            currentPos++;
        }


        mRemainHorizontalSpacings.add(lineIndex, widthSize - nextXPos);

        //if height is fixed, calculate remaining vertical spacing (it will be used in setting of vertical gravity)
        if (isHeightFixed) {
            int spacing = heightSize - height;
            mRemainVerticalSpacing = spacing > 0 ? spacing : 0;
        }


        //6. add padding
        height += getPaddingTop() + getPaddingBottom();
        width += getPaddingLeft() + getPaddingRight();

        setMeasuredDimension(resolveSize(width, widthMeasureSpec),
            resolveSize(height, heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();

            if (lp.lineIndex == INVALID_INDEX) {
                break;
            }

            //trans relative position to absolute position
            int leftSpacing = mRemainHorizontalSpacings.get(lp.lineIndex);
            lp.x += getPaddingLeft() + calculateHorizontalGravitySpacing(leftSpacing);
            lp.y += getPaddingTop() + calculateVerticalGravitySpacing(mRemainVerticalSpacing);
            child.layout(lp.x, lp.y, lp.x + child.getMeasuredWidth(), lp.y + child.getMeasuredHeight());
        }
    }


    private int calculateHorizontalGravitySpacing(int remainingSpacing) {
        switch (mGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
            case Gravity.LEFT:
                return 0;

            case Gravity.CENTER_HORIZONTAL:
                return remainingSpacing / 2;

            case Gravity.RIGHT:
                return remainingSpacing;

            default:
                return 0;
        }
    }

    private int calculateVerticalGravitySpacing(int remainingSpacing) {
        switch (mGravity & Gravity.VERTICAL_GRAVITY_MASK) {
            case Gravity.TOP:
                return 0;

            case Gravity.CENTER_VERTICAL:
                return remainingSpacing / 2;

            case Gravity.BOTTOM:
                return remainingSpacing;

            default:
                return 0;
        }
    }


    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p.width, p.height);
    }


    public static class LayoutParams extends ViewGroup.LayoutParams {
        int x;
        int y;
        int pos;
        int lineIndex = INVALID_INDEX;

        public LayoutParams(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public LayoutParams(int w, int h) {
            super(w, h);
        }
    }

}
