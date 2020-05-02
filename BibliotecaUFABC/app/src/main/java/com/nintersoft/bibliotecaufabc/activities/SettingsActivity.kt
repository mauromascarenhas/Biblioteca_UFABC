package com.nintersoft.bibliotecaufabc.activities

import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.*
import androidx.work.PeriodicWorkRequest
import com.nintersoft.bibliotecaufabc.R
import com.nintersoft.bibliotecaufabc.global.Functions
import java.util.concurrent.TimeUnit

private const val TITLE_TAG = "settingsActivityTitle"

class SettingsActivity : AppCompatActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    companion object{
        /**
         * Preference Summary Binder used by #bindSummaryToValue
         */
        private val sBindToSummary = Preference.OnPreferenceChangeListener { preference, newValue ->
            val value = newValue.toString()

            if (preference is ListPreference){
                val index = preference.findIndexOfValue(value)
                preference.summary = if (index >= 0) preference.entries[index] else null
            }
            else preference.summary = value

            true
        }

        /**
         * Binds the specified Preference element to its value (string representation)
         *
         * @param preference : The preference which must be bound
         */
        fun bindSummaryToValue(preference : Preference){
            preference.onPreferenceChangeListener = sBindToSummary
            sBindToSummary.onPreferenceChange(preference, PreferenceManager
                .getDefaultSharedPreferences(preference.context)
                .getString(preference.key, ""))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.settings,
                    HeaderFragment()
                )
                .commit()
        } else {
            title = savedInstanceState.getCharSequence(TITLE_TAG)
        }
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                setTitle(R.string.activity_settings)
            }
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home
                && supportFragmentManager.backStackEntryCount == 0){
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save current activity title so we can set it again after a configuration change
        outState.putCharSequence(TITLE_TAG, title)
    }

    override fun onSupportNavigateUp(): Boolean {
        if (supportFragmentManager.popBackStackImmediate()) {
            return true
        }
        return super.onSupportNavigateUp()
    }

    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat,
        pref: Preference
    ): Boolean {
        // Instantiate the new Fragment
        val args = pref.extras
        val fragment = supportFragmentManager.fragmentFactory.instantiate(
            classLoader,
            pref.fragment
        ).apply {
            arguments = args
            setTargetFragment(caller, 0)
        }
        // Replace the existing Fragment with the new Fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.settings, fragment)
            .addToBackStack(null)
            .commit()
        title = pref.title
        return true
    }

    class HeaderFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.pref_headers, rootKey)

            PreferenceManager.getDefaultSharedPreferences(activity).let {
                if (!it.getBoolean(getString(R.string.key_privacy_store_password), true)){
                    it.edit().apply{
                        remove(getString(R.string.key_privacy_login_username))
                        remove(getString(R.string.key_privacy_login_password))
                    }.apply()
                }
            }
        }
    }

    @Suppress("unused")
    class SyncFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.pref_sync, rootKey)
            bindSummaryToValue(
                findPreference<ListPreference>(getString(R.string.key_notification_warning_delay))!!)
            bindSummaryToValue(
                findPreference<ListPreference>(getString(R.string.key_notification_sync_interval))!!)

            val preferences = PreferenceManager.getDefaultSharedPreferences(context?.applicationContext)
            val hasPassword = preferences.getBoolean(getString(R.string.key_privacy_store_password), true)
            findPreference<ListPreference>(getString(R.string.key_notification_sync_interval))?.
                isEnabled = hasPassword
            findPreference<ListPreference>(getString(R.string.key_notification_warning_delay))?.
                isEnabled = hasPassword
            findPreference<SwitchPreferenceCompat>(getString(R.string.key_notification_enable_warning))?.
                isEnabled = hasPassword

            findPreference<SwitchPreferenceCompat>(getString(R.string.key_notification_enable_warning))?.
                onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                    findPreference<ListPreference>(getString(R.string.key_notification_sync_interval))?.
                        isEnabled = newValue as Boolean
                    findPreference<ListPreference>(getString(R.string.key_notification_warning_delay))?.
                        isEnabled = newValue

                    if (newValue) {
                        Functions.schedulePeriodicSync(TimeUnit.DAYS.toMillis(preferences.
                            getString(getString(R.string.key_notification_sync_interval), "2")!!.toLong()),
                            PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS)
                    }
                    else Functions.cancelPeriodicSync()
                    true
                }

            findPreference<ListPreference>(getString(R.string.key_notification_sync_interval))?.
                onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
                    Functions.schedulePeriodicSync(TimeUnit.DAYS.toMillis(newValue.toString().
                        toLong()), PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS)
                    preference.summary = newValue.toString()
                    true
                }

            findPreference<ListPreference>(getString(R.string.key_notification_sync_interval))?.
                summary = preferences.getString(getString(R.string.key_notification_sync_interval), "")
        }
    }

    @Suppress("unused")
    class GeneralFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.pref_general, rootKey)
        }
    }

    @Suppress("unused")
    class PrivacyFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.pref_privacy, rootKey)
            bindSummaryToValue(
                findPreference<ListPreference>(getString(R.string.key_privacy_login_username))!!)

            val hasPassword = PreferenceManager.getDefaultSharedPreferences(context?.applicationContext)
                .getBoolean(getString(R.string.key_privacy_store_password), true)
            findPreference<SwitchPreferenceCompat>(getString(R.string.key_privacy_auto_login))?.
                isEnabled = hasPassword
            findPreference<EditTextPreference>(getString(R.string.key_privacy_login_username))?.
                isEnabled = hasPassword
            findPreference<EditTextPreference>(getString(R.string.key_privacy_login_password))?.
                isEnabled = hasPassword

            findPreference<SwitchPreferenceCompat>(getString(R.string.key_privacy_store_password))?.
                onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                    findPreference<SwitchPreferenceCompat>(getString(R.string.key_privacy_auto_login))?.
                        isEnabled = newValue as Boolean
                    findPreference<EditTextPreference>(getString(R.string.key_privacy_login_username))?.
                        isEnabled = newValue
                    findPreference<EditTextPreference>(getString(R.string.key_privacy_login_password))?.
                        isEnabled = newValue
                    true
                }

            findPreference<EditTextPreference>(getString(R.string.key_privacy_login_password))?.
                setOnBindEditTextListener {
                    it.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                }
        }
    }
}
