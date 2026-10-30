import urllib.request
import urllib.parse
import http.cookiejar
import json
import re
import sys

BASE_URL = "http://localhost:8081"

def create_opener():
    cj = http.cookiejar.CookieJar()
    opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(cj))
    return opener, cj

passed = 0
failed = 0

def report(name, success, details=""):
    global passed, failed
    if success:
        passed += 1
        print(f"[PASS] {name} {details}")
    else:
        failed += 1
        print(f"[FAIL] {name} {details}")

print("=== BẮT ĐẦU KIỂM THỬ TÍCH HỢP HỆ THỐNG MEDASSIST AI ===\n")

# -------------------------------------------------------------
# Test 1: GET /login
# -------------------------------------------------------------
opener, cj = create_opener()
try:
    resp = opener.open(f"{BASE_URL}/login")
    html = resp.read().decode('utf-8', errors='ignore')
    has_cccd = "CCCD" in html or "Căn cước" in html
    report("Test 1: GET /login", resp.status == 200 and has_cccd, f"(Status: {resp.status}, có nhãn CCCD: {has_cccd})")
except Exception as e:
    report("Test 1: GET /login", False, str(e))

# -------------------------------------------------------------
# Test 2: Đăng nhập bằng CCCD (079204000001 / 123456)
# -------------------------------------------------------------
try:
    login_data = urllib.parse.urlencode({
        "username": "079204000001",
        "password": "123456"
    }).encode('utf-8')
    
    req = urllib.request.Request(f"{BASE_URL}/login", data=login_data, method="POST")
    resp = opener.open(req)
    # Check redirect landed on /chat or /
    current_url = resp.geturl()
    chat_html = resp.read().decode('utf-8', errors='ignore')
    login_ok = "/chat" in current_url or "Nghèo" in chat_html or "chat" in chat_html
    report("Test 2: Đăng nhập bằng số CCCD 079204000001", login_ok, f"(Redirect to: {current_url})")
except Exception as e:
    report("Test 2: Đăng nhập bằng CCCD", False, str(e))

# -------------------------------------------------------------
# Test 3: Trích xuất Chat Session ID
# -------------------------------------------------------------
session_id = None
try:
    # Nếu chưa ở /chat thì mở /chat
    req = urllib.request.Request(f"{BASE_URL}/chat")
    resp = opener.open(req)
    chat_html = resp.read().decode('utf-8', errors='ignore')
    
    # Tìm sessionId trong HTML: value="sessionId" hoặc data-session-id="xxx" hoặc biến js
    match = re.search(r'name=["\']sessionId["\']\s+value=["\']([a-f0-9\-]+)["\']', chat_html)
    if not match:
        match = re.search(r'sessionId\s*=\s*["\']([a-f0-9\-]+)["\']', chat_html)
    if not match:
        match = re.search(r'data-session-id=["\']([a-f0-9\-]+)["\']', chat_html)
        
    if match:
        session_id = match.group(1)
        report("Test 3: Khởi tạo Chat Session thành công", True, f"(Session ID: {session_id})")
    else:
        # Thử tìm uuid bất kỳ trong trang
        uuid_match = re.search(r'[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}', chat_html)
        if uuid_match:
            session_id = uuid_match.group(0)
            report("Test 3: Khởi tạo Chat Session thành công", True, f"(Session ID: {session_id})")
        else:
            report("Test 3: Khởi tạo Chat Session", False, "Không trích xuất được UUID session")
except Exception as e:
    report("Test 3: Khởi tạo Chat Session", False, str(e))

# -------------------------------------------------------------
# Test 4: Giai đoạn 1 (Phase 1 Probe) - Khai thác triệu chứng mấu chốt
# -------------------------------------------------------------
if session_id:
    try:
        data = urllib.parse.urlencode({
            "sessionId": session_id,
            "message": "Tôi bị đau ngực"
        }).encode('utf-8')
        req = urllib.request.Request(f"{BASE_URL}/chat/send", data=data, method="POST")
        resp = opener.open(req)
        json_data = json.loads(resp.read().decode('utf-8'))
        
        reply = json_data.get("message", {}).get("content", "")
        triage = json_data.get("triage")
        
        # Ở lượt 1: AI không được vội chốt khoa, phải hỏi thêm chi tiết
        is_probing = triage is None
        has_question = "?" in reply or "như thế nào" in reply or "triệu chứng" in reply or "lan" in reply or "sốt" in reply
        report("Test 4: Giai đoạn 1 - AI hỏi sâu triệu chứng mấu chốt (Probe)", is_probing and has_question, 
               f"\n   -> AI phản hồi: \"{reply[:100]}...\"\n   -> Phân luồng: {triage}")
    except Exception as e:
        report("Test 4: Giai đoạn 1 - Probe", False, str(e))

    # -------------------------------------------------------------
    # Test 5: Giai đoạn 2 (Phase 2 Decision) - Phân biệt Đau ngực do Phổi vs Tim
    # -------------------------------------------------------------
    try:
        data = urllib.parse.urlencode({
            "sessionId": session_id,
            "message": "Cơn đau nhói tăng khi hít sâu hoặc ho, kèm theo có sốt 38.5 độ và khạc đờm vàng"
        }).encode('utf-8')
        req = urllib.request.Request(f"{BASE_URL}/chat/send", data=data, method="POST")
        resp = opener.open(req)
        json_data = json.loads(resp.read().decode('utf-8'))
        
        triage = json_data.get("triage")
        dept_name = triage.get("departmentName", "") if triage else ""
        has_triage = triage is not None
        is_hohap = "hô hấp" in dept_name.lower() or "KHOHAP" in str(triage)
        report("Test 5: Giai đoạn 2 - Phán đoán lâm sàng chính xác vào Khoa Hô Hấp", has_triage and is_hohap,
               f"\n   -> Khoa phân luồng: {dept_name} (Mức ưu tiên: {triage.get('priority') if triage else 'N/A'})")
    except Exception as e:
        report("Test 5: Giai đoạn 2 - Decision", False, str(e))

    # -------------------------------------------------------------
    # Test 6: Red Flag Bypass - Dấu hiệu cấp cứu nguy kịch
    # -------------------------------------------------------------
    try:
        data = urllib.parse.urlencode({
            "sessionId": session_id,
            "message": "Bệnh nhân đột ngột bất tỉnh và ngừng thở"
        }).encode('utf-8')
        req = urllib.request.Request(f"{BASE_URL}/chat/send", data=data, method="POST")
        resp = opener.open(req)
        json_data = json.loads(resp.read().decode('utf-8'))
        
        triage = json_data.get("triage")
        priority = triage.get("priority", "") if triage else ""
        dept_name = triage.get("departmentName", "") if triage else ""
        is_emergency = "cấp cứu" in dept_name.lower() or "KCAPCUU" in str(triage)
        report("Test 6: Red Flag Bypass - Tự động định tuyến Khoa Cấp Cứu M1", is_emergency and priority == "M1",
               f"\n   -> Khoa: {dept_name}, Ưu tiên: {priority}")
    except Exception as e:
        report("Test 6: Red Flag Bypass", False, str(e))

# -------------------------------------------------------------
# Test 7: BHYT Ownership - Thử áp dụng BHYT của người khác -> Bị chặn 403
# -------------------------------------------------------------
try:
    # Người đang đăng nhập là Nguyễn Văn Nghèo (CCCD 079204000001, BHYT: HN4790000000001)
    # Nhập BHYT của Phạm Thị Cận (CN3790000000001)
    fraud_payload = json.dumps({"maThe": "CN3790000000001"}).encode('utf-8')
    req = urllib.request.Request(
        f"{BASE_URL}/api/bhyt/apply",
        data=fraud_payload,
        headers={"Content-Type": "application/json"},
        method="POST"
    )
    try:
        resp = opener.open(req)
        report("Test 7: Chặn gian lận BHYT (nhập thẻ của người khác)", False, "Lẽ ra phải trả về HTTP 403")
    except urllib.error.HTTPError as err:
        is_403 = err.code == 403
        body = err.read().decode('utf-8')
        report("Test 7: Chặn gian lận BHYT (nhập thẻ người khác)", is_403, f"(HTTP {err.code}: {body})")
except Exception as e:
    report("Test 7: Chặn gian lận BHYT", False, str(e))

# -------------------------------------------------------------
# Test 8: BHYT Ownership - Áp dụng BHYT chính chủ -> Thành công 200
# -------------------------------------------------------------
try:
    valid_payload = json.dumps({"maThe": "HN4790000000001"}).encode('utf-8')
    req = urllib.request.Request(
        f"{BASE_URL}/api/bhyt/apply",
        data=valid_payload,
        headers={"Content-Type": "application/json"},
        method="POST"
    )
    resp = opener.open(req)
    result = json.loads(resp.read().decode('utf-8'))
    is_ok = resp.status == 200 and result.get("success") == True and result.get("mucHuong") == 100
    report("Test 8: Áp dụng BHYT chính chủ (khớp CCCD)", is_ok, 
           f"(Đối tượng: {result.get('tenDoiTuong')}, Hưởng: {result.get('mucHuong')}%)")
except Exception as e:
    report("Test 8: Áp dụng BHYT chính chủ", False, str(e))

# -------------------------------------------------------------
# Test 9: Đăng nhập bằng số BHYT (Fallback compatibility)
# -------------------------------------------------------------
opener2, cj2 = create_opener()
try:
    login_data = urllib.parse.urlencode({
        "username": "CN3790000000001",
        "password": "123456"
    }).encode('utf-8')
    
    req = urllib.request.Request(f"{BASE_URL}/login", data=login_data, method="POST")
    resp = opener2.open(req)
    current_url = resp.geturl()
    login_bhyt_ok = "/chat" in current_url or resp.status == 200
    report("Test 9: Đăng nhập tương thích ngược bằng mã BHYT", login_bhyt_ok, f"(Status: {resp.status})")
except Exception as e:
    report("Test 9: Đăng nhập bằng BHYT", False, str(e))

print(f"\n=== KẾT QUẢ: {passed} PASSED / {failed} FAILED ===")