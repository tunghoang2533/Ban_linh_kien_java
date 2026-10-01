"""Bước 6 – Rút gọn mục 2.4.2 (thiết kế cơ sở dữ liệu) của Chương 2."""
import sys
sys.path.insert(0, 'tools')
from copy import deepcopy

import report_toolkit as rt
from report_toolkit import (ReportEditor, Paragraph, set_cell_text, drop_columns,
                            resize_columns, fit_table_width, trim_steps, edit_caption,
                            table_height_pt, PAGE_W_PT)

DOC = 'docs/BAO_CAO_DO_AN_PTPMHDV_rut_gon.docx'

KEEP_FULL = ['users', 'products', 'orders', 'order_items', 'vouchers', 'warehouse_logs']

COMPACT_NOTE = {
    'roles': 'Bảng vai trò dùng cho phân quyền mở rộng; hiện hệ thống chủ yếu dựa trên cột '
             'users.role và cờ is_admin.',
    'user_addresses': 'Sổ địa chỉ giao hàng của khách hàng; xóa người dùng thì xóa theo (CASCADE).',
    'categories': 'Danh mục linh kiện; slug dùng cho URL thân thiện, xóa thì sản phẩm chuyển về '
                  'NULL (SET NULL).',
    'brands': 'Thương hiệu sản phẩm; quan hệ nhiều-một với products.',
    'product_specs': 'Thông số kỹ thuật dạng khóa – giá trị (mô hình EAV) phục vụ công cụ Build PC '
                     'và bộ lọc chuyên sâu.',
    'product_comments': 'Bình luận kèm điểm đánh giá 1–5 sao; có cờ is_hidden cho kiểm duyệt.',
    'order_status_history': 'Nhật ký chuyển trạng thái đơn hàng, phục vụ truy vết trách nhiệm.',
    'payment_transactions': 'Giao dịch thanh toán theo từng lần thử, lưu mã phản hồi của cổng '
                            'thanh toán để đối soát.',
    'voucher_usages': 'Lịch sử sử dụng mã; ràng buộc duy nhất (voucher_id, user_id) là chốt chặn '
                      'chống dùng lại mã.',
    'shipping_zones': 'Vùng vận chuyển với phí cơ bản và ngưỡng miễn phí; danh sách tỉnh lưu dạng '
                      'mảng JSON trong cột TEXT (xem nhận xét thiết kế bên dưới).',
    'shop_settings': 'Cấu hình cửa hàng dạng khóa – giá trị, cho phép đổi tham số vận hành không '
                     'cần sửa mã nguồn.',
}


def prev_caption(e, idx):
    for j in range(idx - 1, max(0, idx - 4), -1):
        b = e.blocks[j]
        if isinstance(b, Paragraph) and b.style.name == 'Caption':
            return j
    return None


def table_name_from_caption(txt):
    if 'bảng ' in txt:
        return txt.split('bảng ')[-1].strip()
    return None


def main():
    e = ReportEditor(DOC)

    # ------------------------------------------------------------------ thu thập
    dict_tables = []
    for i, b in enumerate(e.blocks):
        if isinstance(b, Paragraph):
            continue
        if b.rows and b.rows[0].cells[0].text.strip() == 'Tên trường':
            cap = prev_caption(e, i)
            name = table_name_from_caption(e.blocks[cap].text) if cap is not None else None
            dict_tables.append((i, cap, name, b))

    compact = [(i, c, n, t) for (i, c, n, t) in dict_tables if n not in KEEP_FULL]
    keep = [(i, c, n, t) for (i, c, n, t) in dict_tables if n in KEEP_FULL]

    # dữ liệu cho bảng tổng hợp các bảng còn lại
    rows = [["Bảng", "Trường và kiểu dữ liệu", "Ràng buộc và ghi chú chính"]]
    for _, _, name, tbl in compact:
        fields = []
        for r in tbl.rows[1:]:
            cells = [c.text.strip() for c in r.cells]
            fname, ftype = cells[0], cells[1]
            if not fname:
                continue
            fields.append(f"{fname} {ftype}".strip())
        rows.append([name, "; ".join(fields), COMPACT_NOTE.get(name, '')])

    # ------------------------------------------------------------------ chèn bảng tổng hợp
    first_idx, first_cap, _, _ = compact[0]
    cap_src = e.blocks[first_cap]
    tbl_src = e.blocks[first_idx]

    lead = e.new_paragraph('Normal', [
        ("Sáu bảng lõi (users, products, orders, order_items, vouchers, warehouse_logs) được đặc tả "
         "chi tiết đến từng trường trong các bảng 2.19 – 2.24. Các bảng còn lại được liệt kê đầy đủ "
         "trường và kiểu dữ liệu trong bảng tổng hợp dưới đây.", None, None),
    ], cap_src)
    e.refresh()

    new_cap = deepcopy(cap_src._p)
    cap_src._p.addprevious(new_cap)
    e.refresh()
    cap_par = Paragraph(new_cap, cap_src._parent)
    edit_caption(cap_par, ' – Từ điển dữ liệu tổng hợp các bảng còn lại')

    new_tbl = deepcopy(tbl_src._tbl)
    cap_par._p.addnext(new_tbl)
    e.refresh()
    from docx.table import Table
    comp = Table(new_tbl, cap_par._parent)
    body_tr = deepcopy(comp.rows[1]._tr)
    while len(comp.rows) > 1:
        comp._tbl.remove(comp.rows[-1]._tr)
    for _ in rows:
        comp._tbl.append(deepcopy(body_tr))
    resize_columns(comp, 3)
    for r_idx, vals in enumerate(rows):
        for c_idx, val in enumerate(vals):
            set_cell_text(comp.rows[r_idx].cells[c_idx], val)
    # bề rộng hợp lý: Bảng 12%, Trường 48%, Ghi chú 40%
    grid = comp._tbl.find(rt.qn('w:tblGrid'))
    cols = grid.findall(rt.qn('w:gridCol'))
    fracs = [0.12, 0.48, 0.40]
    for c, fr in zip(cols, fracs):
        c.set(rt.qn('w:w'), str(int(PAGE_W_PT * 20 * fr)))
    for tr in comp._tbl.findall(rt.qn('w:tr')):
        for tc, c in zip(tr.findall(rt.qn('w:tc')), cols):
            tcPr = tc.find(rt.qn('w:tcPr'))
            if tcPr is not None:
                tcW = tcPr.find(rt.qn('w:tcW'))
                if tcW is not None:
                    tcW.set(rt.qn('w:w'), c.get(rt.qn('w:w')))
                    tcW.set(rt.qn('w:type'), 'dxa')

    # ------------------------------------------------------------------ xoá 11 bảng chi tiết
    for _, cap_i, name, _ in compact:
        e.refresh()
        cap2 = None
        for j, b in enumerate(e.blocks):
            if isinstance(b, Paragraph) and b.style.name == 'Caption' and \
                    b.text.strip().endswith('bảng ' + name):
                cap2 = j
                break
        if cap2 is None:
            continue
        tbl_j = cap2 + 1
        while tbl_j < len(e.blocks) and isinstance(e.blocks[tbl_j], Paragraph):
            tbl_j += 1
        end = tbl_j
        for j in range(tbl_j + 1, min(tbl_j + 4, len(e.blocks))):
            b = e.blocks[j]
            if isinstance(b, Paragraph) and b.style.name == 'Ghi chú (không thụt đầu dòng)':
                end = j
            else:
                break
        e.delete_blocks(list(range(cap2, end + 1)))
    e.refresh()

    # ------------------------------------------------------------------ thu gọn bảng chi tiết
    for i, b in enumerate(e.blocks):
        if isinstance(b, Paragraph):
            continue
        if b.rows and b.rows[0].cells[0].text.strip() == 'Tên trường':
            drop_columns(b, [3])           # bỏ cột Null
            fit_table_width(b)
            for row in b.rows[1:]:
                cell = row.cells[-1]
                txt = cell.text.strip()
                if len(txt) > 120:
                    cut = txt[:120]
                    cut = cut[:cut.rfind(' ')]
                    set_cell_text(cell, cut.rstrip('.,;') + '…')

    # ------------------------------------------------------------------ bảng danh mục bảng dữ liệu
    for b in e.blocks:
        if isinstance(b, Paragraph):
            continue
        if b.rows and b.rows[0].cells[0].text.strip() == 'STT' and \
                'Tên bảng' in [c.text.strip() for c in b.rows[0].cells]:
            drop_columns(b, [5, 3, 2])     # bỏ Dịch vụ sử dụng, Số cột, Nhóm chức năng
            fit_table_width(b, total=7000)
            for row in b.rows[1:]:
                cell = row.cells[-1]
                txt = cell.text.strip()
                if len(txt) > 110:
                    cut = txt[:110]
                    cut = cut[:cut.rfind(' ')]
                    set_cell_text(cell, cut.rstrip('.,;') + '…')

    # ------------------------------------------------------------------ ma trận khóa ngoại
    for b in e.blocks:
        if isinstance(b, Paragraph):
            continue
        head = [c.text.strip() for c in b.rows[0].cells] if b.rows else []
        if head[:2] == ['STT', 'Tên ràng buộc'] and 'Bảng con' in head:
            for row in b.rows:
                cells = [c.text.strip() for c in row.cells]
                if len(cells) >= 8:
                    merged = f"{cells[2]}.{cells[3]} → {cells[4]}.{cells[5]}"
                    set_cell_text(row.cells[2], merged)
            drop_columns(b, [5, 4, 3, 0])   # giữ Tên ràng buộc | Quan hệ | Hành vi | Lý do
            fit_table_width(b)
            for row in b.rows[1:]:
                cell = row.cells[-1]
                txt = cell.text.strip()
                if len(txt) > 110:
                    cut = txt[:110]
                    cut = cut[:cut.rfind(' ')]
                    set_cell_text(cell, cut.rstrip('.,;') + '…')

    # ------------------------------------------------------------------ máy trạng thái đơn hàng
    for i, b in enumerate(e.blocks):
        if isinstance(b, Paragraph) and b.text.strip().startswith('Trạng thái khởi tạo là duy nhất'):
            e.set_para_text(b, [
                ("Trạng thái khởi tạo là duy nhất. ", True, None),
                ("Mọi đơn hàng đều bắt đầu ở pending; đơn thanh toán trực tuyến chuyển sang paid "
                 "khi cổng thanh toán xác nhận, còn đơn COD hoặc chuyển khoản chuyển sang paid do "
                 "quản trị viên xác nhận.", None, None),
            ])
        if isinstance(b, Paragraph) and b.text.strip().startswith('Hai trạng thái cuối'):
            e.set_para_text(b, [
                ("Hai trạng thái cuối, trong đó một trạng thái tuyệt đối bất biến. ", True, None),
                ("completed và cancelled đều là trạng thái kết thúc; riêng cancelled không thể "
                 "chuyển sang bất kỳ trạng thái nào khác (quy tắc BR-18).", None, None),
            ])
        if isinstance(b, Paragraph) and b.text.strip().startswith('Hai phép chuyển bị chặn'):
            e.set_para_text(b, [
                ("Hai phép chuyển bị chặn tường minh. ", True, None),
                ("shipped → cancelled và completed → cancelled bị từ chối nhằm bảo vệ hàng hóa đã "
                 "xuất kho hoặc đơn đã hoàn tất (quy tắc BR-19).", None, None),
            ])
        if isinstance(b, Paragraph) and b.text.strip().startswith('Về sự tách biệt giữa hai trục'):
            e.set_para_text(b, [
                ("Hai trục trạng thái độc lập. ", True, None),
                ("status mô tả tiến trình xử lý đơn, payment_status mô tả trạng thái thanh toán; "
                 "khi hủy một đơn đã thanh toán, payment_status tự chuyển thành refunded để dữ "
                 "liệu kế toán nhất quán.", None, None),
            ])

    e.save()
    print("Đã rút gọn 2.4.2.")


if __name__ == '__main__':
    main()
