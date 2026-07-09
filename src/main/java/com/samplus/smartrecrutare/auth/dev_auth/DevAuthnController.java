package com.samplus.smartrecrutare.auth.dev_auth;

import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@RestController
// @Profile("dev")
public class DevAuthnController {

    private final JwtEncoder jwtEncoder;

    public DevAuthnController(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    @GetMapping(value = "/dev-auth/token", produces = MediaType.TEXT_HTML_VALUE)
    public String tokenPage() {
        String token = createDevToken();
        String authHeader = "Bearer " + token;

        return """
                <!doctype html>
                <html>
                <head>
                    <title>Dev JWT Token</title>
                    <style>
                        body {
                            background: #111;
                            color: #0f0;
                            font-family: monospace;
                            padding: 40px;
                        }
                        textarea {
                            width: 100%;
                            height: 180px;
                            background: #000;
                            color: #0f0;
                            border: 1px solid #0f0;
                            padding: 12px;
                            font-family: monospace;
                        }
                        button {
                            margin-top: 12px;
                            padding: 10px 16px;
                            background: #111;
                            color: #0f0;
                            border: 1px solid #0f0;
                            cursor: pointer;
                            font-family: monospace;
                        }
                        a {
                            color: #0f0;
                            margin-left: 12px;
                        }
                    </style>
                </head>
                <body>
                    <h2>DEV JWT GENERATED</h2>
                    <p>Paste this into Swagger Authorize:</p>

                    <textarea id="token" readonly>%s</textarea>

                    <br />

                    <button onclick="copyToken()">Copy</button>
                    <a href="/swagger-ui/index.html">Open Swagger</a>

                    <script>
                        function copyToken() {
                            const el = document.getElementById("token");
                            el.select();
                            document.execCommand("copy");
                        }
                    </script>
                </body>
                </html>
                """.formatted(authHeader);
    }

    private String createDevToken() {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("local-dev")
                .subject("swagger-dev-user")
                .issuedAt(now)
                .expiresAt(now.plus(Duration.ofHours(4)))
                .claim("roles", List.of("admin", "recruiter"))
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder
                .encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();
    }
}