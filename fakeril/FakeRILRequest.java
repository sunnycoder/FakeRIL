package android.com.android.fakeril;

import android.os.Message;
import android.os.Parcel;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by donghy on 18-6-13.
 * 模拟RIL请求，正常情况下会下发到rild,这里不做处理，只通过FakeRIL中的mRequestList记录请求
 */

public class FakeRILRequest {

    static final String LOG_TAG = "FakeRILRequest";

    private static final int MAX_POOL_SIZE = 4; // 限制请求数

    private static Object sPoolSync = new Object();
    private static FakeRILRequest sPool = null;
    private static int sPoolSize = 0;

    static AtomicInteger sNextSerial = new AtomicInteger(0);

    int mSerial;
    int mRequest;
    Message mResult;
    Parcel mParcel;
    FakeRILRequest mNext;

    /**
     * 获取请求 目前只维护一个请求，最终保存在mRequestList中，这样可以正确响应对应的请求
     * 如果有必要的话可以维护一个请求池
     * */
    public static FakeRILRequest obtain(int request, Message result) {
        FakeRILRequest rr = null;

        synchronized(sPoolSync) {
            if (sPool != null) {
                rr = sPool;
                sPool = rr.mNext;
                rr.mNext = null;
                sPoolSize--;
            }
        }

        if (rr == null) {
            rr = new FakeRILRequest();
        }

        //TODO
//        rr.mSerial = sNextSerial.getAndIncrement();
        rr.mSerial = 1;

        rr.mRequest = request;
        rr.mResult = result;
        rr.mParcel = Parcel.obtain();

        if (result != null && result.getTarget() == null) {
            throw new NullPointerException("Message target must not be null");
        }

        // first elements in any RIL Parcel
        rr.mParcel.writeInt(request);
        rr.mParcel.writeInt(rr.mSerial);

        return rr;

    }

    /**
     * 释放请求
     * */
    void release() {
        synchronized (sPoolSync) {
            if (sPoolSize < MAX_POOL_SIZE) {
                mNext = sPool;
                sPool = this;
                sPoolSize++;
                mResult = null;
            }
        }
    }
}
