"""Bộ công cụ đọc – sửa – ước lượng số trang cho báo cáo đồ án (.docx).

Mục đích: hỗ trợ rút gọn báo cáo Word mà vẫn giữ nguyên hệ thống định dạng
(style, mục lục tự động, danh mục hình vẽ/bảng biểu, khung chèn sơ đồ).
"""

from __future__ import annotations

import math
import re
from copy import deepcopy

import docx
from docx.oxml.ns import qn
from docx.table import Table
from docx.text.paragraph import Paragraph

# ---------------------------------------------------------------------------
# Hằng số bố cục trang (A4, lề 30/20/20/20 mm theo convert_report.py)
# ---------------------------------------------------------------------------
PAGE_W_PT = (210 - 30 - 20) / 25.4 * 72      # bề rộng vùng chữ  ≈ 453,5 pt
PAGE_H_PT = (297 - 20 - 20) / 25.4 * 72      # chiều cao vùng chữ ≈ 728,5 pt

CHAR_W_FACTOR = 0.505      # bề rộng trung bình 1 ký tự = factor × cỡ chữ
LINE_FACTOR = 1.18         # chiều cao 1 dòng        = factor × cỡ chữ
CELL_PAD_PT = 8.0          # tổng padding trên+dưới của một ô bảng
TABLE_EXTRA_PT = 2.0       # viền/khoảng cộng thêm cho mỗi hàng bảng


# ---------------------------------------------------------------------------
# Tiện ích XML
# ---------------------------------------------------------------------------
def iter_block_items(parent):
    """Sinh ra lần lượt Paragraph và Table theo đúng thứ tự trong body."""
    body = parent.element.body if hasattr(parent, "element") else parent._element
    for child in body.iterchildren():
        if child.tag == qn("w:p"):
            yield Paragraph(child, parent)
        elif child.tag == qn("w:tbl"):
            yield Table(child, parent)


def para_style_size_pt(p: Paragraph) -> float:
    """Cỡ chữ hiệu dụng của đoạn văn (pt)."""
    sizes = []
    for r in p.runs:
        sz = r._element.find(qn("w:rPr"))
        if sz is not None:
            s = sz.find(qn("w:sz"))
            if s is not None:
                sizes.append(int(s.get(qn("w:val"))) / 2)
    if sizes:
        return max(sizes)
    elem = p._p.find(qn("w:pPr"))
    if elem is not None:
        rpr = elem.find(qn("w:rPr"))
        if rpr is not None:
            s = rpr.find(qn("w:sz"))
            if s is not None:
                return int(s.get(qn("w:val"))) / 2
    style_sz = {
        "Heading 1": 14, "Heading 2": 14, "Heading 3": 13, "Heading 4": 13,
        "Title": 22, "caption": 12, "toc 1": 13, "toc 2": 13, "toc 3": 13,
        "table of figures": 12, "Mã nguồn": 9.5, "Mã nhúng": 9.5,
        "Mã nhúng (trong bảng)": 9.5, "Bảng - nội dung": 12,
        "Bảng - nội dung (nhỏ)": 11, "Bảng - tiêu đề cột": 12,
        "Bảng - tiêu đề cột (nhỏ)": 11,
    }
    return style_sz.get(p.style.name, 13.0)


def para_spacing_pt(p: Paragraph) -> tuple[float, float, float]:
    """(space_before, space_after, line_multiple) theo pt/hệ số."""
    pf = p.paragraph_format
    before = pf.space_before.pt if pf.space_before is not None else 0.0
    after = pf.space_after.pt if pf.space_after is not None else 0.0
    line = pf.line_spacing if isinstance(pf.line_spacing, float) else None
    if line is None:
        line = {
            "Heading 1": 1.15, "Heading 2": 1.15, "Heading 3": 1.15,
            "Heading 4": 1.15, "caption": 1.1, "Hình (khung chèn ảnh)": 1.0,
            "Mã nguồn": 1.0,
        }.get(p.style.name, 1.3)
    # giá trị mặc định của style đã nằm trong XML nên vẫn còn hiệu lực
    return before, after, line


def para_height_pt(p: Paragraph) -> float:
    text = p.text
    size = para_style_size_pt(p)
    before, after, line = para_spacing_pt(p)
    n_breaks = p._p.xml.count("<w:br/>") + p._p.xml.count("<w:br />")
    if not text.strip() and n_breaks == 0:
        # đoạn trống: chỉ tính khoảng cách
        return max(before + after, size * line * 0.9)
    cpl = max(PAGE_W_PT / (CHAR_W_FACTOR * size), 10)
    lines = max(1, math.ceil(len(text) / cpl)) + n_breaks
    return before + after + lines * size * LINE_FACTOR * line


def col_widths_pt(tbl: Table) -> list[float]:
    grid = tbl._tbl.find(qn("w:tblGrid"))
    widths = []
    if grid is not None:
        for gc in grid.findall(qn("w:gridCol")):
            w = gc.get(qn("w:w"))
            if w:
                widths.append(int(w) / 20)
    if not widths:
        n = len(tbl.columns)
        widths = [PAGE_W_PT / n] * n
    total = sum(widths)
    if total > PAGE_W_PT:          # bảng rộng hơn vùng chữ → co lại
        widths = [w * PAGE_W_PT / total for w in widths]
    return widths


def table_height_pt(tbl: Table) -> float:
    widths = col_widths_pt(tbl)
    height = 0.0
    for row in tbl.rows:
        row_h = 0.0
        cells = row.cells
        for idx, cell in enumerate(cells):
            w = widths[idx] if idx < len(widths) else PAGE_W_PT / max(len(cells), 1)
            if w <= 0:
                w = 40
            cell_txt = cell.text
            size = 12.0
            for p in cell.paragraphs:
                if p.text.strip():
                    size = para_style_size_pt(p)
                    break
            cpl = max((w - 6) / (CHAR_W_FACTOR * size), 4)
            n_lines = 0
            for p in cell.paragraphs:
                lines = max(1, math.ceil(len(p.text) / cpl)) if p.text.strip() else 0
                n_lines += lines
                # khoảng cách giữa các đoạn trong ô
            n_lines = max(n_lines, 1)
            row_h = max(row_h, n_lines * size * LINE_FACTOR + CELL_PAD_PT)
        height += row_h + TABLE_EXTRA_PT
    return height


def estimate_pages(doc, verbose: bool = False):
    """Ước lượng số trang và trả về cả thông kê chi tiết."""
    filled = 0.0
    pages = 0
    items = []
    for block in iter_block_items(doc):
        if isinstance(block, Paragraph):
            if 'w:br w:type="page"' in block._p.xml or 'w:type="page"' in block._p.xml:
                pages += 1
                filled = 0.0
            h = para_height_pt(block)
            label = f"P:{block.style.name}:{block.text[:40]}"
        else:
            h = table_height_pt(block)
            label = f"T:{len(block.rows)}x{len(block.columns)}"
        items.append((label, h))
        filled += h
        while filled >= PAGE_H_PT:
            pages += 1
            filled -= PAGE_H_PT
    if filled > PAGE_H_PT * 0.12:
        pages += 1
    if verbose:
        return pages, items
    return pages


# ---------------------------------------------------------------------------
# Sửa nội dung
# ---------------------------------------------------------------------------
class ReportEditor:
    def __init__(self, path: str):
        self.path = path
        self.doc = docx.Document(path)
        self.refresh()

    def refresh(self):
        self.blocks = list(iter_block_items(self.doc))
        self.by_index = {i: b for i, b in enumerate(self.blocks)}

    # -- tìm kiếm -----------------------------------------------------------
    def find(self, text: str, exact: bool = False, start: int = 0):
        out = []
        for i, b in enumerate(self.blocks):
            if i < start:
                continue
            if isinstance(b, Paragraph):
                t = b.text.strip()
                if (t == text) if exact else (text in t):
                    out.append(i)
        return out

    def heading_index(self, text: str, start: int = 0) -> int:
        for i in self.find(text, start=start):
            b = self.blocks[i]
            if isinstance(b, Paragraph) and b.style.name.startswith("Heading"):
                return i
        raise KeyError(text)

    # -- xoá ---------------------------------------------------------------
    def delete_range(self, start: int, end: int):
        for i in range(start, end + 1):
            b = self.blocks[i]
            el = b._p if isinstance(b, Paragraph) else b._tbl
            el.getparent().remove(el)
        self.refresh()

    def delete_blocks(self, indices):
        for i in sorted(set(indices), reverse=True):
            b = self.blocks[i]
            el = b._p if isinstance(b, Paragraph) else b._tbl
            el.getparent().remove(el)
        self.refresh()

    # -- tạo đoạn văn ------------------------------------------------------
    @staticmethod
    def _template_run_rpr(p: Paragraph):
        for r in p.runs:
            rpr = r._element.find(qn("w:rPr"))
            if rpr is not None:
                return deepcopy(rpr)
        return None

    def set_para_text(self, p: Paragraph, segments, template_from=None):
        """Ghi lại nội dung đoạn văn. segments: str | [(text, bold, italic)]"""
        if isinstance(segments, str):
            segments = [(segments, None, None)]
        src = template_from if template_from is not None else p
        rpr_tpl = ReportEditor._template_run_rpr(src)
        # xoá toàn bộ run cũ, giữ pPr và bookmark
        for child in list(p._p):
            if child.tag != qn("w:pPr") and child.tag != qn("w:bookmarkStart") \
                    and child.tag != qn("w:bookmarkEnd"):
                p._p.remove(child)
        for text, bold, italic in segments:
            r = p.add_run(text)
            rpr = deepcopy(rpr_tpl) if rpr_tpl is not None else None
            if rpr is None:
                continue
            for tag in ("w:b", "w:i"):
                el = rpr.find(qn(tag))
                if el is not None:
                    rpr.remove(el)
            if bold is True:
                rpr.append(rpr.makeelement(qn("w:b"), {}))
            if italic is True:
                rpr.append(rpr.makeelement(qn("w:i"), {}))
            r._element.insert(0, rpr)
        return p

    def clone_paragraph(self, src: Paragraph, text, dest_after, style: str | None = None):
        """Sao chép một đoạn văn mẫu rồi chèn vào sau đoạn `dest_after`."""
        new_el = deepcopy(src._p)
        # xoá bookmark trong bản sao để tránh trùng id
        for tag in ("w:bookmarkStart", "w:bookmarkEnd"):
            for bm in new_el.findall(qn(tag)):
                new_el.remove(bm)
        dest_after._p.addnext(new_el)
        p = Paragraph(new_el, dest_after._parent)
        if style:
            p.style = self.doc.styles[style]
        self.set_para_text(p, text)
        return p

    def new_paragraph(self, style_name: str, text, dest_after):
        """Tạo đoạn văn mới theo style có sẵn (lấy mẫu từ chính tài liệu)."""
        if isinstance(dest_after, int):
            dest_after = self.blocks[dest_after]
        src = None
        for b in self.blocks:
            if isinstance(b, Paragraph) and b.style.name == style_name and b.text.strip():
                src = b
                break
        if src is None:
            src = next(b for b in self.blocks
                       if isinstance(b, Paragraph) and b.style.name == "Normal")
        p = self.clone_paragraph(src, text, dest_after, style=style_name)
        self.refresh()
        return p

    # -- bảng --------------------------------------------------------------
    def clone_table_row(self, tbl: Table, src_row_idx: int, values, bold=False):
        new_tr = deepcopy(tbl.rows[src_row_idx]._tr)
        tbl._tbl.append(new_tr)
        row = tbl.rows[-1]
        for i, val in enumerate(values):
            if i >= len(row.cells):
                break
            cell = row.cells[i]
            segs = val if isinstance(val, list) else [(val, bold or None, None)]
            self.set_para_text(cell.paragraphs[0], segs)
            for extra in cell.paragraphs[1:]:
                extra._p.getparent().remove(extra._p)
        return row

    def make_table(self, dest_after, template: Table, data, header=True):
        """Tạo bảng mới dựa trên bảng mẫu (giữ định dạng ô/viền)."""
        new_el = deepcopy(template._tbl)
        # xoá hết hàng cũ
        for tr in new_el.findall(qn("w:tr")):
            new_el.remove(tr)
        dest = dest_after._p if isinstance(dest_after, Paragraph) else dest_after._tbl
        dest.addnext(new_el)
        tbl = Table(new_el, dest_after._parent)
        ncols = len(template.columns)
        # dựng hàng theo mẫu: hàng 0 = tiêu đề, hàng 1 = nội dung
        tmpl_header = deepcopy(template.rows[0]._tr)
        tmpl_body = deepcopy(template.rows[min(1, len(template.rows) - 1)]._tr)
        for r_idx, row_vals in enumerate(data):
            tr = deepcopy(tmpl_header if (header and r_idx == 0) else tmpl_body)
            new_el.append(tr)
        self.refresh()
        tbl = Table(new_el, dest_after._parent)
        # co giãn số cột nếu cần
        for r_idx, row_vals in enumerate(data):
            row = tbl.rows[r_idx]
            n = len(row.cells)
            for c_idx in range(n):
                val = row_vals[c_idx] if c_idx < len(row_vals) else ""
                segs = val if isinstance(val, list) else [(val, None, None)]
                self.set_para_text(row.cells[c_idx].paragraphs[0], segs)
                for extra in row.cells[c_idx].paragraphs[1:]:
                    extra._p.getparent().remove(extra._p)
        return tbl

    def save(self, path: str | None = None):
        self.doc.save(path or self.path)


# ---------------------------------------------------------------------------
# Tiện ích bổ sung cho việc rút gọn
# ---------------------------------------------------------------------------
def drop_columns(tbl, idxs, ed=None):
    """Xoá các cột theo chỉ số (đồng thời cập nhật tblGrid)."""
    grid = tbl._tbl.find(qn('w:tblGrid'))
    if grid is not None:
        cols = grid.findall(qn('w:gridCol'))
        for i in sorted(idxs, reverse=True):
            if i < len(cols):
                grid.remove(cols[i])
    for row in tbl._tbl.findall(qn('w:tr')):
        tcs = [c for c in row.findall(qn('w:tc'))]
        for i in sorted(idxs, reverse=True):
            if i < len(tcs):
                row.remove(tcs[i])


def resize_columns(tbl, n):
    """Bảo đảm mỗi hàng có đúng n ô (thêm bằng cách nhân bản ô cuối)."""
    grid = tbl._tbl.find(qn('w:tblGrid'))
    if grid is not None:
        cols = grid.findall(qn('w:gridCol'))
        if cols:
            while len(grid.findall(qn('w:gridCol'))) > n:
                grid.remove(grid.findall(qn('w:gridCol'))[-1])
            while len(grid.findall(qn('w:gridCol'))) < n:
                grid.append(deepcopy(grid.findall(qn('w:gridCol'))[-1]))
    for row in tbl._tbl.findall(qn('w:tr')):
        tcs = row.findall(qn('w:tc'))
        if not tcs:
            continue
        while len(row.findall(qn('w:tc'))) > n:
            row.remove(row.findall(qn('w:tc'))[-1])
        while len(row.findall(qn('w:tc'))) < n:
            row.append(deepcopy(tcs[-1]))


def set_cell_text(cell, text):
    """Ghi nội dung một ô bảng, giữ định dạng của đoạn đầu tiên."""
    p = cell.paragraphs[0]
    ReportEditor.set_para_text(None, p, text)
    for extra in cell.paragraphs[1:]:
        extra._p.getparent().remove(extra._p)


def set_cell_segments(cell, segments):
    p = cell.paragraphs[0]
    ReportEditor.set_para_text(None, p, segments)
    for extra in cell.paragraphs[1:]:
        extra._p.getparent().remove(extra._p)


def trim_steps(text, keep):
    """Giữ lại `keep` bước đầu của một ô có các bước ngăn cách bằng <br/>."""
    parts = [p for p in text.split('<br/>')]
    if len(parts) <= keep:
        return text
    return '<br/>'.join(parts[:keep])


def edit_caption(p, new_tail):
    """Thay phần chữ sau trường SEQ của một đoạn caption (giữ nguyên trường)."""
    runs = p.runs
    for r in reversed(runs):
        el = r._element
        if el.find(qn('w:instrText')) is None and el.find(qn('w:fldChar')) is None:
            if r.text.strip():
                r.text = new_tail
                return True
    return False


def fit_table_width(tbl, total=None):
    """Giãn các cột còn lại cho vừa bề rộng vùng chữ sau khi bỏ bớt cột.

    `total` tính bằng twips (1 pt = 20 twips).
    """
    total = int((total or PAGE_W_PT) * 20) if (total is None or total < 2000) else total
    grid = tbl._tbl.find(qn('w:tblGrid'))
    if grid is None:
        return
    cols = grid.findall(qn('w:gridCol'))
    if not cols:
        return
    widths = [int(c.get(qn('w:w')) or 0) for c in cols]
    s = sum(widths) or 1
    for c, w in zip(cols, widths):
        c.set(qn('w:w'), str(int(w * total / s)))
    for tr in tbl._tbl.findall(qn('w:tr')):
        tcs = tr.findall(qn('w:tc'))
        for tc, c in zip(tcs, cols):
            tcPr = tc.find(qn('w:tcPr'))
            if tcPr is None:
                from docx.oxml import OxmlElement
                tcPr = OxmlElement('w:tcPr')
                tc.insert(0, tcPr)
            tcW = tcPr.find(qn('w:tcW'))
            if tcW is None:
                from docx.oxml import OxmlElement
                tcW = OxmlElement('w:tcW')
                tcPr.append(tcW)
            tcW.set(qn('w:w'), c.get(qn('w:w')))
            tcW.set(qn('w:type'), 'dxa')


def set_col_fractions(tbl, fracs, total=None):
    """Đặt bề rộng cột theo tỉ lệ cho trước (tổng = 1)."""
    total = int((total or PAGE_W_PT) * 20) if (total is None or total < 2000) else total
    grid = tbl._tbl.find(qn('w:tblGrid'))
    if grid is None:
        return
    cols = grid.findall(qn('w:gridCol'))
    for c, fr in zip(cols, fracs):
        c.set(qn('w:w'), str(int(total * fr)))
    for tr in tbl._tbl.findall(qn('w:tr')):
        for tc, c in zip(tr.findall(qn('w:tc')), cols):
            tcPr = tc.find(qn('w:tcPr'))
            if tcPr is None:
                continue
            tcW = tcPr.find(qn('w:tcW'))
            if tcW is not None:
                tcW.set(qn('w:w'), c.get(qn('w:w')))
                tcW.set(qn('w:type'), 'dxa')
