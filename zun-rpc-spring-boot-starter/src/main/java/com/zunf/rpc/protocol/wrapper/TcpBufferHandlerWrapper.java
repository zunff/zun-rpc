package com.zunf.rpc.protocol.wrapper;

import com.zunf.rpc.constants.ProtocolConstants;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.parsetools.RecordParser;

/**
 * 使用装饰者模式让 Handler<Buffer> 具备处理半包、粘包的能力
 *
 * @author zunf
 * @date 2024/5/17 15:58
 */
public class TcpBufferHandlerWrapper implements Handler<Buffer> {

    /**
     * 使用这个对象读数据，能够保证每次只读指定长度的数据
     * 多出的数据会留到下一次读取
     * 少了数据会读取下一个数据包，保证一次读取固定的长度
     */
    private final RecordParser recordParser;

    public TcpBufferHandlerWrapper(Handler<Buffer> handler) {
        this.recordParser = initRecordParser(handler);
    }

    @Override
    public void handle(Buffer buffer) {
        recordParser.handle(buffer);
    }

    private RecordParser initRecordParser(Handler<Buffer> handler) {
        //先读取请求头
        RecordParser parser = RecordParser.newFixed(ProtocolConstants.MESSAGE_HEAD_LENGTH);
        //先处理一下半包和粘包
        parser.setOutput(new Handler<Buffer>() {
            //初始化为-1，表示还没开始读取
            private int size = -1;
            //存储一个完成的读取（头+体）
            private Buffer resultBuffer = Buffer.buffer();
            @Override
            public void handle(Buffer buffer) {
                if (size == -1) {
                    //还没开始读取，先读请求头，获取请求体长度，并且修改下一次读取的固定长度
                    int bodyLength = buffer.getInt(13);
                    size = bodyLength;
                    parser.fixedSizeMode(bodyLength);
                    resultBuffer.appendBuffer(buffer);
                } else {
                    //读取请求体，将请求体写入结果中
                    resultBuffer.appendBuffer(buffer);
                    //完整的数据包已经构建，调用处理程序
                    handler.handle(resultBuffer);
                    //恢复状态，进行下一个数据包处理
                    size = -1;
                    parser.fixedSizeMode(ProtocolConstants.MESSAGE_HEAD_LENGTH);
                    resultBuffer = Buffer.buffer();
                }
            }
        });
        return parser;
    }
}
