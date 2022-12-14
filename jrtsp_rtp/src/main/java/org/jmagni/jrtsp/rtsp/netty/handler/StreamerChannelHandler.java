package org.jmagni.jrtsp.rtsp.netty.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.jmagni.jrtsp.rtsp.Streamer;
import org.jmagni.jrtsp.rtsp.netty.NettyChannelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @class public class StreamerChannelHandler extends ChannelInboundHandlerAdapter
 * @brief StreamerChannelHandler class
 */
public class StreamerChannelHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(StreamerChannelHandler.class);

    private final String callId;

    public StreamerChannelHandler(String callId) {
        this.callId = callId;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // Nothing
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        //logger.warn("({}) StreamerChannelHandler is inactive.", id);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        String causeString = cause.toString();
        logger.warn("({}) StreamerChannelHandler.Exception (cause={})", callId, causeString);

        if (causeString.contains("PortUnreachable")) {
            for (Streamer streamer : NettyChannelManager.getInstance().getStreamerListByCallId(callId)) {
                if (streamer == null) { return; }

                NettyChannelManager.getInstance().stopStreaming(streamer.getSessionId());
                logger.debug("({}) Stop the streaming by [PortUnreachableException].", streamer.getSessionId());

                NettyChannelManager.getInstance().deleteStreamer(streamer);
                logger.debug("({})  Finish to stream the media by [PortUnreachableException].", streamer.getSessionId());
            }
        }

        ctx.close();
    }

}