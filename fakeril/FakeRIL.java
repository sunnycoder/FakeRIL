package android.com.android.fakeril;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Registrant;
import android.os.RegistrantList;
import android.util.Log;
import android.util.SparseArray;


/**
 * Created by donghy on 18-6-13.
 * 单例模式
 */

public class FakeRIL {

    String TAG = "FakeRIL";

    static FakeRIL sFakeRIL;

    static final int RESPONSE_SOLICITED = 0; // RIL响应请求的回调
    static final int RESPONSE_UNSOLICITED = 1; // RIL主动上报消息

    public RegistrantList mCallStateRegistrants = new RegistrantList();

    SparseArray<FakeRILRequest> mRequestList = new SparseArray<>();

    FakeRILRequest mFakeRILRequest;
    FakeRILSender mFakeRILSender;
    FakeRILReceiver mFakeRILReceiver;

    Thread mReceiverThread; // 接收线程
    HandlerThread mSenderThread; // 发送线程

    int count = 0;

    private FakeRIL() {
        mFakeRILRequest = new FakeRILRequest();

        mSenderThread = new HandlerThread("FakeRILSender");
        mSenderThread.start();

        Looper looper = mSenderThread.getLooper();
        mFakeRILSender = new FakeRILSender(looper, this);
    }

    public static FakeRIL getInstance() {
        if (sFakeRIL == null) {
            synchronized (FakeRIL.class) {
                if (sFakeRIL == null) {
                    sFakeRIL = new FakeRIL();
                }
            }
        }
        return sFakeRIL;
    }


    /**
     * 通过FakeRILSender下发消息请求
     * */
    private void
    send(FakeRILRequest rr) {
        Log.d(TAG, "send ---> ");
        Message msg;

        msg = mFakeRILSender.obtainMessage(FakeRILSender.EVENT_SEND, rr);

        msg.sendToTarget();
    }

    /**
     * 添加请求到mRequestList
     * */

    public void appendToRequestList(int serial, FakeRILRequest fakeRILRequest) {
        mRequestList.append(serial, fakeRILRequest);
    }
    /**
     * 清理mRequestList
     * */
    public FakeRILRequest findAndRemoveRequestFromList(int serial) {
        FakeRILRequest rr = null;
        synchronized (mRequestList) {
            rr = mRequestList.get(serial);
            if (rr != null) {
                mRequestList.remove(serial);
            }
        }

        return rr;
    }

    // 启动消息上报线程，根据传入的type,可以处理主动上报和消息返回的数据通知
    /**
     * 启动startRILReceiver线程，用于模拟RIL返回数据
     * @param p  封装好的的序列化的数据
     * @param type 响应的类型 RESPONSE_SOLICITED / RESPONSE_UNSOLICITED
     * */
    public void startRILReceiver(Parcel p, int type, long delay_time) {
        Log.d(TAG, "startRILReceiver ---> type: " + type);
        mFakeRILReceiver = new FakeRILReceiver(this, p, type, delay_time);
        mReceiverThread = new Thread(mFakeRILReceiver, "FakeRILReceiver");
        mReceiverThread.start();

    }


    // 自定义各种监听，用于模拟主动上报
    /**
     * 注册Call状态监听
     * 需要在GsmCallTracker创建时中注册监听
     * */
    public void registerForCallStateChanged(Handler h, int what, Object obj) {
        Log.d(TAG, "registerForCallStateChanged ---> ");
        Registrant r = new Registrant (h, what, obj);

        mCallStateRegistrants.add(r);
    }

    public void unregisterForCallStateChanged(Handler h) {
        mCallStateRegistrants.remove(h);
    }

    /**
     * 通知Call状态变化
     * 用于模拟通知RIL主动上报信息 在需要的时候延迟通知
     * */
    public void notifyCallStateChanged(Object ret) {
        if (mCallStateRegistrants != null) {
            mCallStateRegistrants.notifyRegistrants(
                    new AsyncResult(null, ret, null));
        }
    }


    // 自定义RIL请求方法
    /**
     * Fake getCurrentCalls
     * 在正常下发查询CLCC的地方调用此方法，然后通过startRILReceiver启动数据响应返回
     *
     * */
    public void
    getCurrentCalls (Message result) {
        Log.d(TAG, "getCurrentCalls ---> ");
        FakeRILRequest rr = mFakeRILRequest.obtain(FakeRILConstants.RIL_REQUEST_GET_CURRENT_CALLS, result);

        send(rr); 
    }


    /**
    * 通过count控制每次不同的上报，用于测试多种上报的交互
    */
    private void setData() {
        count ++;
        if (count == 1) {
        // part1
        Parcel p1 = FakeDataSource.getInstance().getThirdCallListParcelPart1();

        startRILReceiver(p1, RESPONSE_SOLICITED, 5000);
        } else if (count == 2) {
        // part2
        Parcel p2 = FakeDataSource.getInstance().getThirdCallListParcelPart2();

        startRILReceiver(p2, RESPONSE_SOLICITED,1000);
        } else {
        // part3
        Parcel p3 = FakeDataSource.getInstance().getThirdCallListParcelPart3();

        startRILReceiver(p3, RESPONSE_SOLICITED, 1000);
        }
    }

}
