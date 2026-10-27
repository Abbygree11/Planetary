# Phase 1.1–1.2 — результаты: canonical frame, traversal chart и физические координаты

**Статус Phase1 PARTIAL; источник — исходные записи, а не новая проверка в игровом клиенте.**

### Выведенные инварианты

1. **Единый физический XYZ**. BlockPos, Level, сырые соседние позиции и столкновения Minecraft остаются физическими. `pos.relative(direction)` делает физический шаг, а не шаг по локальной гравитации. Не глобально заменять `Direction` или `BlockHitResult.direction`: часть ванильных вызовов потребляет их для физического таргетинга.
2. **Два разных владельца ориентации**. `PlanetBlockFrameContext` — traversal chart и path-dependent транспорт на гранях/рёбрах; `PlanetBlockStateFrame` — position-only canonical state frame, необходимый для однозначного BlockState/shape/support/model на позиции. Один edge BlockPos может принадлежать разным допустимым traversal charts, но его canonical state frame обязан быть стабилен, иначе кеш/коллизия зависят от пути обхода.
3. **Тай-брейк на пересечении граней**. [BLOCK_FRAME](../../../research/BLOCK_FRAME_1_21_1.md) документирует `PlanetGravityField.selectCanonicalBlockFace` с явным приоритетом осей **X, затем Y, затем Z**; не восстанавливать порядок `EnumSet` как API.
4. **Transport across edge** должен обновлять локальный базис и определять реального физического соседа, а не просто переименовывать `FACING` в world axis. Для соседей на другой грани передать physical relation в **канонический frame блока-получателя**. `NeighborUpdater` рассылает сообщения физическим шести соседям и не должен быть глобально повернут.

### Исходные материалы

[BLOCK_FRAME_1_21_1](../../../research/BLOCK_FRAME_1_21_1.md) (раздел 3.5 и VoxelShape), [BLOCK_WORLD_TOPOLOGY_MATRIX](../../../research/BLOCK_WORLD_TOPOLOGY_MATRIX_1_21_1.md) (local ≠ physical, vanilla UPDATE_ORDER), [GRAVITY_MECHANISM_MAP](../../../research/GRAVITY_MECHANISM_MAP_1_21_1.md), [план фазы](../../phase-01.md). Это уже согласованные архитектурные решения, **но не доказательство 6/12/8 gameplay приёмки всей Phase1**.

### Осталось

Пакеты 1.1–1.2: регрессионные тесты chart↔canonical на 6 гранях, 12 рёбрах и 8 углах, reverse/transport/step consistency, один physical position в разных traversal paths; вне Planet +Y/noPlanet; клиент/сервер и mod coordinate context. Конкретный CI прогона/модовую версию надо закреплять при проверке, не придумывать здесь. Phase1 не закрыта.
