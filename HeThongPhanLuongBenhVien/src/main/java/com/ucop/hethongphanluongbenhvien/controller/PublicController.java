package com.ucop.hethongphanluongbenhvien.controller;

import com.ucop.hethongphanluongbenhvien.entity.Department;
import com.ucop.hethongphanluongbenhvien.entity.Ticket;
import com.ucop.hethongphanluongbenhvien.service.DepartmentService;
import com.ucop.hethongphanluongbenhvien.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class PublicController {

    private final DepartmentService departmentService;
    private final TicketService     ticketService;

    /**
     * Màn hình sảnh chờ — hiển thị hàng đợi theo Khoa.
     * Mặc định hiển thị khoa Cấp Cứu nếu không truyền deptId.
     * Trang tự reload sau 15 giây để cập nhật số liệu mới.
     */
    @GetMapping({"/public/display", "/display"})
    public String lobbyDisplay(@RequestParam(required = false) UUID deptId, Model model) {
        List<Department> allDepts = departmentService.findActive();
        model.addAttribute("allDepartments", allDepts);

        // Lấy khoa hiển thị (mặc định khoa đầu tiên)
        Optional<Department> deptOpt = (deptId != null)
                ? allDepts.stream().filter(d -> d.getId().equals(deptId)).findFirst()
                : allDepts.stream().findFirst();

        deptOpt.ifPresentOrElse(dept -> {
            model.addAttribute("departmentName",     dept.getName().toUpperCase());
            model.addAttribute("departmentLocation", dept.getBuilding() + " — Tầng " + dept.getFloor()
                                                     + " — Phòng " + dept.getRoomNumbers());
            model.addAttribute("selectedDeptId", dept.getId());

            List<Ticket> activeTickets = ticketService.findActiveByDepartment(dept.getId());
            List<Ticket> called  = activeTickets.stream()
                    .filter(t -> t.getStatus() == Ticket.Status.CALLED).toList();

            // Phiếu đang khám (CALLED → lấy phiếu đầu tiên)
            if (!called.isEmpty()) {
                Ticket current = called.get(0);
                model.addAttribute("currentNumber",      current.getTicketNumber());
                model.addAttribute("currentPatientName", current.getPatient() != null ? current.getPatient().getFullName() : "");
                model.addAttribute("currentPriority",    current.getPriority().name());
            }

            // Danh sách đang chờ (chỉ WAITING, tối đa 10 dòng)
            List<Ticket> waitingOnly = activeTickets.stream()
                    .filter(t -> t.getStatus() == Ticket.Status.WAITING)
                    .limit(10).toList();
            model.addAttribute("waitingList", waitingOnly);

        }, () -> {
            model.addAttribute("departmentName", "CHƯA CÓ KHOA NÀO");
            model.addAttribute("waitingList", List.of());
        });

        return "public/display";
    }
}
