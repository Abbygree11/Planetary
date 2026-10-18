# Planetary — obligatory concise chat report format

Use in **every next chat**, including status updates while CI runs. All user-facing text in Russian. The current named stage appears in **every** update, e.g. `Planetary — 2.4A-9: <owner>` or `Planetary — 3C-1: <BER mechanism>`. Do not hide the stage behind generic “проверил” or “код готов”. No token-heavy repetition.

## A. First brief progress message, when task is nontrivial
```text
Planetary — <ID_ЭТАПА>: <механизм>
Сначала сверю HEAD/CI и владельцев, затем внесу ограниченное изменение и проверю CI.
<Одно-два важных ограничения и что не трогаем.>
```

## B. Finish (previously agreed user-facing style)
```markdown
## Planetary — `<ID_ЭТАПА>`: <полное семейство механизма>

### ✅ <что фактически сделано>
2–4 содержательных предложения: исследованный общий владелец, реализация/лаборатория, что сохранено.
Не называть «готово» то, что не проверено.

| Проверка | Результат |
|---|---|
| Ветка | `2.0` |
| HEAD / commit | [`123abcd`](REAL_COMMIT_URL) |
| CI | [#RUN](REAL_RUN_URL) — SUCCESS / FAILURE / IN PROGRESS |
| Игра | PENDING / частичный smoke / PASS с областью |
| Моды | SOURCE ONLY / LOADED CHECK PENDING / PASS с точной версией |

### <Реализация и совместимость>
Одним абзацем: deepest safe owner, sibling / mod reuse и обходы, исключения.
Не смешивать SOURCE, Java/ASM, реальные клиент/сервер, 6/12/8 или модовый JAR.

### Проверка в Minecraft
1. `git pull && .\\test.ps1 && .\\run-client.ps1` — только после зелёного CI.
2. <конкретное действие на +Y и ожидаемый результат>.
3. <все другие 5 граней; 12 рёбер/8 углов при необходимости>.
4. <no-Planet, реальные моды, жизненный цикл / GUI / redstone по механике>.

**Следующий этап:** `<ID_СЛЕДУЮЩЕГО_ЭТАПА>` — <одна конкретная задача и условие перехода>.
`main` не изменялась.
```

If code was committed and CI is running, report `CODE COMMITTED / CI IN PROGRESS / GAMEPLAY PENDING`; check again when practical, don't fabricate results. If no commit exists, do **not** call the task done. Link paths/commit only after verification. Old stages not manually tested stay GAMEPLAY PENDING. For very small patches reduce paragraphs, but keep stage, actual evidence, next step.
