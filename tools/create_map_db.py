# DepNav -- department navigator.
# Copyright (C) 2022  Timofei Pushkin
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


class LID(Enum):
    EN = 0
    RU = 1


parser = ArgumentParser(
    description="Adds the contents of the specified JSON file (must follow map info schema) into "
                "the specified SQLite database",
)
parser.add_argument(
    "json",
    type=pathlib.Path,
    help="path to the JSON file to read",
)
parser.add_argument(
    "--db",
    type=pathlib.Path,
    default="maps.db",
    help="path to the database to fill, will be created if does not exist",
)
parser.add_argument(
    "--db-version",
    type=int,
    help="if specified, database version will be set to this value",
)
args = parser.parse_args()
if not args.db.exists() and args.db_version is None:
    parser.error(
        f"database file {str(args.db)} does not exist and will be created from scratch — "
        "a database version must be specified in this case"
    )

db = sqlite3.connect(str(args.db))
cur = db.cursor()

if args.db_version is not None:
    cur.execute(f"PRAGMA user_version = {args.db_version}")

cur.executescript(
    """
    CREATE TABLE IF NOT EXISTS map_info
    (
        id            INTEGER NOT NULL PRIMARY KEY,
        internal_name TEXT    NOT NULL,
        floor_width   INTEGER NOT NULL,
        floor_height  INTEGER NOT NULL,
        tile_size     INTEGER NOT NULL,
        levels_num    INTEGER NOT NULL,
        floors_num    INTEGER NOT NULL
    );
    CREATE TABLE IF NOT EXISTS map_title
    (
        map_id      INTEGER NOT NULL REFERENCES map_info (id) ON UPDATE CASCADE ON DELETE RESTRICT,
        language_id TEXT    NOT NULL,
        title       TEXT    NOT NULL,
        PRIMARY KEY (map_id, language_id)
    );
    CREATE TABLE IF NOT EXISTS marker
    (
        id        INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
        map_id    INTEGER NOT NULL REFERENCES map_info (id) ON UPDATE CASCADE ON DELETE RESTRICT,
        "type"    TEXT    NOT NULL,
        floor     INTEGER NOT NULL,
        x         REAL    NOT NULL,
        y         REAL    NOT NULL
    );
    CREATE TABLE IF NOT EXISTS marker_text
    (
        marker_id   INTEGER NOT NULL REFERENCES marker (id) ON UPDATE CASCADE ON DELETE RESTRICT,
        language_id TEXT    NOT NULL,
        title       TEXT,
        location    TEXT,
        description TEXT,
        PRIMARY KEY (marker_id, language_id)
    );
    CREATE VIRTUAL TABLE IF NOT EXISTS marker_text_fts USING FTS4
    (
        title       TEXT,
        location    TEXT,
        description TEXT,
        tokenize=unicode61,
        content=`marker_text`
    );
    CREATE TABLE IF NOT EXISTS search_history_entry
    (
        marker_id   INTEGER NOT NULL PRIMARY KEY REFERENCES marker (id) ON UPDATE CASCADE ON DELETE RESTRICT,
        "timestamp" INTEGER NOT NULL
    );

    CREATE UNIQUE INDEX IF NOT EXISTS index_map_info_internal_name ON map_info (internal_name);
    CREATE        INDEX IF NOT EXISTS index_marker_map_id_floor    ON marker   (map_id, floor);

    CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_marker_text_fts_BEFORE_UPDATE
        BEFORE UPDATE ON marker_text
    BEGIN
        DELETE FROM marker_text_fts WHERE docid = old.rowid;
    END;
    CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_marker_text_fts_BEFORE_DELETE
        BEFORE DELETE ON marker_text
    BEGIN
        DELETE FROM marker_text_fts WHERE docid = old.rowid;
    END;
    CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_marker_text_fts_AFTER_UPDATE
        AFTER UPDATE  ON marker_text
    BEGIN
        INSERT INTO marker_text_fts(docid, title, location, description)
        VALUES (new.rowid, new.title, new.location, new.description);
    END;
    CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_marker_text_fts_AFTER_INSERT
        AFTER INSERT ON marker_text
    BEGIN
        INSERT INTO marker_text_fts(docid, title, location, description)
        VALUES (new.rowid, new.title, new.location, new.description);
    END;
    """
)

m = json.load(open(args.json, encoding="utf8"))

floor_width = m["floorWidth"]
floor_height = m["floorHeight"]

cur.execute(
    "INSERT INTO map_info (id, internal_name, floor_width, floor_height, tile_size, levels_num, floors_num) "
    "VALUES (:id, :internal_name, :floor_width, :floor_height, :tile_size, :levels_num, :floors_num)",
    {
        "id": m["id"],
        "internal_name": m["internalName"],
        "floor_width": floor_width,
        "floor_height": floor_height,
        "tile_size": m["tileSize"],
        "levels_num": m["zoomLevelsNum"],
        "floors_num": len(m["floors"]),
    },
)

for lid_name in LID.__members__:
    cur.execute(
        "INSERT INTO map_title (map_id, language_id, title) VALUES (:map_id, :language_id, :title)",
        {
            "map_id": m["id"],
            "language_id": lid_name,
            "title": m["title"][lid_name.lower()]
        },
    )

for floor in m["floors"]:
    for marker in sorted(floor["markers"], key=lambda it: it["type"]):
        cur.execute(
            "INSERT INTO marker (map_id, type, floor, x, y)"
            "VALUES (:map_id, :type, :floor, :x, :y)",
            {
                "map_id": m["id"],
                "type": marker["type"],
                "floor": floor["floor"],
                "x": marker["x"] / floor_width,
                "y": marker["y"] / floor_height,
            },
        )
        marker_id = cur.lastrowid

        for lid_name in LID.__members__:
            cur.execute(
                "INSERT INTO marker_text (marker_id, language_id, title, location, description) "
                "VALUES (:marker_id, :language_id, :title, :location, :description)",
                {
                    "marker_id": marker_id,
                    "language_id": lid_name,
                    **marker[lid_name.lower()],
                },
            )

db.commit()
cur.close()
