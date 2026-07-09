package com.samplus.smartrecrutare.auth.dev_auth;

import org.springframework.beans.factory.annotation.Qualifier;
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
    private final JwtEncoder devEncoder;

    public DevAuthnController( @Qualifier("devEncoder")JwtEncoder devEncoder) {
        this.devEncoder = devEncoder;
    }

    @GetMapping(value = "/dev-auth/token", produces = MediaType.TEXT_HTML_VALUE)
    public String tokenPage() {
        String token = createDevToken();
        String authHeader = "Bearer " + token;

        return
                "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<head>\n" +
                        "    <title>Dev JWT Token</title>\n" +
                        "    <style>\n" +
                        "        body {\n" +
                        "            background: #111;\n" +
                        "            color: #0f0;\n" +
                        "            font-family: monospace;\n" +
                        "            padding: 40px;\n" +
                        "        }\n" +
                        "        textarea {\n" +
                        "            width: 100%;\n" +
                        "            height: 180px;\n" +
                        "            background: #000;\n" +
                        "            color: #0f0;\n" +
                        "            border: 1px solid #0f0;\n" +
                        "            padding: 12px;\n" +
                        "            font-family: monospace;\n" +
                        "        }\n" +
                        "        button {\n" +
                        "            margin-top: 12px;\n" +
                        "            padding: 10px 16px;\n" +
                        "            background: #111;\n" +
                        "            color: #0f0;\n" +
                        "            border: 1px solid #0f0;\n" +
                        "            cursor: pointer;\n" +
                        "            font-family: monospace;\n" +
                        "        }\n" +
                        "        a {\n" +
                        "            color: #0f0;\n" +
                        "            margin-left: 12px;\n" +
                        "        }\n" +
                        "    </style>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "    <h2>DEV JWT GENERATED</h2>\n" +
                        "    <p>Paste this into Swagger Authorize:</p>\n" +
                        "\n" +
                        "    <textarea id=\"token\" readonly>" + /*<!-- Se adauga tokenul aici -->*/token + "</textarea>\n" +
                        "\n" +
                        "    <br />\n" +
                        "\n" +
                        "    <button onclick=\"copyToken()\">Copy</button>\n" +
                        "    <a href=\"/swagger-ui/index.html\">Open Swagger</a>\n" +
                        "\n" +
                        "    <script>\n" +
                        "        function copyToken() {\n" +
                        "            const el = document.getElementById(\"token\");\n" +
                        "            el.select();\n" +
                        "            document.execCommand(\"copy\");\n" +
                        "        }\n" +
                        "    </script>\n" +
                        "</body>\n" +
                        "</html>";
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

        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256)
                .keyId("dev-key")
                .build();

        return devEncoder
                .encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();
    }
}
