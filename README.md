# Gacha

A card collector Discord bot using [JDA](https://github.com/DV8FromTheWorld/JDA), [MongoDB](https://github.com/mongodb/mongo), and [GSON](https://github.com/google/gson). Cards, missions, and events are read from a data folder and used to create a Gacha game.

## Gacha Data Folder

Gacha data is stored in the user folder and loaded at start.
The color syntax for json files is `r;g;b;a`.

### Cards (cards.json)
- id (integer): The ID to identify this card with.
- name (string): The name of the card.
- image (string): The file name for the image of the card.
- source (string): The URL for the original image of the card.
- star (integer): The star rating for the card (1-6).
- special (boolean): Whether or not the card is special.
- exclusive (boolean): Whether or not the card is exclusive.
- base (integer): The ID of the base to use for this card.
- font (string): The font to use for this card.
- font_size (integer): The size of the font being used.
- base_color (color): The color mask to apply to the card's base.
- text_color (color): The color of the card's text.

### Missions (missions.json)
Work in progress.

### Events (events.json)
Work in progress.
