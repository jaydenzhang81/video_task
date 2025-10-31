-- 插入视频类型数据
INSERT INTO tb_video_type (name, ctime) VALUES 
('带货视频', NOW()),
('种草短视频', NOW());

-- 插入视频平台数据
INSERT INTO tb_video_platform (name, image_url, app_id, app_secret, status) VALUES 
('抖音', '/images/douyin.png', 'douyin_app_id', 'douyin_app_secret', 1),
('快手', '/images/kuaishou.png', 'kuaishou_app_id', 'kuaishou_app_secret', 1),
('小红书', '/images/xiaohongshu.png', 'xiaohongshu_app_id', 'xiaohongshu_app_secret', 1); 