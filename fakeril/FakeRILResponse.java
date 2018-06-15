package android.com.android.fakeril;

import android.os.Parcel;
import android.util.Log;

import com.android.internal.telephony.DriverCall;
import com.android.internal.telephony.GsmrRILConstants;

import java.util.ArrayList;

/**
 * Created by donghy on 18-6-13.
 * 处理各种RIL返回，将RIL返回数据封装为 DriverCall
 * List GsmCallTracker最终在handlePollCalls中处理这个数据
 * 如果返回的数据和最终封装的数据不对应，这里可以做一个适配，例如自定义Index.
 */

public class FakeRILResponse{

    static final String LOG_TAG = "FakeRILResponse";

    /**
     * 需要根据实际情况处理这个数据，也可以将原生的RIL数据处理方法copy过来
     * */
    public Object responseGetCallState(Parcel p) {
        Log.d(LOG_TAG, " responseGetCallState. ");

        int num = p.readInt();
        Log.d(LOG_TAG, " responseGetCallState.num :  " + num);

        ArrayList<DriverCall> response;
        DriverCall dc;

        response = new ArrayList<>(num);

        for (int i = 0; i < num; i++) {
            FakeCallInfo call =  new FakeCallInfo();
            call.gid = p.readString();
            call.gca = p.readInt();
            call.type = p.readInt();
            call.status = p.readInt();
            call.dir = p.readInt();
            call.ackFlag = p.readInt();
            call.priority = p.readString();
            // group call index
            call.index =  p.readInt();

            if (call.status != 1) {
                dc = new DriverCall();

                if (call.status == 0) {
                    dc.state = DriverCall.State.ACTIVE;
                } else if (call.status == 1) {
                    dc.state = DriverCall.State.HOLDING;
                } else {
                    dc.state = DriverCall.State.INCOMING;
                }

                dc.index = call.index;
                dc.isMT = (call.dir == GsmrRILConstants.MT);
                dc.als = 0;
                dc.number = call.type + String.valueOf(call.gid);
                dc.name = "";

                dc.type = call.type;
                dc.gca = call.gca;
                dc.gid = String.valueOf(call.gid);
                dc.ackFlag = call.ackFlag;
                dc.priority = call.priority;

                response.add(dc);
                Log.d(LOG_TAG, "responseGetCallState dc : " + dc);

            }

        }
        return response;
    }


}
