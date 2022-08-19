package org.jmagni.jrtsp.rtsp;

import org.jmagni.jrtsp.config.UserConfig;
import org.jmagni.jrtsp.service.AppInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @class public class ResourceManager
 * @brief ResourceManager class
 */
public class ResourceManager {

    private static final Logger logger = LoggerFactory.getLogger(ResourceManager.class);

    private static ResourceManager resourceManager = null;
    private final ConcurrentLinkedQueue<Integer> channelQueues;

    private int localRtpPortMin = 0;
    private int localRtpPortMax = 0;
    private final int portGap = 2;

    ////////////////////////////////////////////////////////////////////////////////

    public ResourceManager( ) {
        channelQueues = new ConcurrentLinkedQueue<>();
    }

    public static ResourceManager getInstance ( ) {
        if (resourceManager == null) {
            resourceManager = new ResourceManager();
        }

        return resourceManager;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public void initResource() {
        UserConfig userConfig = AppInstance.getInstance().getConfigManager().getUserConfig();
        localRtpPortMin = userConfig.getLocalRtcpPortMin();
        localRtpPortMax = userConfig.getLocalRtcpPortMax();

        for (int idx = localRtpPortMin; idx <= localRtpPortMax; idx += portGap) {
            try {
                channelQueues.add(idx);
            } catch (Exception e) {
                logger.error("Exception to RTP port resource in Queue", e);
                return;
            }
        }

        logger.info("Ready to RTP port resource in Queue. (port range: {} - {}, gap={})",
                localRtpPortMin, localRtpPortMax, portGap
        );
    }

    public void releaseResource () {
        channelQueues.clear();
        logger.info("Release RTP port resource in Queue. (port range: {} - {}, gap={})",
                localRtpPortMin, localRtpPortMax, portGap
        );
    }

    public int takePort () {
        if (channelQueues.isEmpty()) {
            logger.warn("RTP port resource in Queue is empty.");
            return -1;
        }

        int port = -1;
        try {
            Integer value = channelQueues.poll();
            if (value != null) {
                port = value;
            }
        } catch (Exception e) {
            logger.warn("Exception to get RTP port resource in Queue.", e);
        }

        logger.debug("Success to get RTP port(={}) resource in Queue.", port);
        return port;
    }

    public void restorePort (int port) {
        if (!channelQueues.contains(port)) {
            try {
                channelQueues.offer(port);
            } catch (Exception e) {
                logger.warn("Exception to restore RTP port(={}) resource in Queue.", port, e);
            }
        }
    }

    public void removePort (int port) {
        try {
            channelQueues.remove(port);
        } catch (Exception e) {
            logger.warn("Exception to remove to RTP port(={}) resource in Queue.", port, e);
        }
    }

}
