package ru.spbu.depnav.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

/**
 * A displayable marker.
 */
@Entity(tableName = "markers")
data class Marker(
    /** ID of this marker. */
    @PrimaryKey val id: Int,
    /** Type of this marker. */
    val type: MarkerType,
    /** Whether this marker indicates a closed object. */
    @ColumnInfo(name = "is_closed") val isClosed: Boolean,
    /** Numbers of the floor on which this marker is placed. */
    val floor: Int,
    /** X coordinate of this marker. */
    val x: Double,
    /** Y coordinate of this marker. */
    val y: Double
) {
    /**
     * ID of this marker as a string.
     */
    @Ignore
    val idStr = id.toString(10)

    /**
     * Type of an object represented by a [Marker].
     */
    enum class MarkerType {
        /** Building entrance. */
        ENTRANCE,

        /** Room entrance. */
        ROOM,

        /** Staircase leading up. */
        STAIRS_UP,

        /** Staircase leading down. */
        STAIRS_DOWN,

        /** Staircase leading both up and down. */
        STAIRS_BOTH,

        /** Elevator entrance. */
        ELEVATOR,

        /** Men's restroom. */
        WC_MAN,

        /** Women's restroom. */
        WC_WOMAN,

        /** Restroom. */
        WC,

        /** Anything else. */
        OTHER
    }
}
