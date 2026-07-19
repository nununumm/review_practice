package com.example.campaign;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 抽選キャンペーンの応募を受け付けるサービス。
 * 1ユーザーにつき1回だけ応募できるようにしている。
 */
@Service
public class CampaignApplicationService {

    @Autowired
    private CampaignRepository campaignRepository;

    // すでに応募済みのユーザーを覚えておく箱
    private Map<Long, Boolean> appliedUsers = new HashMap<>();

    // これまでの合計応募数
    private int totalApplications = 0;

    public String apply(Long userId) {
        // すでに応募済みなら弾く
        if (appliedUsers.containsKey(userId)) {
            return "すでに応募済みです";
        }

        // 応募を記録して保存する
        appliedUsers.put(userId, true);
        totalApplications = totalApplications + 1;
        campaignRepository.save(new Application(userId));

        return "応募を受け付けました（現在の応募数: " + totalApplications + "）";
    }
}
