# Звітний репозиторій: Water & Vitamin Tracker

**Студентка:** Посунько Дарія  
**Академічна група:** KS51  
**Дисципліна:** Розробка проблемно-орієнтованих мов програмування для мобільних та ігрових застосунків

---

Даний проєкт є результатом поетапної розробки мобільного Android-застосунку для контролю водного балансу та прийому вітамінів. Процес розробки розділено на чотири незалежні етапи (лабораторні роботи), кожен з яких зберігається у відповідній гілці контролю версій. У гілці `main` знаходиться фінальна інтегрована збірка.

## Навігація по етапах розробки (Лабораторні роботи)

Для перевірки конкретного етапу, будь ласка, перейдіть до відповідної гілки репозиторію. **Детальний технічний звіт та скріншоти знаходяться у файлі `README.md` всередині кожної гілки.**

| Етап / Лабораторна | Назва гілки | Ключові технології та реалізовані патерни |
| :--- | :--- | :--- |
| **№1: Базова архітектура** | [`feature/application-framework`](../../tree/feature/application-framework) | Jetpack Compose, Navigation Compose, State Hoisting, Mock Data |
| **№2: Локальне сховище** | [`feature/local-storage-and-api-layer`](../../tree/feature/local-storage-and-api-layer) | Room SQLite (KSP), Repository Pattern, Offline-first стратегія |
| **№3: Мережа та Тести** | [`feature/websockets`](../../tree/feature/websockets) | Імітація WebSocket, Coroutines, Unit Testing (15 тестів) |
| **№4: Безпека (Біометрія)**| [`feature/biometrics`](../../tree/feature/biometrics) | Biometric Prompt API, SharedPreferences, Dependency Inversion |
