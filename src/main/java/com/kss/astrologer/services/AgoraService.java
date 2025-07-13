package com.kss.astrologer.services;

import io.agora.media.RtcTokenBuilder2;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Service;

@Service
public class AgoraService {
    private final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMalformed()
            .ignoreIfMissing()
            .load();

    private final String appId = "your-agora-app-id";
    private final String appCertificate = "your-app-certificate";

    public String generateToken(String channelName, String uid, int requestedMinutes) {
        int expireTimeInSeconds = requestedMinutes * 60;
        int timestamp = (int) (System.currentTimeMillis() / 1000 + expireTimeInSeconds);
        RtcTokenBuilder2 tokenBuilder = new RtcTokenBuilder2();
        return tokenBuilder.buildTokenWithUid(appId, appCertificate, channelName, Integer.parseInt(uid),
                RtcTokenBuilder2.Role.ROLE_PUBLISHER, timestamp, timestamp);
    }
}
