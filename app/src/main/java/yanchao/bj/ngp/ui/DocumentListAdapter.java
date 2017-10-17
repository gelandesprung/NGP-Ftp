package yanchao.bj.ngp.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;
import yanchao.bj.ngp.ui.DocumentListAdapter.DocumentViewHolder;



public abstract class DocumentListAdapter<T> extends Adapter<DocumentViewHolder> {

    private int mLayoutId;
    private Context mContext;
    private List<T> mData;

    public DocumentListAdapter(Context mContext, int mLayoutId, List<T> mData) {
        this.mLayoutId = mLayoutId;
        this.mContext = mContext;
        this.mData = mData;
    }

    @Override
    public DocumentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(mLayoutId, parent, false);
        return new DocumentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DocumentViewHolder holder, int position) {
        convert(holder, mData.get(position));
    }

    public abstract void convert(DocumentViewHolder holder, T t);

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class DocumentViewHolder extends ViewHolder {

        private SparseArray<View> mViewList;
        private View mConvertView;

        public DocumentViewHolder(View itemView) {
            super(itemView);
            mConvertView = itemView;
        }

        public View get(int resId) {
            View view = mViewList.get(resId);
            if (view == null) {
                view = mConvertView.findViewById(resId);
                mViewList.put(resId, view);
            }
            return view;
        }
    }
}
