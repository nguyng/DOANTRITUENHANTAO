import csv
import json
import time
import os

try:
    from google import genai
    from pydantic import BaseModel, Field
except ImportError:
    print("❌ Thư viện google-genai chưa được cài đặt.")
    print("Vui lòng chạy lệnh: pip install google-genai pydantic")
    exit(1)

# Thay thế bằng API Key của bạn
GEMINI_API_KEY = ""

# Cấu hình danh sách Khoa
DEPARTMENTS = [
    "Khoa Cấp Cứu", "Khoa Tim Mạch", "Khoa Tiêu Hoá", "Khoa Xương Khớp",
    "Khoa Thần Kinh", "Khoa Hô Hấp", "Khoa Mắt", "Khoa Tai Mũi Họng",
    "Khoa Da Liễu", "Khoa Nhi", "Khoa Sản - Phụ Khoa"
]

def setup_gemini():
    client = genai.Client(api_key=GEMINI_API_KEY)
    return client

def get_label_from_llm(client, question, original_dept):
    prompt = f"""
    Bạn là một chuyên gia phân loại y tế. Hãy phân loại câu hỏi sau của bệnh nhân dựa trên danh sách các khoa cho trước.
    
    Câu hỏi: "{question}"
    Chuyên khoa bác sĩ trả lời trên web: "{original_dept}"
    
    Danh sách khoa chuẩn của hệ thống:
    {', '.join(DEPARTMENTS)}
    
    Nhiệm vụ:
    1. Chọn một 'department' phù hợp nhất từ danh sách chuẩn (Nếu không thuộc khoa nào, chọn 'Khác').
    2. Xác định 'intent' (SYMPTOM nếu là kể bệnh/triệu chứng, FAQ nếu là hỏi đáp thủ tục, giá cả, lịch khám).
    3. Xác định 'severity' (EMERGENCY nếu nguy hiểm tính mạng/cấp cứu, MEDIUM nếu cần khám sớm, NORMAL nếu bình thường).
    
    Chỉ trả về ĐÚNG MỘT đoạn JSON duy nhất (không có markdown, không giải thích), với cấu trúc sau:
    {{
        "intent": "...",
        "department": "...",
        "severity": "..."
    }}
    """
    
    try:
        response = client.models.generate_content(
            model='gemini-3.6-flash',
            contents=prompt
        )
        text_resp = response.text.strip()
        # Clean markdown code blocks if AI outputs them
        if text_resp.startswith("```json"):
            text_resp = text_resp[7:]
        if text_resp.startswith("```"):
            text_resp = text_resp[3:]
        if text_resp.endswith("```"):
            text_resp = text_resp[:-3]
            
        result = json.loads(text_resp.strip())
        return result
    except Exception as e:
        print(f"⚠️ Lỗi khi gọi LLM: {e}")
        return None

def process_data():
    if GEMINI_API_KEY == "YOUR_GEMINI_API_KEY_HERE":
        print("❌ VUI LÒNG CUNG CẤP GEMINI API KEY CỦA BẠN TRONG FILE CODE!")
        return

    input_file = "raw_scraped.csv"
    output_file = "labeled_scraped.csv"
    
    if not os.path.exists(input_file):
        print(f"❌ Không tìm thấy file {input_file}. Hãy chạy scrape_qa.py trước.")
        return
        
    model = setup_gemini()
    labeled_data = []
    
    with open(input_file, mode='r', encoding='utf-8') as f:
        reader = csv.DictReader(f)
        for row in reader:
            print(f"Đang xử lý câu hỏi: {row['question'][:50]}...")
            label = get_label_from_llm(model, row['question'], row['doctor_dept'])
            
            if label:
                labeled_data.append({
                    "text": row['question'],
                    "intent": label.get("intent", "SYMPTOM"),
                    "department": label.get("department", "Chưa rõ"),
                    "severity": label.get("severity", "NORMAL")
                })
            
            # Rate limit handling (Gemini free tier allows 15 RPM)
            time.sleep(4) 
            
    if labeled_data:
        with open(output_file, mode='w', encoding='utf-8', newline='') as f:
            writer = csv.DictWriter(f, fieldnames=["text", "intent", "department", "severity"])
            writer.writeheader()
            writer.writerows(labeled_data)
        print(f"✅ Hoàn tất gán nhãn! Đã lưu tại {output_file}")

if __name__ == "__main__":
    process_data()
