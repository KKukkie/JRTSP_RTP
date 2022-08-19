package org.jmagni.jrtsp.rtsp.sdp.base.media;

import org.jmagni.jrtsp.rtsp.sdp.base.SdpFactory;
import org.jmagni.jrtsp.rtsp.sdp.base.attribute.RtpAttribute;
import org.jmagni.jrtsp.rtsp.sdp.base.attribute.base.AttributeFactory;
import org.jmagni.jrtsp.rtsp.sdp.base.attribute.base.FmtpAttributeFactory;
import org.jmagni.jrtsp.rtsp.sdp.base.attribute.base.RtpMapAttributeFactory;
import org.jmagni.jrtsp.rtsp.sdp.base.field.BandwidthField;
import org.jmagni.jrtsp.rtsp.sdp.base.field.ConnectionField;
import org.jmagni.jrtsp.rtsp.sdp.base.field.MediaField;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.jmagni.jrtsp.rtsp.sdp.base.attribute.base.RtpMapAttributeFactory.DTMF;

/**
 * @class public class MediaFactory
 * @brief MediaFactory class
 */
public class MediaFactory extends SdpFactory {

    public static final String RTPMAP = "rtpmap";
    public static final String FMTP = "fmtp";

    private final Map<String, RtpAttribute> attributeFactoryMap = new LinkedHashMap<>();
    private List<RtpAttribute> intersectedCodecList = null;

    // Mandatory
    private MediaField mediaField;

    // Optional
    private final List<BandwidthField> bandwidthFieldList = new ArrayList<>();
    private ConnectionField connectionField;

    ////////////////////////////////////////////////////////////////////////////////

    public MediaFactory(
            char type,
            String mediaType, List<String> mediaFormats, int mediaPort,
            String protocol, int portCount) {
        this.mediaField = new MediaField(
                type,
                mediaType,
                mediaPort,
                protocol,
                mediaFormats,
                portCount
        );
    }

    ////////////////////////////////////////////////////////////////////////////////

    public void addBandwidthField(char bandwidthType, String modifier, int value) {
        BandwidthField bandwidthField = new BandwidthField(
                bandwidthType,
                modifier,
                value
        );

        bandwidthFieldList.add(bandwidthField);
    }

    public List<BandwidthField> getBandwidthFieldList() {
        return bandwidthFieldList;
    }

    public String getBandwidthData () {
        StringBuilder result = new StringBuilder();
        //for (int i = bandwidthFieldList.size() - 1; i >= 0; i--) {
        //BandwidthField bandwidthField = bandwidthFieldList.get(i);
        for (BandwidthField bandwidthField : bandwidthFieldList) {
            if (bandwidthField != null) {
                result.append(bandwidthField.getBandwidthType()).append("=").
                        append(bandwidthField.getModifier()).append(":").
                        append(bandwidthField.getValue()).
                        append(CRLF);
            }
        }

        return result.toString();
    }

    public void setConnectionField(char connectionType, String connectionAddress, String connectionAddressType, String connectionNetworkType) {
        this.connectionField = new ConnectionField(
                connectionType,
                connectionAddress,
                connectionAddressType,
                connectionNetworkType
        );
    }

    public ConnectionField getConnectionField() {
        return connectionField;
    }

    public void setConnectionField(ConnectionField connectionField) {
        this.connectionField = connectionField;
    }

    public String getConnectionData () {
        if (connectionField != null) {
            return connectionField.getConnectionType() + "=" +
                    connectionField.getConnectionNetworkType() + " " +
                    connectionField.getConnectionAddressType() + " " +
                    connectionField.getConnectionAddress() +
                    CRLF;
        }

        return "";
    }

    public List<RtpAttribute> getIntersectedCodecList() {
        if (intersectedCodecList == null) {
            return getCodecList();
        }
        return intersectedCodecList;
    }

    public void setIntersectedCodecList(List<RtpAttribute> intersectedCodecList) {
        this.intersectedCodecList = intersectedCodecList;
    }

    public MediaField getMediaField() {
        return mediaField;
    }

    public void setMediaField(MediaField mediaField) {
        this.mediaField = mediaField;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public void addAttributeFactory(AttributeFactory attributeFactory) {
        String payloadId = attributeFactory.getPayloadId();
        if (payloadId == null) {
            return;
        }

        RtpAttribute rtpAttribute = attributeFactoryMap.get(payloadId);
        if (rtpAttribute == null) {
            rtpAttribute = new RtpAttribute(
                    payloadId
            );
            rtpAttribute.setCustomAttributeFactory(attributeFactory);

            attributeFactoryMap.putIfAbsent(
                    payloadId,
                    rtpAttribute
            );
        } else {
            rtpAttribute.setCustomAttributeFactory(attributeFactory);
        }
    }

    public void addRtpAttributeFactory(RtpMapAttributeFactory rtpMapAttributeFactory) {
        String payloadId = rtpMapAttributeFactory.getPayloadId();
        if (payloadId == null) {
            return;
        }

        RtpAttribute rtpAttribute = attributeFactoryMap.get(payloadId);
        if (rtpAttribute == null) {
            rtpAttribute = new RtpAttribute(
                    payloadId
            );
            rtpAttribute.setRtpMapAttributeFactory(rtpMapAttributeFactory);

            attributeFactoryMap.putIfAbsent(
                    payloadId,
                    rtpAttribute
            );
        } else {
            rtpAttribute.setRtpMapAttributeFactory(rtpMapAttributeFactory);
        }
    }

    public void addFmtpAttributeFactory(FmtpAttributeFactory fmtpAttributeFactory) {
        String payloadId = fmtpAttributeFactory.getPayloadId();
        if (payloadId == null) {
            return;
        }

        RtpAttribute rtpAttribute = attributeFactoryMap.get(payloadId);
        if (rtpAttribute == null) {
            rtpAttribute = new RtpAttribute(payloadId);
            attributeFactoryMap.putIfAbsent(
                    payloadId,
                    rtpAttribute
            );
        }
        
        if (fmtpAttributeFactory.getName().equals(FMTP)) {
            rtpAttribute.addFmtpAttributeFactory(fmtpAttributeFactory);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////

    public List<RtpAttribute> getCodecList () {
        List<RtpAttribute> codecList = new ArrayList<>();

        for (String format : mediaField.getMediaFormats()) {
            for (Map.Entry<String, RtpAttribute> entry : attributeFactoryMap.entrySet()) {
                if (entry == null) {
                    continue;
                }

                RtpAttribute rtpAttribute = entry.getValue();
                if (rtpAttribute == null) {
                    continue;
                }

                if (format.equals(rtpAttribute.getPayloadId())) {
                    codecList.add(rtpAttribute);
                    break;
                }
            }
        }

        return codecList;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public String getData (boolean isRaw) {
        // Media
        String mediaString = mediaField.getType() + "=" + mediaField.getMediaType() + " ";
        mediaString += mediaField.getMediaPort() + " ";
        mediaString += mediaField.getProtocol() + " ";

        StringBuilder data = new StringBuilder(
                mediaString
        );

        int i = 0;
        List<RtpAttribute> curCodecList = intersectedCodecList;
        if (curCodecList == null) {
            curCodecList = getCodecList();
        }

        // CODEC : Payload ID
        for (RtpAttribute rtpAttribute : curCodecList) {
            String codecName = rtpAttribute.getRtpMapAttributeFactory().getCodecName();
            if (codecName.equals(DTMF)) {
                i++;
                continue;
            }

            data.append(rtpAttribute.getPayloadId());

            if (!isRaw) {
                break;
            }

            if ((i + 1) < curCodecList.size()) {
                data.append(" ");
            }

            i++;
        }

        if (data.charAt(data.length() - 1) != ' ') {
            data.append(" ");
        }

        // CODEC : Payload ID
        i = 0;
        for (RtpAttribute rtpAttribute : curCodecList) {
            String codecName = rtpAttribute.getRtpMapAttributeFactory().getCodecName();
            if (!codecName.equals(DTMF)) {
                i++;
                continue;
            }

            data.append(rtpAttribute.getPayloadId());

            if (!isRaw) {
                break;
            }

            if ((i + 1) < curCodecList.size()) {
                data.append(" ");
            }

            i++;
        }
        data.append(CRLF);
        //

        // Bandwidth
        data.append(getBandwidthData());
        //

        // Connection
        data.append(getConnectionData());
        //

        // Attributes
        // CODEC : RTPMAP & FMTP
        for (RtpAttribute rtpAttribute : curCodecList) {
            String codecName = rtpAttribute.getRtpMapAttributeFactory().getCodecName();
            if (codecName.equals(DTMF)) {
                continue;
            }

            data.append(rtpAttribute.getData());
            if (!isRaw) {
                break;
            }
        }

        // DTMF : RTPMAP & FMTP
        for (RtpAttribute rtpAttribute : curCodecList) {
            String codecName = rtpAttribute.getRtpMapAttributeFactory().getCodecName();
            if (!codecName.equals(DTMF)) {
                continue;
            }

            data.append(rtpAttribute.getData());
            if (!isRaw) {
                break;
            }
        }

        // CUSTOM
        List<RtpAttribute> rtpAttributeList = new ArrayList<>(attributeFactoryMap.values());
        for (RtpAttribute rtpAttribute : rtpAttributeList) {
            if (!mediaField.getMediaFormats().contains(rtpAttribute.getPayloadId())) {
                AttributeFactory attributeFactory = rtpAttribute.getCustomAttributeFactory();
                if (attributeFactory != null &&
                        (attributeFactory.getName().equals("visited-realm") ||
                                attributeFactory.getName().equals("omr-m-cksum") ||
                                attributeFactory.getName().equals("omr-s-cksum"))) {
                    continue;
                }

                data.append(rtpAttribute.getData());
            }
        }
        //

        return data.toString();
    }

}
