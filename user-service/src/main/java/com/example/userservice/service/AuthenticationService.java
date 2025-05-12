package com.example.userservice.service;

import com.example.userservice.dto.request.*;
import com.example.userservice.dto.response.AuthenticationResponse;
import com.example.userservice.dto.response.ForgotPasswordResponse;
import com.example.userservice.dto.response.IntrospectResponse;
import com.example.userservice.exception.AppException;
import com.example.userservice.exception.ErrorCode;
import com.example.userservice.model.InvalidateToken;
import com.example.userservice.model.Users;
import com.example.userservice.repository.InvalidateTokenRepository;
import com.example.userservice.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@CommonsLog
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository  userRepository;
    InvalidateTokenRepository invalidateTokenRepository;
    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;


    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;
        try {
            verifyToken(token);
        }catch (AppException e)
        {
            isValid = false;
        }
        return  IntrospectResponse.builder().valid(isValid).build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request)
    {
        var users = userRepository.findByUsername(request.getUsername()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITS));
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean authenticate = passwordEncoder.matches(request.getPassword(), users.getPassword());
        boolean active = users.isActive();
        if(!authenticate && !active)
        {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        var token = generateToken(users);
        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        var signToken = verifyToken(request.getToken());

        String jit = signToken.getJWTClaimsSet().getJWTID();
        Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();
        InvalidateToken invalidateToken = InvalidateToken.builder()
                .id(jit)
                .expiryTime(expiryTime)
                .build();
        invalidateTokenRepository.save(invalidateToken);
    }

    public AuthenticationResponse refreshToken(RefreshTokenRequest request) throws ParseException, JOSEException {
        var signerJWT = verifyToken(request.getToken());
        var jit = signerJWT.getJWTClaimsSet().getJWTID();
        var expiryTime = signerJWT.getJWTClaimsSet().getExpirationTime();
        InvalidateToken invalidateToken = InvalidateToken.builder()
                .id(jit)
                .expiryTime(expiryTime)
                .build();
        invalidateTokenRepository.save(invalidateToken);
        var username = signerJWT.getJWTClaimsSet().getSubject();

        var user = userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITS));
        var token = generateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }


    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request)
    {
        Users users = userRepository.findByEmail(request.getEmail()).orElseThrow(()
                -> new AppException(ErrorCode.USER_NOT_EXITS));
        var token = generateResetToken(users);
        return ForgotPasswordResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    String generateToken(Users users) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);

        Date issueTime =new Date();
        Date expirationTime = Date.from(Instant.now().plus(1, ChronoUnit.HOURS));

        JWTClaimsSet jwtClaimNames = new JWTClaimsSet.Builder()
                .subject(users.getUsername())
                .issuer("Thang long")
                .issueTime(issueTime)
                .expirationTime(expirationTime)
                .jwtID(String.valueOf(UUID.randomUUID()))
                .claim("scope", buildScope(users))
                .build();

        Payload payload = new Payload(jwtClaimNames.toJSONObject());
        JWSObject jwsObject = new JWSObject(jwsHeader, payload);

        try{
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes(StandardCharsets.UTF_8)));
            return jwsObject.serialize();
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        var verified = signedJWT.verify(verifier);

        if(!(verified && expiryTime.after(new Date()))) throw new AppException(ErrorCode.UNAUTHENTICATED);
        if(invalidateTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        return signedJWT;
    }

    String generateResetToken(Users users)
    {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);

        Date issueTime = new Date();
        Date expirationTime = Date.from(Instant.now().plus(5, ChronoUnit.MINUTES));

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(users.getEmail())
                .issueTime(issueTime)
                .expirationTime(expirationTime)
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(jwsHeader, payload);
        try{
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes(StandardCharsets.UTF_8)));
            return jwsObject.serialize();
        } catch (KeyLengthException e) {
            throw new RuntimeException(e);
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildScope(Users users)
    {
        StringJoiner stringJoiner = new StringJoiner(" ");
        if(users.getRole() != null)
            stringJoiner.add(users.getRole().getName());
        return stringJoiner.toString();
    }
}
