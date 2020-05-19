package com.example.himalaya.views;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.himalaya.R;

public class ConfirmCheckBoxDialog extends Dialog {

    private TextView mCancel;
    private TextView mConfrim;
    private OnDialogActionClickListener mOnDialogActionClickListener = null;
    private TextView mContent;
    private CheckBox mCheckBox;

    public ConfirmCheckBoxDialog(@NonNull Context context) {
        this(context, 0);
    }

    public ConfirmCheckBoxDialog(@NonNull Context context, int themeResId) {
        this(context, true, null);//true点击外面可以取消，false不行
    }

    protected ConfirmCheckBoxDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_check_box_confirm);
        initView();
        initEvent();
    }

    private void initEvent() {
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnDialogActionClickListener != null) {
                    mOnDialogActionClickListener.onCancelClick();
                    dismiss();//点击后消失
                }
            }
        });
        mConfrim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnDialogActionClickListener != null) {
                    boolean checked = mCheckBox.isChecked();
                    mOnDialogActionClickListener.onConfirmClick(checked);
                    dismiss();//点击后消失
                }
            }
        });
    }

    private void initView() {
        mCancel = findViewById(R.id.dialog_check_box_cancel);
        mConfrim = findViewById(R.id.dialog_check_box_confirm);
        mCheckBox = findViewById(R.id.dialog_check_box);
    }


    public void setOnDialogActionClickListener(OnDialogActionClickListener listener) {
        this.mOnDialogActionClickListener = listener;
    }

    public interface OnDialogActionClickListener {
        void onCancelClick();

        void onConfirmClick(boolean checked);
    }
}
