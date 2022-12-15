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
from argparse import ArgumentParser

# This script applies a linear transformation to the marker coordinates in the specified map info
# json file corresponding to 'map-info-schema.json'.
#
# - Prerequisites: json file corresponding to 'map-info-schema.json'.
# - Result: the file is changed by applying the transformation.


parser = ArgumentParser()
parser.add_argument("json_file", type=str, help="path to the json file")
parser.add_argument(
    "x_multiplier", type=float, help="number on which x coordinate will be multiplied"
)
parser.add_argument(
    "y_multiplier", type=float, help="number on which y coordinate will be multiplied"
)
parser.add_argument(
    "x_shift", type=int, help="number which will be added to x coordinate"
)
parser.add_argument(
    "y_shift", type=int, help="number which will be added to y coordinate"
)
args = parser.parse_args()

with open(args.json_file, "r+", encoding="utf8") as f:
    jf = json.load(f)

    for floor_obj in jf["floors"]:
        for marker_obj in floor_obj["markers"]:
            marker_obj["x"] = round(marker_obj["x"] * args.x_multiplier) + args.x_shift
            marker_obj["y"] = round(marker_obj["y"] * args.y_multiplier) + args.y_shift

    f.seek(0)
    f.truncate()
    json.dump(jf, f, ensure_ascii=False)
