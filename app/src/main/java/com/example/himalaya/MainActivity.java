package com.example.himalaya;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.category.Category;
import com.ximalaya.ting.android.opensdk.model.category.CategoryList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiresApi(api = Build.VERSION_CODES.M)
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int internet = checkSelfPermission(Manifest.permission.INTERNET);
        if (internet != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.INTERNET}, 1);
        } else {
            Map<String, String> map = new HashMap<>();
            CommonRequest.getCategories(map, new IDataCallBack<CategoryList>() {
                @Override
                public void onSuccess(CategoryList categoryList) {
                    List<Category> categories = categoryList.getCategories();
                    int size = categories.size();
                    for (int i = 0; i < categories.size(); i++) {
                        String categoryName = categories.get(i).getCategoryName();
                        String coverUrlLarge = categories.get(i).getCoverUrlLarge();
                        String coverUrlMiddle = categories.get(i).getCoverUrlMiddle();
                        String coverUrlSmall = categories.get(i).getCoverUrlSmall();
                        long id = categories.get(i).getId();
                        String kind = categories.get(i).getKind();
                        Log.d(TAG, "size --> " + size);
                        Log.d(TAG, "categoryName --> " + categoryName);
                        Log.d(TAG, "coverUrlLarge --> " + coverUrlLarge);
                        Log.d(TAG, "coverUrlMiddle --> " + coverUrlMiddle);
                        Log.d(TAG, "coverUrlSmall --> " + coverUrlSmall);
                        Log.d(TAG, "id --> " + id);
                        Log.d(TAG, "kind --> " + kind);
                    }
                }

                @Override
                public void onError(int i, String s) {
                    Log.d(TAG, "error code -- " + i + "  :  " + s);
                }
            });
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "有权限");
                } else {
                    Log.d(TAG, "拒绝权限");
                }
                break;
        }
    }
}
