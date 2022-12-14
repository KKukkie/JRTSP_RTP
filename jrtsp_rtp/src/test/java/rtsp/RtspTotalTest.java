package rtsp;

import lombok.extern.slf4j.Slf4j;
import org.jmagni.jrtsp.config.ConfigManager;
import org.jmagni.jrtsp.rtsp.PortManager;
import org.jmagni.jrtsp.rtsp.netty.NettyChannelManager;
import org.jmagni.jrtsp.service.AppInstance;
import org.junit.Test;

import javax.sound.sampled.Port;

@Slf4j
public class RtspTotalTest {

    //////////////////////////////////////////////////////////////////////////////
    public static void init() {
        AppInstance instance = AppInstance.getInstance();
        instance.setInstanceId(1);
        instance.setConfigPath(System.getProperty("user.dir") + "/src/test/resources/config/");

        log.info("class root: {}", RtspTest_Scenario2_StreamingOfAContainerFile.class.getResource("/").getPath());
        log.info("instance.getConfigPath(): {}", instance.getConfigPath());

        ConfigManager configManager = new ConfigManager(instance.getConfigPath() + "user_conf.ini");
        instance.setConfigManager(configManager);

        PortManager.getInstance().initResource(configManager.getUserConfig().getLocalRtcpPortMin(), configManager.getUserConfig().getLocalRtcpPortMax());

        NettyChannelManager.getInstance().openRtspChannel(
                AppInstance.getInstance().getConfigManager().getUserConfig().getLocalListenIp(),
                AppInstance.getInstance().getConfigManager().getUserConfig().getLocalRtspListenPort()
        );
    }

    public static void close() {
        NettyChannelManager.getInstance().deleteRtspChannel();
        PortManager.getInstance().releaseResource();
    }
    //////////////////////////////////////////////////////////////////////////////

    /**
     * #Scenario-1 14.1 Media on Demand (Unicast)
     *    Client C requests a movie from media servers A ( audio.example.com)
     *    and V (video.example.com). The media description is stored on a web
     *    server W . The media description contains descriptions of the
     *    presentation and all its streams, including the codecs that are
     *    available, dynamic RTP payload types, the protocol stack, and content
     *    information such as language or copyright restrictions. It may also
     *    give an indication about the timeline of the movie.
     *
     *    > SDP ??????, ?????????, ????????? ????????? ????????? ?????? ??????
     *
     * #Scenario-2 Streaming of a Container file
     *    For purposes of this example, a container file is a storage entity in
     *    which multiple continuous media types pertaining to the same end-user
     *    presentation are present. In effect, the container file represents an
     *    RTSP presentation, with each of its components being RTSP streams.
     *    Container files are a widely used means to store such presentations.
     *    While the components are transported as independent streams, it is
     *    desirable to maintain a common context for those streams at the
     *    server end.
     *
     *    > ?????? ???????????? ????????? ???????????? ??????, ????????? ???????????? ????????????(?????????????????????) ?????? (?????????, ?????????)
     *
     * #Scenario-3 Single Stream Container Files
     *    Some RTSP servers may treat all files as though they are "container
     *    files", yet other servers may not support such a concept. Because of
     *    this, clients SHOULD use the rules set forth in the session
     *    description for request URLs, rather than assuming that a consistent
     *    URL may always be used throughout. Here's an example of how a multi-stream
     *    server might expect a single-stream file to be served:
     *           Accept: application/x-rtsp-mh, application/sdp
     *
     *    > ?????????????????? ???????????? ?????? ????????? ?????? ???????????? ???????????? ????????? ???????????? ??????
     *    (sdp > a=control:stream_id=0)
     *
     * #Scenario-4 Live Media Presentation Using Multicast
     *    The media server M chooses the multicast address and port. Here, we
     *    assume that the web server only contains a pointer to the full
     *    description, while the media server M maintains the full description.
     *
     *    > ???????????? ?????????????????? ?????????????????? IP ????????? ?????? ????????? ??????
     *
     * #Scenario-5 Playing media into an existing session
     *    A conference participant C wants to have the media server M play back
     *    a demo tape into an existing conference. C indicates to the media
     *    server that the network addresses and encryption keys are already
     *    given by the conference, so they should not be chosen by the server.
     *    The example omits the simple ACK responses.
     *
     *    > Conference ID ??? ????????? ????????? ????????? ????????? ???????????? ??????
     *
     */

    @Test
    public void testAll() throws Exception {
        RtspTest_Scenario2_StreamingOfAContainerFile scenario5 = new RtspTest_Scenario2_StreamingOfAContainerFile();
        scenario5.testAll();
    }

}
