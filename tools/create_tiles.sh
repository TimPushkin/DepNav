#!/usr/bin/env sh

# DepNav -- department navigator.
# Copyright (C) 2023  Timofei Pushkin
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

# This script splits all WebP images (supposedly map floor images) in the specified directory on
# tile pyramid with tiles of specified size. Only zoom levels that are split evenly are retained. It
# also asks, if any additional levels should be removed.
#
# Note: libvips CLI tool is required (type 'vips' in your shell to check).
#
# Arguments: [ WORKING_DIR [ TILE_SIZE ] ]
# - WORKING_DIR -- the directory to find images and save tiles into. Default: current directory.
# - TILE_SIZE -- size of tile sides. Default: 256.

if [ $# -eq 1 ]; then
  cd "$1" || exit
  TILE_SIZE=256
elif [ $# -eq 2 ]; then
  cd "$1" || exit
  TILE_SIZE="$2"
elif [ $# -gt 2 ]; then
  echo "Usage: $0 [ WORKING_DIR [ TILE_SIZE ] ]"
  exit 1
fi

# Prints the number of levels (starting from the most detailed one) which will be evenly split on
# tiles.
#
# Arga: $1 -- image width or height.
get_unpadded_zoom_levels_num() {
  awk -v size="$1" -v tile_size="$TILE_SIZE" '
    BEGIN {
      levels_num = 0
      for (tiles_num = size / tile_size; tiles_num % 2 == 0; tiles_num /= 2) {
        levels_num++
      }
      print levels_num
    }
  '
}

LVLS_TO_DEL_USER=-1
REMEMBER_LVLS_TO_DEL_USER="?"

for IMAGE in *.webp; do
  # Calculate how many zoom levels (starting from the most detailed one) will be split evenly
  WIDTH="$(vipsheader -f width "$IMAGE")"
  HEIGHT="$(vipsheader -f height "$IMAGE")"
  WIDTH_LVLS="$(get_unpadded_zoom_levels_num "$WIDTH")"
  HEIGHT_LVLS="$(get_unpadded_zoom_levels_num "$HEIGHT")"
  LVLS_TO_RETAIN=$((WIDTH_LVLS < HEIGHT_LVLS ? WIDTH_LVLS : HEIGHT_LVLS))
  if [ $LVLS_TO_RETAIN -eq 0 ]; then
    echo "$IMAGE of size ${WIDTH}x${HEIGHT} is not divisible into ${TILE_SIZE}x${TILE_SIZE} tiles"
    exit 1
  fi

  # Create the tile pyramid
  FLOOR="${IMAGE%.webp}"
  vips dzsave "$IMAGE" "$FLOOR" \
    --layout google \
    --tile-size "$TILE_SIZE" \
    --depth onetile \
    --overlap 0 \
    --background 0 \
    --skip-blanks 0 \
    --strip \
    --suffix ".webp[lossless=1,Q=100,min_size=1,effort=6,strip=1]" \
    --vips-progress || exit

  cd "$FLOOR" || exit
  rm "blank.png" || exit # Remove "blank.png" created by vips

  # Check how many zoom levels have been generated
  LVLS_GENERATED="$(ls | wc -l)"
  if [ "$LVLS_GENERATED" -lt "$LVLS_TO_RETAIN" ]; then # Sanity check, should not happen
    echo "$IMAGE: wrong number of levels to retain (generated $LVLS_GENERATED < to retain $LVLS_TO_RETAIN)"
    exit 1
  fi

  # Calculate the amount of levels to delete and prompt the user to confirm
  LVLS_TO_DEL=$((LVLS_GENERATED - LVLS_TO_RETAIN))
  if [ "$REMEMBER_LVLS_TO_DEL_USER" != "y" ] ||
    [ "$LVLS_TO_DEL_USER" -lt "$LVLS_TO_DEL" ] || [ "$LVLS_TO_DEL_USER" -ge "$LVLS_GENERATED" ]; then
    while [ "$LVLS_TO_DEL_USER" -lt "$LVLS_TO_DEL" ] || [ "$LVLS_TO_DEL_USER" -ge "$LVLS_GENERATED" ]; do
      echo "How many least detailed zoom levels to delete? ($LVLS_TO_DEL..$((LVLS_GENERATED - 1)))"
      read -r LVLS_TO_DEL_USER
    done
  fi
  while [ "$REMEMBER_LVLS_TO_DEL_USER" != "y" ] && [ "$REMEMBER_LVLS_TO_DEL_USER" != "n" ]; do
    echo "Remember this value while it fits? (y/n)"
    read -r REMEMBER_LVLS_TO_DEL_USER
  done
  LVLS_TO_DEL="$LVLS_TO_DEL_USER"

  # Delete the selected levels and shift the remaining ones
  for LVL in $(seq 0 $((LVLS_GENERATED - 1))); do
    if [ "$LVL" -lt "$LVLS_TO_DEL" ]; then
      rm -r "$LVL" || exit
    elif [ "$LVLS_TO_DEL" -gt 0 ]; then
      mv "$LVL" "$((LVL - LVLS_TO_DEL))" || exit
    fi
  done

  cd .. # Leave FLOOR
done
