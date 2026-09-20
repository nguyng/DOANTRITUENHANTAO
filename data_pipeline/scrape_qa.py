import requests
from bs4 import BeautifulSoup
import csv
import time
import os

# Cấu hình
BASE_URL = "https://www.vinmec.com/vi/tin-tuc/hoi-dap-bac-si/?page={}"
HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36",
    "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
}
OUTPUT_FILE = "raw_scraped.csv"
NUM_PAGES = 5 # Số trang cần cào (Mỗi trang khoảng 10-15 câu)

def scrape_vinmec_qa():
    print(f"🚀 Bắt đầu cào dữ liệu Hỏi-Đáp từ Vinmec...")
    results = []
    
    for page in range(1, NUM_PAGES + 1):
        print(f"Đang xử lý trang {page}...")
        url = BASE_URL.format(page)
        
        try:
            response = requests.get(url, headers=HEADERS, timeout=10)
            if response.status_code != 200:
                print(f"⚠️ Lỗi truy cập trang {page}: HTTP {response.status_code}")
                continue
                
            soup = BeautifulSoup(response.content, 'html.parser')
            
            # Lưu ý: Class CSS này có thể thay đổi tùy theo giao diện Vinmec hiện tại.
            # Bạn cần dùng F12 (DevTools) để kiểm tra lại class chính xác nếu script không tìm thấy bài viết.
            articles = soup.find_all('div', class_='post-item') # Giả định class post-item
            
            # Nếu website dùng cấu trúc khác (ví dụ thẻ <li> trong <ul> có class 'list-qa')
            if not articles:
                articles = soup.find_all('li', class_='list-item')
                
            for article in articles:
                # Trích xuất tiêu đề câu hỏi
                title_tag = article.find('h3') or article.find('h2') or article.find('a', class_='title')
                title = title_tag.text.strip() if title_tag else ""
                
                # Trích xuất chuyên khoa (thường nằm ở tag/badge)
                dept_tag = article.find('a', class_='category') or article.find('span', class_='badge')
                dept = dept_tag.text.strip() if dept_tag else "Chưa rõ Khoa"
                
                # Trích xuất đoạn tóm tắt câu hỏi (mô tả triệu chứng)
                desc_tag = article.find('div', class_='summary') or article.find('p', class_='description')
                desc = desc_tag.text.strip() if desc_tag else ""
                
                if title:
                    # Gộp tiêu đề và mô tả làm nội dung câu hỏi hoàn chỉnh
                    full_question = f"{title}. {desc}".strip()
                    results.append({"question": full_question, "doctor_dept": dept})
            
            # Nghỉ 2 giây để tránh bị chặn IP
            time.sleep(2)
            
        except Exception as e:
            print(f"❌ Lỗi khi cào trang {page}: {e}")
            
    # Lưu ra CSV
    if results:
        with open(OUTPUT_FILE, mode='w', encoding='utf-8', newline='') as f:
            writer = csv.DictWriter(f, fieldnames=["question", "doctor_dept"])
            writer.writeheader()
            writer.writerows(results)
        print(f"✅ Hoàn tất! Đã cào được {len(results)} câu hỏi. Lưu tại {OUTPUT_FILE}")
    else:
        print("⚠️ Không tìm thấy dữ liệu nào. Vui lòng kiểm tra lại cấu trúc HTML (CSS classes).")
        
        # --- FALLBACK: TẠO DỮ LIỆU MOCK ĐỂ TEST PIPELINE NẾU WEB BỊ CHẶN ---
        print("🔧 Tự động tạo dữ liệu mẫu (Mock Data) để bạn test luồng LLM...")
        mock_data = [
            {"question": "Bác sĩ ơi tôi bị đau quặn bụng, đi ngoài phân đen 3 ngày nay, thỉnh thoảng nôn ra dịch chua. Giúp tôi với.", "doctor_dept": "Tiêu hóa"},
            {"question": "Cháu nhà tôi 5 tuổi bị nổi mẩn đỏ khắp người, ngứa ngáy nhiều vào ban đêm, kèm sốt nhẹ.", "doctor_dept": "Nhi khoa"},
            {"question": "Tôi bị nhói tim, cảm giác thắt lại ở ngực trái lan ra cánh tay trái, mồ hôi vã ra.", "doctor_dept": "Tim mạch"},
            {"question": "Dạo này tôi hay bị quên, đau nửa đầu bên phải, mất ngủ triền miên làm việc không tập trung được.", "doctor_dept": "Thần kinh"},
            {"question": "Da mặt tôi tự nhiên nổi rất nhiều mụn nước nhỏ, rát và đỏ sau khi dùng kem dưỡng ẩm mới.", "doctor_dept": "Da liễu"}
        ]
        with open(OUTPUT_FILE, mode='w', encoding='utf-8', newline='') as f:
            writer = csv.DictWriter(f, fieldnames=["question", "doctor_dept"])
            writer.writeheader()
            writer.writerows(mock_data)
        print(f"✅ Đã tạo {len(mock_data)} dòng Mock Data tại {OUTPUT_FILE}")

if __name__ == "__main__":
    scrape_vinmec_qa()
