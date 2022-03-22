from argparse import ArgumentParser
import os
import re

# This script renames square tiles of an image (obtained from Photoshop, for example) so that DepNav
# app can use them.
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
