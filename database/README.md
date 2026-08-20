# Frozen MySQL Schema

`schema.sql` is the supplied source-of-truth schema for the FinOps Assistant. It is MySQL-specific and must be applied before running the backend with the `database` profile.

```powershell
Get-Content -Raw .\schema.sql | mysql -u root -p
```

The backend is configured with `spring.jpa.hibernate.ddl-auto=none` and `spring.sql.init.mode=never`; it will connect to this schema but will not create, alter, or initialize it.
