import csv
import random
import os

SYNTHETIC_FILE = "../train_dataset_v2.csv"
SCRAPED_FILE = "labeled_scraped.csv"
OUTPUT_FILE = "../train_dataset_hybrid.csv"

def read_csv(filename):
    data = []
    if os.path.exists(filename):
        # utf-8-sig de xu ly BOM character neu co
        with open(filename, mode='r', encoding='utf-8-sig') as f:
            reader = csv.DictReader(f)
            for row in reader:
                # Chuan hoa: neu co cot 'department' -> doi thanh 'label' (giong file synthetic)
                if 'department' in row:
                    row['label'] = row.pop('department')
                data.append(row)
    else:
        print(f"Warning: Khong tim thay file {filename}")
    return data

def merge_datasets():
    print("🔄 Đang đọc dữ liệu...")
    synthetic_data = read_csv(SYNTHETIC_FILE)
    scraped_data = read_csv(SCRAPED_FILE)
    
    print(f"📊 Synthetic Data: {len(synthetic_data)} mẫu")
    print(f"📊 Scraped Data:   {len(scraped_data)} mẫu")
    
    # Gộp 2 list
    combined_data = synthetic_data + scraped_data
    
    # Xáo trộn (Shuffle) để tránh việc model học theo thứ tự
    random.shuffle(combined_data)
    
    # Ghi ra file mới
    if combined_data:
        with open(OUTPUT_FILE, mode='w', encoding='utf-8', newline='') as f:
            writer = csv.DictWriter(f, fieldnames=["text", "label", "intent", "severity"], extrasaction='ignore')
            writer.writeheader()
            writer.writerows(combined_data)
            
        print(f"✅ Đã gộp và xáo trộn thành công {len(combined_data)} mẫu!")
        print(f"📂 Dataset cuối cùng được lưu tại: {OUTPUT_FILE}")
        
        # Thống kê sơ bộ
        dept_counts = {}
        for row in combined_data:
            dept = row.get('label', 'Unknown')
            dept_counts[dept] = dept_counts.get(dept, 0) + 1
            
        print("\n--- THỐNG KÊ THEO KHOA ---")
        for dept, count in sorted(dept_counts.items(), key=lambda x: x[1], reverse=True):
            print(f"- {dept}: {count} mẫu")
    else:
        print("❌ Không có dữ liệu để gộp.")

if __name__ == "__main__":
    merge_datasets()
