from pathlib import Path
from shutil import copy2

from pypdf import PdfReader, PdfWriter
from reportlab.lib.utils import ImageReader
from reportlab.pdfgen import canvas


CURRENT_PDF = Path(r"D:\TrainingIT_site\output\pdf\main.pdf")
ORIGINAL_PDF = Path(r"C:\Users\raulv\Downloads\TrainingIT.pdf")
WORK_DIR = Path(r"D:\TrainingIT_site\tmp\pdfs")
GENERATED_PDF = WORK_DIR / "main-9.3-updated.pdf"
BACKUP_PDF = WORK_DIR / "main-before-9.3-update.pdf"
EVIDENCE_PDF = WORK_DIR / "functional-testing-evidence.pdf"

SCREENSHOTS = [
    {
        "path": Path(r"C:\Users\raulv\Downloads\1 (2).png"),
        "title": "(a) Authenticated learner dashboard",
        "description": (
            "The screenshot shows the authenticated learner home page with the personalised "
            "'Welcome back, Raul Veca' greeting, learner navigation, live course and learner "
            "statistics, the average rating, and the mentoring indicator. During manual testing, "
            "a USER account was routed to this page after login and the navigation and dashboard "
            "data loaded correctly, confirming that authenticated learner access works."
        ),
    },
    {
        "path": Path(r"C:\Users\raulv\Downloads\3 (3).png"),
        "title": "(b) Course catalogue and category filters",
        "description": (
            "The image displays the course catalogue with category filters and a grid of course "
            "cards showing category, difficulty level, rating, duration, and the More control. "
            "Manual testing confirmed that the catalogue loads the stored courses and that "
            "selecting a category updates the visible results, so course discovery and filtering work."
        ),
    },
    {
        "path": Path(r"C:\Users\raulv\Downloads\2 (2).png"),
        "title": "(c) Personalised AI course recommendations",
        "description": (
            "The screenshot shows the Find your next course adviser with Artificial Intelligence "
            "selected and a ranked response containing match scores and explanations for four "
            "courses. Manual testing confirmed that submitting learner preferences calls the "
            "recommendation endpoint and renders the ranked matches, verifying that the "
            "personalised AI recommendation flow works."
        ),
    },
    {
        "path": Path(r"C:\Users\raulv\Downloads\4.png"),
        "title": "(d) Company-level AI recommendations",
        "description": (
            "The administrator view shows five scored, AI-recommended courses above the employee "
            "table, whose rows provide roles, work areas, and training interests, together with "
            "the Add employee form. Manual testing confirmed that these employee profiles are "
            "analysed into ranked company training suggestions and that the resulting list is "
            "displayed correctly, so the company-level recommendation functionality works."
        ),
    },
]


def draw_wrapped_text(pdf, text, x, y, max_width, font_name, font_size, leading):
    pdf.setFont(font_name, font_size)
    words = text.split()
    lines = []
    current = ""
    for word in words:
        candidate = word if not current else f"{current} {word}"
        if pdf.stringWidth(candidate, font_name, font_size) <= max_width:
            current = candidate
        else:
            lines.append(current)
            current = word
    if current:
        lines.append(current)
    for line in lines:
        pdf.drawString(x, y, line)
        y -= leading
    return y


def draw_image_in_box(pdf, image_path, x, y, width, height):
    image = ImageReader(str(image_path))
    image_width, image_height = image.getSize()
    scale = min(width / image_width, height / image_height)
    draw_width = image_width * scale
    draw_height = image_height * scale
    draw_x = x + (width - draw_width) / 2
    draw_y = y + (height - draw_height) / 2

    pdf.setStrokeColorRGB(0.78, 0.78, 0.78)
    pdf.setLineWidth(0.5)
    pdf.rect(draw_x - 1.5, draw_y - 1.5, draw_width + 3, draw_height + 3, stroke=1, fill=0)
    pdf.drawImage(
        image,
        draw_x,
        draw_y,
        width=draw_width,
        height=draw_height,
        preserveAspectRatio=True,
        mask="auto",
    )


def draw_running_header(pdf, page_width, page_label):
    left = 85.0
    right = page_width - 85.0
    pdf.setFillColorRGB(0.08, 0.08, 0.08)
    pdf.setFont("Times-Roman", 11)
    pdf.drawString(left, 798, "CHAPTER 9.  TESTING")
    pdf.drawRightString(right, 798, page_label)
    pdf.setStrokeColorRGB(0.25, 0.25, 0.25)
    pdf.setLineWidth(0.45)
    pdf.line(left, 789, right, 789)


def draw_evidence_block(pdf, evidence, x, title_y, image_y, image_width, image_height, description_y):
    pdf.setFillColorRGB(0.08, 0.08, 0.08)
    pdf.setFont("Times-Bold", 10.2)
    pdf.drawString(x, title_y, evidence["title"])
    draw_image_in_box(pdf, evidence["path"], x, image_y, image_width, image_height)
    pdf.setFillColorRGB(0.12, 0.12, 0.12)
    draw_wrapped_text(
        pdf,
        evidence["description"],
        x,
        description_y,
        image_width,
        "Times-Roman",
        9.4,
        12.2,
    )


def build_evidence_pdf(page_width, page_height):
    pdf = canvas.Canvas(str(EVIDENCE_PDF), pagesize=(page_width, page_height))
    left = 85.0
    content_width = 425.0

    # Overlay for the existing, otherwise blank lower part of printed page 22.
    pdf.setFillColorRGB(0.08, 0.08, 0.08)
    pdf.setFont("Times-Bold", 11)
    pdf.drawString(left, 592, "Representative functional test evidence")
    intro = (
        "The screenshots below record the visible results returned by the learner and "
        "administrator interfaces during manual end-to-end verification. Each image is "
        "described together with the functionality that was exercised successfully."
    )
    after_intro = draw_wrapped_text(
        pdf, intro, left, 573, content_width, "Times-Roman", 9.4, 12.2
    )
    draw_evidence_block(
        pdf,
        SCREENSHOTS[0],
        left,
        after_intro - 4,
        213,
        content_width,
        300,
        197,
    )
    pdf.showPage()

    # One full-width screenshot per continuation page keeps interface text readable.
    continuation_layouts = [
        (SCREENSHOTS[1], 744, 451, 275, 430),
        (SCREENSHOTS[2], 744, 354, 374, 332),
        (SCREENSHOTS[3], 744, 394, 334, 372),
    ]
    for page_number, (evidence, title_y, image_y, image_height, description_y) in enumerate(
        continuation_layouts, start=1
    ):
        draw_running_header(pdf, page_width, f"22 (continued {page_number}/3)")
        draw_evidence_block(
            pdf,
            evidence,
            left,
            title_y,
            image_y,
            content_width,
            image_height,
            description_y,
        )
        pdf.showPage()

    pdf.save()


def main():
    missing = [str(item["path"]) for item in SCREENSHOTS if not item["path"].exists()]
    if missing:
        raise FileNotFoundError("Missing screenshots: " + ", ".join(missing))
    if not CURRENT_PDF.exists() or not ORIGINAL_PDF.exists():
        raise FileNotFoundError("The current or original PDF is missing")

    current = PdfReader(str(CURRENT_PDF))
    original = PdfReader(str(ORIGINAL_PDF))
    if len(current.pages) != 33 or len(original.pages) != 33:
        raise ValueError("Expected both source PDFs to contain 33 pages")
    if len(original.pages[26].images) != 0:
        raise ValueError("The clean source page for section 9.3 is not blank below its text")

    page_width = float(current.pages[26].mediabox.width)
    page_height = float(current.pages[26].mediabox.height)
    build_evidence_pdf(page_width, page_height)
    evidence = PdfReader(str(EVIDENCE_PDF))
    if len(evidence.pages) != 4:
        raise ValueError("Expected one overlay and three continuation pages")

    writer = PdfWriter()
    for index, page in enumerate(current.pages):
        if index == 26:
            clean_section_page = original.pages[26]
            clean_section_page.merge_page(evidence.pages[0], over=True)
            writer.add_page(clean_section_page)
            for continuation in evidence.pages[1:]:
                writer.add_page(continuation)
        else:
            writer.add_page(page)
    if current.metadata:
        writer.add_metadata(current.metadata)

    with GENERATED_PDF.open("wb") as output_stream:
        writer.write(output_stream)

    result = PdfReader(str(GENERATED_PDF))
    if len(result.pages) != 36:
        raise ValueError(f"Expected 36 output pages, found {len(result.pages)}")

    combined_text = "\n".join((result.pages[i].extract_text() or "") for i in range(26, 30))
    required_text = [item["title"] for item in SCREENSHOTS] + [
        "Representative functional test evidence",
        "authenticated learner access works",
        "course discovery and filtering work",
        "personalised AI recommendation flow works",
        "company-level recommendation functionality works",
    ]
    missing_text = [text for text in required_text if text not in combined_text]
    if missing_text:
        raise ValueError("Missing expected section 9.3 text: " + ", ".join(missing_text))

    # Confirm that every page outside the replaced section remains text-identical.
    for old_index in range(26):
        if (current.pages[old_index].extract_text() or "") != (result.pages[old_index].extract_text() or ""):
            raise ValueError(f"Unexpected text change on physical page {old_index + 1}")
    for old_index in range(27, len(current.pages)):
        new_index = old_index + 3
        if (current.pages[old_index].extract_text() or "") != (result.pages[new_index].extract_text() or ""):
            raise ValueError(f"Unexpected text change on original physical page {old_index + 1}")

    copy2(CURRENT_PDF, BACKUP_PDF)
    copy2(GENERATED_PDF, CURRENT_PDF)
    print(f"Updated: {CURRENT_PDF}")
    print(f"Backup: {BACKUP_PDF}")
    print(f"Pages: {len(result.pages)}")


if __name__ == "__main__":
    main()
