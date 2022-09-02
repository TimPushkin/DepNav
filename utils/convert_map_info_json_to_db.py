# DepNav -- department navigator.
# Copyright (C) 2022  Timofey Pushkin
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <https://www.gnu.org/licenses/>.

import json
import sqlite3
from argparse import ArgumentParser
from enum import Enum


# This script adds the contents of the specified map info json file corresponding to
# 'map-info-schema.json' into the specified SQLite database, creating the database file and the
# needed tables if necessary.
# Json is expected to have absolute coordinates, while the database will have them normalized.
# Coordinates start from top left corner of an image.
#
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

cur.execute("PRAGMA user_version = 4")

cur.execute('''
CREATE TABLE IF NOT EXISTS `map_infos` (
`map_name` TEXT NOT NULL,
`floor_width` INTEGER NOT NULL,
`floor_height` INTEGER NOT NULL,
`tile_size` INTEGER NOT NULL,
`levels_num` INTEGER NOT NULL,
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
`language_id` INTEGER NOT NULL,
`title` TEXT,
`description` TEXT,
tokenize=unicode61,
notindexed=`marker_id`
)
''')
cur.execute('''
CREATE TABLE IF NOT EXISTS `search_history` (
`marker_id` INTEGER NOT NULL,
`timestamp` INTEGER NOT NULL,
PRIMARY KEY(`marker_id`)
)
''')

db.commit()

jf = json.load(open(args.json_file, encoding="utf8"))

floor_width = jf["floorWidth"]
floor_height = jf["floorHeight"]
tile_size = jf["tileSize"]
levels_num = jf["levelsNum"]

floors_num = 0
row_id = 1
for floor_obj in jf["floors"]:
    floors_num += 1
    floor = floor_obj["floor"]
    for marker_obj in sorted(floor_obj["markers"], key=lambda marker: marker["type"]):
        marker_type, is_closed, x, y, ru, en = marker_obj.values()
        cur.execute("INSERT INTO markers VALUES (?, ?, ?, ?, ?, ?)",
                    (row_id, marker_type, is_closed, floor, x / floor_width, y / floor_height))
        cur.execute(
            "INSERT INTO marker_texts"
            "(`marker_id`,`language_id`,`title`,`description`) VALUES (:id, :language_id, :title, :description)",
            {"id": row_id, "language_id": LID.EN.value, **en})
        cur.execute(
            "INSERT INTO marker_texts"
            "(`marker_id`,`language_id`,`title`,`description`) VALUES (:id, :language_id, :title, :description)",
            {"id": row_id, "language_id": LID.RU.value, **ru})
        row_id += 1

cur.execute("INSERT INTO 'map_infos' VALUES (?, ?, ?, ?, ?, ?)",
            (jf["mapName"], floor_width, floor_height, tile_size, levels_num, floors_num))

db.commit()
cur.close()
