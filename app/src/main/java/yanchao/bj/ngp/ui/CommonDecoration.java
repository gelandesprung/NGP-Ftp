package yanchao.bj.ngp.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.support.v7.widget.RecyclerView.State;
import android.view.View;

/**
 * 北京中油瑞飞信息技术有限责任公司 研究院 瑞信项目
 * All Rights Reserved
 * 项目:瑞信项目
 * 类:CommonDecoration
 * 描述:
 * 版本信息：since 2.0
 *
 * @作者: yanchao
 * @日期: 2017-10-17 15:39
 */

class CommonDecoration extends ItemDecoration {

    private Paint painter;

    public CommonDecoration() {
        super();
        painter = new Paint();
        painter.setColor(Color.BLACK);
        painter.setStrikeThruText(true);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, State state) {
        super.onDraw(c, parent, state);
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            int top = child.getBottom();
            int bottom = child.getBottom() + 2;
            c.drawRect(left, top, right, bottom, painter);
        }
    }

}
