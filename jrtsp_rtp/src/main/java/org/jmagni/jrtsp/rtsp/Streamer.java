package org.jmagni.jrtsp.rtsp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.rtsp.RtspHeaderNames;
import io.netty.handler.codec.rtsp.RtspHeaderValues;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ntp.TimeStamp;
import org.jmagni.jrtsp.rtsp.base.MediaType;
import org.jmagni.jrtsp.rtsp.base.RtpPacket;
import org.jmagni.jrtsp.rtsp.rtcp.type.regular.RtcpSenderReport;
import org.jmagni.jrtsp.rtsp.rtcp.type.regular.base.report.RtcpReportBlock;
import org.jmagni.jrtsp.rtsp.statistics.RtpStatistics;
import org.jmagni.jrtsp.rtsp.stream.StreamInfo;
import org.jmagni.jrtsp.rtsp.stream.UdpStream;
import org.jmagni.jrtsp.rtsp.stream.network.LocalNetworkInfo;
import org.jmagni.jrtsp.rtsp.stream.network.TargetNetworkInfo;
import org.jmagni.jrtsp.rtsp.stream.rtp.AudioRtpMeta;
import org.jmagni.jrtsp.rtsp.stream.rtp.RtcpInfo;
import org.jmagni.jrtsp.rtsp.stream.rtp.VideoRtpMeta;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import static org.jmagni.jrtsp.rtsp.stream.StreamInfo.TCP_RTP_MAGIC_NUMBER;
import static org.jmagni.jrtsp.rtsp.stream.rtp.RtcpInfo.RTCP_SR_LIMIT_COUNT;
import static org.jmagni.jrtsp.rtsp.stream.rtp.base.RtpMeta.*;

@Slf4j
public class Streamer {

    private final StreamInfo streamInfo;

    private final LocalNetworkInfo localNetworkInfo;

    private final TargetNetworkInfo targetNetworkInfo;

    private final AudioRtpMeta audioRtpMeta;
    private final VideoRtpMeta videoRtpMeta;

    private final RtcpInfo rtcpInfo = new RtcpInfo();

    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    private final RtpStatistics rtpStatistics = new RtpStatistics();

    public Streamer(MediaType mediaType, String callId, String sessionId, String trackId, boolean isTcp, String listenIp, int listenPort) {
        this.streamInfo = new StreamInfo(
                mediaType, callId, sessionId, trackId
        );
        if (!isTcp) {
            UdpStream udpStream = new UdpStream();
            udpStream.start(getCallId());
            streamInfo.setUdpStream(udpStream);
        }

        this.localNetworkInfo = new LocalNetworkInfo(listenIp, listenPort, isTcp);
        this.targetNetworkInfo = new TargetNetworkInfo();

        this.audioRtpMeta = new AudioRtpMeta();
        this.videoRtpMeta = new VideoRtpMeta();

        log.debug("({}) Streamer({}) is created. (callId={}, trackId={}, localNetworkInfo={})",
                getKey(), mediaType.getName(), callId, trackId, localNetworkInfo
        );
    }

    public void open() {
        try {
            UdpStream udpStream = streamInfo.getUdpStream();
            if (udpStream != null) {
                if (udpStream.connectTargetRtpEndpoint(targetNetworkInfo)) {
                    log.debug("({}) Success to connect the UDP Rtp endpoint. (targetNetworkInfo={})", getKey(), targetNetworkInfo);

                    if (udpStream.connectTargetRtcpEndpoint(targetNetworkInfo)) {
                        log.debug("({}) Success to connect the UDP Rtcp endpoint. (targetNetworkInfo={})", getKey(), targetNetworkInfo);
                    } else {
                        log.warn("({}) Fail to connect the UDP Rtcp endpoint. (targetNetworkInfo={})", getKey(), targetNetworkInfo);
                    }
                } else {
                    log.warn("({}) Fail to connect the UDP Rtp endpoint. (targetNetworkInfo={})", getKey(), targetNetworkInfo);
                }
            }
        } catch (Exception e) {
            log.warn("({}) Streamer.start.Exception", getKey(), e);
        }
    }

    public MediaType getMediaType() {
        return streamInfo.getMediaType();
    }

    public String getCallId() {
        return streamInfo.getCallId();
    }

    public String getSessionId() {
        return streamInfo.getSessionId();
    }

    public boolean isStarted() {
        return isStarted.get();
    }

    public void setStarted(boolean isPaused) {
        this.isStarted.set(isPaused);
    }

    public void close () {
        UdpStream udpStream = streamInfo.getUdpStream();
        if (udpStream != null) {
            udpStream.stop(targetNetworkInfo);
        }
        log.debug("({}) Streamer is finished.", getKey());
    }

    public void start() {
        rtpStatistics.start();

        isStarted.set(true);
        //log.debug("({}) Streamer is started. ({})", getKey(), this);
    }

    public void stop () {
        rtpStatistics.stop();

        close();
        isStarted.set(false);
        //log.debug("({}) Streamer is stopped. ({})", getKey(), this);
    }

    public String getDestIp() {
        return targetNetworkInfo.getDestIp();
    }

    public int getRtpDestPort() {
        return targetNetworkInfo.getRtpDestPort();
    }

    public int getRtcpDestPort() {
        return targetNetworkInfo.getRtcpDestPort();
    }

    public long getAudioSsrc() {
        return audioRtpMeta.getSsrc();
    }

    public void setAudioSsrc(long audioSsrc) {
        audioRtpMeta.setSsrc(audioSsrc);
    }

    public long getVideoSsrc() {
        return videoRtpMeta.getSsrc();
    }

    public void setVideoSsrc(long videoSsrc) {
        videoRtpMeta.setSsrc(videoSsrc);
    }

    public void setDestIp(String destIp) {
        targetNetworkInfo.setDestIp(destIp);
    }

    public void setRtpDestPort(int rtpDestPort) {
        targetNetworkInfo.setRtpDestPort(rtpDestPort);
    }

    public void setRtcpDestPort(int rtcpDestPort) {
        targetNetworkInfo.setRtcpDestPort(rtcpDestPort);
    }

    public void setVideoCurSeqNum(int videoCurSeqNum) {
        videoRtpMeta.setCurSeqNum(videoCurSeqNum);
    }

    public int getVideoCurSeqNum() {
        return videoRtpMeta.getCurSeqNum();
    }

    public void setVideoCurTimeStamp(long videoCurTimeStamp) {
        videoRtpMeta.setCurTimeStamp(videoCurTimeStamp);
    }

    public long getVideoCurTimeStamp() {
        return videoRtpMeta.getCurTimeStamp();
    }

    public int getAudioCurSeqNum() {
        return audioRtpMeta.getCurSeqNum();
    }

    public void setAudioCurSeqNum(int audioCurSeqNum) {
        audioRtpMeta.setCurSeqNum(audioCurSeqNum);
    }

    public long getAudioCurTimeStamp() {
        return audioRtpMeta.getCurTimeStamp();
    }

    public void setAudioCurTimeStamp(long audioCurTimeStamp) {
        audioRtpMeta.setCurTimeStamp(audioCurTimeStamp);
    }

    public String getClientUserAgent() {
        return streamInfo.getClientUserAgent();
    }

    public void setClientUserAgent(String clientUserAgent) {
        streamInfo.setClientUserAgent(clientUserAgent);
    }

    public String getUri() {
        return targetNetworkInfo.getUri();
    }

    public void setUri(String uri) {
        targetNetworkInfo.setUri(uri);
    }

    public boolean isRtpDestActive() {
        Channel rtpDestChannel = targetNetworkInfo.getRtpDestChannel();
        if (rtpDestChannel != null) {
            return rtpDestChannel.isActive() && rtpDestChannel.isOpen();
        } else {
            return false;
        }
    }

    public boolean isRtcpDestActive() {
        Channel rtcpDestChannel = targetNetworkInfo.getRtcpDestChannel();
        if (rtcpDestChannel != null) {
            return rtcpDestChannel.isActive() && rtcpDestChannel.isOpen();
        } else {
            return false;
        }
    }

    public void setRtspChannelContext(ChannelHandlerContext rtspChannelContext) {
        streamInfo.setRtspChannelContext(rtspChannelContext);
    }

    public boolean isTcp() {
        return localNetworkInfo.isTcp();
    }

    public void sendPlayResponse() {
        ChannelHandlerContext rtspChannelContext = streamInfo.getRtspChannelContext();
        DefaultFullHttpResponse playResponse = streamInfo.getPlayResponse();
        ReentrantLock playResponseLock = streamInfo.getPlayResponseLock();

        if (rtspChannelContext == null) {
            log.warn("({}) Fail to send the play response. Context is null.", getKey());
            return;
        } else if (playResponse == null) {
            log.warn("({}) Fail to send the play response. Response is null.", getKey());
            return;
        }

        playResponseLock.lock();
        try {
            playResponse.headers().add(
                    RtspHeaderNames.RTP_INFO,
                    makeRtpInfoData()
            );
            rtspChannelContext.writeAndFlush(playResponse);
            log.debug("({}) [PLAY] > Success to send the response: {}\n", getKey(), playResponse);
            streamInfo.setPlayResponse(null);
        } catch (Exception e) {
            // ignore
        } finally {
            playResponseLock.unlock();
        }
    }

    private String makeRtpInfoData() {
        return RtspHeaderValues.URL + "=" + targetNetworkInfo.getUri() + "/" + TRACK_ID_TAG + "=" + AUDIO_TRACK_ID
                + ";" + RtspHeaderValues.SEQ + "=" + getAudioCurSeqNum()
                + ";" + RtspHeaderValues.RTPTIME + "=" + getAudioCurTimeStamp() + "," +
                RtspHeaderValues.URL + "=" + targetNetworkInfo.getUri() + "/" + TRACK_ID_TAG + "=" + VIDEO_TRACK_ID
                + ";" + RtspHeaderValues.SEQ + "=" + getVideoCurSeqNum()
                + ";" + RtspHeaderValues.RTPTIME + "=" + getVideoCurTimeStamp();
    }

    public void sendRtpPacket(RtpPacket rtpPacket, String mediaType) {
        if (isTcp()) {
            sendRtpPacketWithTcp(rtpPacket);
        } else {
            if (streamInfo.getMediaType().getName().equals(mediaType)) {
                sendRtpPacketWithUdp(rtpPacket);
            }
        }
        rtpStatistics.calculate(rtpPacket.getRawData().length);
    }

    public void sendRtpPacketWithTcp(RtpPacket rtpPacket) {
        ChannelHandlerContext rtspChannelContext = streamInfo.getRtspChannelContext();
        if (rtspChannelContext == null) { return; }

        ByteBuf rtpBuf = Unpooled.copiedBuffer(makeTcpRtpData(rtpPacket));
        rtspChannelContext.writeAndFlush(rtpBuf);
    }

    private byte[] makeTcpRtpData(RtpPacket rtpPacket) {
        /**
         * The RTP data will be encapsulated in the following format:
         *    | magic number | channel number | Embedded data length | data |
         *
         *    1. Magic Number - 1 byte value of hex 0X24 (RTP data identifier)
         *    2. Channel number - 1 byte value to denote the channel number-1 bytes used to indicate the channel
         *    3. The embedded date length - 2 bytes to indicate the length of the inserted data
         *    4. RTP data (byte array)
         */

        byte[] rtpPacketRawData = rtpPacket.getRawData();
        int rtpDataLength = rtpPacketRawData.length;

        byte[] newRtpData = new byte[1 + 1 + 2 + rtpDataLength];
        newRtpData[0] = TCP_RTP_MAGIC_NUMBER;
        newRtpData[1] = Byte.parseByte(streamInfo.getTrackId());

        byte[] rtpDataLengthArray = ByteBuffer.allocate(2).putShort((short) rtpDataLength).array();
        newRtpData[2] = rtpDataLengthArray[0];
        newRtpData[3] = rtpDataLengthArray[1];
        System.arraycopy(rtpPacketRawData, 0, newRtpData, 4, rtpDataLength);
        return newRtpData;
    }

    public void sendRtpPacketWithUdp(RtpPacket rtpPacket) {
        try {
            ByteBuf rtpBuf = Unpooled.copiedBuffer(rtpPacket.getRawData());
            if (rtpBuf == null || rtpBuf.readableBytes() <= 0
                    || targetNetworkInfo.getDestIp() == null || targetNetworkInfo.getRtpDestPort() <= 0) {
                return;
            }

            Channel rtpDestChannel = targetNetworkInfo.getRtpDestChannel();
            if (rtpDestChannel != null) {
                ChannelFuture channelFuture = rtpDestChannel.writeAndFlush(rtpBuf);
                if (channelFuture == null && !isRtpDestActive()) {
                    log.warn("({}) Fail to send the message to rtp target. (targetNetworkInfo={})", getKey(), targetNetworkInfo);
                } /*else {
                    log.debug("RtpPacket: ts={}, seq={}, ssrc={} / destIp={}, rtpDestPort={}",
                            rtpPacket.getTimestamp(), rtpPacket.getSeqNumber(), rtpPacket.getSyncSource(),
                            destIp, rtpDestPort
                    );

                    processRtcpPacket(rtpPacket);
                }*/
            }
        } catch (Exception e) {
            log.warn("({}) Streamer.send.Exception", getKey(), e);
        }
    }

    private void processRtcpPacket(RtpPacket rtpPacket) {
        if (rtcpInfo == null) { return; }

        Channel rtcpDestChannel = targetNetworkInfo.getRtcpDestChannel();
        if (rtcpDestChannel != null) {
            int curRtcpSrCount = rtcpInfo.getCurRtcpSrCount();
            if (curRtcpSrCount < RTCP_SR_LIMIT_COUNT) {
                rtcpInfo.setCurRtcpSrCount(curRtcpSrCount + 1);
                rtcpInfo.setSpc(rtcpInfo.getSpc() + rtpPacket.getPayloadLength());
                return;
            }

            // RTCP SR
            RtcpSenderReport rtcpSenderReport = getRtcpSenderReport(rtpPacket);
            if (rtcpSenderReport == null) { return; }

            ByteBuf rtcpBuf = Unpooled.copiedBuffer(rtcpSenderReport.getData());
            if (rtcpBuf == null || rtcpBuf.readableBytes() <= 0 || targetNetworkInfo.getDestIp() == null || targetNetworkInfo.getRtcpDestPort() <= 0) {
                log.trace("({}) Fail to send the message. RtcpBuf is not defined. (targetNetworkInfo={}, bytes={})",
                        getKey(), targetNetworkInfo, Objects.requireNonNull(rtcpBuf).readableBytes()
                );
                return;
            }

            ChannelFuture rtcpChannelFuture = rtcpDestChannel.writeAndFlush(rtcpBuf);
            if (rtcpChannelFuture == null && !isRtcpDestActive()) {
                log.warn("({}) Fail to send the message to rtcp target. (targetNetworkInfo={})", getKey(), targetNetworkInfo);
            }
        }
    }

    private RtcpSenderReport getRtcpSenderReport(RtpPacket rtpPacket) {
        if (rtcpInfo == null) { return null; }

        long curSeconds = TimeStamp.getCurrentTime().getSeconds();
        long curFraction = TimeStamp.getCurrentTime().getFraction();
        long rtpTimestamp = rtpPacket.getTimestamp();
        long rtpSeqNum = rtpPacket.getSeqNumber();

        // REPORT BLOCK LIST
        List<RtcpReportBlock> rtcpReportBlockList = new ArrayList<>();
        RtcpReportBlock source1 = new RtcpReportBlock(
                rtpPacket.getSyncSource(), (byte) curFraction, 0,
                rtpSeqNum, 0,
                curSeconds, 0
        );
        rtcpReportBlockList.add(source1);

        RtcpSenderReport rtcpSenderReport = new RtcpSenderReport(
                curSeconds,
                curFraction, rtpTimestamp,
                rtcpInfo.getCurRtcpSrCount(), rtcpInfo.getSpc(),
                rtcpReportBlockList,
                null
        );

        rtcpInfo.setCurRtcpSrCount(0);
        rtcpInfo.setSpc(0);
        log.debug("({}) RtcpSenderReport: \n{}", getKey(), rtcpSenderReport);
        return rtcpSenderReport;
    }

    public void setCongestionLevel(int congestionLevel) {
        rtcpInfo.setCongestionLevel(congestionLevel);
    }

    public int getCongestionLevel() {
        return rtcpInfo.getCongestionLevel();
    }

    public DefaultFullHttpResponse getPlayResponse() {
        return streamInfo.getPlayResponse();
    }

    public void setPlayResponse(DefaultFullHttpResponse playResponse) {
        ReentrantLock playResponseLock = streamInfo.getPlayResponseLock();
        playResponseLock.lock();
        try {
            streamInfo.setPlayResponse(playResponse);
        } catch (Exception e) {
            // ignore
        } finally {
            playResponseLock.unlock();
        }
    }

    public String getTrackId() {
        return streamInfo.getTrackId();
    }

    public String getKey() {
        return (streamInfo.getTrackId() != null && !streamInfo.getTrackId().isEmpty()) ?
                streamInfo.getCallId() + ":" + streamInfo.getTrackId() : streamInfo.getCallId();
    }

}
