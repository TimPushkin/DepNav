package ru.spbu.depnav.model

import androidx.room.*

@Entity(tableName = "markers")
data class Marker(
    @PrimaryKey val id: Int,
    val type: MarkerType,
    @ColumnInfo(name = "is_closed") val isClosed: Boolean,
    val floor: Int,
    val x: Double,
    val y: Double
) {
    @Ignore val idStr = id.toString(10)

    enum class MarkerType {
        /** Building entrance */
        ENTRANCE,

        /** Room entrance */
        ROOM,

        /** Staircase leading up */
        STAIRS_UP,

        /** Staircase leading down */
        STAIRS_DOWN,

        /** Staircase leading both up and down */
        STAIRS_BOTH,

        /** Elevator entrance */
        ELEVATOR,

        /** Men's restroom */
        WC_MAN,

        /** Women's restroom */
        WC_WOMAN,

        /** Restroom */
        WC,

        /** Anything else */
        OTHER
    }
}
