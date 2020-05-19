package com.example.himalaya.views;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.himalaya.R;

public class ConfirmDialog extends Dialog {

    private TextView mCancelSub;
    private TextView mGiveUp;
    private OnDialogActionClickListener mOnDialogActionClickListener = null;
    private TextView mContent;

    public ConfirmDialog(@NonNull Context context) {
        this(context, 0);
    }

    public ConfirmDialog(@NonNull Context context, int themeResId) {
        this(context, true, null);//true点击外面可以取消，false不行
    }

    protected ConfirmDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_confirm);
        initView();
        initEvent();
    }

    private void initEvent() {
        mCancelSub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnDialogActionClickListener != null) {
                    mOnDialogActionClickListener.onCancelSubClick();
                    dismiss();//点击后消失
                }
            }
        });
        mGiveUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnDialogActionClickListener != null) {
                    mOnDialogActionClickListener.onGiveUPClick();
                    dismiss();//点击后消失
                }
            }
        });
    }

    private void initView() {
        mCancelSub = findViewById(R.id.dialog_check_box_cancel);
        mGiveUp = findViewById(R.id.dialog_check_box_confirm);
        mContent = findViewById(R.id.dialog_tips_text);
    }

    public void setContent(String content, String delete) {
        if (mContent != null) {
            mContent.setText(content);
        }
        if (mCancelSub != null) {
            mCancelSub.setText(delete);
        }
    }

    public void setOnDialogActionClickListener(OnDialogActionClickListener listener) {
        this.mOnDialogActionClickListener = listener;
    }

    public interface OnDialogActionClickListener {
        void onCancelSubClick();

        void onGiveUPClick();
    }
}
