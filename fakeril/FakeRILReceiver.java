package android.com.android.fakeril;

import android.os.AsyncResult;
import android.os.Parcel;
import android.util.Log;

/**
 * Created by donghy on 18-6-13.
 */

public class FakeRILReceiver implements Runnable {

    static final String LOG_TAG = "FakeRILReceiver";

    FakeRIL fakeRIL;
    Parcel p;
    int type;
    FakeRILResponse fakeRILResponse;

    FakeRILReceiver(FakeRIL fakeRIL, Parcel p, int type) {
        this.fakeRIL = fakeRIL;
        this.p = p;
        this.type = type;
        fakeRILResponse = new FakeRILResponse();
    }

    /**
     * 运行后自动销毁
     */
    @Override
    public void run() {
        Log.d(LOG_TAG, " run -->  start");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        processResponse(p);

    }

    private void
    processResponse(Parcel p) {
        Log.d(LOG_TAG, "processResponse ---> ");

        if (type == FakeRIL.RESPONSE_UNSOLICITED) {
            processUnsolicited(p);
        } else if (type == FakeRIL.RESPONSE_SOLICITED) {
            FakeRILRequest rr = processSolicited(p);
            if (rr != null) {
                rr.release();
            }
        }
    }

    /**
     * RIL主动上报
     * */
    private void
    processUnsolicited(Parcel p) {
        int response;
        Object ret;

        response = p.readInt(); // 注意主动上报的数据中，第一位为响应的注册类别

        switch (response) {
            case FakeRILConstants.RIL_UNSOL_RESPONSE_CALL_STATE_CHANGED:
                // 数据处理
                ret = fakeRILResponse.responseGetCallState(p);
                // 通知回调
                fakeRIL.notifyCallStateChanged(ret);
                break;
            default:

        }
    }

    /**
     * RIL响应请求
     * */
    private FakeRILRequest
    processSolicited(Parcel p) {
/*        int serial;

        serial = p.readInt();*/

        FakeRILRequest rr;

        rr = fakeRIL.findAndRemoveRequestFromList(1);

        if (rr == null) {
            return null;
        }

        Object ret = null;

        // 数据处理
        switch (rr.mRequest) {
            case FakeRILConstants.RIL_REQUEST_GET_CURRENT_CALLS:
                ret = fakeRILResponse.responseGetCallState(p);
                break;
            default:
        }

        // 消息回调
        if (rr.mResult != null) {
            AsyncResult.forMessage(rr.mResult, ret, null);
            Log.d(LOG_TAG, " processSolicited : sendToTarget.");
            rr.mResult.sendToTarget();
        }
        return rr;
    }
}
