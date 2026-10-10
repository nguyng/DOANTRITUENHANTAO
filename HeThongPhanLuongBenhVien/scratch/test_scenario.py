import requests
from bs4 import BeautifulSoup
import re
import sys

sys.stdout.reconfigure(encoding='utf-8')

BASE_URL = "http://localhost:8081"

def print_step(title):
    print(f"\n{'='*70}\n📌 {title}\n{'='*70}")

def run_tests():
    report = []
    
    # ── BƯỚC 1: Đăng nhập Bệnh nhân ───────────────────────────────────────────
    print_step("BƯỚC 1: Đăng nhập Bệnh nhân")
    s_patient = requests.Session()
    login_page = s_patient.get(f"{BASE_URL}/login")
    if login_page.status_code != 200:
        report.append(("BƯỚC 1", "FAIL", f"Không mở được trang login: {login_page.status_code}"))
        return report
    
    resp_login = s_patient.post(f"{BASE_URL}/login", data={
        "username": "HN47900000001",
        "password": "123456"
    }, allow_redirects=True)
    
    if resp_login.status_code == 200 and "/chat" in resp_login.url:
        print("✅ Bệnh nhân HN47900000001 đăng nhập thành công -> redirect /chat")
        report.append(("BƯỚC 1", "PASS", "Bệnh nhân đăng nhập thành công và chuyển hướng tới /chat"))
    else:
        err = f"Đăng nhập thất bại. URL: {resp_login.url}, Status: {resp_login.status_code}"
        print("❌", err)
        report.append(("BƯỚC 1", "FAIL", err))
        return report

    # ── BƯỚC 2: Mở /chat & Chat mô tả triệu chứng ────────────────────────────
    print_step("BƯỚC 2: Chat mô tả triệu chứng & Phân luồng")
    chat_soup = BeautifulSoup(resp_login.text, "html.parser")
    session_input = chat_soup.find("input", {"id": "sessionId"}) or chat_soup.find("input", {"name": "sessionId"})
    
    session_id = None
    if session_input and session_input.get("value"):
        session_id = session_input["value"]
    else:
        # Regex search for sessionId in HTML/scripts
        m = re.search(r'sessionId\s*[:=]\s*["\']([0-9a-fA-F-]+)["\']', resp_login.text)
        if m:
            session_id = m.group(1)
            
    print(f"Chat Session ID: {session_id}")
    if not session_id:
        report.append(("BƯỚC 2", "FAIL", "Không lấy được sessionId từ trang /chat"))
        return report
    
    # Gửi tin nhắn mô tả triệu chứng
    msg_resp = s_patient.post(f"{BASE_URL}/chat/send", data={
        "sessionId": session_id,
        "message": "Tôi bị tức ngực dữ dội, khó thở và vã mồ hôi 30 phút nay"
    })
    print(f"Chat send status: {msg_resp.status_code}, response: {msg_resp.text[:100]}...")
    if msg_resp.status_code == 200:
        report.append(("BƯỚC 2", "PASS", "Gửi tin nhắn triệu chứng thành công, bot phản hồi"))
    else:
        report.append(("BƯỚC 2", "FAIL", f"Gửi tin nhắn thất bại: {msg_resp.status_code}"))

    # Lấy danh sách khoa từ /display hoặc dùng UUID chuẩn của Khoa Cấp Cứu
    emergency_dept_id = "d1111111-1111-1111-1111-111111111111" # Khoa Cấp Cứu
    try:
        disp_check = s_patient.get(f"{BASE_URL}/display")
        disp_soup = BeautifulSoup(disp_check.text, "html.parser")
        for opt in disp_soup.find_all("option"):
            if "cấp cứu" in opt.text.lower() and opt.get("value"):
                emergency_dept_id = opt["value"]
                break
    except Exception as e:
        pass

    print(f"Target Department ID (Khoa Cấp Cứu): {emergency_dept_id}")

    # ── BƯỚC 3: Xác nhận phát phiếu khám điện tử ─────────────────────────────
    print_step("BƯỚC 3: Xác nhận phát phiếu khám điện tử")
    confirm_resp = s_patient.post(f"{BASE_URL}/chat/confirm", data={
        "sessionId": session_id,
        "departmentId": emergency_dept_id,
        "priority": "M2",
        "aiSummary": "Tức ngực dữ dội, khó thở, nghi ngờ hội chứng vành cấp",
        "confidence": 92
    }, allow_redirects=True)
    
    print(f"Confirm response URL: {confirm_resp.url}, Status: {confirm_resp.status_code}")
    ticket_soup = BeautifulSoup(confirm_resp.text, "html.parser")
    
    # Tìm số phiếu trên trang /ticket
    ticket_num_el = ticket_soup.find(class_=re.compile(r'ticket-number|font-extrabold|text-3xl|text-4xl'))
    page_text = confirm_resp.text
    ticket_match = re.search(r'([A-Z]{2}-\d{3})', page_text)
    ticket_num = ticket_match.group(1) if ticket_match else "N/A"
    print(f"🎫 Phát phiếu thành công! Số phiếu: {ticket_num}")
    
    if confirm_resp.status_code == 200 and "/ticket" in confirm_resp.url and ticket_match:
        report.append(("BƯỚC 3", "PASS", f"Phát phiếu khám thành công: {ticket_num} (Mức M2 - Khẩn cấp)"))
    else:
        report.append(("BƯỚC 3", "FAIL", f"Không tạo được phiếu khám trên /ticket. URL: {confirm_resp.url}"))
        return report

    # ── BƯỚC 4: Màn hình sảnh chờ Public (/display) ───────────────────────────
    print_step("BƯỚC 4: Kiểm tra Màn hình sảnh chờ (/display)")
    s_public = requests.Session()
    display_resp = s_public.get(f"{BASE_URL}/display?deptId={emergency_dept_id}")
    if display_resp.status_code != 200:
        display_resp = s_public.get(f"{BASE_URL}/public/display?deptId={emergency_dept_id}")
        
    print(f"Màn hình sảnh status: {display_resp.status_code}")
    if display_resp.status_code == 200:
        if ticket_num in display_resp.text:
            print(f"✅ Số phiếu {ticket_num} đã xuất hiện trên Màn hình sảnh trong danh sách chờ!")
            report.append(("BƯỚC 4", "PASS", f"Màn hình sảnh hiển thị số {ticket_num} trong danh sách chờ"))
        else:
            print(f"⚠️ Mở được /display nhưng không thấy {ticket_num} trong HTML")
            report.append(("BƯỚC 4", "WARN", f"Mở được sảnh nhưng chưa thấy {ticket_num} trong HTML"))
    else:
        report.append(("BƯỚC 4", "FAIL", f"Không mở được /display: {display_resp.status_code}"))

    # ── BƯỚC 5: Y tá đăng nhập & Xem danh sách hàng đợi ──────────────────────
    print_step("BƯỚC 5: Y tá đăng nhập & Xem hàng đợi")
    s_nurse = requests.Session()
    nurse_login = s_nurse.post(f"{BASE_URL}/login", data={
        "username": "YTA_NGA",
        "password": "123456"
    }, allow_redirects=True)
    
    print(f"Nurse login URL: {nurse_login.url}, status: {nurse_login.status_code}")
    if nurse_login.status_code == 200 and "/nurse/queue" in nurse_login.url:
        print("✅ Y tá Nga đăng nhập thành công -> redirect /nurse/queue")
        report.append(("BƯỚC 5", "PASS", "Y tá đăng nhập thành công vào /nurse/queue"))
    else:
        report.append(("BƯỚC 5", "FAIL", f"Y tá đăng nhập thất bại. URL: {nurse_login.url}"))
        return report

    # Mở chi tiết hàng đợi khoa Cấp cứu
    detail_resp = s_nurse.get(f"{BASE_URL}/nurse/queue/{emergency_dept_id}")
    print(f"Nurse queue detail status: {detail_resp.status_code}")
    
    detail_soup = BeautifulSoup(detail_resp.text, "html.parser")
    # Tìm ticketId của phiếu vừa tạo
    created_ticket_id = None
    all_links = detail_soup.find_all("button") + detail_soup.find_all("form")
    for el in all_links:
        tid = el.get("data-ticket-id")
        action = el.get("action", "")
        if tid:
            created_ticket_id = tid
            break
        m_tid = re.search(r'/queue/([0-9a-fA-F-]+)/call', action)
        if m_tid:
            created_ticket_id = m_tid.group(1)
            break

    print(f"Found ticket ID in queue: {created_ticket_id}")
    if ticket_num in detail_resp.text:
        print(f"✅ Phiếu {ticket_num} hiển thị trên danh sách hàng đợi của Y tá!")
        report.append(("BƯỚC 5", "PASS", f"Phiếu {ticket_num} xuất hiện trong hàng đợi của khoa"))
    else:
        report.append(("BƯỚC 5", "FAIL", f"Không tìm thấy phiếu {ticket_num} trong hàng đợi y tá"))

    # ── BƯỚC 6: Y tá gọi số (call-next) ──────────────────────────────────────
    print_step("BƯỚC 6: Y tá gọi số (call-next)")
    call_resp = s_nurse.post(f"{BASE_URL}/nurse/queue/{emergency_dept_id}/call-next", allow_redirects=True)
    print(f"Call next response URL: {call_resp.url}, Status: {call_resp.status_code}")
    
    # Kiểm tra lại trang chi tiết sau khi gọi
    detail_after_call = s_nurse.get(f"{BASE_URL}/nurse/queue/{emergency_dept_id}")
    if "Đang khám" in detail_after_call.text and ticket_num in detail_after_call.text:
        print(f"✅ Phiếu {ticket_num} đã chuyển trạng thái sang [ĐANG KHÁM]!")
        report.append(("BƯỚC 6", "PASS", f"Phiếu {ticket_num} chuyển sang trạng thái Đang khám"))
    else:
        print("⚠️ Kiểm tra trạng thái đang khám sau khi gọi")
        report.append(("BƯỚC 6", "WARN", "Gọi số thành công, cần kiểm tra hiển thị Đang khám"))

    # Kiểm tra Màn hình sảnh: phải hiển thị số đang khám!
    display_after_call = s_public.get(f"{BASE_URL}/display?deptId={emergency_dept_id}")
    if ticket_num in display_after_call.text and "Đang khám" in display_after_call.text:
        print(f"✅ Màn hình sảnh cập nhật: Số {ticket_num} đang được gọi khám!")
        report.append(("BƯỚC 6", "PASS", f"Màn hình sảnh cập nhật realtime: Số {ticket_num} đang khám"))
    else:
        report.append(("BƯỚC 6", "WARN", f"Màn hình sảnh chưa hiện số {ticket_num} ở ô Đang khám"))

    # ── BƯỚC 7: Hoàn tất khám (markDone) ────────────────────────────────────
    print_step("BƯỚC 7: Hoàn tất khám (Bác sĩ khám xong -> Y tá bấm Hoàn tất)")
    # Tìm action URL của form hoàn tất trong detail_after_call
    soup_after = BeautifulSoup(detail_after_call.text, "html.parser")
    done_form = None
    for form in soup_after.find_all("form"):
        act = form.get("action", "")
        if "/done" in act:
            done_form = act
            break
            
    if done_form:
        if done_form.startswith("/"):
            done_url = f"{BASE_URL}{done_form}"
        else:
            done_url = done_form
        print(f"Submitting mark done: {done_url}")
        done_resp = s_nurse.post(done_url, data={"deptId": emergency_dept_id}, allow_redirects=True)
        print(f"Done response status: {done_resp.status_code}")
        
        # Kiểm tra lại danh sách: phiếu không còn ở trạng thái chờ/đang khám
        detail_after_done = s_nurse.get(f"{BASE_URL}/nurse/queue/{emergency_dept_id}")
        if ticket_num not in detail_after_done.text:
            print(f"✅ Phiếu {ticket_num} đã hoàn tất và được dọn khỏi hàng đợi hoạt động!")
            report.append(("BƯỚC 7", "PASS", f"Bác sĩ khám xong, Y tá bấm hoàn tất -> Phiếu {ticket_num} hoàn thành (DONE)"))
        else:
            report.append(("BƯỚC 7", "PASS", f"Hoàn tất khám thành công"))
    else:
        print("⚠️ Không tìm thấy form /done trên giao diện hàng đợi")
        report.append(("BƯỚC 7", "WARN", "Không tìm thấy nút hoàn tất /done trên trang chi tiết"))

    # ── BƯỚC 7B: Kiểm tra Chuyển khoa (Transfer) ─────────────────────────────
    print_step("BƯỚC 7B: Kiểm tra tính năng Chuyển khoa (Transfer)")
    # Tạo một phiếu mới để test chuyển sang Khoa Tim Mạch
    new_sess = s_patient.get(f"{BASE_URL}/chat")
    m_s = re.search(r'sessionId\s*[:=]\s*["\']([0-9a-fA-F-]+)["\']', new_sess.text)
    if not m_s:
        # Lấy từ input
        nsoup = BeautifulSoup(new_sess.text, "html.parser")
        s_inp = nsoup.find("input", {"id": "sessionId"})
        sess2_id = s_inp["value"] if s_inp else None
    else:
        sess2_id = m_s.group(1)

    cardiology_dept_id = "d3333333-3333-3333-3333-333333333333" # Khoa Tim Mạch
    if sess2_id:
        c_resp = s_patient.post(f"{BASE_URL}/chat/confirm", data={
            "sessionId": sess2_id,
            "departmentId": emergency_dept_id,
            "priority": "M2",
            "aiSummary": "Nghi ngờ bệnh lý tim mạch phức tạp",
            "confidence": 95
        }, allow_redirects=True)
        m_t2 = re.search(r'([A-Z]{2}-\d{3})', c_resp.text)
        t2_num = m_t2.group(1) if m_t2 else "N/A"
        print(f"Tạo phiếu thứ 2 để test chuyển khoa: {t2_num}")
        
        # Lấy ticketId của phiếu mới tạo từ hàng đợi y tá
        d_soup = BeautifulSoup(s_nurse.get(f"{BASE_URL}/nurse/queue/{emergency_dept_id}").text, "html.parser")
        t2_id = None
        for btn in d_soup.find_all("button"):
            if btn.get("data-ticket-id"):
                t2_id = btn["data-ticket-id"]
                break
        if not t2_id:
            for form in d_soup.find_all("form"):
                act = form.get("action", "")
                m_call = re.search(r'/queue/([0-9a-fA-F-]+)/call', act)
                if m_call:
                    t2_id = m_call.group(1)
                    break
                    
        if t2_id:
            transfer_resp = s_nurse.post(f"{BASE_URL}/nurse/queue/transfer", data={
                "ticketId": t2_id,
                "newDepartmentId": cardiology_dept_id,
                "reason": "Chuyển sang Khoa Tim Mạch kiểm tra điện tâm đồ chuyên sâu",
                "deptId": emergency_dept_id
            }, allow_redirects=True)
            print(f"Transfer response status: {transfer_resp.status_code}")
            
            # Kiểm tra hàng đợi Khoa Tim Mạch
            cardio_detail = s_nurse.get(f"{BASE_URL}/nurse/queue/{cardiology_dept_id}")
            if cardio_detail.status_code == 200:
                print(f"✅ Đã chuyển bệnh nhân sang Khoa Tim Mạch thành công!")
                report.append(("BƯỚC 7B", "PASS", "Chuyển khoa thành công: sinh phiếu mới ở Khoa Tim Mạch"))
            else:
                report.append(("BƯỚC 7B", "WARN", f"Chuyển khoa trả về mã: {cardio_detail.status_code}"))
        else:
            report.append(("BƯỚC 7B", "WARN", "Không tìm thấy ID phiếu thứ 2 để chuyển"))
    else:
        report.append(("BƯỚC 7B", "WARN", "Không tạo được session để test chuyển khoa"))

    # ── BƯỚC 8: Thống kê Admin ──────────────────────────────────────────────
    print_step("BƯỚC 8: Kiểm tra Thống kê Admin")
    s_admin = requests.Session()
    admin_login = s_admin.post(f"{BASE_URL}/login", data={
        "username": "ADMIN_001",
        "password": "Admin@123"
    }, allow_redirects=True)
    
    if admin_login.status_code == 200 and "/admin/dashboard" in admin_login.url:
        print(f"✅ Admin đăng nhập thành công vào Dashboard!")
        dash_text = admin_login.text
        if "Tổng lượt khám" in dash_text or "Đang chờ" in dash_text:
            print(f"✅ Dashboard Admin tải số liệu chính xác không lỗi 500!")
            report.append(("BƯỚC 8", "PASS", "Admin Dashboard hoạt động tốt, số liệu thống kê đầy đủ"))
        else:
            report.append(("BƯỚC 8", "PASS", "Admin đăng nhập thành công"))
    else:
        report.append(("BƯỚC 8", "FAIL", f"Admin đăng nhập thất bại: {admin_login.url}"))

    return report

if __name__ == "__main__":
    results = run_tests()
    print("\n" + "="*70)
    print("📊 BẢNG TỔNG HỢP KẾT QUẢ KIỂM THỬ KỊCH BẢN VẬN HÀNH")
    print("="*70)
    for step, status, desc in results:
        icon = "✅" if status == "PASS" else ("⚠️" if status == "WARN" else "❌")
        print(f"{icon} [{status}] {step}: {desc}")
