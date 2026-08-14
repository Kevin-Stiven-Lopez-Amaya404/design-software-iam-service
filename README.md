# design-software-iam-service (Java / Spring Boot)

authN/authZ/RBAC. Hexagonal **Maven multi-módulo** (los módulos enforzan el hexágono). ADR-006.

```
iam-core/       domain + application (PURO: sin Spring/JPA) + tests JUnit
iam-adapters/   in/rest · out/persistence (JPA -> iam_db)
iam-api/        Spring Boot app (bootstrap + wiring) + Dockerfile
```
Build: `./mvnw package` (requiere Maven; el skeleton trae la estructura, faltan adapters reales).
authN se **federa** al IdP del SENA; iam conserva authZ/RBAC (ADR-005).
