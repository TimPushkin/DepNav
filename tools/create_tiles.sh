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

# Prints the amount of rows or columns on the specified zoom level.
#
# Args: $1 -- image width (to get the number of columns) or height (to get the number of rows).
#       $2 -- zoom level (0 is the most detailed level).
get_rows_cols_num() {
  awk -v size="$1" -v lvl="$2" -v tile_size="$TILE_SIZE" 'BEGIN { print size / tile_size / 2 ^ lvl }'
}

EMPTY_TILE="empty.webp"

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

  # Calculate the amount of levels to delete, prompting the user if needed
  LVLS_TO_DEL=$((LVLS_GENERATED - LVLS_TO_RETAIN))
  if [ "$REMEMBER_LVLS_TO_DEL_USER" != "y" ] ||
    [ "$LVLS_TO_DEL_USER" -lt "$LVLS_TO_DEL" ] || [ "$LVLS_TO_DEL_USER" -ge "$LVLS_GENERATED" ]; then
    while [ "$LVLS_TO_DEL_USER" -lt "$LVLS_TO_DEL" ] || [ "$LVLS_TO_DEL_USER" -ge "$LVLS_GENERATED" ]; do
      echo "How many least detailed zoom levels to remove? ($LVLS_TO_DEL..$((LVLS_GENERATED - 1)))"
      read -r LVLS_TO_DEL_USER
    done
  fi
  while [ "$REMEMBER_LVLS_TO_DEL_USER" != "y" ] && [ "$REMEMBER_LVLS_TO_DEL_USER" != "n" ]; do
    echo "Remember this value while it fits? (y/n)"
    read -r REMEMBER_LVLS_TO_DEL_USER
  done
  LVLS_TO_DEL="$LVLS_TO_DEL_USER"

  # Create a fully transparent 1x1 tile to place instead of skipped blank tiles
  vips black 'empty.webp[lossless=1,Q=100,min_size=1,effort=6,strip=1]' 1 1 --bands 4

  for LVL in $(seq 0 $((LVLS_GENERATED - 1))); do
    # Remove the level if needed
    if [ "$LVL" -lt "$LVLS_TO_DEL" ]; then
      rm -r "$LVL" || exit
      continue
    fi
    cd "$LVL" || exit

    ROWS=$(get_rows_cols_num "$HEIGHT" $((LVLS_GENERATED - LVL - 1)))
    for ROW in $(seq 0 $((ROWS - 1))); do
      # Create the row if missing (skipped as fully blank)
      if ! [ -e "$ROW" ]; then
        mkdir "$ROW"
      fi
      cd "$ROW" || exit

      # Place 1x1 transparent files instead of non-existent (skipped as blank) tiles
      COLS=$(get_rows_cols_num "$WIDTH" $((LVLS_GENERATED - LVL - 1)))
      for COL in $(seq 0 $((COLS - 1))); do
        if ! [ -e "$COL.webp" ]; then
          cp "../../$EMPTY_TILE" "$COL.webp" || exit
        fi
      done

      cd .. # Leave ROW
    done

    cd .. # Leave LVL

    # Shift the level if any previous levels have been deleted
    if [ "$LVLS_TO_DEL" -gt 0 ]; then
      mv "$LVL" "$((LVL - LVLS_TO_DEL))" || exit
    fi
  done

  # Remove the 1x1 transparent tile we previously created and copied
  rm "$EMPTY_TILE"

  cd .. # Leave FLOOR
done
