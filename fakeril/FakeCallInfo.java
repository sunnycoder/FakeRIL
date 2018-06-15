package android.com.android.fakeril;

import android.os.Parcel;

/**
 * Created by donghy on 18-6-15.
 * 自定义Call对象实体，一般是响应CLCC，可以根据不同的场景定义此数据结构
 */

public class FakeCallInfo {

    public String gid;
    public int gca;
    public int type;
    public int status;
    public int dir;
    public int ackFlag;
    public String priority;
    public int index;

    public FakeCallInfo() {
    }

    public FakeCallInfo(Parcel in) {
        gid = in.readString();
        gca = in.readInt();
        type = in.readInt();
        status = in.readInt();
        dir = in.readInt();
        ackFlag = in.readInt();
        priority = in.readString();
        index = in.readInt();
    }

}
