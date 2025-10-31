package com.videotask.service.impl;

import com.github.kwai.open.response.CreateVideoResponse;
import com.videotask.entity.UserAuth;
import com.videotask.entity.Video;
import com.videotask.entity.VideoShareUserLog;
import com.videotask.repository.UserAuthRepository;
import com.videotask.repository.VideoRepository;
import com.videotask.repository.VideoShareUserLogRepository;
import com.videotask.service.KuaishouSdkService;
import com.videotask.service.VideoUploadSchedulerService;
import com.videotask.util.VideoDownloadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
public class VideoUploadSchedulerServiceImpl implements VideoUploadSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(VideoUploadSchedulerServiceImpl.class);

    @Autowired
    private VideoShareUserLogRepository shareLogRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private UserAuthRepository userAuthRepository;

    @Autowired
    private KuaishouSdkService kuaishouSdkService;

    @Autowired
    private VideoDownloadUtil videoDownloadUtil;

    /**
     * 处理待上传的视频
     */
    @Override
    public void processPendingVideos() {
        logger.info("开始处理待上传的视频...");

        try {
            // 查找状态为0的视频分享记录
            List<VideoShareUserLog> pendingVideos = shareLogRepository.findByStatus(0);

            if (pendingVideos.isEmpty()) {
                logger.info("没有待上传的视频");
                return;
            }

            logger.info("找到 {} 个待上传的视频", pendingVideos.size());

            // 一条一条处理
            for (VideoShareUserLog shareLog : pendingVideos) {
                try {
                    uploadSingleVideo(shareLog.getId());
                    // 每次上传后等待2秒，避免频率过高
                    Thread.sleep(2000);
                } catch (Exception e) {
                    logger.error("处理视频上传失败，shareLogId: {}, 错误: {}", shareLog.getId(), e.getMessage());
                }
            }

        } catch (Exception e) {
            logger.error("处理待上传视频时发生错误: {}", e.getMessage(), e);
        }
    }

    @Override
    public void uploadSingleVideo(Integer shareLogId) {
        logger.info("开始上传视频，shareLogId: {}", shareLogId);

        try {
            // 获取分享记录
            Optional<VideoShareUserLog> shareLogOpt = shareLogRepository.findById(shareLogId);
            if (!shareLogOpt.isPresent()) {
                logger.error("分享记录不存在，shareLogId: {}", shareLogId);
                return;
            }

            VideoShareUserLog shareLog = shareLogOpt.get();

            // 检查状态是否为0
            if (shareLog.getStatus() != 0) {
                logger.info("视频状态不是待上传状态，shareLogId: {}, status: {}", shareLogId, shareLog.getStatus());
                return;
            }

            // 获取视频信息
            Optional<Video> videoOpt = videoRepository.findById(shareLog.getVideoId());
            if (!videoOpt.isPresent()) {
                logger.error("视频不存在，videoId: {}", shareLog.getVideoId());
                updateShareLogStatus(shareLog, -1, "视频不存在");
                return;
            }

            Video video = videoOpt.get();

            // 获取用户授权信息
            Optional<UserAuth> userAuthOpt = userAuthRepository
                    .findByUserIdAndPlatformId(shareLog.getUserId(), shareLog.getPlatformId());
            if (!userAuthOpt.isPresent()) {
                logger.error("用户未授权该平台，userId: {}, platformId: {}",
                        shareLog.getUserId(), shareLog.getPlatformId());
                updateShareLogStatus(shareLog, -1, "用户未授权该平台");
                return;
            }

            UserAuth userAuth = userAuthOpt.get();

            // 根据平台进行上传
            boolean uploadSuccess = false;
            String photoId = null;

            if (shareLog.getPlatformId() == 2) {
                // 快手平台
                uploadSuccess = uploadToKuaishou(video, userAuth, shareLog);
            } else if (shareLog.getPlatformId() == 1) {
                // 抖音平台
                uploadSuccess = uploadToDouyin(video, userAuth, shareLog);
            } else {
                logger.error("不支持的平台，platformId: {}", shareLog.getPlatformId());
                updateShareLogStatus(shareLog, -1, "不支持的平台");
                return;
            }

            // 更新分享记录状态
            if (uploadSuccess) {
                updateShareLogStatus(shareLog, 1, "上传成功");
                // 上传成功时设置data_status为1（正常）
                shareLog.setDataStatus(1);
                shareLogRepository.save(shareLog);
                logger.info("视频上传成功，shareLogId: {}", shareLogId);
            } else {
                updateShareLogStatus(shareLog, -1, "上传失败");
                logger.error("视频上传失败，shareLogId: {}", shareLogId);
            }

        } catch (Exception e) {
            logger.error("上传视频时发生异常，shareLogId: {}, 错误: {}", shareLogId, e.getMessage(), e);

            // 更新状态为失败
            Optional<VideoShareUserLog> shareLogOpt = shareLogRepository.findById(shareLogId);
            if (shareLogOpt.isPresent()) {
                updateShareLogStatus(shareLogOpt.get(), -1, "上传异常: " + e.getMessage());
            }
        }
    }

    /**
     * 上传到快手
     */
    private boolean uploadToKuaishou(Video video, UserAuth userAuth, VideoShareUserLog shareLog) {
        try {
            // 检查access_token是否有效
            if (userAuth.getAccessToken() == null) {
                logger.error("快手access_token为空，userId: {}", userAuth.getUserId());
                return false;
            }

            // 检查access_token是否过期，如果过期则刷新
            String accessToken = userAuth.getAccessToken();
            if (!kuaishouSdkService.isAccessTokenValid(userAuth)) {
                logger.info("快手access_token已过期，尝试刷新，userId: {}", userAuth.getUserId());
                // 这里可以调用刷新token的方法
                // 暂时返回失败，需要实现token刷新逻辑
                return false;
            }

            // 下载视频文件
            MultipartFile videoFile = downloadVideoFile(video.getUrl());
            if (videoFile == null) {
                logger.error("无法下载视频文件，videoUrl: {}", video.getUrl());
                return false;
            }

            // 准备发布参数
            String title = video.getTitle() != null ? video.getTitle() : "精彩视频分享";
            // 调用快手SDK发布视频
            CreateVideoResponse res = kuaishouSdkService.createVideo(
                    accessToken,
                    title,
                    videoFile.getBytes(),
                    downloadVideoFile(video.getCover()).getBytes()
            );

            if (res != null && res.getResult() == 1) {
                // 发布成功，保存photoId
                String photoId = res.getVideoInfo().getPhotoId();
                shareLog.setPhotoId(photoId);
                shareLogRepository.save(shareLog);
                logger.info("快手发布成功，photoId: {}", photoId);
                return true;
            } else {
                logger.error("快手发布失败，response: {}", res);
                return false;
            }

        } catch (Exception e) {
            logger.error("上传到快手失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 上传到抖音
     */
    private boolean uploadToDouyin(Video video, UserAuth userAuth, VideoShareUserLog shareLog) {
        try {
            // 抖音上传逻辑（需要根据实际的抖音SDK实现）
            // 目前使用模拟的photoId，实际应该调用抖音SDK
            String photoId = "douyin_" + System.currentTimeMillis();
            shareLog.setPhotoId(photoId);
            shareLogRepository.save(shareLog);
            logger.info("抖音发布成功（模拟），photoId: {}", photoId);
            return true;
        } catch (Exception e) {
            logger.error("上传到抖音失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 下载视频文件
     */
    private MultipartFile downloadVideoFile(String videoUrl) {
        try {
            return videoDownloadUtil.downloadVideoFile(videoUrl);
        } catch (Exception e) {
            logger.error("下载视频文件失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 更新分享记录状态
     */
    private void updateShareLogStatus(VideoShareUserLog shareLog, Integer status, String message) {
        try {
            shareLog.setStatus(status);
            shareLogRepository.save(shareLog);
            logger.info("更新分享记录状态成功，shareLogId: {}, status: {}, message: {}",
                    shareLog.getId(), status, message);
        } catch (Exception e) {
            logger.error("更新分享记录状态失败，shareLogId: {}, 错误: {}", shareLog.getId(), e.getMessage());
        }
    }
}
