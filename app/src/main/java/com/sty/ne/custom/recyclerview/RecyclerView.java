package com.sty.ne.custom.recyclerview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * 该Demo仅用作理解RecyclerView的回收池原理，不做实际使用用途
 * Created by tian on 2019/10/28.
 */

public class RecyclerView extends ViewGroup {
    private Adapter adapter;
    //当前显示的View
    private List<View> viewList;
    //当前滑动的y值
    private int currentY;
    //行数
    private int rowCount;
    //view的第一行是占内容的第几行
    private int firstRow;
    //y偏移量
    private int scrollY;
    //初始化，第一屏最慢
    private boolean needRelayout;

    private int width;
    private int height;
    private int[] heights; //item高度数组

    Recycler recycler;
    //最小滑动距离
    private int touchSlop;


    public Adapter getAdapter() {
        return adapter;
    }

    public void setAdapter(Adapter adapter) {
        this.adapter = adapter;
        if(adapter != null) {
            recycler = new Recycler(adapter.getViewTypeCount());
            scrollY = 0;
            firstRow = 0;
            needRelayout = true;
            requestLayout(); //1.onMeasure 2.onLayout
        }
    }

    public RecyclerView(Context context) {
        this(context, null);
    }

    public RecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        ViewConfiguration configuration = ViewConfiguration.get(context);
        this.touchSlop = configuration.getScaledTouchSlop();
        this.viewList = new ArrayList<>();
        this.needRelayout = true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int h = 0;
        if(adapter != null) {
            this.rowCount = adapter.getCount();
            heights = new int[rowCount];
            for (int i = 0; i < heights.length; i++) {
                heights[i] = adapter.getHeight(i);
            }
        }
        //数据高度
        int tmpH = sumArray(heights, 0, heights.length);
        h = Math.min(heightSize, tmpH);
        setMeasuredDimension(widthSize, h);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    //初始化
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if(needRelayout || changed) {
            needRelayout = false;

            viewList.clear();
            removeAllViews();
            if(adapter != null) {
                //摆放
                width = r - l;
                height = b - t;
                int left, top = 0, right, bottom;
                for (int i = 0; i < rowCount && top < height; i++) {
                    right = width;
                    bottom = top + heights[i];
                    //生成一个view
                    View view = makeAndStep(i, 0, top, width, bottom);
                    viewList.add(view);
                    top = bottom; //循环摆放
                }
            }
        }
    }

    private View makeAndStep(int row, int left, int top, int right, int bottom) {
        View view = obtainView(row, right - left, bottom - top);
        view.layout(left, top, right, bottom);
        return view;
    }

    private View obtainView(int row, int width, int height) {
        //key type
        int itemType = adapter.getItemViewType(row);
        //取不到
        View recyclerView = recycler.get(itemType);
        View view;
        if(recyclerView == null) {
            view = adapter.onCreateViewHolder(row, recyclerView, this);
            if(view == null) {
                throw new RuntimeException("onCreateViewHolder 必须填充布局");
            }
        }else {
            view = adapter.onBinderViewHolder(row, recyclerView, this);
        }

        view.setTag(R.id.tag_type_view, itemType);
        view.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        addView(view, 0);

        return view;
    }

    /**
     * 获取array数组中从firstIndex 到 firstIndex + count 之间的数据之和
     * @param array
     * @param firstIndex
     * @param count
     * @return
     */
    private int sumArray(int array[], int firstIndex, int count) {
        int sum = 0;
        count += firstIndex;
        for (int i = firstIndex; i < count; i++) {
            sum += array[i];
        }
        return sum;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        boolean intercept = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                currentY = (int) event.getRawY(); //以屏幕左上角为坐标原点(不管当前activity有没有标题栏)
//                currentY = (int) event.getY(); //以当前控件左上角为坐标原点
                break;
            case MotionEvent.ACTION_MOVE:
                int y2 = Math.abs(currentY - (int) event.getRawY());
                if(y2 > touchSlop) {
                    intercept = true;
                }
        }
        return intercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
//                Log.e("sty", "on moving");
               //移动的距离，y方向
                int y2 = (int) event.getRawY();
               //上滑 正    下滑  负
                int diffY = currentY - y2;
                //画布移动，并不影响子控件的位置
                scrollBy(0, diffY);
        }
        return super.onTouchEvent(event);
    }

    /**
     * 边界极限条件
     * @param scrollY
     * @return
     */
    private int scrollBounds(int scrollY) {
        //上滑 参考show/scroll_y5.png
        if(scrollY > 0) {
            scrollY = Math.min(scrollY, sumArray(heights, firstRow, heights.length - firstRow) - height);
        }else { //下滑 参考show/scroll_y4.png
            //极限值取0 非极限值取scrollY
            scrollY = Math.max(scrollY, -sumArray(heights, 0, firstRow));
        }
        return scrollY;
    }

    @Override
    public void scrollBy(int x, int y) {
        //scrollY表示第一个可见item 的左上顶点距离屏幕左上顶点的距离/手指滑动距离
        scrollY += y;
        //极限值修复
        scrollY = scrollBounds(scrollY);

        if(scrollY > 0) {
            //上滑正 下滑负
            //1.上滑移除 参考show/scroll_y1.png
            while (scrollY > heights[firstRow]) { //快速滑动
                removeView(viewList.remove(0));
                scrollY -= heights[firstRow];
                firstRow++;
            }
            //2.上滑加载 参考show/scroll_y2.png
            while( getFillHeight() < height) {
                int addLast = firstRow + viewList.size();
                View view = obtainView(addLast, width, heights[addLast]);
                viewList.add(viewList.size(), view);
            }
        }else if(scrollY < 0) {
            //4.下滑加载
            while (scrollY < 0) {
                int firstAddRow = firstRow - 1;
                View view = obtainView(firstAddRow, width, heights[firstAddRow]);
                viewList.add(0, view);
                firstRow--;
                scrollY += heights[firstRow + 1];
//                scrollY += heights[firstRow];  //?????
            }
            //3.下滑移除 参考show/scroll_y3.png
            while(sumArray(heights, firstRow, viewList.size()) - scrollY -
                    heights[firstRow + viewList.size() - 1] >= height) {
                removeView(viewList.remove(viewList.size() - 1));
            }
        }else {

        }

        repositionViews();
    }

    private void repositionViews() {
        int left, top, right, bottom, i;
        top = - scrollY;
        i = firstRow;
        for (View view : viewList) {
            bottom = top + heights[i++];
            view.layout(0, top, width, bottom);
            top = bottom;
        }
    }

    private int getFillHeight() {
        //数据的高度 - scrollY
        return sumArray(heights, firstRow, viewList.size()) - scrollY;
    }

    @Override
    public void removeView(View view) {
        super.removeView(view);
        int key = (int) view.getTag(R.id.tag_type_view);
        recycler.put(view, key);
    }

    interface Adapter {
        View onCreateViewHolder(int position, View convertView, ViewGroup parent);
        View onBinderViewHolder(int position, View convertView, ViewGroup parent);

        //Item的类型
        int getItemViewType(int row);
        //Item的类型数量
        int getViewTypeCount();

        int getCount();
        int getHeight(int index);
    }
}
