package com.kss.astrologer.services;

import com.kss.astrologer.models.AstrologerDeviceToken;
import com.kss.astrologer.repository.AstrologerDeviceTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DeviceTokenService {
    @Autowired
    private AstrologerDeviceTokenRepository tokenRepo;

    public AstrologerDeviceToken saveOrUpdateToken(UUID userId, String deviceToken) {
        Optional<AstrologerDeviceToken> existingToken = tokenRepo.findByDeviceToken(deviceToken);

        if (existingToken.isEmpty()) {
            AstrologerDeviceToken newToken = new AstrologerDeviceToken();
            newToken.setUserId(userId);
            newToken.setDeviceToken(deviceToken);
            return tokenRepo.save(newToken);
        }
        return existingToken.get();
    }

    public List<String> getTokens(UUID userId) {
        return tokenRepo.findByUserId(userId)
                .stream()
                .map(AstrologerDeviceToken::getDeviceToken)
                .toList();
    }

    public void deleteToken(String deviceToken) {
        Optional<AstrologerDeviceToken> token = tokenRepo.findByDeviceToken(deviceToken);
        token.ifPresent(astrologerDeviceToken -> tokenRepo.deleteById(astrologerDeviceToken.getId()));
    }
}
