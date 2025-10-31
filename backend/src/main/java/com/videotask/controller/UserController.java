package com.videotask.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.videotask.dto.ApiResponse;
import com.videotask.dto.UserDto;
import com.videotask.entity.*;
import com.videotask.repository.UserAccountDrawalRepository;
import com.videotask.repository.UserAccountLogRepository;
import com.videotask.repository.UserAccountRepository;
import com.videotask.repository.UserPlatformSaleStateRepository;
import com.videotask.repository.FinanceFlowRepository;
import com.videotask.repository.UserWechatRepository;
import com.videotask.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.annotation.PostConstruct;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {
    
    // 模拟短信验证码存储（生产环境应使用Redis等缓存）
    private static final Map<String, SmsCode> SMS_CODE_CACHE = new ConcurrentHashMap<>();
    private static final String DEFAULT_SMS_CODE = "123456"; // 默认验证码
    private static final long SMS_CODE_EXPIRE_TIME = 5 * 60 * 1000; // 5分钟过期
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserAccountRepository userAccountRepository;
    
    @Autowired
    private UserAccountLogRepository userAccountLogRepository;
    
    @Autowired
    private UserAccountDrawalRepository userAccountDrawalRepository;
    
    @Autowired
    private UserPlatformSaleStateRepository userPlatformSaleStateRepository;
    
    @Autowired
    private FinanceFlowRepository financeFlowRepository;
    
    @Autowired
    private UserWechatRepository userWechatRepository;
    
    // 短信验证码内部类
    private static class SmsCode {
        private String code;
        private long createTime;
        
        public SmsCode(String code, long createTime) {
            this.code = code;
            this.createTime = createTime;
        }
        
        public String getCode() {
            return code;
        }
        
        public long getCreateTime() {
            return createTime;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() - createTime > SMS_CODE_EXPIRE_TIME;
        }
    }
    
    @PostConstruct
    public void init() {
        // 设置验证码验证器
        userService.setSmsCodeVerifier(this::verifySmsCode);
    }
    
    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody UserDto.RegisterRequest request) {
        return userService.register(request);
    }
    
    /**
     * 发送注册验证码
     */
    @PostMapping("/sendCode")
    public ApiResponse<String> sendCode(@RequestBody Map<String, Object> request) {
        try {
            String phone = request.get("phone") != null ? request.get("phone").toString() : null;
            
            if (phone == null || phone.trim().isEmpty()) {
                return ApiResponse.error("手机号不能为空");
            }
            
            // 验证手机号格式
            if (!phone.matches("^1[3-9]\\d{9}$")) {
                return ApiResponse.error("手机号格式不正确");
            }
            
            // 检查手机号是否已注册
            Optional<User> existingUser = userService.getUserByPhone(phone);
            if (existingUser.isPresent()) {
                return ApiResponse.error("该手机号已注册");
            }
            
            // 检查是否频繁发送（1分钟内只能发送一次）
            SmsCode existingCode = SMS_CODE_CACHE.get(phone);
            if (existingCode != null && !existingCode.isExpired() && 
                (System.currentTimeMillis() - existingCode.getCreateTime()) < 60000) {
                return ApiResponse.error("请勿频繁发送验证码，请稍后再试");
            }
            
            // 生成并存储验证码（模拟短信发送）
            String code = DEFAULT_SMS_CODE; // 使用默认验证码
            SmsCode smsCode = new SmsCode(code, System.currentTimeMillis());
            SMS_CODE_CACHE.put(phone, smsCode);
            
            // 清理过期的验证码
            cleanExpiredSmsCode();
            
            System.out.println("模拟发送短信验证码到手机号: " + phone + ", 验证码: " + code);
            
            return ApiResponse.success("验证码发送成功");
        } catch (Exception e) {
            return ApiResponse.error("发送验证码失败");
        }
    }
    
    /**
     * 验证短信验证码
     */
    public boolean verifySmsCode(String phone, String code) {
        SmsCode smsCode = SMS_CODE_CACHE.get(phone);
        if (smsCode == null) {
            return false;
        }
        
        if (smsCode.isExpired()) {
            SMS_CODE_CACHE.remove(phone);
            return false;
        }
        
        return smsCode.getCode().equals(code);
    }
    
    /**
     * 清理过期的验证码
     */
    private void cleanExpiredSmsCode() {
        SMS_CODE_CACHE.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    
    @PostMapping("/login")
    public ApiResponse<UserDto.LoginResponse> login(@Valid @RequestBody UserDto.LoginRequest request) {
        return userService.login(request);
    }
    
    /**
     * 获取用户信息
     */
    @SaCheckLogin
    @GetMapping("/info")
    public ApiResponse<Object> getUserInfo(HttpServletRequest request) {
        try {
            Integer userId = Integer.valueOf(StpUtil.getLoginId().toString());
            User user = userService.getUserById(userId);
            
            if (user == null) {
                return ApiResponse.error("用户不存在");
            }
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", user.getId());
            userInfo.put("nick", user.getNick());
            userInfo.put("avatar", user.getAvatar());
            userInfo.put("phone", user.getPhone());
            userInfo.put("teamName", "四川梦之队"); // 默认团队名称
            userInfo.put("userType", user.getUserType() != null ? user.getUserType() : 0); // 用户类型：0非带货，1带货用户
            userInfo.put("gender", user.getGender() != null ? user.getGender() : 0); // 性别：0保密，1男，2女
            userInfo.put("region", user.getRegion() != null ? user.getRegion() : ""); // 地区
            
            // 返回平台带货发布状态
            Map<Integer, Integer> saleState = new HashMap<>();
            List<UserPlatformSaleState> states = userPlatformSaleStateRepository.findByUserId(userId);
            for (UserPlatformSaleState s : states) {
                saleState.put(s.getPlatform(), s.getStatus());
            }
            userInfo.put("platformSaleState", saleState);
            
            return ApiResponse.success(userInfo);
        } catch (Exception e) {
            return ApiResponse.error("获取用户信息失败");
        }
    }

    /**
     * 获取用户收益信息
     */
    @SaCheckLogin
    @GetMapping("/earnings")
    public ApiResponse<Object> getUserEarnings(HttpServletRequest request) {
        try {
            Integer userId = Integer.valueOf(StpUtil.getLoginId().toString());
            
            // 获取用户账户信息
            Optional<UserAccount> userAccountOpt = userAccountRepository.findByUserId(userId);
            UserAccount userAccount = userAccountOpt.orElse(null);
            
            // 计算今日收益（从今天0点开始）
            long todayStart = System.currentTimeMillis() / (24 * 60 * 60 * 1000) * (24 * 60 * 60 * 1000);
            long todayEnd = todayStart + 24 * 60 * 60 * 1000 - 1;
            
            List<UserAccountLog> todayLogs = userAccountLogRepository.findByUserIdAndCtimeBetweenOrderByCtimeDesc(userId, todayStart, todayEnd);
            BigDecimal todayEarnings = todayLogs.stream()
                    .filter(log -> log.getType() == 1) // 1视频挣的
                    .map(UserAccountLog::getMoney)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // 计算昨日收益用于计算增长率
            long yesterdayStart = todayStart - 24 * 60 * 60 * 1000;
            long yesterdayEnd = todayStart - 1;
            List<UserAccountLog> yesterdayLogs = userAccountLogRepository.findByUserIdAndCtimeBetweenOrderByCtimeDesc(userId, yesterdayStart, yesterdayEnd);
            BigDecimal yesterdayEarnings = yesterdayLogs.stream()
                    .filter(log -> log.getType() == 1)
                    .map(UserAccountLog::getMoney)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // 计算增长率
            double growthRate = 0.0;
            if (yesterdayEarnings.compareTo(BigDecimal.ZERO) > 0) {
                growthRate = todayEarnings.subtract(yesterdayEarnings)
                        .divide(yesterdayEarnings, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                        .doubleValue();
            }
            
            // 计算待提现金额（状态为0的提现申请）
            List<UserAccountDrawal> pendingDrawals = userAccountDrawalRepository.findByUserIdAndStatusOrderByCtimeDesc(userId, 0);
            BigDecimal pendingWithdraw = pendingDrawals.stream()
                    .map(UserAccountDrawal::getMoney)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            Map<String, Object> earnings = new HashMap<>();
            earnings.put("withdrawable", userAccount != null ? userAccount.getMoney() : BigDecimal.ZERO);
            earnings.put("todayEarnings", todayEarnings);
            earnings.put("todayGrowth", Math.round(growthRate * 10) / 10.0); // 保留一位小数
            earnings.put("totalEarnings", userAccount != null ? userAccount.getHistoryReward() : BigDecimal.ZERO);
            earnings.put("pendingWithdraw", pendingWithdraw);
            earnings.put("estimatedDate", "10月15日"); // 预计到账时间
            
            return ApiResponse.success(earnings);
        } catch (Exception e) {
            return ApiResponse.error("获取用户收益信息失败");
        }
    }

    /**
     * 更新用户资料
     */
    @SaCheckLogin
    @PutMapping("/updateProfile")
    public ApiResponse<Object> updateProfile(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        try {
            Integer userId = Integer.valueOf(StpUtil.getLoginId().toString());
            User user = userService.getUserById(userId);
            
            if (user == null) {
                return ApiResponse.error("用户不存在");
            }
            
            // 更新昵称
            if (request.containsKey("nick")) {
                String nick = request.get("nick").toString().trim();
                if (nick.length() > 0 && nick.length() <= 20) {
                    user.setNick(nick);
                } else {
                    return ApiResponse.error("昵称长度应在1-20个字符之间");
                }
            }
            
            // 更新性别
            if (request.containsKey("gender")) {
                Integer gender = Integer.valueOf(request.get("gender").toString());
                if (gender >= 0 && gender <= 2) {
                    user.setGender(gender);
                } else {
                    return ApiResponse.error("性别参数无效");
                }
            }
            
            // 更新地区
            if (request.containsKey("region")) {
                String region = request.get("region").toString().trim();
                user.setRegion(region);
            }
            
            // 更新头像
            if (request.containsKey("avatar")) {
                String avatar = request.get("avatar").toString().trim();
                user.setAvatar(avatar);
            }
            
            // 设置更新时间
            user.setUtime(System.currentTimeMillis());
            
            // 保存用户信息
            userService.updateUser(user);
            
            return ApiResponse.success("更新成功");
        } catch (Exception e) {
            return ApiResponse.error("更新失败");
        }
    }

    /**
     * 检查微信授权状态
     */
    @SaCheckLogin
    @GetMapping("/checkWechatAuth")
    public ApiResponse<Object> checkWechatAuth() {
        try {
            Integer userId = Integer.valueOf(StpUtil.getLoginId().toString());
            
            // 检查用户是否已绑定微信
            Optional<UserWechat> wechatUser = userWechatRepository.findById(userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("isAuthorized", wechatUser.isPresent());
            
            if (wechatUser.isPresent()) {
                UserWechat wechat = wechatUser.get();
                Map<String, String> wechatInfo = new HashMap<>();
                wechatInfo.put("nickName", wechat.getNickName() != null ? wechat.getNickName() : "");
                wechatInfo.put("headImgUrl", wechat.getHeadImgUrl() != null ? wechat.getHeadImgUrl() : "");
                wechatInfo.put("openId", wechat.getOpenId() != null ? wechat.getOpenId() : "");
                result.put("wechatInfo", wechatInfo);
            }
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("检查微信授权状态失败");
        }
    }

    /**
     * 微信授权（在提现第一步内完成绑定）
     */
    @SaCheckLogin
    @PostMapping("/wechat/authorize")
    public ApiResponse<Object> authorizeWechat(@RequestBody Map<String, Object> request) {
        try {
            Integer userId = Integer.valueOf(StpUtil.getLoginId().toString());
            String code = request.get("code") != null ? request.get("code").toString() : null;
            
            // 检查是否已存在微信绑定
            Optional<UserWechat> wechatUser = userWechatRepository.findById(userId);
            if (!wechatUser.isPresent()) {
                // 创建新的微信绑定记录
                UserWechat newWechat = new UserWechat();
                newWechat.setId(userId);
                newWechat.setCtime(System.currentTimeMillis());
                newWechat.setUtime(System.currentTimeMillis());
                
                // 如果有code，可以在这里调用微信API获取更多用户信息
                // 目前先简单绑定，后续可扩展获取openId、unionId等
                if (code != null && !code.isEmpty()) {
                    // TODO: 可以通过code调用微信API获取用户信息
                    // 暂时只记录授权时间
                    newWechat.setUtime(System.currentTimeMillis());
                }
                
                userWechatRepository.save(newWechat);
            }
            return ApiResponse.success("授权成功");
        } catch (Exception e) {
            return ApiResponse.error("授权失败");
        }
    }

    /**
     * 微信登录接口
     */
    @PostMapping("/wechat/login")
    public ApiResponse<Map<String, Object>> wechatLogin(@RequestBody Map<String, Object> request) {
        try {
            String code = (String) request.get("code");
            String unionid = (String) request.get("unionid");
            String openId = (String) request.get("openId");
            String nickName = (String) request.get("nickName");
            String headImgUrl = (String) request.get("headImgUrl");
            Integer sex = request.get("sex") != null ? Integer.valueOf(request.get("sex").toString()) : 0;
            String country = (String) request.get("country");
            String province = (String) request.get("province");
            String city = (String) request.get("city");
            
            if (code == null || code.trim().isEmpty()) {
                return ApiResponse.error("微信授权码不能为空");
            }
            
            // 根据openId或unionid查找是否已存在微信用户
            // 由于当前表结构限制，这里简化处理
            // 实际项目中应该根据openId或unionid进行查询
            
            Map<String, Object> result = new HashMap<>();
            
            // 临时逻辑：假设都是新用户，需要绑定手机号
            // 实际应该查询数据库判断是否已存在该微信用户
            result.put("isNewUser", true);
            result.put("wechatInfo", request);
            result.put("message", "检测到您是新用户，请绑定手机号完成注册");
            
            return ApiResponse.success(result);
            
        } catch (Exception e) {
            return ApiResponse.error("微信登录失败: " + e.getMessage());
        }
    }

    /**
     * 获取财务流水
     */
    @SaCheckLogin
    @GetMapping("/financeFlow")
    public ApiResponse<Object> getFinanceFlow(@RequestParam(required = false) String type,
                                              @RequestParam(required = false) String startDate,
                                              @RequestParam(required = false) String endDate,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        try {
            Integer userId = Integer.valueOf(StpUtil.getLoginId().toString());
            
            // 构建分页参数
            Pageable pageable = PageRequest.of(page, size);
            
            // 转换时间参数
            Long startTime = null;
            Long endTime = null;
            if (startDate != null && !startDate.isEmpty()) {
                startTime = java.time.LocalDate.parse(startDate).atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
            }
            if (endDate != null && !endDate.isEmpty()) {
                endTime = java.time.LocalDate.parse(endDate).atTime(23, 59, 59).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
            }
            
            Page<FinanceFlow> flowPage;
            BigDecimal totalIncome;
            BigDecimal totalExpense;
            
            // 根据条件查询
            if (type != null && !type.isEmpty() && startTime != null && endTime != null) {
                // 类型 + 时间范围
                flowPage = financeFlowRepository.findByUserIdAndTypeAndTimeRange(userId, type, startTime, endTime, pageable);
                totalIncome = financeFlowRepository.getTotalIncomeByUserIdAndTimeRange(userId, startTime, endTime);
                totalExpense = financeFlowRepository.getTotalExpenseByUserIdAndTimeRange(userId, startTime, endTime);
            } else if (type != null && !type.isEmpty()) {
                // 仅类型
                flowPage = financeFlowRepository.findByUserIdAndTypeOrderByCreateTimeDesc(userId, type, pageable);
                totalIncome = financeFlowRepository.getTotalIncomeByUserId(userId);
                totalExpense = financeFlowRepository.getTotalExpenseByUserId(userId);
            } else if (startTime != null && endTime != null) {
                // 仅时间范围
                flowPage = financeFlowRepository.findByUserIdAndTimeRange(userId, startTime, endTime, pageable);
                totalIncome = financeFlowRepository.getTotalIncomeByUserIdAndTimeRange(userId, startTime, endTime);
                totalExpense = financeFlowRepository.getTotalExpenseByUserIdAndTimeRange(userId, startTime, endTime);
            } else {
                // 全部
                flowPage = financeFlowRepository.findByUserIdOrderByCreateTimeDesc(userId, pageable);
                totalIncome = financeFlowRepository.getTotalIncomeByUserId(userId);
                totalExpense = financeFlowRepository.getTotalExpenseByUserId(userId);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("list", flowPage.getContent());
            result.put("totalElements", flowPage.getTotalElements());
            result.put("totalPages", flowPage.getTotalPages());
            result.put("currentPage", flowPage.getNumber());
            result.put("size", flowPage.getSize());
            result.put("totalIncome", totalIncome);
            result.put("totalExpense", totalExpense);
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("获取财务流水失败");
        }
    }

    /**
     * 用户提现
     */
    @SaCheckLogin
    @PostMapping("/withdraw")
    public ApiResponse<Object> withdraw(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        try {
            Integer userId = Integer.valueOf(StpUtil.getLoginId().toString());
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String accountType = request.get("accountType") != null ? request.get("accountType").toString() : "wechat";
            
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return ApiResponse.error("提现金额必须大于0");
            }
            
            // 如果选择微信提现，检查微信授权状态；未授权则自动绑定当前用户
            if ("wechat".equals(accountType)) {
                Optional<UserWechat> wechatUser = userWechatRepository.findById(userId);
                if (!wechatUser.isPresent()) {
                    UserWechat newWechat = new UserWechat();
                    newWechat.setId(userId);
                    newWechat.setCtime(System.currentTimeMillis());
                    newWechat.setUtime(System.currentTimeMillis());
                    userWechatRepository.save(newWechat);
                }
            }
            
            // 检查用户余额
            Optional<UserAccount> userAccountOpt = userAccountRepository.findByUserId(userId);
            if (!userAccountOpt.isPresent()) {
                return ApiResponse.error("用户账户不存在");
            }
            
            UserAccount userAccount = userAccountOpt.get();
            if (userAccount.getMoney().compareTo(amount) < 0) {
                return ApiResponse.error("余额不足");
            }
            
            // 创建提现申请记录
            UserAccountDrawal drawal = new UserAccountDrawal();
            drawal.setUserId(userId);
            drawal.setMoney(amount);
            drawal.setStatus(0); // 0申请提现
            drawal.setAccountType(accountType); // 账户类型
            drawal.setCtime(System.currentTimeMillis());
            
            userAccountDrawalRepository.save(drawal);
            
            // 创建账户流水记录
            UserAccountLog log = new UserAccountLog();
            log.setUserId(userId);
            log.setTitle("提现申请");
            log.setContent("提现申请金额：" + amount);
            log.setMoney(amount);
            log.setType(2); // 2提现了
            log.setSource("提现申请ID：" + drawal.getId());
            log.setCtime(System.currentTimeMillis());
            
            userAccountLogRepository.save(log);
            
            return ApiResponse.success("提现申请已提交");
        } catch (Exception e) {
            return ApiResponse.error("提现失败");
        }
    }
} 