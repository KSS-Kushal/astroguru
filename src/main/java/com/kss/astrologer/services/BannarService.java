package com.kss.astrologer.services;

import com.kss.astrologer.exceptions.CustomException;
import com.kss.astrologer.models.Bannar;
import com.kss.astrologer.models.TopBannar;
import com.kss.astrologer.repository.BannarRepository;
import com.kss.astrologer.repository.TopBannarRepository;
import com.kss.astrologer.services.aws.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public class BannarService {

    @Autowired
    private BannarRepository bannarRepository;

    @Autowired
    private TopBannarRepository topBannarRepository;

    @Autowired
    private S3Service s3Service;

    public Bannar uploadBannar(MultipartFile file) {
//        List<Bannar> bannars = bannarRepository.findAll();
//        if(!bannars.isEmpty()) {
//            bannars.forEach(b -> {
//                s3Service.deleteFileByUrl(b.getImgUrl());
//                deleteBannar(b.getId());
//            });
//        }
        String imgUrl = s3Service.uploadFile(file, "bannar");
        Bannar bannar = new Bannar();
        bannar.setImgUrl(imgUrl);
        return bannarRepository.save(bannar);
    }

    public Bannar deleteBannar(UUID id) {
        Bannar bannar = bannarRepository.findById(id).orElseThrow(()->new CustomException("Bannar not found"));
        bannarRepository.deleteById(id);
        return bannar;
    }

    public List<Bannar> getBannar() {
        return bannarRepository.findAll();
    }

    public TopBannar uploadTopBannar(MultipartFile file) {
        String imgUrl = s3Service.uploadFile(file, "top-bannar");
        List<TopBannar> bannars = topBannarRepository.findAll();
        TopBannar bannar;
        if (bannars.isEmpty()) {
            bannar = new TopBannar();
        } else {
            bannar = bannars.get(0);
            String url = bannar.getImgUrl();
            s3Service.deleteFileByUrl(url);
        }
        bannar.setImgUrl(imgUrl);
        return topBannarRepository.save(bannar);
    }

    public TopBannar getTopBannar() {
        List<TopBannar> bannars = topBannarRepository.findAll();
        if (bannars.isEmpty()) {
            return null;
        }
        return bannars.get(0);
    }
}
