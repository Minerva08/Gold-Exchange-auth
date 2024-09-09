package com.gold.auth.gold_auth.grpc;

import com.gold.auth.gold_auth.global.error.ErrorCode;
import com.gold.auth.gold_auth.global.exception.CustomException;
import com.gold.auth.gold_auth.redis.RedisService;
import com.gold.auth.gold_auth.user.MemberStatus;
import com.gold.auth.gold_auth.user.entity.User;
import com.gold.auth.gold_auth.user.repository.UserRepository;
import com.gold.auth.gold_auth.util.BCryptPasswordEncoderBean;
import com.gold.auth.gold_auth.util.jwt.JwtUtil;
import com.gold.auth.gold_auth.util.jwt.TokenType;
import com.gold.proto.JwtServiceGrpc;
import com.gold.proto.LoginRequestDto;
import com.gold.proto.LoginResponseDto;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

@GrpcService
@Slf4j
public class JwtServiceImpl extends JwtServiceGrpc.JwtServiceImplBase {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RedisService redisService;
    private final BCryptPasswordEncoderBean encoder;


    @Autowired
    public JwtServiceImpl(BCryptPasswordEncoderBean encoder,UserRepository userRepository,
        JwtUtil jwtUtil, RedisService redisService) {
        this.encoder = encoder;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.redisService = redisService;
    }


    // 로그인 메서드 구현
    @Override
    public void loginUser(LoginRequestDto request, StreamObserver<LoginResponseDto> responseObserver) {
        // 로그인 처리 로직 (예: 사용자 인증 및 토큰 발급)
        // 사용자 계정 상태 'USE'로 업데이트
        User userInfo = userRepository.findByUserId(request.getUserId());

        if(userInfo.getStatus()== MemberStatus.INIT){
            userInfo.updateUserStatus();
            userRepository.save(userInfo);

        }

        // refreshToken Redis
        // JWT 생성
        String accessToken = jwtUtil.createJwt(TokenType.AT.getType(), request.getUserId());
        String refreshToken = jwtUtil.createJwt(TokenType.RT.getType(), request.getUserId());

        // Redis에 Refresh Token 저장
        saveRefreshToken(request.getUserId(), refreshToken);

         LoginResponseDto response = LoginResponseDto.newBuilder()
             .setUserId(userInfo.getUserId())
             .setPassword(userInfo.getPassword())
             .setAddress(userInfo.getAddress())
             .setAccessToken(accessToken)
             .setRefreshToken(refreshToken)
             .build();

         responseObserver.onNext(response);
         responseObserver.onCompleted();

    }

    // Refresh Token Redis에 저장
    private void saveRefreshToken(String username, String refreshToken) {
        try {
            redisService.saveRefreshToken(username, refreshToken); // Redis에 저장
        } catch (Exception e) {
            throw new CustomException(ErrorCode.REDIS_SERVER_ERROR);
        }
    }

}