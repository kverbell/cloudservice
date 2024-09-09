//package ru.netology.cloudservice;
//
//import ru.netology.cloudservice.Configuration.TokenProvider;
//
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.web.servlet.MockMvc;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//public class SecurityConfigIntegrationTest {
//
//    private final MockMvc mockMvc;
//    private final TokenProvider tokenProvider;
//
//    @Autowired
//    public SecurityConfigIntegrationTest(MockMvc mockMvc, TokenProvider tokenProvider) {
//        this.mockMvc = mockMvc;
//        this.tokenProvider = tokenProvider;
//    }
//
//    @Test
//    public void whenAccessingCSRFProtectedEndpoint_thenForbiddenWithoutCSRF() throws Exception {
//        mockMvc.perform(post("/secure-endpoint"))
//                .andExpect(status().isForbidden());
//    }
//
//    @Test
//    public void whenAccessingPublicCSRFProtectedEndpointWithCSRF_thenOk() throws Exception {
//        mockMvc.perform(post("/public-endpoint").with(csrf()))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    public void whenAccessingPublicEndpoint_thenOk() throws Exception {
//        mockMvc.perform(get("/public-endpoint"))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    public void whenAuthenticatedUserAccessesSecureEndpoint_thenOk() throws Exception {
//        String token = tokenProvider.createToken("user");
//        mockMvc.perform(get("/secure-endpoint")
//                        .header("auth-token", token))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    public void whenUnauthenticatedUserAccessesSecureEndpoint_thenForbidden() throws Exception {
//        mockMvc.perform(get("/secure-endpoint"))
//                .andExpect(status().isForbidden());
//    }
//
//    @Test
//    public void whenValidTokenUsed_thenAuthenticationIsSet() throws Exception {
//        String token = tokenProvider.createToken("user");
//        mockMvc.perform(get("/secure-endpoint")
//                        .header("auth-token", token))
//                .andExpect(status().isOk())
//                .andExpect(result -> Assertions.assertNotNull(SecurityContextHolder.getContext().getAuthentication()));
//    }
//}

