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
import pathlib
import sqlite3
from argparse import ArgumentParser
from enum import Enum

# This script adds the contents of the specified map info json file corresponding to
# 'map-info-schema.json' into the specified SQLite database.
# Json is expected to have absolute coordinates, while the database will have them normalized.
# Coordinates start from top left corner of an image.
#
# - Prerequisites: json file corresponding to 'map-info-schema.json'.
# - Result: database file with the contents of the json file.


class LID(Enum):
    EN = 0
    RU = 1


parser = ArgumentParser()
parser.add_argument("json_file", type=pathlib.Path, help="path to the json file")
parser.add_argument(
    "-d", "--db_file", type=pathlib.Path, default="maps.db", help="path to the database file"
)
args = parser.parse_args()

db = sqlite3.connect(str(args.db_file))
cur = db.cursor()

cur.executescript(
    """
    CREATE TABLE IF NOT EXISTS map_info
    (
        "name"       TEXT    NOT NULL PRIMARY KEY,
        floor_width  INTEGER NOT NULL,
        floor_height INTEGER NOT NULL,
        tile_size    INTEGER NOT NULL,
        levels_num   INTEGER NOT NULL,
        floors_num   INTEGER NOT NULL
    );
    CREATE TABLE IF NOT EXISTS marker
    (
        id        INTEGER NOT NULL PRIMARY KEY,
        map_name  TEXT    NOT NULL,
        "type"    TEXT    NOT NULL,
        is_closed INTEGER NOT NULL,
        floor     INTEGER NOT NULL,
        x         REAL    NOT NULL,
        y         REAL    NOT NULL,
        FOREIGN KEY (map_name) REFERENCES map_info ("name") ON DELETE RESTRICT
    );
    CREATE TABLE IF NOT EXISTS marker_text
    (
        marker_id   INTEGER NOT NULL,
        language_id TEXT NOT NULL,
        title       TEXT,
        description TEXT,
        PRIMARY KEY (marker_id, language_id),
        FOREIGN KEY (marker_id) REFERENCES marker(id) ON DELETE RESTRICT
    );
    CREATE VIRTUAL TABLE IF NOT EXISTS marker_text_fts USING FTS4
    (
        title       TEXT,
        description TEXT,
        tokenize=unicode61,
        content=`marker_text`
    );
    CREATE TABLE IF NOT EXISTS search_history_entry
    (
        marker_id   INTEGER NOT NULL PRIMARY KEY,
        "timestamp" INTEGER NOT NULL,
        FOREIGN KEY (marker_id) REFERENCES marker(id) ON DELETE RESTRICT
    );

    CREATE INDEX IF NOT EXISTS index_marker_map_name ON marker (map_name);

    CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_marker_text_fts_BEFORE_UPDATE
        BEFORE UPDATE
        ON marker_text
    BEGIN
        DELETE FROM marker_text_fts WHERE docid = old.rowid;
    END;
    CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_marker_text_fts_BEFORE_DELETE
        BEFORE DELETE
        ON marker_text
    BEGIN
        DELETE FROM marker_text_fts WHERE docid = old.rowid;
    END;
    CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_marker_text_fts_AFTER_UPDATE
        AFTER UPDATE
        ON marker_text
    BEGIN
        INSERT INTO marker_text_fts(docid, title, description) VALUES (new.rowid, new.title, new.description);
    END;
    CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_marker_text_fts_AFTER_INSERT
        AFTER INSERT
        ON marker_text
    BEGIN
        INSERT INTO marker_text_fts(docid, title, description) VALUES (new.rowid, new.title, new.description);
    END;
    """
)

m = json.load(open(args.json_file, encoding="utf8"))

cur.execute(f"PRAGMA user_version = {m['version']}")

map_name = m["mapName"]
floor_width = m["floorWidth"]
floor_height = m["floorHeight"]

cur.execute(
    "INSERT INTO map_info "
    "VALUES (:name, :floor_width, :floor_height, :tile_size, :levels_num, :floors_num)",
    {
        "name": map_name,
        "floor_width": floor_width,
        "floor_height": floor_height,
        "tile_size": m["tileSize"],
        "levels_num": m["zoomLevelsNum"],
        "floors_num": len(m["floors"]),
    },
)

row_id = 0
for floor in m["floors"]:
    for marker in sorted(floor["markers"], key=lambda it: it["type"]):
        row_id += 1

        marker_type, is_closed, x, y, *_ = marker.values()

        cur.execute(
            "INSERT INTO marker "
            "VALUES (:id, :map_name, :type, :is_closed, :floor, :x, :y)",
            {
                "id": row_id,
                "map_name": map_name,
                "type": marker_type,
                "is_closed": is_closed,
                "floor": floor["floor"],
                "x": x / floor_width,
                "y": y / floor_height,
            },
        )

        for lid_name in LID.__members__:
            cur.execute(
                "INSERT INTO marker_text "
                "VALUES (:marker_id, :language_id, :title, :description)",
                {
                    "marker_id": row_id,
                    "language_id": lid_name,
                    **marker[lid_name.lower()],
                },
            )

db.commit()
cur.close()
