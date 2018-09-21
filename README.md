# Gacha

Created using [Discord4J](http://github.com/Discord4J/Discord4J), [MongoDB](https://github.com/mongodb/mongo), and [GSON](https://github.com/google/gson).

# Customizations

Customizable Gacha data is stored in the user folder under "Gacha".

## Gacha Data
User Folder
- Gacha
  - img
    - cards
      - base
        - card_base_id.png
      - card_id.png
  - cards.json
  - events.json
  - quests.json

### cards.json
**Required Fields**: id, name, star
```json
[
  {
    "id": "card_id",
    "name": "Card Name",
    "star": 2,
    "gen": 0,
    "special": false,
    "color": "1.0,0.5,0.0,0.35",
    "text_color": "1.0,1.0,1.0,1.0"
  }
]
```

### events.json
**Required Fields**: type, start_date
Check [Event.java](https://github.com/oopsjpeg/gacha/blob/master/src/main/java/com/oopsjpeg/gacha/data/impl/Event.java) for the available event types.
```json
[
  {
    "type": "NONE",
    "message": "Event Message",
    "start_date": "2018-09-21T00:00:00",
    "end_date": "2018-09-24T00:00:00",
  }
]
```

### quests.json
**Required Fields**: id, title, interval, reward, conditions
Check [Quest.java](https://github.com/oopsjpeg/gacha/blob/master/src/main/java/com/oopsjpeg/gacha/data/impl/Quest.java) for the available condition types.
```json
[
  {
    "id": "quest_id",
    "title": "Quest Title",
    "interval": 1,
    "reward": 500,
    "conditions": [
      {
        "id": "condition_id",
        "type": "GACHA_ANY",
        "data": [0]
      }
    ]
  }
]
```
