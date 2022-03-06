package ru.spbu.depnav.models

data class Marker(val id: String, val type: MarkerType, val x: Double, val y: Double) {

    enum class MarkerType {
        /** Building entrance */
        ENTRANCE,

        /** Room entrance */
        ROOM,

        /** Staircase */
        STAIRS,

        /** Elevator entrance */
        ELEVATOR,

        /** Men's restroom */
        WC_MALE,

        /** Women's restroom */
        WC_FEMALE
    }
}
