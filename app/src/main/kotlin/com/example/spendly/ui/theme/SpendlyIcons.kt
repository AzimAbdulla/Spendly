package com.example.spendly.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

private var _pieChart: ImageVector? = null

val Icons.Filled.PieChart: ImageVector
    get() {
        if (_pieChart != null) {
            return _pieChart!!
        }
        _pieChart = materialIcon(name = "Filled.PieChart") {
            materialPath {
                moveTo(12.0f, 2.0f)
                curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f)
                reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f)
                reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
                close()
                moveTo(13.0f, 4.07f)
                curveToRelative(3.61f, 0.45f, 6.48f, 3.33f, 6.93f, 6.93f)
                lineTo(13.0f, 11.0f)
                lineTo(13.0f, 4.07f)
                close()
                moveTo(4.0f, 12.0f)
                curveToRelative(0.0f, -4.07f, 3.06f, -7.44f, 7.0f, -7.93f)
                verticalLineToRelative(15.87f)
                curveToRelative(-3.94f, -0.49f, -7.0f, -3.86f, -7.0f, -7.94f)
                close()
                moveTo(13.0f, 19.93f)
                lineTo(13.0f, 13.0f)
                horizontalLineToRelative(6.93f)
                curveToRelative(-0.45f, 3.61f, -3.32f, 6.48f, -6.93f, 6.93f)
                close()
            }
        }
        return _pieChart!!
    }

private var _calendarToday: ImageVector? = null

val Icons.Filled.CalendarToday: ImageVector
    get() {
        if (_calendarToday != null) return _calendarToday!!
        _calendarToday = materialIcon(name = "Filled.CalendarToday") {
            materialPath {
                moveTo(20.0f, 3.0f)
                horizontalLineToRelative(-1.0f)
                verticalLineTo(1.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(7.0f)
                verticalLineTo(1.0f)
                horizontalLineTo(5.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(4.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(16.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(20.0f, 21.0f)
                horizontalLineTo(4.0f)
                verticalLineTo(8.0f)
                horizontalLineToRelative(16.0f)
                verticalLineToRelative(13.0f)
                close()
            }
        }
        return _calendarToday!!
    }

private var _tune: ImageVector? = null

val Icons.Filled.Tune: ImageVector
    get() {
        if (_tune != null) return _tune!!
        _tune = materialIcon(name = "Filled.Tune") {
            materialPath {
                moveTo(3.0f, 17.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(6.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineTo(3.0f)
                close()
                moveTo(3.0f, 5.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(10.0f)
                verticalLineTo(5.0f)
                horizontalLineTo(3.0f)
                close()
                moveTo(13.0f, 21.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(-8.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(6.0f)
                horizontalLineToRelative(2.0f)
                close()
                moveTo(7.0f, 9.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(3.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(9.0f)
                horizontalLineTo(7.0f)
                close()
                moveTo(21.0f, 13.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineTo(11.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(10.0f)
                close()
                moveTo(15.0f, 9.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(7.0f)
                horizontalLineToRelative(4.0f)
                verticalLineTo(5.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineTo(3.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(6.0f)
                close()
            }
        }
        return _tune!!
    }

private var _chevronLeft: ImageVector? = null

val Icons.Filled.ChevronLeft: ImageVector
    get() {
        if (_chevronLeft != null) return _chevronLeft!!
        _chevronLeft = materialIcon(name = "Filled.ChevronLeft") {
            materialPath {
                moveTo(15.41f, 7.41f)
                lineTo(14.0f, 6.0f)
                lineToRelative(-6.0f, 6.0f)
                lineToRelative(6.0f, 6.0f)
                lineToRelative(1.41f, -1.41f)
                lineTo(10.83f, 12.0f)
                close()
            }
        }
        return _chevronLeft!!
    }

private var _chevronRight: ImageVector? = null

val Icons.Filled.ChevronRight: ImageVector
    get() {
        if (_chevronRight != null) return _chevronRight!!
        _chevronRight = materialIcon(name = "Filled.ChevronRight") {
            materialPath {
                moveTo(10.0f, 6.0f)
                lineTo(8.59f, 7.41f)
                lineTo(13.17f, 12.0f)
                lineToRelative(-4.58f, 4.59f)
                lineTo(10.0f, 18.0f)
                lineToRelative(6.0f, -6.0f)
                close()
            }
        }
        return _chevronRight!!
    }

