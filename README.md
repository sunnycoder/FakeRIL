# FakeRIL
用于模拟RIL消息返回，自定义测试通话的各种场景

## v0.1

第一版基于Android5.0,Android原生Tele-Fw与RIL的通信采用的是Socket，自8.0开始，更换为Binder通信，后续会更新基于8.0的版本


## 设计说明

FakeRIL设计为单例模式，支持模拟RIL的两种消息处理方法

    １．消息下发－> 消息返回
    ２．消息主动上报　－> 通知到注册监听者

下面分别说明

### 消息下发－> 消息返回

目前只支持单个请求，不支持同时有多个请求

下发的流程为：

    1. 调用FakeRIL自定义的消息下发请求
    2. 通过FakeRILRequest获取消息请求
    3. 通过FakeRILSender下发消息请求，当然这里不会下发到RILD,只会保存消息请求
    4. 调用startRILReceiver方法启动FakeRILReceiver消息接收回调线程，传入需要模拟返回的数据和消息类型，这里为RESPONSE_SOLICITED
    5. 最终在FakeRILReceiver中处理数据，调用在FakeRILResponse中自定义的处理方法，这个方法需要根据实际情况而定
    6. 最终完成消息回调

### 使用说明

这里以getCurrentCalls说明
```
Message mLastRelevantPoll = obtainMessage(EVENT_POLL_CALLS_RESULT);
// mCi.getCurrentCalls(mLastRelevantPoll); // 注释原生走RIL的流程 注意需要将所有的入口都注释掉
// 调用FakeRIL的自定义请求接口
FakeRIL.getInstance().getCurrentCalls(mLastRelevantPoll);

// 获取数据
需要自定义获取数据的方法，根据实际情况，对数据进行封装，最终封装为Parcel类型
Parcel p = FakeDataSource.getInstance().getActiveCall(); // 这里的FakeDataSource方法需要大家自己实现

// 模拟消息回调

FakeRIL.getInstance().startRILReceiver(p, FakeRIL.RESPONSE_SOLICITED);

// 最终的消息处理见FakeRILResponse中的responseGetCallState方法，实际使用时需要定义自己的数据处理方法
```

### 消息主动上报　－> 通知到注册监听者

    1. 调用FakeRIL的监听注册
    2. 调用startRILReceiver方法启动FakeRILReceiver消息接收回调线程，传入需要模拟返回的数据和消息类型，这里为RESPONSE_UNSOLICITED
    3. 需要注意的是这里的返回数据的Parcel中第一位需要为对应的监听的编号
    4. 最终在FakeRILReceiver中处理数据，调用在FakeRILResponse中自定义的处理方法，这个方法需要根据实际情况而定
    5. 最终完成消息的主动上报

```
// 注册消息监听
FakeRIL.getInstance().registerForCallStateChanged(this, EVENT_CALL_STATE_CHANGE, null);

// 获取模拟数据 注意这里的Parcel的第一位需要为消息的编号，例如这里为 RIL_UNSOL_RESPONSE_CALL_STATE_CHANGED

Parcel p = FakeDataSource.getInstance().getIncomingCall();

// 模拟消息上报
FakeRIL.getInstance().startRILReceiver(p, FakeRIL.RESPONSE_UNSOLICITED);

// 同样，最终的消息处理见FakeRILResponse中的responseGetCallState方法，实际使用时需要定义自己的数据处理方法

```

### 关于FakeRILReceiver

1. startRILReceiver方法的参数，需要传入模拟的返回数据以及响应的请求类型
        
        - RESPONSE_SOLICITED = 0; // RIL响应请求的回调
        - RESPONSE_UNSOLICITED = 1; // RIL主动上报消息

注意不同的消息类型传入的数据类型也不一样，主动上报的回调的Parcel第一位需要为对应的监听编号，之后才是数据，请求回调则不需要，从FakeRIL维护的mRequestList中取即可

2. FakeRILReceiver是一次性线程，运行完即销毁，可以根据问题场景调整其中的延迟时长

## 使用说明

1. 将代码整体放到fw/opt/telephony中的android.com.android目录下

2. 拦截原有的RIL流程，替换为FakeRIL流程，具体的流程参考上面的设计说明中的两种消息场景的使用说明

## TODO

   目前功能还比较简单，之后需要优化的几点：

    1. 更简单的自定义数据
    2. 同时处理多个请求
    3. 如何能让配置更加简化
    4. 更新基于8.0的版本

## 关于Parcel
