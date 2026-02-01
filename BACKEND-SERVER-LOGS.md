blinuxoussama@LAPTOP-S9R4M8TB:~/mobile-projects/cabinet-medical-backend$ ./mvnw spring-boot:run
[INFO] Scanning for projects...
[INFO]
[INFO] ----------------< com.cabinet:cabinet-medical-backend >-----------------
[INFO] Building cabinet-medical-backend 0.0.1-SNAPSHOT
[INFO]   from pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO]
[INFO] >>> spring-boot:3.5.0:run (default-cli) > test-compile @ cabinet-medical-backend >>>
[INFO]
[INFO] --- resources:3.3.1:resources (default-resources) @ cabinet-medical-backend ---
[INFO] Copying 1 resource from src/main/resources to target/classes
[INFO] Copying 0 resource from src/main/resources to target/classes
[INFO]
[INFO] --- compiler:3.14.0:compile (default-compile) @ cabinet-medical-backend ---
[INFO] Nothing to compile - all classes are up to date.
[INFO]
[INFO] --- resources:3.3.1:testResources (default-testResources) @ cabinet-medical-backend ---
[INFO] skip non existing resourceDirectory /home/blinuxoussama/mobile-projects/cabinet-medical-backend/src/test/resources
[INFO]
[INFO] --- compiler:3.14.0:testCompile (default-testCompile) @ cabinet-medical-backend ---
[INFO] Nothing to compile - all classes are up to date.
[INFO]
[INFO] <<< spring-boot:3.5.0:run (default-cli) < test-compile @ cabinet-medical-backend <<<
[INFO]
[INFO]
[INFO] --- spring-boot:3.5.0:run (default-cli) @ cabinet-medical-backend ---
[INFO] Attaching agents: []

  .   ____          ____ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.5.0)

2026-02-01T09:06:25.903+01:00  INFO 325920 --- [cabinet-medical-backend] [           main] c.c.m.CabinetMedicalBackendApplication   : Starting CabinetMedicalBackendApplication using Java 21.0.9 with PID 325920 (/home/blinuxoussama/mobile-projects/cabinet-medical-backend/target/classes started by blinuxoussama in /home/blinuxoussama/mobile-projects/cabinet-medical-backend)
2026-02-01T09:06:25.907+01:00  INFO 325920 --- [cabinet-medical-backend] [           main] c.c.m.CabinetMedicalBackendApplication   : No active profile set, falling back to 1 default profile: "default"
2026-02-01T09:06:27.431+01:00  INFO 325920 --- [cabinet-medical-backend] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-02-01T09:06:27.584+01:00  INFO 325920 --- [cabinet-medical-backend] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 138 ms. Found 5 JPA repository interfaces.
2026-02-01T09:06:28.878+01:00  INFO 325920 --- [cabinet-medical-backend] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 8080 (http)
2026-02-01T09:06:28.908+01:00  INFO 325920 --- [cabinet-medical-backend] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-02-01T09:06:28.909+01:00  INFO 325920 --- [cabinet-medical-backend] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.41]
2026-02-01T09:06:29.001+01:00  INFO 325920 --- [cabinet-medical-backend] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2026-02-01T09:06:29.003+01:00  INFO 325920 --- [cabinet-medical-backend] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 3008 ms
2026-02-01T09:06:29.431+01:00  INFO 325920 --- [cabinet-medical-backend] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-02-01T09:06:29.514+01:00  INFO 325920 --- [cabinet-medical-backend] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.6.15.Final
2026-02-01T09:06:29.568+01:00  INFO 325920 --- [cabinet-medical-backend] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-02-01T09:06:30.039+01:00  INFO 325920 --- [cabinet-medical-backend] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-02-01T09:06:30.086+01:00  INFO 325920 --- [cabinet-medical-backend] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2026-02-01T09:06:30.752+01:00  INFO 325920 --- [cabinet-medical-backend] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@312f3050
2026-02-01T09:06:30.756+01:00  INFO 325920 --- [cabinet-medical-backend] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2026-02-01T09:06:30.809+01:00  WARN 325920 --- [cabinet-medical-backend] [           main] org.hibernate.orm.deprecation            : HHH90000025: PostgreSQLDialect does not need to be specified explicitly using 'hibernate.dialect' (remove the property setting and it will be selected by default)
2026-02-01T09:06:30.844+01:00  INFO 325920 --- [cabinet-medical-backend] [           main] org.hibernate.orm.connections.pooling    : HHH10001005: Database info:
        Database JDBC URL [Connecting through datasource 'HikariDataSource (HikariPool-1)']
        Database driver: undefined/unknown
        Database version: 16.11
        Autocommit mode: undefined/unknown
        Isolation level: undefined/unknown
        Minimum pool size: undefined/unknown
        Maximum pool size: undefined/unknown
2026-02-01T09:06:33.750+01:00  INFO 325920 --- [cabinet-medical-backend] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-02-01T09:06:34.671+01:00  INFO 325920 --- [cabinet-medical-backend] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-02-01T09:06:35.305+01:00  INFO 325920 --- [cabinet-medical-backend] [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
2026-02-01T09:06:37.443+01:00  WARN 325920 --- [cabinet-medical-backend] [           main] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
2026-02-01T09:06:37.980+01:00  WARN 325920 --- [cabinet-medical-backend] [           main] .s.s.UserDetailsServiceAutoConfiguration :

Using generated security password: 342a6231-a14b-4a45-b752-2accfead74e5

This generated password is for development use only. Your security configuration must be updated before running your application in production.

2026-02-01T09:06:37.994+01:00  INFO 325920 --- [cabinet-medical-backend] [           main] r$InitializeUserDetailsManagerConfigurer : Global AuthenticationManager configured with UserDetailsService bean with name inMemoryUserDetailsManager
2026-02-01T09:06:38.819+01:00  INFO 325920 --- [cabinet-medical-backend] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 8080 (http) with context path '/'
2026-02-01T09:06:38.836+01:00  INFO 325920 --- [cabinet-medical-backend] [           main] c.c.m.CabinetMedicalBackendApplication   : Started CabinetMedicalBackendApplication in 13.825 seconds (process running for 13.467)
2026-02-01T09:06:48.041+01:00  INFO 325920 --- [cabinet-medical-backend] [0.0-8080-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2026-02-01T09:06:48.042+01:00  INFO 325920 --- [cabinet-medical-backend] [0.0-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2026-02-01T09:06:48.045+01:00  INFO 325920 --- [cabinet-medical-backend] [0.0-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 3 ms
Hibernate:
    select
        p1_0.id,
        p1_0.created_at,
        p1_0.user_id,
        u1_0.id,
        u1_0.created_at,
        d1_0.id,
        d1_0.created_at,
        d1_0.specialty,
        d1_0.user_id,
        u1_0.email,
        u1_0.first_name,
        u1_0.last_login_at,
        u1_0.last_name,
        u1_0.password_hash,
        u1_0.phone,
        u1_0.role
    from
        patient p1_0
    join
        users u1_0
            on u1_0.id=p1_0.user_id
    left join
        doctor d1_0
            on u1_0.id=d1_0.user_id
    where
        p1_0.id=?
Hibernate:
    select
        a1_0.id,
        a1_0.cancellation_reason,
        a1_0.cancelled_by,
        a1_0.created_at,
        a1_0.date_time,
        a1_0.doctor_id,
        a1_0.patient_id,
        a1_0.reason,
        a1_0.status,
        a1_0.updated_at
    from
        appointment a1_0
    where
        a1_0.patient_id=?
Hibernate:
    select
        d1_0.id,
        d1_0.created_at,
        d1_0.specialty,
        d1_0.user_id
    from
        doctor d1_0
Hibernate:
    select
        u1_0.id,
        u1_0.created_at,
        d1_0.id,
        d1_0.created_at,
        d1_0.specialty,
        d1_0.user_id,
        u1_0.email,
        u1_0.first_name,
        u1_0.last_login_at,
        u1_0.last_name,
        u1_0.password_hash,
        p1_0.id,
        p1_0.created_at,
        p1_0.user_id,
        u1_0.phone,
        u1_0.role
    from
        users u1_0
    left join
        doctor d1_0
            on u1_0.id=d1_0.user_id
    left join
        patient p1_0
            on u1_0.id=p1_0.user_id
    where
        u1_0.id=?
Hibernate:
    select
        d1_0.id,
        d1_0.created_at,
        d1_0.specialty,
        d1_0.user_id,
        u1_0.id,
        u1_0.created_at,
        u1_0.email,
        u1_0.first_name,
        u1_0.last_login_at,
        u1_0.last_name,
        u1_0.password_hash,
        p1_0.id,
        p1_0.created_at,
        p1_0.user_id,
        u1_0.phone,
        u1_0.role
    from
        doctor d1_0
    join
        users u1_0
            on u1_0.id=d1_0.user_id
    left join
        patient p1_0
            on u1_0.id=p1_0.user_id
    where
        d1_0.id=?
Hibernate:
    select
        ts1_0.id,
        ts1_0.created_at,
        ts1_0.day_of_week,
        ts1_0.doctor_id,
        ts1_0.end_time,
        ts1_0.start_time
    from
        timeslot ts1_0
    where
        ts1_0.doctor_id=?
        and ts1_0.day_of_week=?
