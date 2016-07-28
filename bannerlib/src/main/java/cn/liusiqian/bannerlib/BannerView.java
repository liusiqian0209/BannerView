package cn.liusiqian.bannerlib;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Créé par liusiqian 16/7/24.
 */

public class BannerView extends FrameLayout
{
    private static final int DEFAULT_PAGE_COUNT = 5;
    private static final int MSG_PAGE_SWITCH = 0x100;

    private ViewPager viewpager;
    private List<String> mImageUrls;
    private List<Integer> mImageDrawableRes;
    private List<ImageView> mImageViews;
    private CurImageType mCurType;          //当前使用类型

    private Paint paint;
    private static final int MARGIN = 35;   //px 指示器圆心间距
    private static final int RADIUS = 10;   //px 指示器小圆半径
    private static final float CENTER_X = 1 / 2f;   //指示器中心位置占比
    private static final float CENTER_Y = 5 / 6f;   //指示器中心位置占比
    private int mCurPos;
    private float mCurOffset;

    private static final long PAGE_CHANGE_INTERVAL = 3000;  //ms 轮播间隔
    private Handler mHandler;
    private long mLastPageChangedMills;

    private int mPicWidth;
    private int mPicHeight;
    private boolean mResized;

    public BannerView(Context context)
    {
        this(context, null);
    }

    public BannerView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public BannerView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init()
    {
        viewpager = new ViewPager(getContext());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        viewpager.setLayoutParams(params);
        addView(viewpager);
        mCurPos = 0;
        mCurOffset = 0;
        mLastPageChangedMills = System.currentTimeMillis();
        mHandler = new Handler(new Handler.Callback()
        {
            @Override
            public boolean handleMessage(Message message)
            {
                if(message.what == MSG_PAGE_SWITCH)
                {
                    handlePageSwitchReq();
                }
                return false;
            }
        });
        mHandler.sendEmptyMessageDelayed(MSG_PAGE_SWITCH,PAGE_CHANGE_INTERVAL);     //init
        viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
                mCurPos = position;
                mCurOffset = positionOffset;
                invalidate();
            }

            @Override
            public void onPageSelected(int position)
            {
                mLastPageChangedMills = System.currentTimeMillis();
                mHandler.sendEmptyMessageDelayed(MSG_PAGE_SWITCH,PAGE_CHANGE_INTERVAL);
            }

            @Override
            public void onPageScrollStateChanged(int state)
            {

            }
        });

        mImageViews = new ArrayList<>();
        mCurType = CurImageType.TYPE_NONE;
        paint = new Paint();
        updatePagerAdapter();
    }

    private void handlePageSwitchReq()
    {
        long current = System.currentTimeMillis();
        if(current - mLastPageChangedMills >= PAGE_CHANGE_INTERVAL)
        {
            mCurPos++;
            if(mCurPos >= mImageViews.size())
            {
                mCurPos -= mImageViews.size();
            }
            viewpager.setCurrentItem(mCurPos);
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        if (mResized)
        {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            int mw = getMeasuredWidth();
            int mh = mw * mPicHeight / mPicWidth;
            ViewGroup.LayoutParams params = getLayoutParams();
            params.height = mh;
            params.width = mw;
            setLayoutParams(params);
            mResized = false;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void dispatchDraw(Canvas canvas)
    {
        super.dispatchDraw(canvas);
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        int centerX = (int) (getWidth() * CENTER_X);
        int centerY = (int) (getHeight() * CENTER_Y);
        int count = mImageViews.size();
        int startX = centerX - MARGIN * (count - 1) / 2;
        for (int i = 0; i < count; i++)
        {
            canvas.drawCircle(startX + i * MARGIN, centerY, RADIUS, paint);
        }

        paint.setColor(Color.RED);
        canvas.drawCircle(startX + (mCurPos + mCurOffset) * MARGIN, centerY, RADIUS, paint);
    }

    private void generateImageViews()
    {
        mImageViews.clear();
        int count;
        switch (mCurType)
        {
            case TYPE_NONE:
                count = DEFAULT_PAGE_COUNT;
                break;
            case TYPE_URL:
                count = mImageUrls.size();
                break;
            case TYPE_DRAWABLE:
                count = mImageDrawableRes.size();
                break;
            default:
                count = DEFAULT_PAGE_COUNT;
        }

        for (int i = 0; i < count; i++)
        {
            ImageView view = new ImageView(getContext());
            mImageViews.add(view);
        }
    }

    private void updatePagerAdapter()
    {
        generateImageViews();
        viewpager.setAdapter(new PagerAdapter()
        {
            @Override
            public int getCount()
            {
                switch (mCurType)
                {
                    case TYPE_NONE:
                        return DEFAULT_PAGE_COUNT;
                    case TYPE_URL:
                        return mImageUrls.size();
                    case TYPE_DRAWABLE:
                        return mImageDrawableRes.size();
                    default:
                        return DEFAULT_PAGE_COUNT;
                }
            }

            @Override
            public boolean isViewFromObject(View view, Object object)
            {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position)
            {
                ImageView view = mImageViews.get(position);
                container.addView(view);
                switch (mCurType)
                {
                    case TYPE_URL:
                        loadPicFromNet(view,mImageUrls.get(position));
                        break;
                    case TYPE_DRAWABLE:
                        loadPicFromLocal(view,mImageDrawableRes.get(position));
                        break;
                }
                return mImageViews.get(position);
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object)
            {
                container.removeView(mImageViews.get(position));
            }


        });
    }

    //Override this method to customize your own loading
    protected void loadPicFromLocal(ImageView view, int resInt)
    {
        Picasso.with(getContext()).load(resInt).into(view);
    }

    //Override this method to customize your own loading
    protected void loadPicFromNet(ImageView view, String url)
    {
        Picasso.with(getContext()).load(url).into(view);
    }

    public void resize(int width, int height)
    {
        if (width > 0 && height > 0)
        {
            mPicWidth = width;
            mPicHeight = height;
            mResized = true;
        }
    }

    public void setImageUrls(List<String> urls)
    {
        if (urls != null && urls.size() > 0)
        {
            mImageUrls = urls;
            mCurType = CurImageType.TYPE_URL;
            updatePagerAdapter();
        }
    }

    public void setImageDrawables(List<Integer> drawables)
    {
        if (drawables != null && drawables.size() > 0)
        {
            mImageDrawableRes = drawables;
            mCurType = CurImageType.TYPE_DRAWABLE;
            updatePagerAdapter();
        }
    }

    private enum CurImageType
    {
        TYPE_NONE,
        TYPE_URL,
        TYPE_DRAWABLE
    }
}
