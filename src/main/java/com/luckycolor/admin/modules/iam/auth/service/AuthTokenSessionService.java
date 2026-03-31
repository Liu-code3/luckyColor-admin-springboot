package com.luckycolor.admin.modules.iam.auth.service;

import java.time.Instant;

public interface AuthTokenSessionService {

    void revoke(String token, Instant expiresAt);

    boolean isRevoked(String token);
}
