package com.videotask.repository;

import com.videotask.entity.UserWechat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserWechatRepository extends JpaRepository<UserWechat, Integer> {
    
    // 根据unionid查找微信用户
    Optional<UserWechat> findByUnionid(String unionid);
    
    // 根据openId查找微信用户
    Optional<UserWechat> findByOpenId(String openId);
    
    // 根据手机号查找微信用户
    Optional<UserWechat> findByPhoneNumber(String phoneNumber);
    
    // 检查unionid是否存在
    boolean existsByUnionid(String unionid);
    
    // 检查openId是否存在
    boolean existsByOpenId(String openId);
}
