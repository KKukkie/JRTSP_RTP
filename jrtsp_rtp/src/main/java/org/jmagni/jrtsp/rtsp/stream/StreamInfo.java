package org.jmagni.jrtsp.rtsp.stream;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jmagni.jrtsp.rtsp.base.MediaType;

import java.util.concurrent.locks.ReentrantLock;

@Data
@RequiredArgsConstructor
public class StreamInfo {

    public static final byte TCP_RTP_MAGIC_NUMBER = 0X24;

    private final MediaType mediaType;
    private final String callId;
    private final String sessionId;
    private String clientUserAgent = null;
    private final String trackId;

    private UdpStream udpStream = null;

    private ChannelHandlerContext rtspChannelContext = null;

    private DefaultFullHttpResponse playResponse = null;
    private final ReentrantLock playResponseLock = new ReentrantLock();

}
