package noclay.treehole3.SelectPopupWindow;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;

import java.io.File;
import java.io.IOException;

import noclay.treehole3.R;

/**
 * Created by no_clay on 2017/2/9.
 */

public class InputPopWindow extends PopupWindow {

    EditText mInputStudentNumber;
    EditText mInputPassWord;
    EditText mInputVerifyNumber;
    ImageView mVerifyImage;
    Button mLogin;
    private View mainView;
    private static final String TAG = "InputPopWindow";
    private Context context;

    public InputPopWindow(Context context, View.OnClickListener itemOnClick) {
        super(context);
        this.context = context;
        mainView = LayoutInflater.from(context).inflate(R.layout.input_accent, null);
        this.setContentView(mainView);
        mInputPassWord = (EditText) mainView.findViewById(R.id.input_passWord);
        mInputStudentNumber = (EditText) mainView.findViewById(R.id.input_student_number);
        mInputVerifyNumber = (EditText) mainView.findViewById(R.id.input_verify_number);
        mVerifyImage = (ImageView) mainView.findViewById(R.id.verify_image);
        mLogin = (Button) mainView.findViewById(R.id.login);
        Log.d(TAG, "InputPopWindow: the item = " + (itemOnClick == null));
        mVerifyImage.setOnClickListener(itemOnClick);
        mLogin.setOnClickListener(itemOnClick);
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setFocusable(true);
        ColorDrawable dw = new ColorDrawable(0x88000000);
        this.setBackgroundDrawable(dw);
        this.setAnimationStyle(R.style.PopupAnimation);
    }

    public String getStudentNumber(){
        return mInputStudentNumber.getText().toString();
    }
    public String getPassWord(){

        return mInputPassWord.getText().toString();
    }
    public String getVerifyNumber(){
        return mInputVerifyNumber.getText().toString();
    }
    public void setVerifyImage(String path){
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.
                    getContentResolver(), Uri.fromFile(new File(path)));
            mVerifyImage.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        Glide.with(context).load(url)
//                .placeholder(R.drawable.load_failed)
//                .crossFade().into(mVerifyImage);
    }

    public void initData(String number, String password){
        mInputStudentNumber.setText(number);
        mInputPassWord.setText(password);
    }
}
