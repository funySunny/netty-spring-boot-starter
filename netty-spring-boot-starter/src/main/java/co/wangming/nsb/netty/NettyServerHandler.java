package co.wangming.nsb.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Created By WangMing On 2019-12-06
 **/
@Slf4j
public class NettyServerHandler extends ByteToMessageDecoder {

    private static final int MIN_PACKAGE_SIZE = 8;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List out) throws Exception {

        if (in.readableBytes() < MIN_PACKAGE_SIZE) {
            return;
        }

        in.markReaderIndex();

        // TODO 改成INT
        int messageSize = in.readByte();
        int messageId = in.readByte();
        int readableBytes = in.readableBytes();

        log.debug("接收到远端[{}]消息. messageSize:{}, readableBytes:{}", ctx.channel().remoteAddress(), messageSize, readableBytes);
        if (readableBytes < messageSize) {
            in.resetReaderIndex();
            return;
        }

        log.debug("处理远端[{}]消息", ctx.channel().remoteAddress());

        // TODO 优化, 每次都分配一块内存很浪费资源
        byte[] messageBytes = new byte[messageSize];
        in.readBytes(messageBytes);
        CommandDispatcher.dispatch(ctx, messageId, messageBytes);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("接受自远端连接:{}", ctx.channel().remoteAddress());
        // TODO 处理创建连接
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("远端连接关闭:{}", ctx.channel().remoteAddress());
        // TODO 处理关闭连接
        super.channelInactive(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            log.info("远端连接超时, 状态:{}, 地址:{}", event.state(), ctx.channel().remoteAddress());

            switch (event.state()) {
                case READER_IDLE:
                    // TODO 处理读超时
                    break;
                case WRITER_IDLE:
                    // TODO 处理写超时
                    break;
                case ALL_IDLE:
                    // TODO 处理读写超时
                    break;
                default:
                    break;
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.info("远端连接发生异常:{}", ctx.channel().remoteAddress(), cause);
        // TODO 处理异常
    }
}
