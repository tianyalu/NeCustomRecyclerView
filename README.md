## NeCustomRecyclerView 手写RecyclerView（item回收池）
该例子仅作为理解RecyclerView回收池原理的示例，不做实际用途。
### 1. 核心思想
RecyclerView之所以能支持千万级Item,是因为其仅处理需要在屏幕显示的View以及View的回收复用机制。
即首先生成第一屏的ItemView,当有ItemView划出屏幕时，将该ItemView放到回收池中；对于将要划进屏幕的ItemView,
首先从回收池中找与之相同type的ItemView，若有，则直接使用，否则重新生成。
### 2. 难点代码
```android
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
```
scroll_y1.png  

![image](https://github.com/tianyalu/NeCustomRecyclerView/blob/master/show/scroll_y1.png)  

scroll_y2.png  
 
![image](https://github.com/tianyalu/NeCustomRecyclerView/blob/master/show/scroll_y2.png)  

scroll_y3.png   

![image](https://github.com/tianyalu/NeCustomRecyclerView/blob/master/show/scroll_y3.png)  

scroll_y4.png   

![image](https://github.com/tianyalu/NeCustomRecyclerView/blob/master/show/scroll_y4.png)  

scroll_y5.png   

![image](https://github.com/tianyalu/NeCustomRecyclerView/blob/master/show/scroll_y5.png)  

