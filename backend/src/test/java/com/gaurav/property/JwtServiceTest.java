package com.gaurav.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.gaurav.property.entity.UserAccount;
import com.gaurav.property.enums.UserRole;
import com.gaurav.property.security.JwtService;

class JwtServiceTest {

    private static final String SECRET = "ci-test-secret-must-be-at-least-32-characters-long";

    @Test
    void constructorRejectsShortSigningSecret() {
        assertThrows(IllegalArgumentException.class,
                () -> new JwtService("too-short", "property-registration-system"));
    }

    @Test
    void constructorRejectsBlankIssuer() {
        assertThrows(IllegalArgumentException.class,
                () -> new JwtService(SECRET, "   "));
    }

    @Test
    void generatedTokenContainsExpectedIdentityAndRoleClaims() {
        JwtService service = new JwtService(SECRET, "property-registration-system");
        UserAccount user = UserAccount.builder()
                .username("applicant-one")
                .role(UserRole.APPLICANT)
                .build();
        user.setId(42L);

        var claims = service.parseToken(service.generateToken(user));

        assertEquals("property-registration-system", claims.getIssuer());
        assertEquals("applicant-one", claims.getSubject());
        assertEquals(42, ((Number) claims.get("userId")).longValue());
        assertEquals("APPLICANT", claims.get("role", String.class));
        assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
    }

    @Test
    void tokenCannotBeParsedWithAnotherSigningSecretOrIssuer() {
        UserAccount user = UserAccount.builder()
                .username("applicant-one")
                .role(UserRole.APPLICANT)
                .build();
        user.setId(42L);
        String token = new JwtService(SECRET, "property-registration-system").generateToken(user);

        assertThrows(Exception.class,
                () -> new JwtService("different-test-secret-must-also-be-at-least-32-chars", 
                        "property-registration-system").parseToken(token));
        assertThrows(Exception.class,
                () -> new JwtService(SECRET, "another-issuer").parseToken(token));
    }
}
