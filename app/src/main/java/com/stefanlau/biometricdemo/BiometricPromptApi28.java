package com.stefanlau.biometricdemo;

import android.app.Activity;
import android.content.DialogInterface;
import android.hardware.biometrics.BiometricPrompt;
import android.os.Build;
import android.os.CancellationSignal;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Stefan Lau on 2018/12/11.
 */
@RequiresApi(Build.VERSION_CODES.P)
public class BiometricPromptApi28 implements IBiometricPromptImpl {

    private Activity mActivity;
    private BiometricPrompt mBiometricPrompt;
    private BiometricPromptManager.OnBiometricIdentifyCallback mManagerIdentifyCallback;
    private CancellationSignal mCancellationSignal;

    private ACache aCache;

    @RequiresApi(Build.VERSION_CODES.P)
    public BiometricPromptApi28(Activity activity) {
        mActivity = activity;
        aCache = ACache.get(App.getContext());
        mBiometricPrompt = new BiometricPrompt
                .Builder(activity)
                .setTitle(activity.getResources().getString(R.string.title))
                .setDescription(activity.getResources().getString(R.string.touch_2_auth))
                .setSubtitle("")
                .setNegativeButton(activity.getResources().getString(R.string.use_password),
                        activity.getMainExecutor(), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (mManagerIdentifyCallback != null) {
                                    mManagerIdentifyCallback.onUsePassword();
                                }
                                mCancellationSignal.cancel();
                            }
                        })
                .build();

    }

    @RequiresApi(Build.VERSION_CODES.P)
    @Override
    public void authenticate(boolean loginFlg, @Nullable CancellationSignal cancel,
                             @NonNull BiometricPromptManager.OnBiometricIdentifyCallback callback) {
        mManagerIdentifyCallback = callback;

        mCancellationSignal = cancel;
        if (mCancellationSignal == null) {
            mCancellationSignal = new CancellationSignal();
        }
        mCancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
            @Override
            public void onCancel() {
            }
        });

        KeyGenTool mKeyGenTool = new KeyGenTool(mActivity);
        BiometricPrompt.CryptoObject object;
        if (loginFlg) {
            //解密
            try {
                /**
                 * 可通过服务器保存iv,然后在使用之前从服务器获取
                 */
                //保存用于做AES-CBC
                String ivStr = aCache.getAsString("iv");
                byte[] iv = Base64.decode(ivStr, Base64.URL_SAFE);

                object = new BiometricPrompt.CryptoObject(mKeyGenTool.getDecryptCipher(iv));
                mBiometricPrompt.authenticate(object,
                        mCancellationSignal, mActivity.getMainExecutor(), new BiometricPromptCallbackImpl());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            //加密
            try {
                object = new BiometricPrompt.CryptoObject(mKeyGenTool.getEncryptCipher());
                mBiometricPrompt.authenticate(object,
                        mCancellationSignal, mActivity.getMainExecutor(), new BiometricPromptCallbackImpl());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.P)
    private class BiometricPromptCallbackImpl extends BiometricPrompt.AuthenticationCallback {
        @Override
        public void onAuthenticationError(int errorCode, CharSequence errString) {
            mCancellationSignal.cancel();

        }

        @Override
        public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        }

        @Override
        public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
//            try {
//                Field field = result.getClass().getDeclaredField("mFingerprint");
//                field.setAccessible(true);
//                Object fingerprint = field.get(result);
//
//                Class<?> clazz = Class.forName("android.hardware.fingerprint.Fingerprint");
//                Method getName = clazz.getDeclaredMethod("getName");
//                Method getFingerId = clazz.getDeclaredMethod("getFingerId");
//                Method getGroupId = clazz.getDeclaredMethod("getGroupId");
//                Method getDeviceId = clazz.getDeclaredMethod("getDeviceId");
//                CharSequence name = (CharSequence) getName.invoke(fingerprint);
//                int fingerId = (int) getFingerId.invoke(fingerprint);
//                int groupId = (int) getGroupId.invoke(fingerprint);
//                int deviceId = (int) getDeviceId.invoke(fingerprint);

//                Log.i("test", "name: " + name);
//                Log.i("test", "fingerId: " + fingerId);
//                Log.i("test", "groupId: " + groupId);
//                Log.i("test", "deviceId: " + deviceId);

//            } catch (NoSuchFieldException e) {
//                e.printStackTrace();
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
//            } catch (NoSuchMethodException e) {
//                e.printStackTrace();
//            } catch (InvocationTargetException e) {
//                e.printStackTrace();
//            }


            mManagerIdentifyCallback.onSucceeded(result);
            mCancellationSignal.cancel();
        }

        @Override
        public void onAuthenticationFailed() {

        }
    }

}
