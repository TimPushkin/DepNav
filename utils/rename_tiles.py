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

from argparse import ArgumentParser
import os
import re

# This script renames square tiles of an image (obtained from Photoshop, for example) so that DepNav
# app can use them (the format is "[root path]/[theme name]/[floor prefix][floor]/[level]/[row]_[col].png").
# In Photoshop the appropriate tiles may be obtained as follows:
# 1. Select "Slice tool".
# 2. Right mouse click on the image and select "Divide slice".
# 3. Set the needed settings.
# 4. Select "File" - "Export" - "Save for Web (Legacy)", set the needed settings and save.
#
# - Prerequisites: an image split into tiles, named in alphabetical order.
# - Result: tiles are renamed to use in DepNav.

parser = ArgumentParser()
parser.add_argument("root", type=str, help="path to the directory containing only the tiles")
parser.add_argument("width", type=int, help="full width of the original image")
parser.add_argument("height", type=int, help="full height of the original image")
parser.add_argument("-t", "--tile_size", type=int, default=1024, help="size of the square tiles")
args = parser.parse_args()

assert args.width % args.tile_size == 0 and args.height % args.tile_size == 0

_, _, files = os.walk(args.root).__next__()
files.sort(key=lambda f: int(re.sub(r'\D', '', f)))

row_size = args.width // args.tile_size

row = 0
col = 0

for file in files:
    name = f"{row}_{col}{os.path.splitext(file)[1]}"

    old_path = os.path.join(args.root, file)
    new_path = os.path.join(args.root, name)

    os.rename(old_path, new_path)

    col += 1
    if col % row_size == 0:
        row += 1
        col = 0
