package com.gold.auth.gold_auth.grpc;

import com.gold.auth.gold_auth.global.error.ErrorCode;
import com.gold.auth.gold_auth.global.exception.CustomException;
import com.gold.auth.gold_auth.user.MemberStatus;
import com.gold.auth.gold_auth.user.entity.User;
import com.gold.auth.gold_auth.user.repository.UserRepository;
import com.gold.proto.AuthServiceGrpc;
import com.gold.proto.JoinRequestDto;
import com.gold.proto.JoinResponseDto;
import com.gold.proto.LoginRequestDto;
import com.gold.proto.LoginResponseDto;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import jakarta.transaction.Transactional;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;

@GrpcService
@AllArgsConstructor
@Slf4j
public class AuthServiceImpl extends AuthServiceGrpc.AuthServiceImplBase {

    private final UserRepository userRepository;


    // 회원가입 메서드 구현
    @Override
    public void registerUser(JoinRequestDto request, StreamObserver<JoinResponseDto> responseObserver) {

        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date birthdate = format.parse(request.getBirth());

            User joinUser =  User.builder()
                .userId(request.getUserId())
                .address(request.getAddress())
                .birth(birthdate)
                .status(MemberStatus.INIT)
                .build();

            try{
                userRepository.save(joinUser);

            }catch (DataIntegrityViolationException e){
                throw new CustomException(ErrorCode.USER_ALREADY_EXIST);
            }
            responseObserver.onNext(JoinResponseDto.newBuilder()
                .setUserId(request.getUserId())
                .build());
            responseObserver.onCompleted();

        }catch (StatusRuntimeException e) {
            // gRPC 에러 상태 전달
            responseObserver.onError(e);
        }catch (CustomException e){
            ErrorCode errorCode = e.getErrorCode();
            System.out.println("errorCode:"+errorCode.getMessage());
            responseObserver.onError(
                Status.ALREADY_EXISTS.withDescription(e.getMessage()).withCause(e).asRuntimeException());
        }catch (Exception e) {
            // 일반적인 예외 처리
            responseObserver.onError(
                Status.INVALID_ARGUMENT.withDescription("생년월일 날짜 포맷 잘못된 요청").withCause(e).asRuntimeException());
        }


    }

    // 로그인 메서드 구현
    @Override
    public void loginUser(LoginRequestDto request, StreamObserver<LoginResponseDto> responseObserver) {
        // 로그인 처리 로직 (예: 사용자 인증 및 토큰 발급)

       User userInfo = userRepository.findByUserId(request.getUserId());
       userInfo.updateUserStatus();

       userRepository.save(userInfo);


        String accesstoken = "exampleAToken"; // 인증 성공 시 발급할 토큰 예시
        String refreshtoken = "exampleRToken"; // 인증 성공 시 발급할 토큰 예시

        LoginResponseDto response = LoginResponseDto.newBuilder()
            .setUserId(userInfo.getUserId())
            .setAccessToken(accesstoken)
            .setRefreshToken(refreshtoken)
            .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();

    }

}