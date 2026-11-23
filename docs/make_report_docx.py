import re
from pathlib import Path

from docx import Document
from docx.shared import Pt, Inches
from docx.enum.text import WD_ALIGN_PARAGRAPH

BASE = Path(r"C:\Users\rajan\OneDrive\Documents\deepseek-harness\default-workspace\student-attendance-grade-tracker")
MD = BASE / "docs" / "ProjectReport.md"
OUT = BASE / "docs" / "ProjectReport.docx"

doc = Document()

normal = doc.styles["Normal"]
normal.font.name = "Calibri"
normal.font.size = Pt(11)

lines = MD.read_text(encoding="utf-8").splitlines()

INLINE = re.compile(r"(\*\*.*?\*\*|`[^`]*`)")
SEP_CELL = re.compile(r":?-{2,}:?")


def add_inline(paragraph, text):
    for token in INLINE.split(text):
        if not token:
            continue
        if token.startswith("**") and token.endswith("**"):
            run = paragraph.add_run(token[2:-2])
            run.bold = True
        elif token.startswith("`") and token.endswith("`"):
            run = paragraph.add_run(token[1:-1])
            run.font.name = "Consolas"
            run.font.size = Pt(10)
        else:
            paragraph.add_run(token)


def add_code_block(code_lines):
    for cl in code_lines:
        p = doc.add_paragraph()
        p.paragraph_format.left_indent = Inches(0.25)
        p.paragraph_format.space_after = Pt(0)
        run = p.add_run(cl if cl else " ")
        run.font.name = "Consolas"
        run.font.size = Pt(8.5)


i = 0
in_code = False
code_buf = []
while i < len(lines):
    line = lines[i]

    if line.strip().startswith("```"):
        if in_code:
            add_code_block(code_buf)
            code_buf = []
            in_code = False
        else:
            in_code = True
        i += 1
        continue

    if in_code:
        code_buf.append(line)
        i += 1
        continue

    if line.strip() == "---":
        i += 1
        continue

    if line.startswith("|"):
        tbl = []
        while i < len(lines) and lines[i].startswith("|"):
            tbl.append(lines[i])
            i += 1
        rows = [[c.strip() for c in r.strip().strip("|").split("|")] for r in tbl]
        data = [r for r in rows if not all(SEP_CELL.fullmatch(c) for c in r)]
        if data:
            ncols = max(len(r) for r in data)
            table = doc.add_table(rows=len(data), cols=ncols)
            table.style = "Table Grid"
            for ri, r in enumerate(data):
                for ci in range(ncols):
                    cell = table.cell(ri, ci)
                    cell.text = ""
                    add_inline(cell.paragraphs[0], r[ci] if ci < len(r) else "")
                    if ri == 0:
                        for run in cell.paragraphs[0].runs:
                            run.bold = True
        continue

    if line.startswith("# "):
        doc.add_heading(line[2:], level=0)
        i += 1
        continue

    m = re.match(r"(#{2,6}) (.*)", line)
    if m:
        doc.add_heading(m.group(2), level=len(m.group(1)) - 1)
        i += 1
        continue

    m = re.match(r"!\[.*?\]\((.*?)\)", line)
    if m:
        path = (MD.parent / m.group(1)).resolve()
        if path.exists():
            p = doc.add_paragraph()
            p.alignment = WD_ALIGN_PARAGRAPH.CENTER
            p.add_run().add_picture(str(path), width=Inches(6.2))
        i += 1
        continue

    if line.startswith("- "):
        p = doc.add_paragraph(style="List Bullet")
        add_inline(p, line[2:])
        i += 1
        continue

    if line.strip():
        p = doc.add_paragraph()
        add_inline(p, line)
    i += 1

doc.save(OUT)

# Verification pass
check = Document(OUT)
print("Saved:", OUT)
print("Paragraphs:", len(check.paragraphs), "| Tables:", len(check.tables),
      "| Inline pictures:", len(check.inline_shapes))
