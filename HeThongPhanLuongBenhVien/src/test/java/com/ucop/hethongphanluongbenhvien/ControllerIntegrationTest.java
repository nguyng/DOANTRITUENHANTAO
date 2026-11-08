package com.ucop.hethongphanluongbenhvien;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class ControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(username = "ADMIN_001", roles = {"ADMIN"})
    @DisplayName("Kiểm tra Admin Dashboard /admin/dashboard")
    void testAdminDashboard() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "ADMIN_001", roles = {"ADMIN"})
    @DisplayName("Kiểm tra Admin Patients /admin/patients")
    void testAdminPatients() throws Exception {
        mockMvc.perform(get("/admin/patients"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "ADMIN_001", roles = {"ADMIN"})
    @DisplayName("Kiểm tra Admin Logs /admin/logs")
    void testAdminLogs() throws Exception {
        mockMvc.perform(get("/admin/logs"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "ADMIN_001", roles = {"ADMIN"})
    @DisplayName("Kiểm tra Admin Staff /admin/staff")
    void testAdminStaff() throws Exception {
        mockMvc.perform(get("/admin/staff"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "ADMIN_001", roles = {"ADMIN"})
    @DisplayName("Kiểm tra Admin Departments /admin/departments")
    void testAdminDepartments() throws Exception {
        mockMvc.perform(get("/admin/departments"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "YTA_NGA", roles = {"NURSE"})
    @DisplayName("Kiểm tra Nurse Queue /nurse/queue")
    void testNurseQueue() throws Exception {
        mockMvc.perform(get("/nurse/queue"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "079204000001", roles = {"PATIENT"})
    @DisplayName("Kiểm tra Patient Chat /chat với CCCD")
    void testPatientChatWithCccd() throws Exception {
        mockMvc.perform(get("/chat"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "HN4790000000001", roles = {"PATIENT"})
    @DisplayName("Kiểm tra Patient Chat /chat với BHYT")
    void testPatientChatWithBhyt() throws Exception {
        mockMvc.perform(get("/chat"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "079204000010", roles = {"PATIENT"})
    @DisplayName("Kiểm tra Patient Chat /chat với Khám dịch vụ không có BHYT")
    void testPatientChatServiceUser() throws Exception {
        mockMvc.perform(get("/chat"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "079204000001", roles = {"PATIENT"})
    @DisplayName("Kiểm tra trang Profile /profile")
    void testPatientProfile() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "079204000001", roles = {"PATIENT"})
    @DisplayName("Kiểm tra trang Visits /visits")
    void testPatientVisits() throws Exception {
        mockMvc.perform(get("/visits"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "079204000001", roles = {"PATIENT"})
    @DisplayName("Kiểm tra trang Ticket /ticket")
    void testPatientTicket() throws Exception {
        mockMvc.perform(get("/ticket"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Kiểm tra trang công khai /display")
    void testPublicDisplay() throws Exception {
        mockMvc.perform(get("/display"))
                .andExpect(status().isOk());
    }
}
