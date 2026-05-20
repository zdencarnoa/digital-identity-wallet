package fer.zavrsni.wallet.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.security.crypto.MasterKeys


/*
Pohrana vjerodajnice na uredaju
Ovaj dio nije detaljno propisan ARF-om pa sam se vodio vlastitim nahodenjem i
odlucio ipak adekvatno zastiti vjerodajnicu iako ona sama po sebi nije inkriminirajuc podatak
*/
class PidStorage(context: Context) {

    //Objekt u kojem se nalazi ime filea u kojem ce biti PID i kljuc kojim se enkriptira
    companion object{
        private const val PREFS_NAME = "wallet_pid_storage"
        private const val KEY_SD_JWT = "sd_jwt_vc"
    }

    //Enkriptira sve kljuceve i podatke, nalazi se u Android Keystoreu
    private val masterKey = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()

    //Enkriptiranje i pohrana SD-JWT VC-a, master key enkriptira kljuceve za dekripciju kljuceva i vrijednosti u enkriptiranom shared preferenves
    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)

    //Funkcije za spremanje, dohvacanje i brisanje SD-JWT VC

    fun savePid(sdJwt: String){
        prefs.edit().putString(KEY_SD_JWT, sdJwt).apply()
    }

    fun loadPid(): String? {
        return prefs.getString(KEY_SD_JWT, null)
    }

    fun clearPid(){
        prefs.edit().remove(KEY_SD_JWT).apply()
    }

    fun hasPid(): Boolean {
        return prefs.contains(KEY_SD_JWT)
    }
}