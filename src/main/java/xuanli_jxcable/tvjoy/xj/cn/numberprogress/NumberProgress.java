package xuanli_jxcable.tvjoy.xj.cn.numberprogress;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by wzy on 2018/10/12.
 */

public class NumberProgress extends View {
    private int mMaxProgress = 100;
    private int mCurrentProgress = 0;
    private int mReachedBarColor;
    private int mUnreachedBarColor;
    private int mTextColor;
    private float mTextSize;
    private float mReachedBarHeight;
    private float mUnreachedBarHeight;
    private String mSuffix = "%";
    private String mPreffix = "";
    private static final int default_text_color = Color.rgb(66, 145, 241);
    private static final int default_reached_color = Color.rgb(66, 145, 241);
    private static final int default_unreached_color = Color.rgb(204, 204, 204);


    private final float default_progress_text_offset;
    private final float default_text_size;
    private final float default_reached_bar_height;
    private final float default_unreached_bar_height;

    /**
     * For save and restore instance of progressbar.
     */
    private static final String INSTANCE_STATE = "saved_instance";
    private static final String INSTANCE_TEXT_COLOR = "text_color";
    private static final String INSTANCE_TEXT_SIZE = "text_size";
    private static final String INSTANCE_REACHED_BAR_HEIGHT = "reached_bar_height";
    private static final String INSTANCE_REACHED_BAR_COLOR = "reached_bar_color";
    private static final String INSTANCE_UNREACHED_BAR_HEIGHT = "unreached_bar_height";
    private static final String INSTANCE_UNREACHED_BAR_COLOR = "unreached_bar_color";
    private static final String INSTANCE_MAX = "max";
    private static final String INSTANCE_PROGRESS = "progress";
    private static final String INSTANCE_SUFFIX = "suffix";
    private static final String INSTANCE_PREFIX = "prefix";
    private static final String INSTANCE_TEXT_VISIBILITY = "text_visibility";
    private static final int PROGRESS_TEXT_VISIBLE = 0;


    /**
     * The width of the text that to be drawn.
     */
    private float mDrawTextWidth;

    /**
     * The drawn text start.
     */
    private float mDrawTextStart;

    /**
     * The drawn text end.
     */
    private float mDrawTextEnd;

    /**
     * The text that to be drawn in onDraw().
     */
    private String mCurrentDrawText;

    /**
     * The Paint of the reached area.
     */
    private Paint mReachedBarPaint;
    /**
     * The Paint of the unreached area.
     */
    private Paint mUnreachedBarPaint;
    /**
     * The Paint of the progress text.
     */
    private Paint mTextPaint;

    /**
     * Unreached bar area to draw rect.
     */
    private RectF mUnreachedRectF = new RectF(0, 0, 0, 0);
    /**
     * Reached bar area rect.
     */
    private RectF mReachedRectF = new RectF(0, 0, 0, 0);

    /**
     * The progress text offset.
     */
    private float mOffset;

    /**
     * Determine if need to draw unreached area.
     */
    private boolean mDrawUnreachedBar = true;

    private boolean mDrawReachedBar = true;

    private boolean mIfDrawText = true;
    /**
     * Listener
     */
    private OnProgressBarListener mListener;

    public enum ProgressTextVisibility {
        Visible, Invisible
    }

    public NumberProgress(Context context) {
        this(context, null);
    }

    public NumberProgress(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NumberProgress(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //初始化默认信息
        default_reached_bar_height = dp2px(1.5f);
        default_unreached_bar_height = dp2px(1.0f);
        default_text_size = sp2px(10);
        default_progress_text_offset = dp2px(3.0f);
        //获取xml属性
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.NumberProgress, defStyleAttr, 0);
        mReachedBarColor = typedArray.getColor(R.styleable.NumberProgress_progress_reached_color, default_reached_color);
        mUnreachedBarColor = typedArray.getColor(R.styleable.NumberProgress_progress_unreached_color, default_unreached_color);
        mTextColor = typedArray.getColor(R.styleable.NumberProgress_progress_text_color, default_text_color);
        mTextSize = typedArray.getDimension(R.styleable.NumberProgress_progress_text_size, default_text_size);
        mReachedBarHeight = typedArray.getDimension(R.styleable.NumberProgress_progress_reached_bar_height, default_reached_bar_height);
        mUnreachedBarHeight = typedArray.getDimension(R.styleable.NumberProgress_progress_unreached_bar_height, default_unreached_bar_height);
        mOffset = typedArray.getDimension(R.styleable.NumberProgress_progress_text_offset, default_progress_text_offset);
        int text_visible = typedArray.getInt(R.styleable.NumberProgress_progress_text_visibility, PROGRESS_TEXT_VISIBLE);
        mIfDrawText = text_visible == PROGRESS_TEXT_VISIBLE;
        setProgress(typedArray.getInt(R.styleable.NumberProgress_progress_current, 0));
        setMax(typedArray.getInt(R.styleable.NumberProgress_progress_max, 100));
        typedArray.recycle();
        initializePainters();


    }

    private void initializePainters() {
        mReachedBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mReachedBarPaint.setColor(mReachedBarColor);
        mUnreachedBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mUnreachedBarPaint.setColor(mUnreachedBarColor);
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSize);

    }

    @Override
    protected int getSuggestedMinimumWidth() {
        return (int) mTextSize;
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        return (int) Math.max(mTextSize, Math.max(mUnreachedBarHeight, mReachedBarHeight));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measure(widthMeasureSpec, true), measure(heightMeasureSpec, false));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mIfDrawText) {
            calculateDrawRectf();
        } else {
            calculateDrawRectfWithoutprogressText();
        }

        if (mDrawReachedBar){
            canvas.drawRect(mReachedRectF,mReachedBarPaint);
        }
        if (mDrawUnreachedBar){
            canvas.drawRect(mUnreachedRectF,mUnreachedBarPaint);
        }
        if (mIfDrawText){
            canvas.drawText(mCurrentDrawText,mDrawTextStart,mDrawTextEnd,mTextPaint);
        }
    }

    public void calculateDrawRectf() {
        mCurrentDrawText = String.format("%d", getProgress() * 100 / getMax());
        mCurrentDrawText = mPreffix + mCurrentDrawText + mSuffix;
        mDrawTextWidth = mTextPaint.measureText(mCurrentDrawText);
        if (getProgress() == 0) {
            mDrawReachedBar = false;
            mDrawTextStart = getPaddingLeft();
        } else {
            mDrawReachedBar = true;
            mReachedRectF.left = getPaddingLeft();
            mReachedRectF.top = getHeight() / 2.0f - mReachedBarHeight / 2.0f;
            mReachedRectF.right = (getWidth() - getPaddingLeft() - getPaddingRight()) / (getMax() * 1.0f) * getProgress() - mOffset + getPaddingLeft();
            mReachedRectF.bottom = getHeight() / 2.0f + mReachedBarHeight / 2.0f;
            mDrawTextStart = (mReachedRectF.right + mOffset);
        }


        mDrawTextEnd = ((getHeight() / 2.0f) - ((mTextPaint.descent() + mTextPaint.ascent())) / 2.0f);
        if (mDrawTextStart + mDrawTextWidth >= getWidth() - getPaddingRight()) {
            mDrawTextStart = getWidth() - getPaddingRight() - mDrawTextWidth;
            mReachedRectF.right = mDrawTextStart - mOffset;
        }

        float unReachedBarStart = mDrawTextStart + mDrawTextWidth + mOffset;
        if (unReachedBarStart >= getWidth() - getPaddingRight()) {
            mDrawUnreachedBar = false;
        } else {
            mDrawUnreachedBar = true;
            mUnreachedRectF.left = unReachedBarStart;
            mUnreachedRectF.right = getWidth() - getPaddingRight();
            mUnreachedRectF.top = getHeight() / 2.0f + -mUnreachedBarHeight / 2.0f;
            mUnreachedRectF.bottom = getHeight() / 2.0f + mUnreachedBarHeight / 2.0f;
        }
    }

    public void calculateDrawRectfWithoutprogressText() {
        mReachedRectF.left = getPaddingLeft();
        mReachedRectF.top = getHeight() / 2.0f - mReachedBarHeight / 2.0f - mReachedBarHeight / 2.0f;
        mReachedRectF.right = (getWidth() - getPaddingLeft() - getPaddingRight()) / (getMax() * 1.0f) * getProgress() + getPaddingLeft();
        mReachedRectF.bottom = getHeight() / 2.0f + mReachedBarHeight / 2.0f;

        mUnreachedRectF.left = mReachedRectF.right;
        mUnreachedRectF.right = getWidth() - getPaddingRight();
        mUnreachedRectF.top = getHeight() / 2.0f + -mUnreachedBarHeight / 2.0f;
        mUnreachedRectF.bottom = getHeight() / 2.0f + mUnreachedBarHeight / 2.0f;


    }

    private int measure(int measureSpec, boolean isWidth) {
        int result;
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        int padding = isWidth ? getPaddingLeft() + getPaddingRight() : getPaddingTop() + getPaddingBottom();

//        MeasureSpec.AT_MOST       至多测量模式   当宽或高设置wrapcontent时使用
//        MeasureSpec.UNSPECIFIED   不确定        当使用scrollView使用,使用频率较小
//        MeasureSpec.EXACTLY       完全测量模式   当给宽或高设置具体数值,或者Match_Parent时使用
        if (mode == MeasureSpec.EXACTLY) {
            result = size;
        } else {
            result = isWidth ? getSuggestedMinimumWidth() : getSuggestedMinimumHeight();
            result += padding;
            if (mode == MeasureSpec.AT_MOST) {
                if (isWidth) {
                    result = Math.max(result, size);
                } else {
                    result = Math.min(result, size);
                }
            }
        }
        return result;
    }


    private interface OnProgressBarListener {
        void onProgressChange(int current, int max);
    }

    public float dp2px(float dp) {
        float density = getResources().getDisplayMetrics().density;
        return dp * density + 0.5f;
    }

    public float sp2px(float sp) {
        float density = getResources().getDisplayMetrics().scaledDensity;
        return density * sp + 0.5f;
    }

    public void setProgress(int progress) {
        if (progress <= getMax() && progress >= 0) {
            this.mCurrentProgress = progress;
            invalidate();
        }
    }

    public int getMax() {
        return mMaxProgress;
    }

    public int getProgress() {
        return mCurrentProgress;
    }

    public void setMax(int max) {
        mMaxProgress = max;
    }

}
