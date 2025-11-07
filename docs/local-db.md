## 本地数据库（DBeaver）

1. 在 DBeaver 里连接到你自己的 PostgreSQL（使用默认库 `postgres`，Schema 用 `app`），然后执行 `src/main/resources/db/migration` 下的 Flyway 脚本初始化结构。
2. 根目录 `.env` 已经改成 Spring 可读取的属性文件，填上和 DBeaver 里一致的 `spring.datasource.url/username/password` 即可让应用直连同一个实例。
3. 运行 `mvn spring-boot:run`（或 `scripts/dev-run.ps1`）就会读取 `.env`，无需再启动任何 Docker 服务；如果要验证连通性，可以用 `psql -h <host> -U <user> -d postgres -c "select 1"`。
