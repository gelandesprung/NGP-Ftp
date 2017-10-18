package yanchao.bj.ngp.utils;

import android.view.View;

/**
 * 北京中油瑞飞信息技术有限责任公司 研究院 瑞信项目
 * All Rights Reserved
 * 项目:瑞信项目
 * 类:OnItemClickListener
 * 描述:
 * 版本信息：since 2.0
 *
 * @作者: yanchao
 * @日期: 2017-10-18 11:24
 */

public interface OnItemClickListener<T> {

    void onItemClickListener(View view, T itemData);

    void onItemLongClickListener(View view, T itemData);
}
