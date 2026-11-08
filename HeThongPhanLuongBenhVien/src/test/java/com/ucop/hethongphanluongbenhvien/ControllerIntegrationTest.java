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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
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
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string(org.hamcrest.Matchers.containsString("Gateway")));
    }

    @Test
    @WithMockUser(username = "ADMIN_001", roles = {"ADMIN"})
    @DisplayName("Cập nhật cấu hình Hybrid AI Gateway /admin/ai-config")
    void testAdminAiConfig() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/admin/ai-config")
                        .param("provider", "hybrid")
                        .param("geminiModel", "gemini-1.5-flash"))
                .andExpect(status().is3xxRedirection());
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
    @WithMockUser(username = "ADMIN_001", roles = {"ADMIN"})
    @DisplayName("Kiểm tra Admin Reports /admin/reports")
    void testAdminReports() throws Exception {
        mockMvc.perform(get("/admin/reports"))
                .andExpect(status().isOk());
    }



    @Test
    @WithMockUser(username = "YTA_NGA", roles = {"NURSE"})
    @DisplayName("Kiểm tra Nurse Queue /nurse/queue tự động redirect về khoa phụ trách")
    void testNurseQueue() throws Exception {
        mockMvc.perform(get("/nurse/queue"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/nurse/queue/d1111111-1111-1111-1111-111111111111"));
    }

    @Test
    @WithMockUser(username = "ADMIN_001", roles = {"ADMIN"})
    @DisplayName("Kiểm tra Admin vào Nurse Queue /nurse/queue xem toàn viện 200 OK")
    void testAdminNurseQueue() throws Exception {
        mockMvc.perform(get("/nurse/queue"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "YTA_NGA", roles = {"NURSE"})
    @DisplayName("Cô lập dữ liệu: Y tá Cấp cứu truy cập Khoa Tim Mạch bị chặn và redirect về khoa mình")
    void testDepartmentIsolationBlocked() throws Exception {
        mockMvc.perform(get("/nurse/queue/d3333333-3333-3333-3333-333333333333"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/nurse/queue/d1111111-1111-1111-1111-111111111111"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @WithMockUser(username = "BS_TIMMACH", roles = {"DOCTOR"})
    @DisplayName("Bác sĩ Tim Mạch vào khoa Tim Mạch của mình 200 OK")
    void testDoctorInOwnDepartment() throws Exception {
        mockMvc.perform(get("/nurse/queue/d3333333-3333-3333-3333-333333333333"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("department"))
                .andExpect(model().attributeExists("todayShifts"));
    }

    @Test
    @WithMockUser(username = "BS_TIMMACH", roles = {"DOCTOR"})
    @DisplayName("Kiểm tra trang Lịch trực & Ca làm /nurse/schedule")
    void testNurseSchedule() throws Exception {
        mockMvc.perform(get("/nurse/schedule"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("weeklySchedule"))
                .andExpect(model().attributeExists("mySchedules"));
    }

    @Test
    @WithMockUser(username = "YTA_NGA", roles = {"NURSE"})
    @DisplayName("Kiểm tra tra cứu bệnh nhân /nurse/search có kết quả")
    void testNurseSearch() throws Exception {
        mockMvc.perform(get("/nurse/search").param("q", "079204000001"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("patient"));
    }



    @Test

    @WithMockUser(username = "YTA_NGA", roles = {"NURSE"})

    @DisplayName("Kiểm tra Nurse Queue Detail /nurse/queue/{deptId} có form chuyển khoa đủ deptId")

    void testNurseQueueDetail() throws Exception {

        mockMvc.perform(get("/nurse/queue/d1111111-1111-1111-1111-111111111111"))

                .andExpect(status().isOk())

                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()

                        .string(org.hamcrest.Matchers.containsString("name=\"newDepartmentId\"")));

    }



    @Test

    @org.springframework.transaction.annotation.Transactional

    @WithMockUser(username = "YTA_NGA", roles = {"NURSE"})

    @DisplayName("Chuyển khoa POST /nurse/queue/transfer -> redirect (không còn 400)")

    void testNurseTransfer() throws Exception {

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/nurse/queue/transfer")

                        .param("ticketId", "90000000-0000-0000-0000-000000000003")

                        .param("newDepartmentId", "dbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")

                        .param("reason", "Chấn thương phần mềm, chuyển Xương Khớp")

                        .param("deptId", "d1111111-1111-1111-1111-111111111111"))

                .andExpect(status().is3xxRedirection());

    }



    @Test

    @org.springframework.transaction.annotation.Transactional

    @WithMockUser(username = "YTA_NGA", roles = {"NURSE"})

    @DisplayName("Chuyển khoa khi thiếu deptId vẫn redirect (không 400)")

    void testNurseTransferWithoutDeptId() throws Exception {

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/nurse/queue/transfer")

                        .param("ticketId", "90000000-0000-0000-0000-000000000002")

                        .param("newDepartmentId", "d3333333-3333-3333-3333-333333333333"))

                .andExpect(status().is3xxRedirection());

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

    @WithMockUser(username = "YTA_NGA", roles = {"NURSE"})

    @DisplayName("GET /nurse/queue/transfer -> redirect /nurse/queue (an toàn tránh lỗi 405)")

    void testNurseTransferGet() throws Exception {

        mockMvc.perform(get("/nurse/queue/transfer"))

                .andExpect(status().is3xxRedirection());

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

    @DisplayName("Kiểm tra trang Chi tiết khám /visits/{id}")

    void testPatientVisitDetail() throws Exception {

        mockMvc.perform(get("/visits/80000000-0000-0000-0000-000000000001"))

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

