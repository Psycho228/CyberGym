"""Generate one-time CyberGym Workshop result QR samples."""

from __future__ import annotations

import argparse
import json
import sys
import uuid
from datetime import UTC, datetime
from html import escape
from pathlib import Path

TOOLS_DIR = Path(__file__).resolve().parents[1] / ".qr_tools"
if TOOLS_DIR.exists():
    sys.path.insert(0, str(TOOLS_DIR))

try:
    import qrcode
    from qrcode.constants import ERROR_CORRECT_M
except ImportError as error:
    raise SystemExit(
        "Install dependencies first: "
        'python -m pip install -r "workshop/qr_samples/requirements.txt"'
    ) from error


def metric_set(exercise: str, variant: int) -> dict[str, int | float]:
    if exercise == "warmup_flicks":
        return {
            "attempts": 40,
            "hits": 29 + variant,
            "accuracy": round((29 + variant) / 40 * 100, 1),
            "average_time_ms": 510 - variant * 12,
            "score": 68 + variant * 3,
        }
    if exercise == "aim_headshots":
        return {
            "attempts": 70,
            "hits": 48 + variant,
            "headshots": 43 + variant,
            "accuracy": round((48 + variant) / 70 * 100, 1),
            "score": 72 + variant * 3,
        }
    if exercise == "ak_spray":
        return {
            "attempts": 5,
            "hits": 92 + variant * 3,
            "accuracy": 61 + variant * 2,
            "average_grouping": round(0.72 + variant * 0.03, 2),
            "score": 70 + variant * 4,
        }
    if exercise == "counter_strafe":
        return {
            "attempts": 50,
            "successful_stops": 39 + variant,
            "hits": 36 + variant,
            "accuracy": 72 + variant * 2,
            "avg_stop_speed": round(24.5 - variant * 1.7, 1),
            "score": 74 + variant * 3,
        }
    raise ValueError(f"Unknown exercise: {exercise}")


def payload(exercises: list[str], variant: int) -> dict[str, object]:
    return {
        "v": 1,
        "source": "cybergym_workshop",
        "map": "cybergym_training_hub",
        "run_id": f"sample-{uuid.uuid4()}",
        "completed_at": datetime.now(UTC).isoformat().replace("+00:00", "Z"),
        "results": [
            {
                "exercise": exercise,
                "metrics": metric_set(exercise, variant),
            }
            for exercise in exercises
        ],
    }


def save_qr(output_dir: Path, name: str, data: dict[str, object]) -> dict[str, object]:
    raw_json = json.dumps(data, ensure_ascii=False, separators=(",", ":"))
    image = qrcode.make(
        raw_json,
        error_correction=ERROR_CORRECT_M,
        box_size=12,
        border=4,
    )
    file_name = f"{name}.png"
    image.save(output_dir / file_name)
    return {
        "name": name,
        "file": file_name,
        "run_id": data["run_id"],
        "payload": data,
    }


def write_index(output_dir: Path, samples: list[dict[str, object]]) -> None:
    cards = "\n".join(
        (
            '<article class="card">'
            f"<h2>{escape(str(sample['name']))}</h2>"
            f'<img src="{escape(str(sample["file"]))}" '
            f'alt="{escape(str(sample["name"]))}">'
            f"<code>{escape(str(sample['run_id']))}</code>"
            "</article>"
        )
        for sample in samples
    )
    html = f"""<!doctype html>
<html lang="ru">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>CyberGym QR samples</title>
  <style>
    body {{ margin: 0; padding: 32px; color: #e9faff; background: #07111a;
      font: 16px system-ui, sans-serif; }}
    h1 {{ color: #9cff3b; }}
    .grid {{ display: grid; grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
      gap: 24px; }}
    .card {{ padding: 20px; border: 1px solid #2d4858; border-radius: 12px;
      background: #0d1a24; }}
    img {{ display: block; width: 100%; max-width: 520px; image-rendering: pixelated;
      background: white; }}
    code {{ display: block; margin-top: 12px; overflow-wrap: anywhere; color: #ff9d36; }}
  </style>
</head>
<body>
  <h1>CyberGym — тестовые результаты Workshop</h1>
  <p>Каждый QR одноразовый. Открывай нужное изображение на другом экране и сканируй телефоном.</p>
  <main class="grid">{cards}</main>
</body>
</html>
"""
    (output_dir / "index.html").write_text(html, encoding="utf-8")
    (output_dir / "payloads.json").write_text(
        json.dumps(samples, ensure_ascii=False, indent=2),
        encoding="utf-8",
    )


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--output",
        type=Path,
        default=Path(__file__).resolve().parent / "generated",
    )
    args = parser.parse_args()
    output_dir: Path = args.output.resolve()
    output_dir.mkdir(parents=True, exist_ok=True)

    samples: list[dict[str, object]] = []
    daily_exercises = ["warmup_flicks", "aim_headshots", "counter_strafe"]
    for variant in range(1, 6):
        samples.append(
            save_qr(
                output_dir,
                f"daily_foundation_{variant:02d}",
                payload(daily_exercises, variant),
            )
        )

    for exercise in [
        "warmup_flicks",
        "aim_headshots",
        "ak_spray",
        "counter_strafe",
    ]:
        for variant in range(1, 4):
            samples.append(
                save_qr(
                    output_dir,
                    f"practice_{exercise}_{variant:02d}",
                    payload([exercise], variant),
                )
            )

    write_index(output_dir, samples)
    print(f"Generated {len(samples)} QR codes in {output_dir}")


if __name__ == "__main__":
    main()
