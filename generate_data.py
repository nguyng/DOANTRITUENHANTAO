import csv
import random

# Danh sách các khoa và triệu chứng tương ứng
symptoms_by_dept = {
    "Khoa Cấp Cứu": [
        (["khó thở dữ dội", "ngất xỉu", "tím tái", "co giật", "đau tức ngực dữ dội", "chảy máu không cầm", "tai nạn giao thông", "bỏng nặng", "chấn thương sọ não", "yếu liệt nửa người đột ngột", "nhồi máu cơ tim", "đột quỵ"], "EMERGENCY"),
        (["đau bụng quằn quại", "nôn ra máu", "khó thở nhẹ", "đau đầu dữ dội", "sốt cao co giật"], "EMERGENCY")
    ],
    "Khoa Tim Mạch": [
        (["đau tức ngực", "hồi hộp", "đánh trống ngực", "khó thở khi nằm", "mệt mỏi bất thường", "chóng mặt", "phù chân", "hụt hơi", "tim đập nhanh"], "MEDIUM"),
        (["nhói ở tim", "đau thắt ngực lan ra tay", "tăng huyết áp đột ngột"], "EMERGENCY")
    ],
    "Khoa Tiêu Hoá": [
        (["đau dạ dày", "ợ chua", "ợ hơi", "đau bụng âm ỉ", "đầy bụng", "khó tiêu", "chán ăn", "buồn nôn", "tiêu chảy", "táo bón", "đi ngoài ra máu", "đau vùng thượng vị"], "NORMAL"),
        (["đau quặn bụng", "nôn liên tục", "đi ngoài phân đen", "viêm loét dạ dày"], "MEDIUM")
    ],
    "Khoa Xương Khớp": [
        (["đau lưng", "nhức mỏi vai gáy", "đau khớp gối", "tê bì chân tay", "mỏi cổ", "cứng khớp buổi sáng", "lục cục ở khớp khi vận động", "đau cột sống thắt lưng"], "NORMAL"),
        (["sưng tấy khớp", "đau nhức không đi lại được", "trật khớp", "gút cấp"], "MEDIUM")
    ],
    "Khoa Thần Kinh": [
        (["đau nửa đầu", "mất ngủ", "chóng mặt", "hay quên", "rối loạn tiền đình", "căng thẳng", "mệt mỏi", "run tay chân"], "NORMAL"),
        (["đau đầu dữ dội", "tê liệt một bên mặt", "nói đớ", "mờ mắt đột ngột kèm đau đầu"], "EMERGENCY")
    ],
    "Khoa Hô Hấp": [
        (["ho khan", "ho có đờm", "sổ mũi", "viêm họng", "ngạt mũi", "thở khò khè", "đau rát họng"], "NORMAL"),
        (["khó thở", "ho ra máu", "tức ngực khi ho", "hen suyễn tái phát"], "MEDIUM")
    ],
    "Khoa Mắt": [
        (["cộm mắt", "mờ mắt", "chảy nước mắt", "ngứa mắt", "nhức mỏi mắt", "khô mắt", "ruồi bay trước mắt"], "NORMAL"),
        (["đau nhức hốc mắt", "mất thị lực đột ngột", "sưng tấy đỏ mắt", "chấn thương mắt"], "MEDIUM")
    ],
    "Khoa Tai Mũi Họng": [
        (["ù tai", "ngứa tai", "viêm họng", "nuốt vướng", "sổ mũi", "ngạt mũi", "hắt hơi liên tục", "khàn tiếng", "viêm amidan"], "NORMAL"),
        (["chảy mủ tai", "đau nhức trong tai", "chảy máu cam không cầm", "điếc đột ngột"], "MEDIUM")
    ],
    "Khoa Da Liễu": [
        (["nổi mẩn đỏ", "ngứa ngáy", "nổi mụn nước", "khô da", "bong tróc da", "rụng tóc", "nhiều gàu", "dị ứng mỹ phẩm", "viêm da cơ địa"], "NORMAL"),
        (["nổi mề đay toàn thân", "viêm loét da", "nấm da lan rộng", "zona thần kinh"], "MEDIUM")
    ],
    "Khoa Nhi": [
        (["trẻ bị sốt nhẹ", "bé biếng ăn", "cháu bị trớ sữa", "trẻ ho đờm", "bé bị sổ mũi", "cháu nổi mẩn đỏ", "bé quấy khóc"], "NORMAL"),
        (["trẻ sốt cao", "bé bị tiêu chảy liên tục", "cháu khó thở", "bé lừ đừ không chịu chơi", "trẻ bị tay chân miệng"], "MEDIUM")
    ],
    "Khoa Sản - Phụ Khoa": [
        (["chậm kinh", "đau bụng kinh", "ngứa ngáy vùng kín", "ra khí hư bất thường", "khám thai định kỳ", "buồn nôn thai kỳ"], "NORMAL"),
        (["ra máu bất thường", "đau bụng dưới dữ dội", "động thai"], "EMERGENCY")
    ]
}

# Các câu hỏi FAQ
faqs = [
    "Bệnh viện có làm việc thứ 7, chủ nhật không?",
    "Cho tôi hỏi lịch khám của giáo sư A là khi nào?",
    "Khám BHYT có được hỗ trợ 100% không ạ?",
    "Tôi muốn đăng ký khám tổng quát thì gói nào tốt?",
    "Giá chụp MRI ở bệnh viện là bao nhiêu?",
    "Tôi muốn đặt lịch khám trước thì làm thế nào?",
    "Bệnh viện có khám ngoài giờ hành chính không?",
    "Thủ tục nhập viện gồm những giấy tờ gì?",
    "Xin hỏi bệnh viện có thanh toán bằng thẻ tín dụng không?",
    "Phòng khám nhi nằm ở tầng mấy vậy?",
    "Tôi bị mất thẻ BHYT thì dùng VssID được không?",
    "Lấy kết quả xét nghiệm máu mất bao lâu?"
]

# Các template câu nói tự nhiên
templates_1_symp = [
    "Bác sĩ ơi, tôi bị {symp1} mấy hôm nay rồi.",
    "Chào bác sĩ, dạo này tôi hay cảm thấy {symp1}.",
    "Cho tôi hỏi, tôi bị {symp1} thì nên khám khoa nào ạ?",
    "Tự nhiên hôm nay tôi bị {symp1}, khó chịu quá.",
    "Tôi bị {symp1} uống thuốc mãi không khỏi.",
    "Gần đây tôi bị {symp1} kéo dài.",
    "Bác sĩ cho em hỏi, em hay bị {symp1} là dấu hiệu của bệnh gì?",
    "Cháu nhà tôi bị {symp1} từ tối hôm qua.",
    "Tôi cảm thấy {symp1} liên tục trong nhiều ngày nay.",
    "{symp1} quá bác sĩ ơi, khám ở đâu được ạ?",
    "Dạ bác sĩ, em đang bị {symp1} thì phải làm sao ạ?",
    "Tôi bị {symp1}, mong bác sĩ tư vấn giúp."
]

templates_2_symp = [
    "Bác sĩ ơi, tôi bị {symp1}, thỉnh thoảng còn kèm theo {symp2}.",
    "Chào bác sĩ, dạo này tôi hay cảm thấy {symp1}, có lúc còn bị {symp2} nữa.",
    "Cháu nhà tôi bị {symp1} từ hôm qua, giờ thấy {symp2}.",
    "Tự nhiên hôm nay tôi bị {symp1} và {symp2}.",
    "Mấy hôm nay tôi vừa bị {symp1} vừa bị {symp2}, mệt mỏi quá.",
    "Tôi bị {symp1}, đồng thời dạo này còn hay bị {symp2}.",
    "Cho tôi hỏi tôi bị {symp1} với {symp2} thì khám khoa nào ạ?",
    "Bác sĩ ơi, em bị {symp1}, ngoài ra còn có dấu hiệu {symp2}.",
    "Tôi đang có triệu chứng {symp1}, và tôi cũng cảm thấy {symp2}."
]

urgent_templates = [
    "Cứu với, người nhà tôi bị {symp1}!!!",
    "Bác sĩ ơi cấp cứu, tôi bị {symp1} đau không chịu nổi.",
    "Tôi bị {symp1} kèm theo {symp2}, giờ người rất mệt, cấp cứu giúp tôi.",
    "Khẩn cấp, tôi đang bị {symp1}, phải làm sao đây?",
    "Bố tôi bị {symp1}, xin cho hỏi gọi cấp cứu số mấy?"
]

# Hàm random viết hoa đầu câu
def format_sentence(sentence):
    sentence = sentence.strip()
    return sentence[0].upper() + sentence[1:] if sentence else sentence

data = []

# Sinh dữ liệu triệu chứng
total_samples = 15000
target_per_dept = total_samples // len(symptoms_by_dept)

for dept, symptom_groups in symptoms_by_dept.items():
    for _ in range(target_per_dept):
        # Chọn ngẫu nhiên nhóm triệu chứng (EMERGENCY/MEDIUM/NORMAL)
        group, severity = random.choice(symptom_groups)
        
        # Quyết định chọn 1 hay 2 triệu chứng
        num_symps = random.choice([1, 2]) if len(group) > 1 else 1
        
        if num_symps == 1:
            symp = random.choice(group)
            if severity == "EMERGENCY" and random.random() < 0.4:
                template = random.choice(urgent_templates)
                sentence = template.format(symp1=symp, symp2="")
            else:
                template = random.choice(templates_1_symp)
                sentence = template.format(symp1=symp)
        else:
            symp1, symp2 = random.sample(group, 2)
            if severity == "EMERGENCY" and random.random() < 0.4:
                template = random.choice(urgent_templates)
                sentence = template.format(symp1=symp1, symp2=symp2)
            else:
                template = random.choice(templates_2_symp)
                sentence = template.format(symp1=symp1, symp2=symp2)
                
        # Loại bỏ các khoảng trắng kép nếu có
        sentence = " ".join(sentence.split())
        sentence = format_sentence(sentence)
        data.append([sentence, dept, "SYMPTOM", severity])

# Sinh dữ liệu FAQ (khoảng 3000 mẫu)
faq_templates = [
    "{faq}",
    "Bác sĩ cho hỏi, {faq}",
    "Dạ cho em hỏi: {faq}",
    "Admin cho mình hỏi {faq}",
    "Xin lỗi, {faq}",
    "{faq} Tư vấn giúp mình với."
]

for _ in range(3000):
    base_faq = random.choice(faqs)
    base_faq = base_faq.lower() if random.random() < 0.5 else base_faq
    template = random.choice(faq_templates)
    sentence = template.format(faq=base_faq)
    sentence = format_sentence(sentence)
    data.append([sentence, "FAQ", "FAQ", "NORMAL"])

# Xáo trộn dữ liệu
random.shuffle(data)

# Ghi ra file
output_file = "d:/DOANTRITUENHANTAO/train_dataset_v2.csv"
with open(output_file, mode="w", encoding="utf-8-sig", newline="") as f:
    writer = csv.writer(f)
    writer.writerow(["text", "label", "intent", "severity"])
    writer.writerows(data)

print(f"Đã tạo thành công {len(data)} dòng dữ liệu tại {output_file}")
