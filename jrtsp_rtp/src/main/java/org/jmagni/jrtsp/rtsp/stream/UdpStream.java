package org.jmagni.jrtsp.rtsp.stream;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.NoArgsConstructor;
import org.jmagni.jrtsp.config.UserConfig;
import org.jmagni.jrtsp.rtsp.netty.handler.StreamerChannelHandler;
import org.jmagni.jrtsp.rtsp.stream.network.TargetNetworkInfo;
import org.jmagni.jrtsp.service.AppInstance;

import java.net.InetAddress;
import java.net.InetSocketAddress;

@NoArgsConstructor
public class UdpStream {

    private NioEventLoopGroup nioEventLoopGroup = null;
    private final Bootstrap bootstrap = new Bootstrap();

    public void start(String callId) {
        UserConfig userConfig = AppInstance.getInstance().getConfigManager().getUserConfig();
        nioEventLoopGroup = new NioEventLoopGroup(userConfig.getStreamThreadPoolSize());
        bootstrap.group(nioEventLoopGroup).channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, false)
                .option(ChannelOption.SO_SNDBUF, userConfig.getSendBufSize())
                .option(ChannelOption.SO_RCVBUF, userConfig.getRecvBufSize())
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000)
                .handler(new ChannelInitializer<NioDatagramChannel>() {
                    @Override
                    public void initChannel (final NioDatagramChannel ch) {
                        final ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(
                                //new DefaultEventExecutorGroup(1),
                                new StreamerChannelHandler(callId)
                        );
                    }
                });
    }

    public void stop(TargetNetworkInfo targetNetworkInfo) {
        closeTargetRtpEndpoint(targetNetworkInfo);
        closeTargetRtcpEndpoint(targetNetworkInfo);

        if (nioEventLoopGroup != null) {
            nioEventLoopGroup.shutdownGracefully();
        }
    }

    public boolean connectTargetRtpEndpoint(TargetNetworkInfo targetNetworkInfo) throws Exception {
        if (targetNetworkInfo.getRtpDestPort() > 0) {
            InetAddress address = InetAddress.getByName(targetNetworkInfo.getDestIp());
            ChannelFuture rtpChannelFuture = bootstrap.connect(address, targetNetworkInfo.getRtpDestPort()).sync();
            if (rtpChannelFuture == null) {
                return false;
            }

            targetNetworkInfo.setRtpDestChannel(rtpChannelFuture.channel());
            targetNetworkInfo.setRtpTargetAddress(
                    new InetSocketAddress(targetNetworkInfo.getDestIp(), targetNetworkInfo.getRtpDestPort())
            );
            return true;
        } else {
            return false;
        }
    }

    private void closeTargetRtpEndpoint(TargetNetworkInfo targetNetworkInfo) {
        Channel rtpDestChannel = targetNetworkInfo.getRtpDestChannel();
        if (rtpDestChannel != null) {
            rtpDestChannel.closeFuture();
            rtpDestChannel.close();
            targetNetworkInfo.setRtpDestChannel(null);
        }
    }

    public boolean connectTargetRtcpEndpoint(TargetNetworkInfo targetNetworkInfo) throws Exception {
        if (targetNetworkInfo.getRtcpDestPort() > 0) {
            InetAddress address = InetAddress.getByName(targetNetworkInfo.getDestIp());
            ChannelFuture rtcpChannelFuture = bootstrap.connect(address, targetNetworkInfo.getRtcpDestPort()).sync();
            if (rtcpChannelFuture == null) {
                return false;
            }

            targetNetworkInfo.setRtcpDestChannel(rtcpChannelFuture.channel());
            targetNetworkInfo.setRtcpTargetAddress(
                    new InetSocketAddress(targetNetworkInfo.getDestIp(), targetNetworkInfo.getRtcpDestPort())
            );
            return true;
        } else {
            return false;
        }
    }

    private void closeTargetRtcpEndpoint(TargetNetworkInfo targetNetworkInfo) {
        Channel rtcpDestChannel = targetNetworkInfo.getRtcpDestChannel();
        if (rtcpDestChannel != null) {
            rtcpDestChannel.closeFuture();
            rtcpDestChannel.close();
            targetNetworkInfo.setRtcpDestChannel(null);
        }
    }

}
