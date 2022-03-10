package ru.spbu.depnav.models

data class Marker(val id: String, val type: MarkerType, val x: Double, val y: Double) {

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
