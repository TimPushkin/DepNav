package ru.spbu.depnav

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import ru.spbu.depnav.db.AppDatabase
import ru.spbu.depnav.model.MarkerText.LanguageId
import ru.spbu.depnav.ui.search.MarkerSearchView
import ru.spbu.depnav.ui.theme.DepNavTheme
import ru.spbu.depnav.viewmodel.MarkerSearchViewModel

private const val TAG = "SearchActivity"

const val EXTRA_MARKER_ID = "ru.spbu.depnav.MARKER_ID"

class SearchActivity : ComponentActivity() {
    private lateinit var mAppDatabase: AppDatabase
    private lateinit var mMarkerSearchViewModel: MarkerSearchViewModel

    private val systemLanguage: LanguageId
        get() {
            val language = Locale.current.language

            Log.d(TAG, "Current system language is $language")

            return when (language) {
                "en" -> LanguageId.EN
                "ru" -> LanguageId.RU
                else -> LanguageId.EN
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAppDatabase = AppDatabase.getInstance(this)
        mMarkerSearchViewModel = MarkerSearchViewModel(mAppDatabase.markerTextDao(), systemLanguage)

        setContent {
            DepNavTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MarkerSearchView(
                        markerSearchViewModel = mMarkerSearchViewModel,
                        onResultClick = this::onMarkerSelected
                    )
                }
            }
        }
    }

    private fun onMarkerSelected(id: Int) {
        Log.i(TAG, "Marker $id has been selected")

        val intent = Intent(this, MainActivity::class.java).putExtra(EXTRA_MARKER_ID, id)
        setResult(Activity.RESULT_OK, intent)

        finish()
    }
}
