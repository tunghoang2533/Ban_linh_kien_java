"""Bước 11 – Tinh chỉnh cuối: bỏ bảng trùng, gộp nhóm endpoint đơn giản, siết cột bảng."""
import sys
sys.path.insert(0, 'tools')

import report_toolkit as rt
from report_toolkit import ReportEditor, Paragraph, set_cell_text

DOC = 'docs/BAO_CAO_DO_AN_PTPMHDV_rut_gon.docx'


def cut(text: str, limit: int) -> str:
    t = ' '.join(text.split())
    if len(t) <= limit:
        return text
    s = t[:limit]
    s = s[:s.rfind(' ')]
    return s.rstrip(' .,;:-–') + '…'


def main():
    e = ReportEditor(DOC)

    # 1. Bỏ khối mã entity Product ở §1.3.2 (giữ câu dẫn, thay bằng mô tả ngắn)
    idx = e.find('Cơ chế ánh xạ được minh họa qua entity Product')
    if idx:
        a = idx[0]
        b = e.find('Ba quyết định thiết kế quan trọng cần phân tích')[0]
        kills = [i for i in range(a + 1, b)
                 if isinstance(e.blocks[i], Paragraph) and e.blocks[i].style.name == 'Mã nguồn']
        if kills:
            e.set_para_text(e.blocks[a], [
                ("Ánh xạ được khai báo bằng chú thích JPA trên lớp entity: @Entity và @Table đặt tên "
                 "bảng, @Id với @GeneratedValue sinh khóa chính, @Column ràng buộc kiểu dữ liệu.",
                 None, None)])
            e.delete_blocks(kills)

    # 2. Bỏ khối mã SecurityFilterChain ở §1.3.3
    e.refresh()
    idx = e.find('Spring Security 6 loại bỏ lớp WebSecurityConfigurerAdapter')
    if idx:
        a = idx[0]
        b = e.find('b) Quy trình xác thực', start=a)[0]
        kills = [i for i in range(a + 1, b)
                 if isinstance(e.blocks[i], Paragraph) and e.blocks[i].style.name == 'Mã nguồn']
        if kills:
            e.delete_blocks(kills)

    # 3. Gộp hai nhóm endpoint đơn giản (Địa giới, Sản phẩm) thành một câu dẫn
    e.refresh()
    d = e.find('d) Nhóm tài nguyên Địa giới hành chính')
    f = e.find('f) Nhóm tài nguyên Thanh toán')
    if d and f and d[0] < f[0]:
        keep_from = f[0]
        kills = list(range(d[0], f[0]))
        e.new_paragraph('Normal',
                        "Hai nhóm tài nguyên Địa giới hành chính và Sản phẩm chỉ gồm ba endpoint GET "
                        "đơn giản (tỉnh/thành, quận/huyện và dữ liệu tóm tắt sản phẩm); các endpoint "
                        "này được liệt kê đầy đủ trong bảng tổng hợp toàn bộ hợp đồng dịch vụ ở mục e) "
                        "dưới đây.",
                        e.blocks[keep_from])
        e.refresh()
        e.delete_blocks([i for i in kills if i < len(e.blocks)])
        # đổi số thứ tự tiêu đề còn lại
        for old, new in (('f) Nhóm tài nguyên Thanh toán', 'd) Nhóm tài nguyên Thanh toán'),
                         ('g) Bảng tổng hợp toàn bộ hợp đồng dịch vụ', 'e) Bảng tổng hợp toàn bộ hợp đồng dịch vụ')):
            j = e.find(old)
            if j:
                e.set_para_text(e.blocks[j[0]], [(new, None, None)])

    # 4. Bỏ bảng danh mục lớp kiểm thử (trùng nội dung với hai bảng kết quả UT/IT)
    e.refresh()
    idx = e.find('Danh mục lớp kiểm thử tự động')
    if idx:
        i = idx[0]
        kill = [i]
        nxt = e.blocks[i + 1] if i + 1 < len(e.blocks) else None
        if not isinstance(nxt, Paragraph):
            kill.append(i + 1)
            nxt2 = e.blocks[i + 2] if i + 2 < len(e.blocks) else None
            if isinstance(nxt2, Paragraph) and nxt2.text.strip().startswith('Bảng 3.11'):
                kill.append(i + 2)
        e.delete_blocks(kill)
    e.refresh()
    idx = e.find('b) Danh mục lớp kiểm thử', exact=False)
    if idx:
        nxt = e.blocks[idx[0] + 1] if idx[0] + 1 < len(e.blocks) else None
        if not isinstance(nxt, Paragraph) or 'Đối tượng kiểm chứng' in nxt.text or not nxt.text.strip():
            e.set_para_text(e.blocks[idx[0]], [
                ("b) Kết quả theo lớp kiểm thử. Danh mục lớp kiểm thử và số ca của từng lớp được "
                 "trình bày trực tiếp trong hai bảng kết quả dưới đây.", None, None)])

    # 5. Siết thêm độ dài ô bảng
    e.refresh()
    RULES = [
        (['Từ viết tắt'], {2: 42}),
        (['Thành phần kiến trúc'], {3: 32, 4: 34}),
        (['Mã', 'Thuộc tính chất lượng'], {2: 46, 3: 46}),
        (['Mã', 'Tên quy tắc'], {2: 50, 3: 34}),
        (['Mã', 'Tên yêu cầu'], {2: 48}),
        (['Mã', 'Tên Use Case'], {4: 65}),
        (['Tên trường', 'Kiểu dữ liệu'], {4: 26}),
        (['Hạng mục quy ước'], {1: 40, 2: 30}),
        (['STT', 'Màn hình', 'Chức năng client'], {2: 22, 3: 16}),
        (['Mã', 'Lớp kiểm thử', 'Tên ca kiểm thử'], {2: 34}),
        (['Test ID'], {1: 26, 2: 34}),
        (['Nhóm yêu cầu'], {1: 22, 2: 16, 3: 34}),
        (['Mã', 'Hạn chế'], {1: 28, 2: 44, 3: 40, 4: 42}),
        (['Hạng mục', 'Công cụ'], {1: 28, 3: 38}),
        (['Nhóm', 'Khóa cấu hình'], {1: 26, 2: 28, 3: 34}),
        (['Tên ràng buộc'], {1: 28, 3: 30}),
    ]
    for b in e.blocks:
        if isinstance(b, Paragraph) or not b.rows:
            continue
        head = [c.text.strip() for c in b.rows[0].cells]
        for sig, rules in RULES:
            if head[:len(sig)] == sig:
                for ci, limit in rules.items():
                    if ci >= len(b.columns):
                        continue
                    for row in b.rows[1:]:
                        cell = row.cells[ci]
                        txt = cell.text.strip()
                        if len(txt) > limit:
                            set_cell_text(cell, cut(txt, limit))
                break
    e.save()
    print("Đã tinh chỉnh cuối.")


if __name__ == '__main__':
    main()
