# Planetary

NeoForge-мод для Minecraft 1.21.1 с конечным миром в форме кубического планетоида.

## Архитектура 2.0

- 6 гравитационных граней; у каждой собственные local UP/DOWN/NORTH/SOUTH/EAST/WEST.
- Local DOWN всегда направлен к ядру текущей грани.
- При переходе через ребро локальная система координат поворачивается на 90°.
- Блоки остаются обычными Minecraft `BlockState`: модели, `VoxelShape`, hardness, loot, звуки, жидкости, redstone и block entities должны переиспользовать vanilla-механику.
- Готовые vanilla `BakedQuad` в будущем поворачиваются целиком; UV не пересчитываются.
- Базовая единица 3D-загрузки — `PlanetSection 16×16×16`.
- `Render Distance = N` означает сферическую область радиусом `N` секций, то есть примерно `N × 16` блоков во всех направлениях.
- `Simulation Distance` — отдельная меньшая 3D-область активной симуляции.
- Небо, солнце, луна и звёзды общие для всей планеты; локальный горизонт определяется текущей гранью.
- Центральная область ядра будет недоступна, чтобы исключить область с неоднозначной гравитацией.

## Что уже реализовано в ветке `2.0`

Первый слой — чистая топология куба без рендера, физики и генерации:

- `PlanetFace` — 6 граней и их локальные ортонормальные базисы;
- `PlanetDirection` — face-local направления;
- `PlanetPos` — целочисленная позиция в локальном пространстве грани;
- `FaceTransform` — точный переход через ребро с поворотом направлений и возможным разворотом координаты вдоль шва;
- `PlanetTopology` — six-neighbor навигация с автоматическим переходом между гранями;
- unit-тесты покрывают все 24 направленных ребра и обратимость переходов.

## Проверка

```powershell
.\test.ps1
```

Для запуска dev-клиента:

```powershell
.\run-client.ps1
```

### Диагностический планетоид

В dev-клиенте при входе в мир перед игроком автоматически появляется
небольшой визуальный планетоид 12×12 блоков на грань. Это первый render-prototype
поверх настоящего `PlanetWorldAccess`: все шесть граней используют свои
face-local системы координат и жёстко поворачиваются на 90° без деформации
кубов.

Пока это только визуальная геометрия: у неё ещё нет player gravity, collision,
raycast и серверного взаимодействия. Координаты центра выводятся в чат; удобнее
всего создать Creative-мир и облететь объект со всех сторон.

## Зафиксированная модель планеты

Каноническое пространство мира — обычная Minecraft XYZ-сетка с обычными
`BlockPos`. Планета не состоит из шести отдельных хранилищ блоков.

У каждой планеты есть один центральный неразрушимый core-блок и нечётный
базовый диаметр `D = 2R + 1`. Для позиции относительно центра используется
кубический shell-радиус:

`max(|dx|, |dy|, |dz|)`.

Шесть областей локальной гравитации — бесконечные квадратные пирамиды. Область
определяется доминирующей по модулю координатой, а `DOWN` направлен по этой
оси к core. На диагональных границах двух/трёх областей сохраняются все
допустимые gravity-face, чтобы одно физическое edge/corner место не
дублировалось.

Обычная XYZ-сетка отвечает за физическое соседство блоков. `FaceTransform` и
gravity-frame нужны для другой задачи: запускать vanilla/modded поведение,
которое предполагает глобальный Y как UP, так, будто локальный UP текущей
области и есть обычный Minecraft UP. Это необходимо для рельс, дверей,
растений, жидкостей, сущностей и размещения блоков на любой стороне планеты.


## Playable debug planet

The current development harness creates a real 13×13×13 cube planet in the
Overworld around core block `(0, 200, 0)`. The blocks are ordinary Minecraft
blocks in the normal XYZ grid; edges and corners are not duplicated.

On login the server builds the debug cube once, binds its
`PlanetGravityField`, disables active creative flight, and teleports the
player to the top face. The client binds the same fixed core locally so movement
prediction and server physics use the same six infinite pyramid gravity
regions.

This is temporary scaffolding. It will be removed once the dedicated Planet
world type synchronizes its real core/worldgen data.


## Seamless Planet generation space

Worldgen does not treat the six gravity pyramids as six generators or six
terrain faces. `PlanetGenerationSpace` maps the ordinary cubic physical world
into one continuous spherical procedural space. The Euclidean radius in
generation space equals the physical Chebyshev shell radius
`max(|dx|, |dy|, |dz|)`, and the transform is exactly reversible.

Terrain noise, caves, lakes, biome climate, ore fields and other procedural
fields use `WRAP` placement and therefore have no gravity-edge seam. Rigid
structures use `AVOID_EDGE`; their origin/bounding extent must have enough
clearance from the nearest gravity-boundary plane.

`PlanetFace` remains a physics/local-orientation concept. It is deliberately
not part of the generation-space transform.
