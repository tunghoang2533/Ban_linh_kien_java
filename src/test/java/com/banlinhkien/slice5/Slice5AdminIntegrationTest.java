package com.banlinhkien.slice5;

import com.banlinhkien.config.WebMvcConfig;
import com.banlinhkien.entity.Brand;
import com.banlinhkien.entity.Category;
import com.banlinhkien.entity.Product;
import com.banlinhkien.entity.ProductSpec;
import com.banlinhkien.repository.BrandRepository;
import com.banlinhkien.repository.CategoryRepository;
import com.banlinhkien.repository.ProductRepository;
import com.banlinhkien.repository.ProductSpecRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
public class Slice5AdminIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductSpecRepository productSpecRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Test
    @DisplayName("Khách chưa đăng nhập truy cập /admin phải bị chuyển hướng sang /login (302)")
    void testUnauthenticatedCannotAccessAdminDashboard() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("Khách hàng thông thường (ROLE_USER) truy cập /admin bị từ chối 403 Forbidden")
    @WithMockUser(username = "customer1", roles = {"USER"})
    void testCustomerForbiddenFromAdminDashboard() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Quản trị viên (ROLE_ADMIN) truy cập /admin thành công 200 OK với đầy đủ KPI và biểu đồ")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAdminCanAccessDashboard() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attributeExists("kpis"))
                .andExpect(model().attributeExists("revenueTrend"))
                .andExpect(model().attributeExists("recentOrders"))
                .andExpect(model().attributeExists("lowStockProducts"))
                .andExpect(content().string(containsString("Xu Hướng Doanh Thu")))
                .andExpect(content().string(containsString("revenueTrendChart")));
    }

    @Test
    @DisplayName("Quản trị viên truy cập danh sách sản phẩm /admin/products thành công 200 OK")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAdminCanAccessProductsList() throws Exception {
        mockMvc.perform(get("/admin/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/products/index"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attributeExists("brands"))
                .andExpect(content().string(containsString("Danh Sách Sản Phẩm")));
    }

    @Test
    @DisplayName("Quản trị viên truy cập danh sách đơn hàng /admin/orders thành công 200 OK")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAdminCanAccessOrdersList() throws Exception {
        mockMvc.perform(get("/admin/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/orders/index"))
                .andExpect(model().attributeExists("orders"))
                .andExpect(model().attributeExists("countAll"))
                .andExpect(content().string(containsString("Quản Lý Đơn Hàng")));
    }

    @Test
    @DisplayName("Quản trị viên truy cập quản lý danh mục /admin/categories thành công 200 OK")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAdminCanAccessCategoriesList() throws Exception {
        mockMvc.perform(get("/admin/categories"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/categories/index"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(content().string(containsString("Quản Lý Danh Mục")));
    }

    @Test
    @DisplayName("Quản trị viên truy cập quản lý thương hiệu /admin/brands thành công 200 OK")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAdminCanAccessBrandsList() throws Exception {
        mockMvc.perform(get("/admin/brands"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/brands/index"))
                .andExpect(model().attributeExists("brands"))
                .andExpect(content().string(containsString("Quản Lý Thương Hiệu")));
    }

    @Test
    @DisplayName("Admin tạo sản phẩm mới kèm upload ảnh bền vững và lưu thông số kỹ thuật (specs)")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAdminCreateProductWithUploadAndSpecs() throws Exception {
        Category cat = categoryRepository.findAll().stream().findFirst().orElse(null);
        Brand brand = brandRepository.findAll().stream().findFirst().orElse(null);

        MockMultipartFile imageFile = new MockMultipartFile(
                "imageFile",
                "rtx_4090_gaming.jpg",
                "image/jpeg",
                "BINARY_IMAGE_DATA_TEST".getBytes()
        );

        String testProductName = "VGA Test RTX 4090 Slice 5 " + System.currentTimeMillis();

        mockMvc.perform(multipart("/admin/products/save")
                        .file(imageFile)
                        .param("name", testProductName)
                        .param("price", "45000000")
                        .param("costPrice", "38000000")
                        .param("discountPercent", "5")
                        .param("quantity", "12")
                        .param("isActive", "true")
                        .param("isFeatured", "true")
                        .param("description", "Card đồ họa đỉnh cao kiểm thử Slice 5")
                        .param("categoryId", cat != null ? cat.getId().toString() : "1")
                        .param("brandId", brand != null ? brand.getId().toString() : "1")
                        .param("specNames", "Chipset", "VRAM")
                        .param("specValues", "RTX 4090", "24GB GDDR6X")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/products"))
                .andExpect(flash().attributeExists("successMessage"));

        // Verify product was persisted
        List<Product> savedList = productRepository.findAll().stream()
                .filter(p -> testProductName.equals(p.getName()))
                .toList();

        assertFalse(savedList.isEmpty(), "Sản phẩm phải được tạo thành công trong DB");
        Product createdProduct = savedList.get(0);

        assertEquals(new BigDecimal("45000000.00"), createdProduct.getPrice());
        assertEquals(12, createdProduct.getQuantity());
        assertTrue(createdProduct.getIsActive());
        assertNotNull(createdProduct.getImage());
        assertTrue(createdProduct.getImage().contains("rtx_4090_gaming.jpg"));

        // Verify image physically exists in uploads/products
        Path uploadedImagePath = Paths.get(WebMvcConfig.UPLOAD_DIR).resolve(createdProduct.getImage());
        assertTrue(Files.exists(uploadedImagePath), "File ảnh phải được ghi vật lý vào uploads/products/");

        // Clean up test image
        Files.deleteIfExists(uploadedImagePath);

        // Verify specs were persisted
        List<ProductSpec> specs = productSpecRepository.findByProductId(createdProduct.getId());
        assertEquals(2, specs.size());
        assertTrue(specs.stream().anyMatch(s -> "Chipset".equals(s.getSpecName()) && "RTX 4090".equals(s.getSpecValue())));
        assertTrue(specs.stream().anyMatch(s -> "VRAM".equals(s.getSpecName()) && "24GB GDDR6X".equals(s.getSpecValue())));

        // Test Soft-Deactivate
        mockMvc.perform(post("/admin/products/" + createdProduct.getId() + "/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/products"));

        Product softDeleted = productRepository.findById(createdProduct.getId()).orElseThrow();
        assertFalse(softDeleted.getIsActive(), "Sản phẩm phải chuyển sang trạng thái ngưng kinh doanh");

        // Specifications must be PRESERVED
        List<ProductSpec> specsAfterDeactivate = productSpecRepository.findByProductId(createdProduct.getId());
        assertEquals(2, specsAfterDeactivate.size(), "Thông số kỹ thuật phải được giữ nguyên khi ngưng kinh doanh");

        // Clean up DB
        productSpecRepository.deleteAll(specsAfterDeactivate);
        productRepository.delete(softDeleted);
    }
}
