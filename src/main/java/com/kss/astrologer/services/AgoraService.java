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

    private final String appId = dotenv.get("AGORA_APP_ID");
    private final String appCertificate = dotenv.get("AGORA_APP_CERTIFICATE");

    public String generateToken(String channelName, String uid, int requestedMinutes) {
        int expireTimeInSeconds = requestedMinutes * 60;
        int timestamp = (int) (System.currentTimeMillis() / 1000 + expireTimeInSeconds);
        RtcTokenBuilder2 tokenBuilder = new RtcTokenBuilder2();
        return tokenBuilder.buildTokenWithUserAccount(appId, appCertificate, channelName, uid,
                RtcTokenBuilder2.Role.ROLE_PUBLISHER, timestamp, timestamp);
    }
}
