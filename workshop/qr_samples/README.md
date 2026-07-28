# Тестовые QR-коды CyberGym

Папка `generated` содержит готовые одноразовые результаты:

- `daily_foundation_01`–`05` — полный план из `warmup_flicks`, `aim_headshots` и `counter_strafe`;
- `practice_warmup_flicks_*` — одиночные флики;
- `practice_aim_headshots_*` — одиночная тренировка хедшотов;
- `practice_ak_spray_*` — одиночный spray control;
- `practice_counter_strafe_*` — одиночный counter-strafe.

Откройте `generated/index.html` на компьютере, выберите QR-код нужного типа и отсканируйте его из CyberGym. Один код нельзя подтверждать в разных тренировках: сервер блокирует повторное использование `run_id`.

Чтобы выпустить новую пачку:

```powershell
python -m pip install -r "workshop/qr_samples/requirements.txt"
python "workshop/qr_samples/generate_qr_samples.py"
```

Новая генерация перезаписывает файлы с теми же именами, но создаёт новые уникальные `run_id`.
