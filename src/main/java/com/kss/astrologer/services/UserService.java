package com.kss.astrologer.services;

import java.time.LocalDateTime;
import java.util.UUID;

import com.kss.astrologer.services.aws.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kss.astrologer.dto.UserDto;
import com.kss.astrologer.exceptions.CustomException;
import com.kss.astrologer.models.User;
import com.kss.astrologer.models.Wallet;
import com.kss.astrologer.repository.UserRepository;
import com.kss.astrologer.repository.WalletRepository;
import com.kss.astrologer.request.KundliRequest;
import com.kss.astrologer.types.Role;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private S3Service s3Service;

    public User getUserByMobile(String mobile) {
        return userRepository.findByMobile(mobile).orElse(null);
    }

    @Transactional
    public User getOrCreateUser(String mobile) {
        return userRepository.findByMobile(mobile).orElseGet(() ->
        {
            Wallet wallet = new Wallet();
            wallet.setBalance(0.0);
            wallet = walletRepository.save(wallet);
            User user = userRepository.save(User.builder()
                    .mobile(mobile)
                    .role(Role.USER)
                    .isFreeChatUsed(false)
                    .isFirstTopUpDone(false)
                    .wallet(wallet)
//                    .imgUri("https://img.freepik.com/free-vector/young-man-orange-hoodie_1308-175788.jpg?ga=GA1.1.1570607994.1749976697&semt=ais_hybrid&w=740")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());
            wallet.setUser(user);
            walletRepository.save(wallet);
            return user;
        });
    }

    @Transactional
    public User getOrCreateAdmin(String mobile) {
        return userRepository.findByMobile(mobile).orElseGet(() ->
        {
            Wallet wallet = new Wallet();
            wallet.setBalance(0.0);
            wallet = walletRepository.save(wallet);
            User user = userRepository.save(User.builder()
                    .mobile(mobile)
                    .role(Role.ADMIN)
                    .isFreeChatUsed(false)
                    .isFirstTopUpDone(false)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());
            wallet.setUser(user);
            walletRepository.save(wallet);
            return user;
        });
    }

    public UserDto getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "User not found"));
        return new UserDto(user);
    }

    public UserDto addUserDetails(KundliRequest data, UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "User not found"));

        user.setName(data.getName());
        user.setGender(data.getGender());
        user.setBirthDate(data.getBirthDate());
        user.setBirthTime(data.getBirthTime());
        user.setBirthPlace(data.getBirthPlace());
        user.setLatitude(data.getLatitude());
        user.setLongitude(data.getLongitude());
        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);
        return new UserDto(updatedUser);
    }

    public User getById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public UserDto uploadProfileImage(UUID userId, MultipartFile file) {
        User user = getById(userId);
        String imgUrl = s3Service.uploadFile(file, "user");
        if(user.getImgUri() != null) {
            s3Service.deleteFileByUrl(user.getImgUri());
        }
        user.setImgUri(imgUrl);
        user = userRepository.save(user);
        return new UserDto(user);
    }
}
