package org.jmagni.jrtsp.rtsp.rtcp.type.extended.feedback.transportlayer;

/**
 * @Reference https://datatracker.ietf.org/doc/html/rfc4585#section-6.1
 *
 *     0                   1                   2                   3
 *     0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |V=2|P|   FMT   |       PT      |          length               |
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |                  SSRC of packet sender                        |
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |                  SSRC of media source                         |
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    :            Feedback Control Information (FCI)                 :
 *    :                                                               :
 *
 *            Figure 3: Common Packet Format for Feedback Messages
 */


import org.jmagni.jrtsp.rtsp.rtcp.type.extended.feedback.RtcpFeedback;
import org.jmagni.jrtsp.rtsp.rtcp.type.extended.feedback.base.RtcpFeedbackMessageHeader;

/**
 * @Reference https://datatracker.ietf.org/doc/html/rfc5104#section-2.2
 *
 *    Decoder Refresh Point:
 *           A bit string, packetized in one or more RTP packets, that
 *           completely resets the decoder to a known state.
 *
 *           Examples for "hard" decoder refresh points are Intra pictures
 *           in H.261, H.263, MPEG-1, MPEG-2, and MPEG-4 part 2, and
 *           Instantaneous Decoder Refresh (IDR) pictures in H.264.
 *           "Gradual" decoder refresh points may also be used; see for
 *           example [AVC].  While both "hard" and "gradual" decoder
 *           refresh points are acceptable in the scope of this
 *           specification, in most cases the user experience will benefit
 *           from using a "hard" decoder refresh point.
 *
 *           A decoder refresh point also contains all header information
 *           above the picture layer (or equivalent, depending on the video
 *           compression standard) that is conveyed in-band.  In H.264, for
 *           example, a decoder refresh point contains parameter set
 *           Network Adaptation Layer (NAL) units that generate parameter
 *           sets necessary for the decoding of the following slice/data
 *           partition NAL units (and that are not conveyed out of band).
 *
 *    Overhead:
 *           All protocol header information required to convey a packet
 *           with media data from sender to receiver, from the application
 *           layer down to a pre-defined protocol level (for example, down
 *           to, and including, the IP header).  Overhead may include, for
 *           example, IP, UDP, and RTP headers, any layer 2 headers, any
 *           Contributing Sources (CSRCs), RTP padding, and RTP header
 *           extensions.  Overhead excludes any RTP payload headers and the
 *           payload itself.
 *
 *    Bounding set:
 *           The set of TMMBR tuples, selected from all those received at a
 *           given media sender, that define the feasible region for that
 *           media sender.  The media sender uses an algorithm such as that
 *           in section 3.5.4.2 to determine or iteratively approximate the
 *           current bounding set, and reports that set back to the media
 *           receivers in a Temporary Maximum Media Stream Bit Rate
 *           Notification (TMMBN) message.
 *
 */

public class RtcpTemporaryMaximumMediaStreamBitRateNotification extends RtcpFeedback {

    /**
     * @Reference https://datatracker.ietf.org/doc/html/rfc5104#page-29
     */

    /**
     * @ Semantics
     *
     *  The "SSRC of packet sender" field indicates the source of the request,
     *  and the "SSRC of media source" is not used and SHALL be set to 0.
     *
     *  The SSRCs of the media senders to which the FIR command applies are in the corresponding FCI entries.
     *
     *  A FIR message MAY contain requests to multiple media senders,
     *  using one FCI entry per target media sender.
     *
     *
     *  FIR SHALL NOT be sent as a reaction to picture losses -- it is
     *    RECOMMENDED to use PLI [RFC4585] instead.
     *    FIR SHOULD be used only in situations
     *    where not sending a decoder refresh point would render the video unusable for the users.
     *    > FIR ??? ????????? ????????? ?????? ???????????? ??? ?????? ??????.
     *      FIR ??? PLI ??? ???????????? ??????????????? ???????????? ??????.
     *    > FIR ??? decoder refresh point ??? ????????? ??????????????? ??????????????? ????????? ?????? ????????? ??? ??? ?????? ?????? ?????????.
     *
     *  A typical example where sending FIR is appropriate is when, in a
     *    multipoint conference, a new user joins the session and
     *    no regular decoder refresh point interval is established.
     *    > FIR ??? ?????? ?????? ???????????? ???????????? ?????? ?????????????????? ????????? ??????????????? ????????? ???????????? ?????? ????????? ????????? ???,
     *      decoder refresh point ????????? ????????? ????????? ?????? ??????
     *
     *  Another example would
     *    be a video switching MCU that changes streams.  Here, normally, the
     *    MCU issues a FIR to the new sender so to force it to emit a decoder
     *    refresh point.  The decoder refresh point normally includes a Freeze
     *    Picture Release (defined outside this specification), which re-starts
     *    the rendering process of the receivers.  Both techniques mentioned
     *    are commonly used in MCU-based multipoint conferences.
     *    > MCU ????????? ?????? ??? ??????, decoder refresh point ??? MCU ??? ???????????? ????????? ????????? ????????? ??????????????? FIR ?????? ??????
     */

    /**
     * The Temporary Maximum Media Stream Bit Rate Notification is
     *    identified by RTCP packet type value PT=RTPFB and FMT=4.
     *
     *    The FCI field of the TMMBN feedback message may contain zero, one, or
     *    more TMMBN FCI entries.
     *
     *    The Feedback Control Information (FCI) consists of zero, one, or more
     *    TMMBN FCI entries with the following syntax:
     *    Syntax of an FCI Entry in the TMMBN Message
     *
     *     0                   1                   2                   3
     *     0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     *    |                              SSRC                             |
     *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     *    | MxTBR Exp |  MxTBR Mantissa                 |Measured Overhead|
     *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     *
     *      SSRC (32 bits): The SSRC value of the "owner" of this tuple.
     *
     *      MxTBR Exp (6 bits): The exponential scaling of the mantissa for the
     *               maximum total media bit rate value.  The value is an
     *               unsigned integer [0..63].
     *
     *      MxTBR Mantissa (17 bits): The mantissa of the maximum total media
     *               bit rate value as an unsigned integer.
     *
     *      Measured Overhead (9 bits): The measured average packet overhead
     *               value in bytes represented as an unsigned integer
     *               [0..511].
     *
     *   Thus, the FCI within the TMMBN message contains entries indicating
     *    the bounding tuples.  For each tuple, the entry gives the owner by
     *    the SSRC, followed by the applicable maximum total media bit rate and
     *    overhead value.
     *    > FCI ??????????????? ???????????? ???????????? ???????????? ??????.
     *      ????????? ??????????????? owner ??? SSRC ??? ?????? ????????? ?????????????????? ???????????? ?????? ????????????.
     *
     *    The length of the TMMBN message SHALL be set to 2+2*N where N is the
     *    number of TMMBN FCI entries.
     *    > TMMBN ????????? ????????? 2 + 2*N ??? ???????????? N ??? TMMBN ?????? ????????? FCI ????????? ????????????.
     *
     */

    ////////////////////////////////////////////////////////////
    // VARIABLES
    public static final int MIN_LENGTH = RtcpFeedbackMessageHeader.LENGTH; // bytes


    ////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    public RtcpTemporaryMaximumMediaStreamBitRateNotification(RtcpFeedbackMessageHeader rtcpFeedbackMessageHeader) {
        super(rtcpFeedbackMessageHeader);
    }

    public RtcpTemporaryMaximumMediaStreamBitRateNotification() {
    }

    public RtcpTemporaryMaximumMediaStreamBitRateNotification(byte[] data) {
        super(data);
    }
    ////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////
    // FUNCTIONS


    ////////////////////////////////////////////////////////////

}
