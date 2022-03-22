from enum import Enum
from argparse import ArgumentParser
import json
import sqlite3


# This script adds the contents of the specified map info json file corresponding to
# 'map-info-schema.json' into the specified SQLite database, creating the database file and the
# needed tables if necessary.
# - Prerequisites: json file corresponding to 'map-info-schema.json'.
# - Result: database file with the contents of the json file.


class LID(Enum):
    EN = 0
    RU = 1


parser = ArgumentParser()
parser.add_argument("json_file", type=str, help="path to the json file")
parser.add_argument("-d", "--db_file", type=str, default="markers.db",
                    help="path to the database file")
args = parser.parse_args()

db = sqlite3.connect(args.db_file)
cur = db.cursor()

cur.execute('''
CREATE TABLE IF NOT EXISTS `map_infos` (
`map_name` TEXT NOT NULL,
`floor_width` INTEGER NOT NULL,
`floor_height` INTEGER NOT NULL,
`floors_num` INTEGER NOT NULL,
PRIMARY KEY(`map_name`)
)
''')
cur.execute('''
CREATE TABLE IF NOT EXISTS `markers` (
`id` INTEGER NOT NULL,
`type` TEXT NOT NULL,
`is_closed` INTEGER NOT NULL,
`floor` INTEGER NOT NULL,
`x` REAL NOT NULL,
`y` REAL NOT NULL,
PRIMARY KEY(`id`)
)
''')
cur.execute('''
CREATE VIRTUAL TABLE IF NOT EXISTS `marker_texts` USING FTS4(
`marker_id` INTEGER NOT NULL,
`title` TEXT,
`description` TEXT,
tokenize=unicode61,
languageid=`lid`,
notindexed=`marker_id`
)
''')

db.commit()

jf = json.load(open(args.json_file, encoding="utf8"))

floor_num = 0

rowid = 1
for f in jf["floors"]:
    floor_num += 1
    floor = f["floor"]
    for m in sorted(f["markers"], key=lambda marker: marker["type"]):
        marker_type, is_closed, x, y, ru, en = m.values()
        cur.execute("INSERT INTO markers VALUES (?, ?, ?, ?, ?, ?)",
                    (rowid, marker_type, is_closed, floor, x, y))
        cur.execute(
            "INSERT INTO marker_texts"
            "(`marker_id`,`lid`,`title`,`description`) VALUES (:id, :lid, :title, :description)",
            {"id": rowid, "lid": LID.EN.value, **en})
        cur.execute(
            "INSERT INTO marker_texts"
            "(`marker_id`,`lid`,`title`,`description`) VALUES (:id, :lid, :title, :description)",
            {"id": rowid, "lid": LID.RU.value, **ru})
        rowid += 1

cur.execute("INSERT INTO 'map_infos' VALUES (?, ?, ?, ?)",
            (jf["mapName"], jf["floorWidth"], jf["floorHeight"], floor_num))

db.commit()
cur.close()
