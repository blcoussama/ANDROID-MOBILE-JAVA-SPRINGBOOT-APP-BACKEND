● CardView = Le design visuel des items dans vos listes

  C'est un rectangle avec:

- 🔲 Coins arrondis
- 🌑 Ombre (élévation)
- 📦 Padding automatique

  Exemple dans votre app:

```xml
<!-- item_doctor.xml -->
<CardView>  ← Le rectangle avec ombre
    <TextView>Dr. Ahmed</TextView>
    <TextView>Cardiologue</TextView>
</CardView>
```

  Résultat visuel:

```
┌─────────────────────┐
│  Dr. Ahmed Benali   │  ← Carte avec ombre
│  Cardiologue        │     et coins arrondis
└─────────────────────┘
  ▼ Ombre
```

  Sans CardView = Items plats collés ensemble
  Avec CardView = Items séparés, effet "cartes empilées" 🃏

  En résumé: Purement esthétique, donne un look moderne Material Design!
