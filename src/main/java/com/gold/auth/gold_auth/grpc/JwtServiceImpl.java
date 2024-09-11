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
import com.gold.proto.JwtTokenReIssueResponseDto;
import com.gold.proto.LoginRequestDto;
import com.gold.proto.LoginResponseDto;
import com.gold.proto.RefreshTokenRequestDto;
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
        String accessToken = jwtUtil.createJwt(TokenType.AT.getType(), request.getUserId(),userInfo.getAddress());
        String refreshToken = jwtUtil.createJwt(TokenType.RT.getType(), request.getUserId(),null);

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

    @Override
    public void reIssueToken(RefreshTokenRequestDto request,
        StreamObserver<JwtTokenReIssueResponseDto> responseObserver) {

        String userId = request.getUserId();
        String refreshToken = redisService.getRefreshToken(userId);
        User byUserId = userRepository.findByUserId(userId);

        if(byUserId==null){
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        if(refreshToken==null){
            throw new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        if(request.getRefreshToken().equals(refreshToken)){

            // refreshToken Redis
            // JWT 생성
            String reRefreshToken = jwtUtil.createJwt(TokenType.RT.getType(), request.getUserId(),null);
            redisService.saveRefreshToken(userId,reRefreshToken);
        }
        String reAccessToken = jwtUtil.createJwt(TokenType.AT.getType(), request.getUserId(),byUserId.getAddress());

        JwtTokenReIssueResponseDto reIssueResDto = JwtTokenReIssueResponseDto.newBuilder()
            .setUserId(userId)
            .setAccessToken(reAccessToken)
            .setRefreshToken(refreshToken)
            .build();

        responseObserver.onNext(reIssueResDto);
        responseObserver.onCompleted();
    }
}