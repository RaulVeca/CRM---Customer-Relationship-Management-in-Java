import json
import sys
from pathlib import Path

import pdfplumber


def main() -> None:
    pdf_path = Path(sys.argv[1])
    output_path = Path(sys.argv[2])
    pages = []
    with pdfplumber.open(pdf_path) as pdf:
        for number, page in enumerate(pdf.pages, start=1):
            text = page.extract_text(x_tolerance=2, y_tolerance=3, layout=False) or ""
            words = page.extract_words(use_text_flow=True, keep_blank_chars=False)
            pages.append(
                {
                    "page": number,
                    "width": page.width,
                    "height": page.height,
                    "text": text,
                    "words": words,
                }
            )
    output_path.write_text(
        json.dumps(pages, ensure_ascii=False, indent=2), encoding="utf-8"
    )
    print(f"Extracted {len(pages)} pages from {pdf_path.name} -> {output_path}")


if __name__ == "__main__":
    main()
