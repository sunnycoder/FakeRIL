package android.com.android.fakeril;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;


/**
 * Created by donghy on 18-6-13.
 */

public class FakeRILSender extends Handler implements Runnable{

    String LOG_TAG = "FakeRILSend";

    FakeRIL fakeRIL;
    public FakeRILSender(Looper looper, FakeRIL fakeRIL) {
        super(looper);
        this.fakeRIL = fakeRIL;
    }


    @Override
    public void run() {

    }

    static final int EVENT_SEND = 1;

    @Override public void
    handleMessage(Message msg) {
        FakeRILRequest rr = (FakeRILRequest)(msg.obj);
        FakeRILRequest req = null;

        switch (msg.what) {
            case EVENT_SEND:
                //TODO
                Log.d(LOG_TAG, "handle message: ENEVT_SEND. ");
                synchronized (fakeRIL.mRequestList) {
                    fakeRIL.appendToRequestList(rr.mSerial, rr);
                    fakeRIL.mRequestList.append(rr.mSerial, rr);
                }
                break;

        }
    }
}
