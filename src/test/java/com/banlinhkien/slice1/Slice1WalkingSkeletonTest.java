package com.banlinhkien.slice1;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
public class Slice1WalkingSkeletonTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Slice 1: Homepage (/) should load successfully with HTTP 200 and categories/products")
    void shouldLoadHomepage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attributeExists("featuredProducts"))
                .andExpect(model().attributeExists("latestProducts"));
    }

    @Test
    @DisplayName("Slice 1: Products catalog (/san-pham) should load successfully with pagination")
    void shouldLoadProductsCatalog() throws Exception {
        mockMvc.perform(get("/san-pham"))
                .andExpect(status().isOk())
                .andExpect(view().name("products/index"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attributeExists("totalPages"));
    }

    @Test
    @DisplayName("Slice 1: Login page (/login) should load with HTTP 200")
    void shouldLoadLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    @DisplayName("Slice 1: Quick View REST API (/api/san-pham/1/quick-view) should return JSON")
    void shouldReturnProductQuickViewJson() throws Exception {
        mockMvc.perform(get("/api/san-pham/1/quick-view"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").exists());
    }

    @Test
    @DisplayName("Slice 1: Form Login should authenticate real database user nguyenvana / password")
    void shouldAuthenticateRealUser() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "nguyenvana")
                        .param("password", "password")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(authenticated().withUsername("nguyenvana"));
    }

    @Test
    @DisplayName("Slice 1: Form Login should reject invalid credentials")
    void shouldRejectInvalidLogin() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "nguyenvana")
                        .param("password", "completely_wrong_password_999")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"))
                .andExpect(unauthenticated());
    }
}
