● NON! OkHttp a deux rôles:

  1. OkHttp (okhttp:4.12.0) = Le vrai client HTTP qui fait les requêtes réseau
    - Utilisé EN COULISSE par Retrofit
    - Gère: connexions, timeout, cache, cookies, etc.
  2. Logging Interceptor (logging-interceptor:4.12.0) = Juste pour debug (voir les requêtes dans Logcat)
    - Optionnel, juste pratique en développement

  Relation:

```
Retrofit → utilise → OkHttp → fait les vraies requêtes HTTP
                       ↓
                  Logging Interceptor (optionnel, juste pour voir les logs)
```

  Sans OkHttp, Retrofit ne pourrait pas fonctionner! C'est le moteur qui fait tourner Retrofit. 🚗💨
